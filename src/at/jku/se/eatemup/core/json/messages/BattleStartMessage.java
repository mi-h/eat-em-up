package at.jku.se.eatemup.core.json.messages;

import at.jku.se.eatemup.core.json.Message;

public class BattleStartMessage extends Message{
	public String username1;
	public String username2;
	public String question;
	public int timelimit;
}
