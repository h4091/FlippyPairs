package org.faudroids.distributedmemory.core;

import android.os.Handler;
import android.os.Looper;

import org.faudroids.distributedmemory.network.ConnectionHandler;
import org.faudroids.distributedmemory.utils.Assert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.inject.Inject;
import javax.inject.Singleton;

import timber.log.Timber;


@Singleton
public final class HostGameManager {

	private final GameStateManager gameStateManager;

	private final Map<Integer, Card> closedCards = new HashMap<>();
	private final List<Card> selectedCards = new LinkedList<>();
	private final Map<Integer, Card> matchedCards = new HashMap<>();

	private final Map<Integer, ConnectionHandler> connectionHandlers = new HashMap<>();
	private final Map<Integer, Device> devices = new HashMap<>();

    private HostGameListener hostGameListener;

    private int acks = 0;

	// used to postpone execution of tasks until method is finished (dirty hack?!)
	private final Handler handler = new Handler(Looper.getMainLooper());


	@Inject
	public HostGameManager(GameStateManager gameStateManager) {
		this.gameStateManager = gameStateManager;
	}


    public void broadcast(String msg) {
        for (Integer id : devices.keySet()) {
            ConnectionHandler connectionHandler = connectionHandlers.get(id);
            connectionHandler.sendMessage(msg);
        }
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
		changeState(GameState.SETUP);

		int pairsCount = 0;
		for (Device device : devices.values()) pairsCount += device.getPairsCount();

		// setup cards locally
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

		for (Integer id : devices.keySet()) {
			ConnectionHandler connectionHandler = connectionHandlers.get(id);
			Device device = devices.get(id);
			StringBuilder msgBuilder = new StringBuilder();

			for (int i = 0; i < device.getPairsCount() * 2; ++i) {
				Card card = allCards.get(currentCardCount);
				++currentCardCount;
				msgBuilder.append("(").append(card.getId()).append(",").append(card.getValue()).append(")");
			}
			connectionHandler.sendMessage(msgBuilder.toString());
            changeState(GameState.SELECT_1ST_CARD);
		}
    }


	/**
	 * Select one card and transition to the next state
	 */
	private void selectCard(int cardId, GameState currentState, GameState nextState) {
		assertValidState(currentState);
		Card card = closedCards.remove(cardId);
		selectedCards.add(card);
		broadcast(Integer.toString(cardId));
		changeState(nextState);
	}


	/**
	 * Evaluate the two cards that were selected and either start next round or
	 * finish the game if no cards are left.
	 */
	private GameState evaluateCardSelection() {
		assertValidState(GameState.UPDATE_CARDS);

		if (selectedCards.get(0).getValue() == selectedCards.get(1).getValue()) {
			for (Card card : selectedCards) matchedCards.put(card.getId(), card);
			selectedCards.clear();
            if (closedCards.size() == 0) {
                broadcast(Message.EVALUATION_MATCH_FINISH);
                return GameState.FINISHED;
            } else {
                broadcast(Message.EVALUATION_MATCH_CONTINUE);
                return GameState.SELECT_1ST_CARD;
            }
		} else {
            for (Card card : selectedCards) closedCards.put(card.getId(), card);
            selectedCards.clear();
            broadcast(Message.EVALUATION_MISS);
            return GameState.SELECT_1ST_CARD;
        }
	}


	public void finish() {
		for (ConnectionHandler handler : connectionHandlers.values()) handler.stop();
	}


	public List<Device> getConnectedDevices() {
		return new LinkedList<>(devices.values());
	}


	private void assertValidState(GameState state) {
		if (!gameStateManager.getState().equals(state)) throw new IllegalStateException("must be in state " + state + " to perform this action");
	}


	private void changeState(GameState nextState) {
		Timber.d("Changing host game state to " + nextState);
        this.acks = 0;
		gameStateManager.changeState(nextState);
	}


    public void registerHostGameListener(HostGameListener listener) {
		Assert.assertTrue(this.hostGameListener == null, "already registered");
		this.hostGameListener = listener;
    }


    public void unregisterHostGameListener() {
		Assert.assertTrue(this.hostGameListener != null, "not registered");
        this.hostGameListener = null;
    }


    private final class HostMessageListener implements ConnectionHandler.MessageListener {

		private final int deviceId;

		public HostMessageListener(int deviceId) {
			this.deviceId = deviceId;
		}

        public boolean allAcksReceived(String msg) {
			return Message.ACK.equals(msg) && ++acks == devices.size();
        }

		@Override
		public void onNewMessage(String msg) {
            Timber.d("Got mail: " + msg);
			switch(gameStateManager.getState()) {
				case CONNECTING:
					String[] tokens = msg.split(" ");
					String deviceName = tokens[0];
					int pairsCount = Integer.valueOf(tokens[1]);
					devices.put(deviceId, new Device(deviceId, deviceName, pairsCount));
                    ++acks;
                    if(hostGameListener!=null) {
                        hostGameListener.onClientAdded();
                    }
                    Timber.i("CONNECTING");
					break;

				case SETUP:
                    Timber.i("SETUP");
                    if(allAcksReceived(msg)) {
                        changeState(GameState.SELECT_1ST_CARD);
                    } else {
                        Timber.d("current acks: " + acks);
                        Timber.d("needed acks: " + devices.size());
                        Timber.i("I can haz ACK?");
                    }
					break;

                case SELECT_1ST_CARD:
                    if(!allAcksReceived(msg)) {
                        int id = Integer.parseInt(msg);
                        Timber.i("Received first card " + id);
                        selectCard(id, GameState.SELECT_1ST_CARD, GameState.SELECT_2ND_CARD);
                    }
                    break;

                case SELECT_2ND_CARD:
                    if(!allAcksReceived(msg)) {
                        int id = Integer.parseInt(msg);
                        Timber.i("Received second card " + id);
						selectCard(id, GameState.SELECT_2ND_CARD, GameState.UPDATE_CARDS);
                    }
                    break;

                case UPDATE_CARDS:
                    GameState next = evaluateCardSelection();
                    Timber.d("Remaining open pairs: " + closedCards.size()/2);
                    if(allAcksReceived(msg)) {
                        changeState(next);
                    }
                    break;
			}
		}
	}
}
