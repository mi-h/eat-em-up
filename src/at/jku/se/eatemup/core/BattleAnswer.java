package at.jku.se.eatemup.core;

public class BattleAnswer {
	public String username;
	public int answer;
	public long timestamp;
	
	public BattleAnswer(String username, int answer, long timestamp){
		this.username = username;
		this.answer = answer;
		this.timestamp = timestamp;
	}
}
