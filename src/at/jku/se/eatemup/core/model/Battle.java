package at.jku.se.eatemup.core.model;

public class Battle {
	private String username1;
	private String username2;
	private String question;
	private int time;
	/**
	 * first result results[0] is correct
	 */
	private int[] results;

	public String getUsername1() {
		return username1;
	}

	public void setUsername1(String username1) {
		this.username1 = username1;
	}

	public String getUsername2() {
		return username2;
	}

	public void setUsername2(String username2) {
		this.username2 = username2;
	}

	public String getQuestion() {
		return question;
	}

	public void setQuestion(String question) {
		this.question = question;
	}

	public int[] getResult() {
		return results;
	}

	public void setResult(int[] results) {
		this.results = results;
	}

	public int getTime() {
		return time;
	}

	public void setTime(int time) {
		this.time = time;
	}
}
