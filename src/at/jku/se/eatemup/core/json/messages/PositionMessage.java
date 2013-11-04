package at.jku.se.eatemup.core.json.messages;

import at.jku.se.eatemup.core.json.IncomingMessage;
import at.jku.se.eatemup.core.json.MessageType;

public class PositionMessage extends IncomingMessage {
	public double latitude;
	public double longitude;
	public long timestamp;

	@Override
	public MessageType getType() {
		return MessageType.Position;
	}
}
