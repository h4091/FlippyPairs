package org.faudroids.distributedmemory.core;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.faudroids.distributedmemory.utils.Assert;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.inject.Singleton;

import timber.log.Timber;


@Singleton
public final class GameManager {

	private final Map<Integer, Card> closedCards = new HashMap<>();
	private final List<Card> selectedCards = new LinkedList<>();
	private final Map<Integer, Card> matchedCards = new HashMap<>();

	private final Map<Integer, Device> devices = new HashMap<>();
	private int setupDevices = 0;

    private final List<Player> players = new ArrayList<>();

	private GameState currentState = GameState.CONNECTING;
    private int currentPlayer;

	// used to postpone execution of tasks until method is finished (dirty hack?!)
	private final Handler handler = new Handler(Looper.getMainLooper());


	/**
	 * Register a device containing information about how many pairs it can show on its screen.
	 */
	public Device addDevice(String name, int pairsCount) {
		assertValidState(GameState.CONNECTING);
		Device device = new Device(devices.size(), name, pairsCount);
		devices.put(device.getId(), device);
		return device;
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

        currentPlayer = 0;
    }


	public List<Card> getCardsForDevice(int deviceId) {
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


	public void selectFirstCard(int cardId) {
		assertValidState(GameState.SELECT_1ST_CARD);
		Assert.assertTrue(closedCards.containsKey(cardId), "invalid or close card with id " + cardId);

		Card card = closedCards.remove(cardId);
		selectedCards.add(card);

		changeState(GameState.SELECT_2ND_CARD);
	}


	public void selectSecondCard(int cardId) {
		assertValidState(GameState.SELECT_2ND_CARD);
		Assert.assertTrue(closedCards.containsKey(cardId), "invalid or close card with id " + cardId);

		Card card = closedCards.remove(cardId);
		selectedCards.add(card);

		changeState(GameState.UPDATE_CARDS);
	}


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


	public int getCurrentPlayer() {
		return currentPlayer;
	}


	private void assertValidState(GameState state) {
		if (!currentState.equals(state)) throw new IllegalStateException("must be in state " + state + " to perform this action");
	}


	private void changeState(GameState nextState) {
		currentState = nextState;
	}

}
