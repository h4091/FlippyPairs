package org.faudroids.distributedmemory.core;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

final class MessageWriter implements MessageConstants {

	private final ObjectMapper mapper;

	@Inject
	public MessageWriter(ObjectMapper mapper) {
		this.mapper = mapper;
	}


	public JsonNode createAck() {
		return MSG_ACK;
	}


	/**
	 * Message about device details.
	 */
	public JsonNode createDeviceInfoMessage(Device device) {
		return mapper.valueToTree(device);
	}


	/**
	 * Message with multiple card IDs and their values.
	 */
	public JsonNode createCardsMessage(Map<Integer, Integer> cards) {
		ObjectNode msg = createBaseMessage();
		for (Map.Entry<Integer, Integer> e : cards.entrySet()) msg.put(e.getKey().toString(), e.getValue());
		return msg;
	}


	/**
	 * Message with the ID of one card.
	 */
	public JsonNode createCardIdMessage(int cardId) {
		ObjectNode msg = createBaseMessage();
		msg.put(KEY_CARD_ID, cardId);
		return msg;
	}


	/**
	 * Message about result of evaluation state and continues game.
	 */
	public JsonNode createEvaluationMessage(boolean cardsMatched, int nextPlayerId) {
		ObjectNode msg = createBaseMessage();
		msg.put(KEY_EVALUATION_CARDS_MATCHED, cardsMatched);
		msg.put(KEY_EVALUATION_CONTINUE_GAME, true);
		msg.put(KEY_EVALUATION_NEXT_PLAYER_ID, nextPlayerId);
		return msg;
	}


	/**
	 * Message about end of game.
	 */
	public JsonNode createEvaluationMessage(List<Player> winners) {
		ObjectNode msg = createBaseMessage();
		msg.put(KEY_EVALUATION_CARDS_MATCHED, true);
		msg.put(KEY_EVALUATION_CONTINUE_GAME, false);
		msg.put(KEY_EVALUATION_WINNERS, mapper.valueToTree(winners));
		return msg;
	}


	private ObjectNode createBaseMessage() {
		return new ObjectNode(JsonNodeFactory.instance);
	}

}