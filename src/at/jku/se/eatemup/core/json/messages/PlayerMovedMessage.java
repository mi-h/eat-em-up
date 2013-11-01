package at.jku.se.eatemup.core.json.messages;

import java.util.HashMap;

import at.jku.se.eatemup.core.json.CastType;
import at.jku.se.eatemup.core.json.DirectionType;
import at.jku.se.eatemup.core.json.Message;
import at.jku.se.eatemup.core.json.MessageType;

public class PlayerMovedMessage extends Message {
	public String username;
	public HashMap<String, Integer> position;

	@Override
	public MessageType getType() {
		return MessageType.PlayerMoved;
	}

	@Override
	public DirectionType getDirection() {
		return DirectionType.Outgoing;
	}

	@Override
	public CastType getCastType() {
		return CastType.Broadcast;
	}
}
