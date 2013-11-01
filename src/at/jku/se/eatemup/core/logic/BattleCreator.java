package at.jku.se.eatemup.core.logic;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

import at.jku.se.eatemup.core.model.Battle;

public class BattleCreator {
	
	private static final int addTime = 5;
	private static final int subTime = 5;
	private static final int multTime = 7;
	private static final int divTime = 10;
	private static final int addMaxValue = 100;
	private static final int subMaxValue = 100;
	private static final int multMaxValue = 10;
	private static final int divMaxValue = 100;

	public static Battle CreateBattle(String user1, String user2){
		Battle battle = new Battle();
		battle.setUsername1(user1);
		battle.setUsername2(user2);
		Random rand = new Random();
		int dec = rand.nextInt(4)+1;
		Object[] q = createQuestion(dec,rand);
		battle.setQuestion((String)q[0]);
		int[] results = createResults((int)q[1],dec==4?true:false);
		battle.setResult(results);
		battle.setTime((int)q[2]);
		return battle;
	}
	
	private static int[] createResults(int correctResult, boolean even) {
		Random rand = new Random();
		HashSet<Integer> existingVals = new HashSet<>(4);
		existingVals.add(correctResult);
		existingVals.add(getNextGauss(correctResult,rand,1,existingVals,even));
		existingVals.add(getNextGauss(correctResult,rand,2,existingVals,even));
		existingVals.add(getNextGauss(correctResult,rand,1,existingVals,even));
		int[] arr = new int[4];
		int cnt = 0;
		  for (int i : existingVals){
		   arr[cnt] = i;
		   cnt++;
		  }
		return arr;
	}
	
	private static int getNextGauss(int correct, Random rand, int interval, HashSet<Integer> existing, boolean even){
		int val;
		if (correct<=5){
			do{
			val = rand.nextInt(10)+1;
			} while (val<0 || existing.contains(val) || (even && val%2!=0));
		} else {
			do{
				val = (int) Math.round(correct + (correct / (double) interval) * rand.nextGaussian());
			} while (val<0 || existing.contains(val) || (even && val%2!=0));
		}
		return val;
	}

	private static Object[] createQuestion(int decision, Random rand){		
		switch(decision){
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
			n1 = rand.nextInt(divMaxValue)+1;
		} while (n1%2!=0);
		int n2;
		do{
			n2 = rand.nextInt(divMaxValue)+1;
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
		int n1 = rand.nextInt(multMaxValue)+1;
		int n2 = rand.nextInt(multMaxValue)+1;
		int res = n1*n2;
		String q = n1+" * "+n2;
		ret[0] = q;
		ret[1] = res;
		ret[2] = multTime;
		return ret;
	}

	private static Object[] createSubQuestion(Random rand) {
		Object[] ret = new Object[3];
		int n1 = rand.nextInt(subMaxValue)+1;
		int n2;
		do{
			n2 = rand.nextInt(subMaxValue)+1;
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
		int n1 = rand.nextInt(addMaxValue)+1;
		int n2 = rand.nextInt(addMaxValue)+1;
		int res = n1+n2;
		String q = n1+" + "+n2;
		ret[0] = q;
		ret[1] = res;
		ret[2] = addTime;
		return ret;
	}
}
