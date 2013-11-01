package at.jku.se.eatemup.core.logic;

import java.util.Random;

import at.jku.se.eatemup.core.model.Battle;

public class BattleCreator {
	
	private static final int addTime = 5;
	private static final int subTime = 5;
	private static final int multTime = 7;
	private static final int divTime = 10;

	public static Battle CreateBattle(String user1, String user2){
		Battle battle = new Battle();
		battle.setUsername1(user1);
		battle.setUsername2(user2);
		Object[] q = createQuestion();
		battle.setQuestion((String)q[0]);
		battle.setResult((int)q[1]);
		battle.setTime((int)q[2]);
		return battle;
	}
	
	private static Object[] createQuestion(){
		Random rand = new Random();
		switch(rand.nextInt(4)+1){
		case 1:return createAddQuestion(rand);
		case 2:return createSubQuestion(rand);
		case 3:return createMultQuestion(rand);
		default:return createDivQuestion(rand);
		}
	}

	private static Object[] createDivQuestion(Random rand) {
		Object[] ret = new Object[3];
		int n1;
		do{
			n1 = rand.nextInt(100)+1;
		} while (n1%2!=0);
		int n2;
		do{
			n2 = rand.nextInt(100)+1;
		} while (n1%n2!=0);
		int res = n1-n2;
		String q = n1+" / "+n2;
		ret[0] = q;
		ret[1] = res;
		ret[2] = divTime;
		return ret;
	}

	private static Object[] createMultQuestion(Random rand) {
		Object[] ret = new Object[3];
		int n1 = rand.nextInt(10)+1;
		int n2 = rand.nextInt(10)+1;
		int res = n1*n2;
		String q = n1+" * "+n2;
		ret[0] = q;
		ret[1] = res;
		ret[2] = multTime;
		return ret;
	}

	private static Object[] createSubQuestion(Random rand) {
		Object[] ret = new Object[3];
		int n1 = rand.nextInt(100)+1;
		int n2;
		do{
			n2 = rand.nextInt(100)+1;
		} while (n2>n1);
		int res = n1-n2;
		String q = n1+" - "+n2;
		ret[0] = q;
		ret[1] = res;
		ret[2] = subTime;
		return ret;
	}

	private static Object[] createAddQuestion(Random rand) {
		Object[] ret = new Object[3];
		int n1 = rand.nextInt(100)+1;
		int n2 = rand.nextInt(100)+1;
		int res = n1+n2;
		String q = n1+" + "+n2;
		ret[0] = q;
		ret[1] = res;
		ret[2] = addTime;
		return ret;
	}
}
