package at.jku.se.eatemup.core.json.messages;

import at.jku.se.eatemup.core.json.IncomingMessage;
import at.jku.se.eatemup.core.json.MessageType;

public class LoginMessage extends IncomingMessage {

	public String password;
	public String type;
	public String facebookId;

	@Override
	public MessageType getType() {
		return MessageType.Login;
	}
}
