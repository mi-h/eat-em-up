package at.jku.se.eatemup.core.json.messages;

import at.jku.se.eatemup.core.json.IncomingMessage;
import at.jku.se.eatemup.core.json.MessageType;

public class BattleAnswerMessage extends IncomingMessage {
	public String answer;
	public int duration;

	@Override
	public MessageType getType() {
		return MessageType.BattleAnswer;
	}
}
