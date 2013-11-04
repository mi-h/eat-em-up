package at.jku.se.eatemup.core.json.messages;

import at.jku.se.eatemup.core.json.CastType;
import at.jku.se.eatemup.core.json.MessageType;
import at.jku.se.eatemup.core.json.OutgoingMessage;

public class LogoutMessage extends OutgoingMessage {

	public String username;
	public String reason;

	@Override
	public MessageType getType() {
		return MessageType.Logout;
	}

	@Override
	public CastType getCastType() {
		return CastType.Unicast;
	}

}
