package org.faudroids.distributedmemory.core;

import android.os.Handler;
import android.os.Looper;

import org.faudroids.distributedmemory.network.BroadcastMessage;
import org.faudroids.distributedmemory.network.ConnectionHandler;
import org.faudroids.distributedmemory.utils.Assert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import javax.inject.Inject;
import javax.inject.Singleton;

import timber.log.Timber;


@Singleton
public final class HostGameManager implements HostStateTransitionListener {

	private final HostGameStateManager gameStateManager;

	private final Map<Integer, Card> closedCards = new HashMap<>();
	private final List<Card> selectedCards = new LinkedList<>();
	private final Map<Integer, Card> matchedCards = new HashMap<>();

	private final TreeMap<Integer, ConnectionHandler> connectionHandlers = new TreeMap<>();
	private final TreeMap<Integer, Device> devices = new TreeMap<>();

	// used to postpone execution of tasks until method is finished (dirty hack?!)
	private final Handler handler = new Handler(Looper.getMainLooper());

	private HostGameListener hostGameListener = null;

	@Inject
	public HostGameManager(HostGameStateManager gameStateManager) {
		this.gameStateManager = gameStateManager;
		this.gameStateManager.registerStateTransitionListener(this);
	}


	/**
	 * Registers a device with this manager.
	 * State {@link GameState#CONNECTING}.
	 */
	public void addDevice(ConnectionHandler connectionHandler) {
		assertValidState(GameState.CONNECTING);

		int deviceId = connectionHandlers.size();
		connectionHandlers.put(deviceId, connectionHandler);
		connectionHandler.registerMessageListener(new HostMessageListener(deviceId), handler);
		connectionHandler.start();
		connectionHandler.sendMessage("hello world!"); // chicken and egg problem otherwise?
		Timber.i("Adding connection handler with id " + deviceId);
	}


	/**
	 * Setup and distribute the cards after all devices have been connected.
	 * Will change state from {@link GameState#CONNECTING} {@link GameState#SETUP}.
	 */
	public void startGame() {
		assertValidState(GameState.CONNECTING);
		gameStateManager.changeState(GameState.SETUP); // manual new game state, no ack from clients required

		// setup cards locally
		int pairsCount = 0;
		for (Device device : devices.values()) pairsCount += device.getPairsCount();
        Timber.i("Pairs: " + pairsCount);
		Random rand = new Random();
		int cardId = 0;
        for(int i = 0; i < pairsCount; ++i) {
            int randomValue = rand.nextInt(pairsCount);
			closedCards.put(cardId, new Card(cardId, randomValue));
			++cardId;
			closedCards.put(cardId, new Card(cardId, randomValue));
			++cardId;

            Timber.i("Added card " + randomValue + " (" + (cardId - 2) + ")");
			Timber.i("Added card " + randomValue + " (" + (cardId - 1) + ")");
        }

		// TODO race condition between connections being added and clients sending device info

		// send card details to devices
		int currentCardCount = 0;
		List<Card> allCards = new ArrayList<>(closedCards.values());
		Collections.shuffle(allCards);

		List<String> cardDetailMessages = new LinkedList<>();
		for (Integer id : devices.keySet()) {
			Device device = devices.get(id);
			StringBuilder msgBuilder = new StringBuilder();

			for (int i = 0; i < device.getPairsCount() * 2; ++i) {
				Card card = allCards.get(currentCardCount);
				++currentCardCount;
				msgBuilder.append("(").append(card.getId()).append(",").append(card.getValue()).append(")");
			}
			cardDetailMessages.add(msgBuilder.toString());
		}
		transitionState(GameState.SELECT_1ST_CARD, cardDetailMessages);
    }


	public void shutdown() {
		for (ConnectionHandler handler : connectionHandlers.values()) handler.stop();
	}


	public List<Device> getConnectedDevices() {
		return new LinkedList<>(devices.values());
	}


	public void registerHostGameListener(HostGameListener listener) {
		Assert.assertTrue(this.hostGameListener == null, "already registered");
		this.hostGameListener = listener;
	}


	public void unregisterHostGameListener() {
		Assert.assertTrue(this.hostGameListener != null, "not registered");
		this.hostGameListener = null;
	}


	/**
	 * Select one card and transition to the next state
	 */
	private void selectCard(int cardId, GameState selectionState) {
		assertValidState(selectionState);
		Card card = closedCards.remove(cardId);
		selectedCards.add(card);
	}


	/**
	 * Compare the two cards that were selected and update internal card data structures
	 * accordingly.
	 * @return true if the selected cards matched, false otherwise
	 */
	private boolean evaluateCardSelection() {
		assertValidState(GameState.UPDATE_CARDS);

		if (selectedCards.get(0).getValue() == selectedCards.get(1).getValue()) {
			for (Card card : selectedCards) matchedCards.put(card.getId(), card);
			selectedCards.clear();
			return true;
		} else {
            for (Card card : selectedCards) closedCards.put(card.getId(), card);
            selectedCards.clear();
			return false;
        }
	}


	private boolean isGameFinished() {
		return closedCards.size() == 0;
	}


	private void assertValidState(GameState state) {
		if (!gameStateManager.getState().equals(state)) throw new IllegalStateException("must be in state " + state + " to perform this action");
	}


	@Override
	public void onTransitionFinished(GameState nextState) {
		Timber.d("Finished host game state transition to " + nextState);
		gameStateManager.changeState(nextState);

		switch (nextState) {
			case UPDATE_CARDS:
				// once all clients have acked the last selected card evaluate selection
				boolean match = evaluateCardSelection();
				String responseMsg;
				GameState responseState;

				if (match && isGameFinished()) {
					responseMsg = Message.EVALUATION_MATCH_FINISH;
					responseState = GameState.FINISHED;
				} else if (match) {
					responseMsg = Message.EVALUATION_MATCH_CONTINUE;
					responseState = GameState.SELECT_1ST_CARD;
				} else {
					responseMsg = Message.EVALUATION_MISS;
					responseState = GameState.SELECT_1ST_CARD;
				}

				transitionState(responseState, responseMsg);
				Timber.d("Remaining open pairs: " + closedCards.size()/2);
				break;
		}
	}


	private void transitionState(GameState nextState, String message) {
		gameStateManager.startStateTransition(new BroadcastMessage(connectionHandlers.values(), message), nextState);
	}


	private void transitionState(GameState nextState, List<String> messages) {
		gameStateManager.startStateTransition(new BroadcastMessage(connectionHandlers.values(), messages), nextState);
	}


	private final class HostMessageListener implements ConnectionHandler.MessageListener {

		private final int deviceId;

		public HostMessageListener(int deviceId) {
			this.deviceId = deviceId;
		}


		@Override
		public void onNewMessage(String msg) {
            Timber.d("Host received message: " + msg);

			// if ack than take note and do nothing
			if (msg.equals(Message.ACK)) {
				gameStateManager.onAckReceived();
				return;
			}

			switch(gameStateManager.getState()) {
				case CONNECTING:
					String[] tokens = msg.split(" ");
					String deviceName = tokens[0];
					int pairsCount = Integer.valueOf(tokens[1]);
					devices.put(deviceId, new Device(deviceId, deviceName, pairsCount));
                    if (hostGameListener != null) hostGameListener.onClientAdded();
					break;

				case SETUP:
					// nothing to do, clients will only send ack
					break;

                case SELECT_1ST_CARD:
					int firstCardId = Integer.parseInt(msg);
					Timber.i("Received first card " + firstCardId);
					selectCard(firstCardId, GameState.SELECT_1ST_CARD);
					transitionState(GameState.SELECT_2ND_CARD, String.valueOf(firstCardId));
                    break;

                case SELECT_2ND_CARD:
					int secondCardId = Integer.parseInt(msg);
					Timber.i("Received second card " + secondCardId);
					selectCard(secondCardId, GameState.SELECT_2ND_CARD);
					transitionState(GameState.UPDATE_CARDS, String.valueOf(secondCardId));
                    break;

                case UPDATE_CARDS:
					// nothing to do, client will only send ack
                    break;
			}
		}
	}


}
