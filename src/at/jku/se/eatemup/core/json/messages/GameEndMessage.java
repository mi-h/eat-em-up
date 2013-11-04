package at.jku.se.eatemup.core.json.messages;

import java.util.ArrayList;
import java.util.HashMap;

import at.jku.se.eatemup.core.json.CastType;
import at.jku.se.eatemup.core.json.MessageType;
import at.jku.se.eatemup.core.json.OutgoingMessage;

public class GameEndMessage extends OutgoingMessage {
	public boolean teamRedWin;
	public ArrayList<HashMap<String, Object>> playerResults;

	@Override
	public MessageType getType() {
		return MessageType.GameEnd;
	}

	@Override
	public CastType getCastType() {
		return CastType.Broadcast;
	}
}
