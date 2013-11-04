package at.jku.se.eatemup.core.json.messages;

import at.jku.se.eatemup.core.json.CastType;
import at.jku.se.eatemup.core.json.MessageType;
import at.jku.se.eatemup.core.json.OutgoingMessage;

public class GameStartSurveyMessage extends OutgoingMessage {
	public String requestingUser;

	@Override
	public MessageType getType() {
		return MessageType.GameStartSurvey;
	}

	@Override
	public CastType getCastType() {
		return CastType.Multicast;
	}
}
