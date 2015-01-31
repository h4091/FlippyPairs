package org.faudroids.distributedmemory.ui;


import android.content.Context;
import android.os.Build;

import com.fasterxml.jackson.databind.JsonNode;

import org.faudroids.distributedmemory.R;
import org.faudroids.distributedmemory.core.ClientGameManager;
import org.faudroids.distributedmemory.network.ConnectionHandler;

import javax.inject.Inject;

/**
 * Class for sharing client logic between regular clients ({@link org.faudroids.distributedmemory.ui.JoinGameActivity})
 * and the host client ({@link org.faudroids.distributedmemory.ui.HostService}).
 */
final class ClientUtils {

	private final ClientGameManager clientGameManager;
	private final Context context;

	@Inject
	public ClientUtils(ClientGameManager clientGameManager, Context context) {
		this.clientGameManager = clientGameManager;
		this.context = context;
	}


	/**
	 * Inits the client game manager registerseres this device with the manager.
	 */
	public void setupClient(ConnectionHandler<JsonNode> connectionHandler) {
		int cardsCount = context.getResources().getInteger(R.integer.grid_column_count)
				* context.getResources().getInteger(R.integer.grid_row_count);

		clientGameManager.initGame();
		clientGameManager.registerDevice(connectionHandler, Build.MODEL, cardsCount / 2);
	}

}
