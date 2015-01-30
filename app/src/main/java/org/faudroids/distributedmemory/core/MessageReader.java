package org.faudroids.distributedmemory.core;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

final class MessageReader implements MessageConstants {

	private final ObjectMapper mapper;

	@Inject
	public MessageReader(ObjectMapper mapper) {
		this.mapper = mapper;
	}


	public boolean isAck(JsonNode msg) {
		return msg.equals(MessageWriter.MSG_ACK);
	}


	public Device readDeviceInfoMessage(JsonNode msg) {
		try {
			return mapper.treeToValue(msg, Device.class);
		} catch (IOException ioe) {
			throw new RuntimeException("failed to read Device", ioe);
		}
	}


	public Map<Integer, Integer> readCardsMessage(JsonNode msg) {
		Map<Integer, Integer> cards = new HashMap<>();
		Iterator<Map.Entry<String, JsonNode>> iter = msg.fields();
		while (iter.hasNext()) {
			Map.Entry<String, JsonNode> card = iter.next();
			cards.put(Integer.valueOf(card.getKey()), card.getValue().asInt());
		}
		return cards;
	}


	public int readCardIdMessage(JsonNode msg) {
		return msg.get(KEY_CARD_ID).asInt();
	}


	public boolean readEvaluationContinueGame(JsonNode msg) {
		return msg.get(KEY_EVALUATION_CONTINUE_GAME).asBoolean();
	}


	public boolean readEvaluationCardsMatched(JsonNode msg) {
		return msg.get(KEY_EVALUATION_CARDS_MATCHED).asBoolean();
	}


	public int readEvaluationNextPlayerId(JsonNode msg) {
		return msg.get(KEY_EVALUATION_NEXT_PLAYER_ID).asInt();
	}


	public List<Player> readEvaluationWinners(JsonNode msg) {
		try {
			return mapper.readValue(msg.get(KEY_EVALUATION_WINNERS).toString(), new TypeReference<List<Player>>() {
			});
		} catch (IOException ioe) {
			throw new RuntimeException("failed to parse winners", ioe);
		}
	}

}
