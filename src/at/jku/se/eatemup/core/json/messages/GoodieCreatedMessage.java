package at.jku.se.eatemup.core.json.messages;

import at.jku.se.eatemup.core.json.CastType;
import at.jku.se.eatemup.core.json.MessageType;
import at.jku.se.eatemup.core.json.OutgoingMessage;

public class GoodieCreatedMessage extends OutgoingMessage {

	public double latitude;
	public double longitude;
	public String specialAction;
	public int points;

	@Override
	public CastType getCastType() {
		return CastType.Broadcast;
	}

	@Override
	public MessageType getType() {
		return MessageType.GoodieCreated;
	}

}
