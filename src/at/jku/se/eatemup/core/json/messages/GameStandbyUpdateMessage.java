package at.jku.se.eatemup.core.json.messages;

import java.util.ArrayList;
import java.util.HashMap;

import at.jku.se.eatemup.core.json.CastType;
import at.jku.se.eatemup.core.json.MessageType;
import at.jku.se.eatemup.core.json.OutgoingMessage;

public class GameStandbyUpdateMessage extends OutgoingMessage {

	public ArrayList<HashMap<String, Object>> players;
	public boolean readyForStart;

	@Override
	public MessageType getType() {
		return MessageType.GameStandbyUpdate;
	}

	@Override
	public CastType getCastType() {
		return CastType.Broadcast;
	}
}
