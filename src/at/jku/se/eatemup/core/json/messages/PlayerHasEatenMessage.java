package at.jku.se.eatemup.core.json.messages;

import java.util.HashMap;

import at.jku.se.eatemup.core.json.CastType;
import at.jku.se.eatemup.core.json.MessageType;
import at.jku.se.eatemup.core.json.OutgoingMessage;

public class PlayerHasEatenMessage extends OutgoingMessage {
	public String username;
	public String userid;
	public HashMap<String, Double> goodie;
	public int points;
	public HashMap<String, Object> team;

	@Override
	public CastType getCastType() {
		return CastType.Broadcast;
	}

	@Override
	public MessageType getType() {
		return MessageType.PlayerHasEaten;
	}
}
