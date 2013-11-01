package at.jku.se.eatemup.core.json.messages;

import java.util.ArrayList;
import java.util.HashMap;

import at.jku.se.eatemup.core.json.CastType;
import at.jku.se.eatemup.core.json.DirectionType;
import at.jku.se.eatemup.core.json.Message;
import at.jku.se.eatemup.core.json.MessageType;

public class GameEndMessage extends Message {
	public boolean teamRedWin;
	public ArrayList<HashMap<String, Object>> playerResults;

	@Override
	public MessageType getType() {
		return MessageType.GameEnd;
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
