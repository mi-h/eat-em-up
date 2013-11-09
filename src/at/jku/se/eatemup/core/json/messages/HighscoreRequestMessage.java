package at.jku.se.eatemup.core.json.messages;

import at.jku.se.eatemup.core.json.IncomingMessage;
import at.jku.se.eatemup.core.json.MessageType;

public class HighscoreRequestMessage extends IncomingMessage {
	
	public int topx;

	@Override
	public MessageType getType() {
		return MessageType.HighscoreRequest;
	}

}
