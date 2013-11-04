package at.jku.se.eatemup.core.json.messages;

import java.util.ArrayList;
import java.util.HashMap;

import at.jku.se.eatemup.core.json.CastType;
import at.jku.se.eatemup.core.json.MessageType;
import at.jku.se.eatemup.core.json.OutgoingMessage;

public class GameStartMessage extends OutgoingMessage {
	public int remainingTime;
	public ArrayList<HashMap<String, Object>> playerInfo;
	public ArrayList<HashMap<String, Object>> goodies;

	@Override
	public MessageType getType() {
		return MessageType.GameStart;
	}

	@Override
	public CastType getCastType() {
		return CastType.Broadcast;
	}
}
