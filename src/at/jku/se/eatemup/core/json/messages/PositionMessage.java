package at.jku.se.eatemup.core.json.messages;

import at.jku.se.eatemup.core.json.CastType;
import at.jku.se.eatemup.core.json.DirectionType;
import at.jku.se.eatemup.core.json.Message;
import at.jku.se.eatemup.core.json.MessageType;

public class PositionMessage extends Message {
	public String username;
	public double latitude;
	public double longitude;
	public long timestamp;

	@Override
	public MessageType getType() {
		return MessageType.Position;
	}

	@Override
	public DirectionType getDirection() {
		return DirectionType.Incoming;
	}

	@Override
	public CastType getCastType() {
		return CastType.None;
	}
}
