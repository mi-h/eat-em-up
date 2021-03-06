package at.jku.se.eatemup.core.json.messages;

import at.jku.se.eatemup.core.json.CastType;
import at.jku.se.eatemup.core.json.MessageType;
import at.jku.se.eatemup.core.json.OutgoingMessage;

public class ReadyForGameMessage extends OutgoingMessage {

	public boolean loginSuccess;
	public Integer points;
	public String adCode;
	public String userid;

	@Override
	public CastType getCastType() {
		return CastType.Unicast;
	}

	@Override
	public MessageType getType() {
		return MessageType.ReadyForGame;
	}
}
