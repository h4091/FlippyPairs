package org.faudroids.distributedmemory.core;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

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
	 * Message info required for initial client setup.
	 */
	public JsonNode createSetupMessage(GameSetupInfo gameSetupInfo) {
		return mapper.valueToTree(gameSetupInfo);
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
	 * Message about result of evaluation.
	 */
	public JsonNode createEvaluationMessage(Evaluation evaluation) {
		return mapper.valueToTree(evaluation);
	}


	private ObjectNode createBaseMessage() {
		return new ObjectNode(JsonNodeFactory.instance);
	}

}
