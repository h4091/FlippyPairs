package org.faudroids.distributedmemory.core;

import android.os.Handler;
import android.os.Looper;

import org.faudroids.distributedmemory.network.ConnectionHandler;
import org.faudroids.distributedmemory.utils.Assert;

import java.util.ArrayList;
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

	private final Map<Integer, Card> closedCards = new HashMap<>();
	private final List<Card> selectedCards = new LinkedList<>();
	private final Map<Integer, Card> matchedCards = new HashMap<>();

	private final Map<Integer, ConnectionHandler> connectionHandlers = new HashMap<>();
	private final Map<Integer, Device> devices = new HashMap<>();
	private int setupDevices = 0;

    private final List<Player> players = new ArrayList<>();

	private GameState currentState = GameState.CONNECTING;
    private int currentPlayer;

    private HostGameListener hostGameListener;

	// used to postpone execution of tasks until method is finished (dirty hack?!)
	private final Handler handler = new Handler(Looper.getMainLooper());

	@Inject
	public HostGameManager() { }

	/**
	 * Registers a device with this manager.
	 */
	public void addDevice(ConnectionHandler connectionHandler) {
		assertValidState(GameState.CONNECTING);

		int deviceId = connectionHandlers.size();
		connectionHandlers.put(deviceId, connectionHandler);
		connectionHandler.registerMessageListener(new HostMessageListener(deviceId), handler);
		connectionHandler.start();
		connectionHandler.sendMessage("hello world!"); // chicken and egg problem otherwise?
	}


	/**
	 * Setup and distribute the cards after all devices have been connected.
	 */
	public void startGame() {
		assertValidState(GameState.CONNECTING);
		changeState(GameState.SETUP);

		int playerCount = 2; // TODO
		int pairsCount = 0;
		for (Device device : devices.values()) pairsCount += device.getPairsCount();

		// setup game logic
        Timber.i("Pairs: " + pairsCount);
		Random rand = new Random();
        for(int id = 0; id < pairsCount * 2; id += 2) {
            int randomValue = rand.nextInt(pairsCount);
            closedCards.put(id, new Card(id, randomValue));
			closedCards.put(id + 1, new Card(id + 1, randomValue));
            Timber.i("Value " + id + " : " + closedCards.get(id).getValue());
            Timber.i("Value " + (id + 1) + " : " + closedCards.get(id + 1).getValue());
        }

        for(int id = 0; id < playerCount; ++id) {
            players.add(new Player(id, "Player" + id));
        }

		// send card details to devices
		for (Map.Entry<Integer, ConnectionHandler> entry : connectionHandlers.entrySet()) {
			entry.getValue().sendMessage("Here are your cards: foo and bar!");
		}

        currentPlayer = 0;
    }


	/**
	 * Returns all cards that belong to one device. After having been called by all clients
	 * the game will start.
	 */
	private List<Card> setupDevice(int deviceId) {
		assertValidState(GameState.CONNECTING);
		if (++setupDevices == devices.size()) changeState(GameState.SELECT_1ST_CARD);

		// TODO this does not distribute cards evenly across devices!!
		List<Card> cards = new LinkedList<>();
		int pairsCount = devices.get(deviceId).getPairsCount();
		for (int i = 0; i < pairsCount; ++i) {
			cards.add(closedCards.get(i));
		}
		return cards;
	}


	/**
	 * Store the first card selected.
	 */
	public void selectFirstCard(int cardId) {
		assertValidState(GameState.SELECT_1ST_CARD);
		Assert.assertTrue(closedCards.containsKey(cardId), "invalid or close card with id " + cardId);

		Card card = closedCards.remove(cardId);
		selectedCards.add(card);

		changeState(GameState.SELECT_2ND_CARD);
	}


	/**
	 * Store the second card selected.
	 */
	public void selectSecondCard(int cardId) {
		assertValidState(GameState.SELECT_2ND_CARD);
		Assert.assertTrue(closedCards.containsKey(cardId), "invalid or close card with id " + cardId);

		Card card = closedCards.remove(cardId);
		selectedCards.add(card);

		changeState(GameState.UPDATE_CARDS);
	}


	/**
	 * Evaluate the two cards that were selected and either start next round or
	 * finish the game if no cards are left.
	 */
	public void evaluateCardSelection() {
		assertValidState(GameState.UPDATE_CARDS);
		changeState(GameState.CONNECTING);

		if (selectedCards.get(0).getValue() == selectedCards.get(1).getValue()) {
			for (Card card : selectedCards) matchedCards.put(card.getId(), card);
			selectedCards.clear();
		}

		if (closedCards.size() == 0) changeState(GameState.FINISHED);
		else changeState(GameState.SELECT_1ST_CARD);
	}


	public void  finish() {
		for (ConnectionHandler handler : connectionHandlers.values()) handler.stop();
	}


	public int getCurrentPlayer() {
		return currentPlayer;
	}


	public List<Device> getConnectedDevices() {
		return new LinkedList<>(devices.values());
	}


	private void assertValidState(GameState state) {
		if (!currentState.equals(state)) throw new IllegalStateException("must be in state " + state + " to perform this action");
	}


	private void changeState(final GameState nextState) {
		currentState = nextState;
	}

    public void registerHostGameListener(HostGameListener l) {
        if(this.hostGameListener!=null) {
            throw new IllegalArgumentException("There's already a listener registered.");
        } else {
            this.hostGameListener = l;
        }
    }

    public void unregisterHostGameListener() {
        this.hostGameListener = null;
    }

	private final class HostMessageListener implements ConnectionHandler.MessageListener {

		private final int deviceId;

		public HostMessageListener(int deviceId) {
			this.deviceId = deviceId;
		}

		@Override
		public void onNewMessage(String msg) {
			switch(currentState) {
				case CONNECTING:
					String[] tokens = msg.split(" ");
					String deviceName = tokens[0];
					int pairsCount = Integer.valueOf(tokens[1]);
					devices.put(deviceId, new Device(deviceId, deviceName, pairsCount));
                    if(hostGameListener!=null) {
                        hostGameListener.onClientAdded();
                    }
					break;

				case SETUP:
					break;
			}
		}
	}
}
