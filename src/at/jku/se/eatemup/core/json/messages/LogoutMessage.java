package at.jku.se.eatemup.core.json.messages;

import at.jku.se.eatemup.core.json.CastType;
import at.jku.se.eatemup.core.json.MessageType;
import at.jku.se.eatemup.core.json.OutgoingMessage;

public class LogoutMessage extends OutgoingMessage {

	public String username;
	public String reason;
	public String userid;

	@Override
	public CastType getCastType() {
		return CastType.Unicast;
	}

	@Override
	public MessageType getType() {
		return MessageType.Logout;
	}

}
