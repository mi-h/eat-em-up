package at.jku.se.eatemup.core.json.messages;

import at.jku.se.eatemup.core.json.CastType;
import at.jku.se.eatemup.core.json.MessageType;
import at.jku.se.eatemup.core.json.OutgoingMessage;

public class PingMessage extends OutgoingMessage {
	
	public String username;
	public String userid;
	public boolean secondAttempt;

	@Override
	public CastType getCastType() {
		return CastType.Unicast;
	}

	@Override
	public MessageType getType() {
		return MessageType.Ping;
	}

}
