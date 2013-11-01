package at.jku.se.eatemup.core.json;

import java.util.ArrayList;

public class MessageContainer {
	public MessageType type;
	public Message message;
	public DirectionType direction;
	public ArrayList<String> receivers;
	public String sender;
}
