package at.jku.se.eatemup.core.model;

import at.jku.se.eatemup.core.BattleAnswer;
import at.jku.se.eatemup.core.BattleWinner;

public class Battle {
	private String userid1;
	private String userid2;
	private String question;
	private int time;
	/**
	 * first result results[0] is correct
	 */
	private int[] results;
	private BattleAnswer[] answers = new BattleAnswer[2];

	public String getUserid1() {
		return userid1;
	}

	public void setUserid1(String userid1) {
		this.userid1 = userid1;
	}

	public String getUserid2() {
		return userid2;
	}

	public void setUserid2(String userid2) {
		this.userid2 = userid2;
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
	
	public boolean setAnswer(String userid, int answer, long timestamp){
		BattleAnswer ba = new BattleAnswer(userid,answer,timestamp);
		if (userid1.equals(userid)){
			answers[0] = ba;
		} else if (userid2.equals(userid)){
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
	
	public boolean isParticipant(String userid){
		return userid.equals(userid1) || userid.equals(userid2);
	}
}
