package at.jku.se.eatemup.core.json.messages;

import java.util.Random;

import at.jku.se.eatemup.core.Engine;
import at.jku.se.eatemup.core.json.CastType;
import at.jku.se.eatemup.core.json.MessageType;
import at.jku.se.eatemup.core.json.OutgoingMessage;
import at.jku.se.eatemup.core.model.Battle;

public class BattleStartMessage extends OutgoingMessage {
	private static void shuffleArray(int[] ar) {
		Random rnd = new Random();
		for (int i = ar.length - 1; i > 0; i--) {
			int index = rnd.nextInt(i + 1);
			int a = ar[index];
			ar[index] = ar[i];
			ar[i] = a;
		}
	}

	public String username1;
	public String username2;
	public String userid1;
	public String userid2;
	public String question;
	public int timelimit;

	public int[] answers;

	@Override
	public CastType getCastType() {
		return CastType.Multicast;
	}

	@Override
	public MessageType getType() {
		return MessageType.BattleStart;
	}

	public void setBattle(Battle battle) {
		userid1 = battle.getUserid1();
		userid2 = battle.getUserid2();
		username1 = Engine.userManager.getUsernameByUserid(userid1);
		username2 = Engine.userManager.getUsernameByUserid(userid2);
		question = battle.getQuestion();
		timelimit = battle.getTime();
		answers = battle.getResult().clone();
		shuffleArray(answers);
	}
}
