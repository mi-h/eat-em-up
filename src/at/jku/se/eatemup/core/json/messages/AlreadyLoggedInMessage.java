package at.jku.se.eatemup.core.json.messages;

import at.jku.se.eatemup.core.json.CastType;
import at.jku.se.eatemup.core.json.MessageType;
import at.jku.se.eatemup.core.json.OutgoingMessage;

public class AlreadyLoggedInMessage extends OutgoingMessage {

	@Override
	public CastType getCastType() {
		return CastType.Unicast;
	}

	@Override
	public MessageType getType() {
		return MessageType.AlreadyLoggedIn;
	}

}
