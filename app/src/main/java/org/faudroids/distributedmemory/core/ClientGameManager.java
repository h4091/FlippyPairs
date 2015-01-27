package org.faudroids.distributedmemory.core;

import android.os.Handler;
import android.os.Looper;

import org.faudroids.distributedmemory.network.ConnectionHandler;
import org.faudroids.distributedmemory.utils.Assert;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Singleton;

import timber.log.Timber;


@Singleton
public final class ClientGameManager implements ConnectionHandler.MessageListener {

	private final Pattern setupCardPattern = Pattern.compile("\\((\\d+),(\\d+)\\)");

	private final GameStateManager gameStateManager;

	private final Map<Integer, Card> closedCards = new HashMap<>();
	private final Map<Integer, Card> matchedCards = new HashMap<>();
	private final Map<Integer, Card> selectedCards = new HashMap<>();

	private ConnectionHandler connectionHandler;
	private Device device;
	private final List<ClientGameListener> clientGameListeners = new LinkedList<>();

	@Inject
	public ClientGameManager(GameStateManager gameStateManager) {
		this.gameStateManager = gameStateManager;
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
	public void registerDevice(ConnectionHandler connectionHandler, String deviceName, int pairsCount) {
		assertValidState(GameState.CONNECTING);

		this.connectionHandler = connectionHandler;
		this.device = new Device(0, deviceName, pairsCount); // dummy id, not needed on client side

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


	public void stopGame() {
		if (connectionHandler != null) connectionHandler.stop();
		for (ClientGameListener listener : clientGameListeners) listener.onGameStopped();
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
		for (ClientGameListener listener : clientGameListeners) listener.onCardsChanged();
		connectionHandler.sendMessage(String.valueOf(cardId));
	}


	@Override
	public void onNewMessage(String msg) {
		Timber.i("received msg from host" + msg);
		switch(gameStateManager.getState()) {
			case CONNECTING:
				connectionHandler.sendMessage(device.getName() + " " + device.getPairsCount());
				gameStateManager.changeState(GameState.SETUP);
				break;

			case SETUP:
				Matcher matcher = setupCardPattern.matcher(msg);
				while (matcher.find()) {
					Card card = new Card(Integer.valueOf(matcher.group(1)), Integer.valueOf(matcher.group(2)));
					closedCards.put(card.getId(), card);
				}

                connectionHandler.sendMessage(Message.ACK);
				for (ClientGameListener listener : clientGameListeners) listener.onGameStarted();
				gameStateManager.changeState(GameState.SELECT_1ST_CARD);
				break;

			case SELECT_1ST_CARD:
				int card1Id = Integer.valueOf(msg);
				Timber.i("selected first card with id " + card1Id);
				connectionHandler.sendMessage(Message.ACK);
				gameStateManager.changeState(GameState.SELECT_2ND_CARD);
				break;

			case SELECT_2ND_CARD:
				int card2Id = Integer.valueOf(msg);
				Timber.i("selected second card with id " + card2Id);
				connectionHandler.sendMessage(Message.ACK);
				gameStateManager.changeState(GameState.UPDATE_CARDS);
				break;

			case UPDATE_CARDS:
                Timber.d("Result: " + msg);

				// update cards
				switch (msg) {
					case Message.EVALUATION_MATCH_CONTINUE:
					case Message.EVALUATION_MATCH_FINISH:
						if (!selectedCards.isEmpty()) {
							matchedCards.putAll(selectedCards);
							selectedCards.clear();
							for (ClientGameListener listener : clientGameListeners) listener.onCardsChanged();
							for (ClientGameListener listener : clientGameListeners) listener.onCardsMatch();
						}
						break;

					case Message.EVALUATION_MISS:
						if (!selectedCards.isEmpty()) {
							closedCards.putAll(selectedCards);
							for (ClientGameListener listener : clientGameListeners) listener.onCardsChanged();
							for (ClientGameListener listener : clientGameListeners) listener.onCardsMismatch();
							selectedCards.clear();
						}

						break;
				}

				// change state
				switch (msg) {
					case Message.EVALUATION_MATCH_FINISH:
						gameStateManager.changeState(GameState.FINISHED);
						stopGame();
						break;

					case Message.EVALUATION_MATCH_CONTINUE:
					case Message.EVALUATION_MISS:
						gameStateManager.changeState(GameState.SELECT_1ST_CARD);
						break;
				}
				connectionHandler.sendMessage(Message.ACK);
				break;
		}
	}


	@Override
	public void onConnectionError() {
		for (ClientGameListener listener : clientGameListeners) listener.onHostLost();
	}


	private void assertValidState(GameState state) {
		if (!gameStateManager.getState().equals(state)) throw new IllegalStateException("must be in state " + state + " to perform this action");
	}

}
