package org.faudroids.distributedmemory.core;

import android.os.Handler;
import android.os.Looper;

import org.faudroids.distributedmemory.network.ConnectionHandler;

import javax.inject.Inject;
import javax.inject.Singleton;

import timber.log.Timber;


@Singleton
public final class ClientGameManager implements ConnectionHandler.MessageListener {

	private ConnectionHandler connectionHandler;
	private String deviceName;
	private int pairsCount;

	private GameState currentState = GameState.CONNECTING;

	@Inject
	public ClientGameManager() { }

	/**
	 * Registers a device with this manager.
	 * Call in state {@link GameState#CONNECTING}.
	 */
	public void register(ConnectionHandler connectionHandler, String deviceName, int pairsCount) {
		assertValidState(GameState.CONNECTING);

		this.connectionHandler = connectionHandler;
		this.deviceName = deviceName;
		this.pairsCount = pairsCount;

		connectionHandler.registerMessageListener(this, new Handler(Looper.myLooper()));
		connectionHandler.start();
	}


	private void assertValidState(GameState state) {
		if (!currentState.equals(state)) throw new IllegalStateException("must be in state " + state + " to perform this action");
	}


	public void stopGame() {
		connectionHandler.stop();
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
				break;
		}
	}

}
