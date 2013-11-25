package at.jku.se.eatemup.core.json.messages;

import java.util.ArrayList;
import java.util.HashMap;

import at.jku.se.eatemup.core.json.CastType;
import at.jku.se.eatemup.core.json.MessageType;
import at.jku.se.eatemup.core.json.OutgoingMessage;

public class HighscoreMessage extends OutgoingMessage {

	public ArrayList<HashMap<String, Object>> highscore;
	public int topx;

	@Override
	public CastType getCastType() {
		return CastType.Unicast;
	}

	@Override
	public MessageType getType() {
		return MessageType.Highscore;
	}

}
