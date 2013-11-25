package at.jku.se.eatemup.core.json.messages;

import java.util.ArrayList;
import java.util.HashMap;

import at.jku.se.eatemup.core.json.CastType;
import at.jku.se.eatemup.core.json.MessageType;
import at.jku.se.eatemup.core.json.OutgoingMessage;

public class GameStartSurveyMessage extends OutgoingMessage {
	public ArrayList<HashMap<String, Object>> players;

	public GameStartSurveyMessage() {
		players = new ArrayList<>();
	}

	@Override
	public CastType getCastType() {
		return CastType.Multicast;
	}

	@Override
	public MessageType getType() {
		return MessageType.GameStartSurvey;
	}
}
