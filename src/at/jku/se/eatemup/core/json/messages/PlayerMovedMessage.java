package at.jku.se.eatemup.core.json.messages;

import java.util.HashMap;

import at.jku.se.eatemup.core.json.CastType;
import at.jku.se.eatemup.core.json.MessageType;
import at.jku.se.eatemup.core.json.OutgoingMessage;

public class PlayerMovedMessage extends OutgoingMessage {
	public String username;
	public String userid;
	public HashMap<String, Double> position;

	@Override
	public MessageType getType() {
		return MessageType.PlayerMoved;
	}

	@Override
	public CastType getCastType() {
		return CastType.Broadcast;
	}
}
