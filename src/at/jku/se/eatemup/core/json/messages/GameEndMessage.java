package at.jku.se.eatemup.core.json.messages;

import java.util.ArrayList;
import java.util.HashMap;

import at.jku.se.eatemup.core.json.Message;

public class GameEndMessage extends Message{
	public boolean teamRedWin;
	public ArrayList<HashMap<String,Object>> playerResults;
}
