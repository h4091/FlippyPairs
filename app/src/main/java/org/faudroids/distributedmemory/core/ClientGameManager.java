package org.faudroids.distributedmemory.core;

import android.os.Handler;
import android.os.Looper;

import org.faudroids.distributedmemory.network.ConnectionHandler;
import org.faudroids.distributedmemory.utils.Assert;

import java.util.HashMap;
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
	private String deviceName;
	private int pairsCount;
	private ClientGameListener clientGameListener;

	@Inject
	public ClientGameManager(GameStateManager gameStateManager) {
		this.gameStateManager = gameStateManager;
	}


	/**
	 * Registers a device with this manager.
	 * Call in state {@link GameState#CONNECTING}.
	 */
	public void registerDevice(ConnectionHandler connectionHandler, String deviceName, int pairsCount) {
		assertValidState(GameState.CONNECTING);

		this.connectionHandler = connectionHandler;
		this.deviceName = deviceName;
		this.pairsCount = pairsCount;

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
		connectionHandler.stop();
	}


	public void registerClientGameListener(ClientGameListener clientGameListener) {
		Assert.assertTrue(this.clientGameListener == null, "already registered");
		this.clientGameListener = clientGameListener;
	}


	public void unregisterClientGameListener() {
		Assert.assertTrue(clientGameListener != null, "not registered");
		this.clientGameListener = null;
	}


	public void selectCard(int cardId) {
		selectedCards.put(cardId, closedCards.remove(cardId));
		if (clientGameListener != null) clientGameListener.onCardsChanged();
		connectionHandler.sendMessage(String.valueOf(cardId));
	}


	@Override
	public void onNewMessage(String msg) {
		Timber.i("received msg from host" + msg);
		switch(gameStateManager.getState()) {
			case CONNECTING:
				connectionHandler.sendMessage(deviceName + " " + pairsCount);
				changeState(GameState.SETUP);
				break;

			case SETUP:
				Matcher matcher = setupCardPattern.matcher(msg);
				while (matcher.find()) {
					Card card = new Card(Integer.valueOf(matcher.group(1)), Integer.valueOf(matcher.group(2)));
					closedCards.put(card.getId(), card);
				}

                connectionHandler.sendMessage(Message.ACK);
				if (clientGameListener != null) clientGameListener.onGameStarted();
				changeState(GameState.SELECT_1ST_CARD);
				break;

			case SELECT_1ST_CARD:
				int card1Id = Integer.valueOf(msg);
				Timber.i("selected first card with id " + card1Id);
				connectionHandler.sendMessage(Message.ACK);
				changeState(GameState.SELECT_2ND_CARD);
				break;

			case SELECT_2ND_CARD:
				int card2Id = Integer.valueOf(msg);
				Timber.i("selected second card with id " + card2Id);
				connectionHandler.sendMessage(Message.ACK);
				changeState(GameState.UPDATE_CARDS);
				break;

			case UPDATE_CARDS:
                Timber.d("Result: " + msg);
				switch (msg) {
					case Message.EVALUATION_MATCH_CONTINUE:
						changeState(GameState.SELECT_1ST_CARD);
						if (!selectedCards.isEmpty()) {
							matchedCards.putAll(selectedCards);
							selectedCards.clear();
							if (clientGameListener != null) {
								clientGameListener.onCardsChanged();
								clientGameListener.onCardsMatch();
							}
						}
						break;

					case Message.EVALUATION_MISS:
						changeState(GameState.SELECT_1ST_CARD);
						if (!selectedCards.isEmpty()) {
							closedCards.putAll(selectedCards);
							selectedCards.clear();
							if (clientGameListener != null) {
								clientGameListener.onCardsChanged();
								clientGameListener.onCardsMismatch();
							}
						}

						break;
					case Message.EVALUATION_MATCH_FINISH:
						changeState(GameState.FINISHED);
						break;
				}
				connectionHandler.sendMessage(Message.ACK);
				break;
		}
	}


	private void assertValidState(GameState state) {
		if (!gameStateManager.getState().equals(state)) throw new IllegalStateException("must be in state " + state + " to perform this action");
	}


	private void changeState(GameState nextState) {
		Timber.d("Changing client game state to " + nextState);
		gameStateManager.changeState(nextState);
	}

}
