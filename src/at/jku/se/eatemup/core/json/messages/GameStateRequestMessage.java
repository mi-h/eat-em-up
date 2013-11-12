package at.jku.se.eatemup.core.json.messages;

import at.jku.se.eatemup.core.json.IncomingMessage;
import at.jku.se.eatemup.core.json.MessageType;

public class GameStateRequestMessage extends IncomingMessage {
	
	public String username;

	@Override
	public MessageType getType() {
		return MessageType.GameStateRequest;
	}

}
