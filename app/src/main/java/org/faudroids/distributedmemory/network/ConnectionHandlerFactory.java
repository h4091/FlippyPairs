package org.faudroids.distributedmemory.network;


import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

import javax.inject.Inject;

final class ConnectionHandlerFactory {

	@Inject
	public ConnectionHandlerFactory() { }


	public ConnectionHandler<String> createStringConnectionHandler(Socket socket) throws IOException {
		return new StringConnectionHandler(socket);
	}


	public ConnectionHandler<String> createStringConnectionHandler(InetAddress inetAddress, int port) throws IOException {
		return new StringConnectionHandler(inetAddress, port);
	}


	public ConnectionHandler<JsonNode> createJsonConnectionHandler(Socket socket) throws IOException {
		return new JsonConnectionHandler(createStringConnectionHandler(socket));
	}


	public ConnectionHandler<JsonNode> createJsonConnectionHandler(InetAddress inetAddress, int port) throws IOException {
		return new JsonConnectionHandler(createStringConnectionHandler(inetAddress, port));
	}

}
