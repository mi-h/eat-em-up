package at.jku.se.eatemup.core.json.messages;

import at.jku.se.eatemup.core.json.CastType;
import at.jku.se.eatemup.core.json.MessageType;
import at.jku.se.eatemup.core.json.OutgoingMessage;

public class SpecialActionActivatedMessage extends OutgoingMessage {
	public String specialAction;
	public String username;

	@Override
	public MessageType getType() {
		return MessageType.SpecialActionActivated;
	}

	@Override
	public CastType getCastType() {
		return CastType.Broadcast;
	}
}
