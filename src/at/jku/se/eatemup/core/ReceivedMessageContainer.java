package at.jku.se.eatemup.core;

import java.util.HashMap;

import at.jku.se.eatemup.core.json.MessageType;

public class ReceivedMessageContainer {
	public MessageType type;
	public HashMap<String,Object> message;
}
