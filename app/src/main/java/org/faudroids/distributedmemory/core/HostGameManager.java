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

	private GameState currentState = GameState.CONNECTING;

    private HostGameListener hostGameListener;

    private int acks = 0;

	// used to postpone execution of tasks until method is finished (dirty hack?!)
	private final Handler handler = new Handler(Looper.getMainLooper());


	@Inject
	public HostGameManager() { }

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
        for(int id = 0; id < pairsCount * 2; ++id) {
            int randomValue = rand.nextInt(pairsCount);
            int randomId = rand.nextInt(pairsCount);
            if(closedCards.get(randomId)!=null) {
                closedCards.put(id, new Card(id, randomValue));
            }
            Timber.i("Value " + id + " : " + closedCards.get(id).getValue());
        }

		// TODO race condition between connections beeing added and clients seding device info

		// send card details to devices
		int currentPairCount = 0;
		List<Card> allCards = new ArrayList<>(closedCards.values());

		for (Integer id : devices.keySet()) {
			ConnectionHandler connectionHandler = connectionHandlers.get(id);
			Device device = devices.get(id);
			StringBuilder msgBuilder = new StringBuilder();

			for (int i = 0; i < device.getPairsCount(); ++i) {
				Card card = allCards.get(currentPairCount);
				++currentPairCount;
				msgBuilder.append("(").append(card.getId()).append(",").append(card.getValue()).append(")");
			}
			connectionHandler.sendMessage(msgBuilder.toString());
		}
    }



	/**
	 * Store the first card selected.
	 */
	public void selectFirstCard(int cardId) {
		assertValidState(GameState.SELECT_1ST_CARD);
		Assert.assertTrue(closedCards.containsKey(cardId), "invalid or close card with id " + cardId);

		Card card = closedCards.remove(cardId);
		selectedCards.add(card);
        broadcast(Integer.toString(cardId));

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
        broadcast(Integer.toString(cardId));

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
            broadcast("MATCH");
		} else {
            for (Card card : selectedCards) closedCards.put(card.getId(), card);
            selectedCards.clear();
            broadcast("MISS");
        }

		if (closedCards.size() == 0) changeState(GameState.FINISHED);
		else changeState(GameState.SELECT_1ST_CARD);
	}


	public void  finish() {
		for (ConnectionHandler handler : connectionHandlers.values()) handler.stop();
	}


	public List<Device> getConnectedDevices() {
		return new LinkedList<>(devices.values());
	}


	private void assertValidState(GameState state) {
		if (!currentState.equals(state)) throw new IllegalStateException("must be in state " + state + " to perform this action");
	}


	private void changeState(final GameState nextState) {
        if(this.acks==devices.size()) {
            currentState = nextState;
            this.acks=0;
        }
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

        public boolean checkForAck(String msg) {
            if(msg.compareTo("ACK")==0) {
                ++acks;
                return true;
            } else {
                return false;
            }
        }

		@Override
		public void onNewMessage(String msg) {
			Timber.i("Server received msg from device " + deviceId + ": " + msg);
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
                    checkForAck(msg);
					break;

                case SELECT_1ST_CARD:
                    if(!checkForAck(msg)) {
                        int id = Integer.parseInt(msg);
                        selectFirstCard(id);
                    }
                    break;

                case SELECT_2ND_CARD:
                    if(!checkForAck(msg)) {
                        int id = Integer.parseInt(msg);
                        selectSecondCard(id);
                    }
                    break;
			}
		}
	}
}
