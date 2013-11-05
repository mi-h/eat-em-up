package at.jku.se.eatemup.core.model;

import at.jku.se.eatemup.core.BattleAnswer;
import at.jku.se.eatemup.core.BattleWinner;

public class Battle {
	private String username1;
	private String username2;
	private String question;
	private int time;
	/**
	 * first result results[0] is correct
	 */
	private int[] results;
	private BattleAnswer[] answers = new BattleAnswer[2];

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
	
	public boolean setAnswer(String username, int answer, long timestamp){
		BattleAnswer ba = new BattleAnswer(username,answer,timestamp);
		if (username1.equals(username)){
			answers[0] = ba;
		}
		if (username2.equals(username)){
			answers[1] = ba;
		}
		return allAnswersReady();
	}
	
	public boolean allAnswersReady(){
		return answers[0] != null && answers[1] != null;
	}
	
	public BattleWinner getWinner(){
		if (allAnswersReady()){
			BattleAnswer ba1 = answers[0];
			BattleAnswer ba2 = answers[1];
			if (isAnswerCorrect(ba1) && !isAnswerCorrect(ba2)){
				return BattleWinner.User1;
			}else if (!isAnswerCorrect(ba1) && isAnswerCorrect(ba2)){
				return BattleWinner.User2;
			} else if (isAnswerCorrect(ba1) && isAnswerCorrect(ba2)){
				if (ba1.timestamp<ba2.timestamp){
					return BattleWinner.User1;
				} else if (ba1.timestamp>ba2.timestamp){
					return BattleWinner.User2;
				}
			}
		}
		return BattleWinner.Draw;
	}
	
	private boolean isAnswerCorrect(BattleAnswer ba){
		return ba.answer == results[0];
	}
	
	public boolean isParticipant(String username){
		return username.equals(username1) || username.equals(username2);
	}
}
