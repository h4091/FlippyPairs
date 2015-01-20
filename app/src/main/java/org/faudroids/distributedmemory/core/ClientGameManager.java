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

	private final Map<Integer, Card> closedCards = new HashMap<>();
	private final Map<Integer, Card> matchedCards = new HashMap<>();

	private ConnectionHandler connectionHandler;
	private String deviceName;
	private int pairsCount;
	private ClientGameListener clientGameListener;

	private GameState currentState = GameState.CONNECTING;

	@Inject
	public ClientGameManager() { }

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
		return currentState;
	}


	public List<Card> getClosedCards() {
		return new LinkedList<>(closedCards.values());
	}


	public List<Card> getMatchedCards() {
		return new LinkedList<>(matchedCards.values());
	}


	public void stopGame() {
		connectionHandler.stop();
	}


	public void registerClientGameListener(ClientGameListener clientGameListener) {
		Assert.assertTrue(this.clientGameListener ==  null, "already registered");
		this.clientGameListener = clientGameListener;
	}


	public void unregisterClientGameListener() {
		Assert.assertTrue(clientGameListener != null, "not registered");
		this.clientGameListener = null;
	}


	private void assertValidState(GameState state) {
		if (!currentState.equals(state)) throw new IllegalStateException("must be in state " + state + " to perform this action");
	}


	private void changeState(final GameState nextState) {
		currentState = nextState;
	}


	@Override
	public void onNewMessage(String msg) {
		Timber.i("received msg from host" + msg);
		switch(currentState) {
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

				if (clientGameListener != null) clientGameListener.onGameStarted();
				connectionHandler.sendMessage(Message.ACK);
				changeState(GameState.SELECT_1ST_CARD);
				break;

			case SELECT_1ST_CARD:
				int card1Id = Integer.valueOf(msg);
				// TODO update UI / cards!
				Timber.i("selected first card with id " + card1Id);
				connectionHandler.sendMessage(Message.ACK);
				changeState(GameState.SELECT_2ND_CARD);
				break;


			case SELECT_2ND_CARD:
				int card2Id = Integer.valueOf(msg);
				// TODO update UI / cards!
				Timber.i("selected second card with id " + card2Id);
				connectionHandler.sendMessage(Message.ACK);
				changeState(GameState.UPDATE_CARDS);
				break;

			case UPDATE_CARDS:
				switch (msg) {
					case Message.EVALUATION_MATCH_CONTINUE:
						// TODO update UI / cards!
					case Message.EVALUATION_MISS:
						changeState(GameState.SELECT_1ST_CARD);
						break;
					case Message.EVALUATION_MATCH_FINISH:
						changeState(GameState.FINISHED);
						break;
				}
				connectionHandler.sendMessage(Message.ACK);
				break;
		}
	}

}
