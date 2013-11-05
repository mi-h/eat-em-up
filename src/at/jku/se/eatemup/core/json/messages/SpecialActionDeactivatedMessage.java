package at.jku.se.eatemup.core.json.messages;

import at.jku.se.eatemup.core.json.CastType;
import at.jku.se.eatemup.core.json.MessageType;
import at.jku.se.eatemup.core.json.OutgoingMessage;

public class SpecialActionDeactivatedMessage extends OutgoingMessage {
	public String specialAction;

	@Override
	public MessageType getType() {
		return MessageType.SpecialActionDeactivated;
	}

	@Override
	public CastType getCastType() {
		return CastType.Unicast;
	}
}
