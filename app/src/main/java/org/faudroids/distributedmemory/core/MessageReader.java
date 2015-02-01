package org.faudroids.distributedmemory.core;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

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


	public boolean isBackoffMessage(JsonNode msg) {
		return msg.equals(MSG_BACKOFF);
	}


	public Device readDeviceInfoMessage(JsonNode msg) {
		try {
			return mapper.treeToValue(msg, Device.class);
		} catch (IOException ioe) {
			throw new RuntimeException("failed to read Device", ioe);
		}
	}


	public GameSetupInfo readSetupMessage(JsonNode msg) {
		try {
			return mapper.treeToValue(msg, GameSetupInfo.class);
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}


	public int readCardIdMessage(JsonNode msg) {
		return msg.get(KEY_CARD_ID).asInt();
	}


	public Evaluation readEvaluation(JsonNode msg) {
		try {
			return mapper.treeToValue(msg, Evaluation.class);
		} catch (IOException ioe) {
			throw new RuntimeException("failed to parse evaluation results", ioe);
		}
	}

}
