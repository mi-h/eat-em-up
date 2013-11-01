package at.jku.se.eatemup.core.json.messages;

import at.jku.se.eatemup.core.json.CastType;
import at.jku.se.eatemup.core.json.DirectionType;
import at.jku.se.eatemup.core.json.Message;
import at.jku.se.eatemup.core.json.MessageType;

public class RequestForGameStartMessage extends Message {
	public String username;
	public boolean startGame;

	@Override
	public MessageType getType() {
		return MessageType.RequestForGameStart;
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
