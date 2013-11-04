package at.jku.se.eatemup.core;

import java.util.ArrayList;

import at.jku.se.eatemup.core.json.CastType;
import at.jku.se.eatemup.core.json.DirectionType;
import at.jku.se.eatemup.core.json.Message;
import at.jku.se.eatemup.core.json.MessageType;

public class MessageContainer {
	public MessageType type;
	public Message message;
	public DirectionType direction;
	public ArrayList<String> receivers;
	public Sender sender;
	public CastType castType;
}
