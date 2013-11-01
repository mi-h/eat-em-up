package at.jku.se.eatemup.core.json.messages;

import java.util.ArrayList;
import java.util.HashMap;

import at.jku.se.eatemup.core.json.Message;

public class GameStandbyUpdateMessage extends Message {
	
	public ArrayList<HashMap<String,Object>> players;
	public boolean readyForStart;	
}
