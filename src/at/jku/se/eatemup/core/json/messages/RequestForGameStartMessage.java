package at.jku.se.eatemup.core.json.messages;

import at.jku.se.eatemup.core.json.IncomingMessage;
import at.jku.se.eatemup.core.json.MessageType;

public class RequestForGameStartMessage extends IncomingMessage {
	public boolean startGame;

	@Override
	public MessageType getType() {
		return MessageType.RequestForGameStart;
	}
}
