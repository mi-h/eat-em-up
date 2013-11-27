package at.jku.se.eatemup.core.json.messages;

import at.jku.se.eatemup.core.json.IncomingMessage;
import at.jku.se.eatemup.core.json.MessageType;

public class LeaveGameMessage extends IncomingMessage {

	@Override
	public MessageType getType() {
		return MessageType.LeaveGame;
	}

}
