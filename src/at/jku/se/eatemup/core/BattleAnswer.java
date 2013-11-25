package at.jku.se.eatemup.core;

public class BattleAnswer {
	public String userid;
	public int answer;
	public long timestamp;

	public BattleAnswer(String userid, int answer, long timestamp) {
		this.userid = userid;
		this.answer = answer;
		this.timestamp = timestamp;
	}
}
