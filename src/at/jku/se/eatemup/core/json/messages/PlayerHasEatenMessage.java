package at.jku.se.eatemup.core.json.messages;

import java.util.HashMap;

import at.jku.se.eatemup.core.json.Message;

public class PlayerHasEatenMessage extends Message{
	public String username;
	public HashMap<String,Integer> goodie;
	public int newPoints;
	public HashMap<String,Object> team;
}
