package org.faudroids.distributedmemory.core;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;

interface MessageConstants {

	static final JsonNode MSG_ACK = new TextNode("MSG_ACK");
	static final JsonNode MSG_BACKOFF = new TextNode("MSG_BACKOFF");

	static final String
			KEY_CARD_ID = "cardId",
			KEY_EVALUATION_CARDS_MATCHED = "cardsMatched",
			KEY_EVALUATION_CONTINUE_GAME = "continueGame",
			KEY_EVALUATION_NEXT_PLAYER_ID = "nextPlayerId",
			KEY_EVALUATION_WINNERS = "winners";


}
