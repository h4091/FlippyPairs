package org.faudroids.distributedmemory.core;

import android.os.Handler;
import android.os.Looper;

import com.fasterxml.jackson.databind.JsonNode;

import org.faudroids.distributedmemory.network.ConnectionHandler;
import org.faudroids.distributedmemory.utils.Assert;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import timber.log.Timber;


@Singleton
public final class ClientGameManager implements ConnectionHandler.MessageListener<JsonNode> {

	private final GameStateManager gameStateManager;

	private final Map<Integer, Card> closedCards = new HashMap<>();
	private final Map<Integer, Card> matchedCards = new HashMap<>();
	private final Map<Integer, Card> selectedCards = new HashMap<>();

	private int currentPlayerIdx;
	private final List<Player> players = new LinkedList<>();
	private final List<Integer> playerPoints = new LinkedList<>();

	private final MessageWriter messageWriter;
	private final MessageReader messageReader;
	private ConnectionHandler<JsonNode> connectionHandler;
	private Device device;

	private final List<ClientGameListener> clientGameListeners = new LinkedList<>();

	// used to delay actions that should run on the main (UI) thread
	private final Handler handler = new Handler(Looper.getMainLooper());

	@Inject
	public ClientGameManager(GameStateManager gameStateManager, MessageWriter messageWriter, MessageReader messageReader) {
		this.gameStateManager = gameStateManager;
		this.messageWriter = messageWriter;
		this.messageReader = messageReader;
	}


	public void initGame() {
		gameStateManager.reset();
		closedCards.clear();
		matchedCards.clear();
		selectedCards.clear();
	}


	/**
	 * Registers the local device with this manager.
	 * Call in state {@link GameState#CONNECTING}.
	 */
	public void registerDevice(ConnectionHandler<JsonNode> connectionHandler, String deviceName, int pairsCount) {
		assertValidState(GameState.CONNECTING);

		this.connectionHandler = connectionHandler;
		this.device = new Device(deviceName, pairsCount);

		connectionHandler.registerMessageListener(this, new Handler(Looper.myLooper()));
		connectionHandler.start();
	}


	public GameState getCurrentState() {
		return gameStateManager.getState();
	}


	public Map<Integer, Card> getClosedCards() {
		return closedCards;
	}


	public Map<Integer, Card> getMatchedCards() {
		return matchedCards;
	}


	public Map<Integer, Card> getSelectedCards() {
		return selectedCards;
	}


	public List<Player> getPlayers() {
		return players;
	}


	public List<Integer> getPlayerPoints() {
		return playerPoints;
	}


	public void stopGame() {
		if (connectionHandler != null) connectionHandler.stop();
	}


	public void registerClientGameListener(ClientGameListener listener) {
		Assert.assertTrue(!clientGameListeners.contains(listener), "already registered");
		clientGameListeners.add(listener);
	}


	public void unregisterClientGameListener(ClientGameListener listener) {
		Assert.assertTrue(clientGameListeners.contains(listener), "not registered");
		clientGameListeners.remove(listener);
	}


	public void selectCard(int cardId) {
		selectedCards.put(cardId, closedCards.remove(cardId));
		connectionHandler.sendMessage(messageWriter.createCardIdMessage(cardId));
	}


	@Override
	public void onNewMessage(JsonNode msg) {
		Timber.i("received msg from host" + msg);
		switch(gameStateManager.getState()) {
			case CONNECTING:
				connectionHandler.sendMessage(messageWriter.createDeviceInfoMessage(device));
				gameStateManager.changeState(GameState.SETUP);
				break;

			case SETUP:
				GameSetupInfo setupInfo = messageReader.readSetupMessage(msg);
				for (Map.Entry<Integer, Integer> entry : setupInfo.getCards().entrySet()) {
					Card card = new Card(entry.getKey(), entry.getValue());
					closedCards.put(card.getId(), card);
				}

				currentPlayerIdx = setupInfo.getStartingPlayerIdx();
				players.clear();
				players.addAll(setupInfo.getPlayers());
				for (int i = 0; i < players.size(); ++i) playerPoints.add(0);

                connectionHandler.sendMessage(messageWriter.createAck());
				for (ClientGameListener listener : clientGameListeners) listener.onGameStarted();
				gameStateManager.changeState(GameState.SELECT_1ST_CARD);
				for (ClientGameListener listener : clientGameListeners) listener.onNewRound();
				break;

			case SELECT_1ST_CARD:
				int card1Id = messageReader.readCardIdMessage(msg);
				Timber.i("selected first card with id " + card1Id);
				connectionHandler.sendMessage(messageWriter.createAck());
				gameStateManager.changeState(GameState.SELECT_2ND_CARD);
				break;

			case SELECT_2ND_CARD:
				int card2Id = messageReader.readCardIdMessage(msg);
				Timber.i("selected second card with id " + card2Id);
				connectionHandler.sendMessage(messageWriter.createAck());
				gameStateManager.changeState(GameState.UPDATE_CARDS);
				break;

			case UPDATE_CARDS:
                Timber.d("Result: " + msg);
				Evaluation evaluation = messageReader.readEvaluation(msg);

				// update players
				currentPlayerIdx = evaluation.getNextPlayerId();
				playerPoints.clear();
				playerPoints.addAll(evaluation.getPlayerPoints());

				// update cards
				if (evaluation.getCardsMatched()) {
					if (!selectedCards.isEmpty()) {
						for (ClientGameListener listener : clientGameListeners) listener.onCardsMatch(selectedCards.values());
						matchedCards.putAll(selectedCards);
						selectedCards.clear();
					}
				} else {
					if (!selectedCards.isEmpty()) {
						for (ClientGameListener listener : clientGameListeners) listener.onCardsMismatch(selectedCards.values());
						closedCards.putAll(selectedCards);
						selectedCards.clear();
					}
				}

				// change state
				if (!evaluation.getContinueGame()) {
					gameStateManager.changeState(GameState.FINISHED);
					for (ClientGameListener listener : clientGameListeners) listener.onGameFinished();
					// delay closing of connection such that server can receive ack
					handler.postDelayed(new Runnable() {
						@Override
						public void run() {
							stopGame();
						}
					}, 100);
				} else {
					gameStateManager.changeState(GameState.SELECT_1ST_CARD);
					for (ClientGameListener listener : clientGameListeners) listener.onNewRound();
				}

				connectionHandler.sendMessage(messageWriter.createAck());
				break;
		}
	}


	@Override
	public void onConnectionError() {
		// if already finished this is expected as connections are being closed
		if (gameStateManager.getState() == GameState.FINISHED) return;
		for (ClientGameListener listener : clientGameListeners) listener.onHostLost();
	}


	private void assertValidState(GameState state) {
		if (!gameStateManager.getState().equals(state)) throw new IllegalStateException("must be in state " + state + " to perform this action");
	}

}
