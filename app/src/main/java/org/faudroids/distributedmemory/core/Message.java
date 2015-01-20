package org.faudroids.distributedmemory.core;

final class Message {

	private Message() { }

	static final String
			ACK = "ACK",
			EVALUATION_MISS = "MISS",
			EVALUATION_MATCH_CONTINUE = "MATCH CONTINUE",
			EVALUATION_MATCH_FINISH = "MATCH FINISH";

}
