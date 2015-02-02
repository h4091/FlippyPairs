package org.faudroids.distributedmemory.core;

import android.os.Handler;
import android.os.Looper;

import com.fasterxml.jackson.databind.JsonNode;

import org.faudroids.distributedmemory.network.BroadcastMessage;
import org.faudroids.distributedmemory.network.ConnectionHandler;
import org.faudroids.distributedmemory.utils.Assert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import timber.log.Timber;


@Singleton
public final class HostGameManager implements HostStateTransitionListener {

	public static final String TOTAL_CARD_IMAGES = "totalCardImages";

	private final HostGameStateManager gameStateManager;

	private final Map<Integer, Card> closedCards = new HashMap<>();
	private final List<Card> selectedCards = new LinkedList<>();
	private final Map<Integer, Card> matchedCards = new HashMap<>();


    private final int totalCardImages;
    private int usedCardImages;

	private final MessageWriter messageWriter;
	private final MessageReader messageReader;
	private final TreeMap<Integer, ConnectionHandler<JsonNode>> connectionHandlers = new TreeMap<>();
	private final TreeMap<Integer, Device> devices = new TreeMap<>();

	private final List<HostGameListener> hostGameListeners = new LinkedList<>();

	private int currentPlayerIdx;
	private final List<Player> players = new LinkedList<>();
	private final List<Integer> playerPoints = new LinkedList<>();

	// used to postpone execution of tasks that should run on the same thread (UI / main thread)
	private final Handler handler = new Handler(Looper.getMainLooper());

	@Inject
	public HostGameManager(
			HostGameStateManager gameStateManager,
			MessageWriter messageWriter,
			MessageReader messageReader,
			@Named(TOTAL_CARD_IMAGES) int totalCardImages) {

		this.gameStateManager = gameStateManager;
		this.gameStateManager.registerStateTransitionListener(this);
		this.messageWriter = messageWriter;
		this.messageReader = messageReader;
		this.totalCardImages = totalCardImages;
	}


	public void initGame() {
		gameStateManager.reset();
		closedCards.clear();
		selectedCards.clear();
		matchedCards.clear();
		connectionHandlers.clear();
		devices.clear();
        players.clear();
        playerPoints.clear();
	}


	/**
	 * Registers a device with this manager.
	 * State {@link GameState#CONNECTING}.
	 */
	public void addDevice(ConnectionHandler<JsonNode> connectionHandler) {
		assertValidState(GameState.CONNECTING);

		int deviceId = connectionHandlers.size();
		connectionHandlers.put(deviceId, connectionHandler);
		connectionHandler.registerMessageListener(new HostMessageListener(deviceId, connectionHandler), handler);
		connectionHandler.start();
		connectionHandler.sendMessage(messageWriter.createAck()); // chicken and egg problem otherwise?
		Timber.d("Adding connection handler with id " + deviceId);
	}


	/**
	 * Setup and distribute the cards after all devices have been connected.
	 * Will change state from {@link GameState#CONNECTING} {@link GameState#SETUP}.
	 */
	public void startGame() {
		assertValidState(GameState.CONNECTING);
		gameStateManager.changeState(GameState.SETUP); // manual new game state, no ack from clients required
		for (HostGameListener listener : hostGameListeners) listener.onGameStarted();

        // setup players
        currentPlayerIdx = 0;
        for(int i = 0; i < players.size(); ++i) {
            playerPoints.add(0);
        }

		// setup cards locally
		int pairsCount = 0;
		for (Device device : devices.values()) pairsCount += device.getPairsCount();
        Timber.d("Pairs: " + pairsCount);
		int cardId = 0;
		int cardValue = 0;
        for(int i = 0; i < pairsCount; ++i) {
			closedCards.put(cardId, new Card(cardId, cardValue));
			++cardId;
			closedCards.put(cardId, new Card(cardId, cardValue));
			++cardId;
			cardValue = (cardValue + 1) % usedCardImages;

            Timber.d("Added card " + i + " (" + (cardId - 2) + ")");
			Timber.d("Added card " + i + " (" + (cardId - 1) + ")");
        }

		// TODO race condition between connections being added and clients sending device info

		// send card details to devices
		int currentCardCount = 0;
		List<Card> allCards = new ArrayList<>(closedCards.values());
		Collections.shuffle(allCards);

		List<JsonNode> cardDetailMessages = new LinkedList<>();
		for (Integer id : devices.keySet()) {
			Device device = devices.get(id);
			Map<Integer, Integer> selectedCards = new HashMap<>();

			for (int i = 0; i < device.getPairsCount() * 2; ++i) {
				Card card = allCards.get(currentCardCount);
				++currentCardCount;
				selectedCards.put(card.getId(), card.getValue());
			}
			cardDetailMessages.add(messageWriter.createSetupMessage(new GameSetupInfo(selectedCards, currentPlayerIdx, players)));
		}
		transitionState(GameState.SELECT_1ST_CARD, cardDetailMessages);
    }


	public void stopGame() {
		for (ConnectionHandler<JsonNode> handler : connectionHandlers.values()) handler.stop();
		for (HostGameListener listener : hostGameListeners) listener.onGameStopped();
	}


	/**
	 * Returns true if {@link #startGame()} has been called and the game is not yet finished,
	 * false otherwise.
	 */
	public boolean isGameRunning() {
		return gameStateManager.getState() != GameState.CONNECTING && gameStateManager.getState() != GameState.FINISHED;
	}


	public List<Device> getConnectedDevices() {
		return new LinkedList<>(devices.values());
	}


    public List<Player> getPlayers() {
        return new LinkedList<>(players);
    }


    public void setUsedCardImages(int count) {
        usedCardImages = count;
    }


	public void registerHostGameListener(HostGameListener listener) {
		Assert.assertTrue(!hostGameListeners.contains(listener), "already registered");
		hostGameListeners.add(listener);
	}


	public void unregisterHostGameListener(HostGameListener listener) {
		Assert.assertTrue(hostGameListeners.contains(listener), "not registered");
		hostGameListeners.remove(listener);
	}


	public void addPlayer(Player player) {
		assertValidState(GameState.CONNECTING);
		players.add(player);
	}


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


	private void assertValidState(GameState state) {
		if (!gameStateManager.getState().equals(state)) throw new IllegalStateException("must be in state " + state + " to perform this action");
	}


	@Override
	public void onTransitionFinished(GameState nextState) {
		Timber.d("Finished host game state transition to " + nextState);
		switch (nextState) {
			case UPDATE_CARDS:
				// once all clients have acked the last selected card evaluate selection
				boolean match = evaluateCardSelection();

				// update players
				if (match) {
					playerPoints.set(currentPlayerIdx, playerPoints.get(currentPlayerIdx) + 1);
				} else {
					currentPlayerIdx = (currentPlayerIdx + 1) % players.size();
				}

				// send response and handle end of game
				JsonNode responseMsg;
				GameState responseState;

				if (match && closedCards.size() == 0) {
					responseMsg = messageWriter.createEvaluationMessage(new Evaluation(true, false, currentPlayerIdx, playerPoints));
					responseState = GameState.FINISHED;
				} else if (match) {
					responseMsg = messageWriter.createEvaluationMessage(new Evaluation(true, true, currentPlayerIdx, playerPoints));
					responseState = GameState.SELECT_1ST_CARD;
					// include next player
				} else {
					responseMsg = messageWriter.createEvaluationMessage(new Evaluation(false, true, currentPlayerIdx, playerPoints));
					responseState = GameState.SELECT_1ST_CARD;
				}
				transitionState(responseState, responseMsg);
				Timber.d("Remaining open pairs: " + closedCards.size()/2);
				break;

			case FINISHED:
				stopGame();
				break;
		}
	}


	private void transitionState(GameState nextState, JsonNode message) {
		gameStateManager.startStateTransition(new BroadcastMessage<>(connectionHandlers.values(), message), nextState);
	}


	private void transitionState(GameState nextState, List<JsonNode> messages) {
		gameStateManager.startStateTransition(new BroadcastMessage<>(connectionHandlers.values(), messages), nextState);
	}


	private final class HostMessageListener implements ConnectionHandler.MessageListener<JsonNode> {

		private final int deviceId;
		private final ConnectionHandler<JsonNode> connectionHandler;

		public HostMessageListener(int deviceId, ConnectionHandler<JsonNode> connectionHandler) {
			this.deviceId = deviceId;
			this.connectionHandler = connectionHandler;
		}


		@Override
		public void onNewMessage(JsonNode msg) {
            Timber.d("Host received message: " + msg);

			// if ack than take note and do nothing
			if (messageReader.isAck(msg)) {
				gameStateManager.onAckReceived();
				return;
			}

			// message other than ack while others might not have sent ack --> backoff
			if (!gameStateManager.isStateTransitionComplete()) {
				connectionHandler.sendMessage(messageWriter.createBackoffMessage());
				return;
			}

			switch(gameStateManager.getState()) {
				case CONNECTING:
					Device device = messageReader.readDeviceInfoMessage(msg);
					devices.put(deviceId, device);
					for (HostGameListener listener : hostGameListeners) listener.onClientAdded(device);
					break;

				case SETUP:
					// nothing to do, clients will only send ack
					break;

                case SELECT_1ST_CARD:
					int firstCardId = messageReader.readCardIdMessage(msg);
					Timber.d("Received first card " + firstCardId);
					selectCard(firstCardId, GameState.SELECT_1ST_CARD);
					transitionState(GameState.SELECT_2ND_CARD, msg);
                    break;

                case SELECT_2ND_CARD:
					int secondCardId = messageReader.readCardIdMessage(msg);
					Timber.d("Received second card " + secondCardId);
					selectCard(secondCardId, GameState.SELECT_2ND_CARD);
					transitionState(GameState.UPDATE_CARDS, msg);
                    break;

                case UPDATE_CARDS:
					// nothing to do, client will only send ack
                    break;
			}
		}


		@Override
		public void onConnectionError() {
			// if already finished this is expected as connections are being closed
			if (gameStateManager.getState() == GameState.FINISHED) return;
			for (HostGameListener listener : hostGameListeners) listener.onClientLost(devices.get(deviceId));
		}

	}


}
