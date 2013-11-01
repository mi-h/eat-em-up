package at.jku.se.eatemup.core.json.messages;

import java.util.HashMap;

import at.jku.se.eatemup.core.json.CastType;
import at.jku.se.eatemup.core.json.DirectionType;
import at.jku.se.eatemup.core.json.Message;
import at.jku.se.eatemup.core.json.MessageType;

public class PlayerHasEatenMessage extends Message {
	public String username;
	public HashMap<String, Integer> goodie;
	public int newPoints;
	public HashMap<String, Object> team;

	@Override
	public MessageType getType() {
		return MessageType.PlayerHasEaten;
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
