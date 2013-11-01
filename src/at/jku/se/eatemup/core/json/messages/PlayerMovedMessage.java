package at.jku.se.eatemup.core.json.messages;

import java.util.HashMap;

import at.jku.se.eatemup.core.json.Message;

public class PlayerMovedMessage extends Message{
	public String username;
	public HashMap<String,Integer> position;
}
