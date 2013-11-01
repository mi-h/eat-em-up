package at.jku.se.eatemup.core.json.messages;

import java.util.ArrayList;
import java.util.HashMap;

import at.jku.se.eatemup.core.json.Message;

public class GameStartMessage extends Message{
	public int remainingTime;
	public ArrayList<HashMap<String,Object>> playerInfo;
	public ArrayList<HashMap<String,Object>> goodies;
}
