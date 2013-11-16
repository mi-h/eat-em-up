package at.jku.se.eatemup.core.json.messages;

import at.jku.se.eatemup.core.json.IncomingMessage;
import at.jku.se.eatemup.core.json.MessageType;

public class PongMessage extends IncomingMessage {
	
	public boolean secondAttempt;
	public String pingid;

	@Override
	public MessageType getType() {
		return MessageType.Pong;
	}

}
