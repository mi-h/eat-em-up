package at.jku.se.eatemup.core.json.messages;

import at.jku.se.eatemup.core.json.CastType;
import at.jku.se.eatemup.core.json.MessageType;
import at.jku.se.eatemup.core.json.OutgoingMessage;

public class SpecialActionDeactivatedMessage extends OutgoingMessage {
	public String specialAction;
	public String username;
	public String userid;

	@Override
	public CastType getCastType() {
		return CastType.Broadcast;
	}

	@Override
	public MessageType getType() {
		return MessageType.SpecialActionDeactivated;
	}
}
