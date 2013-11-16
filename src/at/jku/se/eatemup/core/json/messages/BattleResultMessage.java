package at.jku.se.eatemup.core.json.messages;

import at.jku.se.eatemup.core.json.CastType;
import at.jku.se.eatemup.core.json.MessageType;
import at.jku.se.eatemup.core.json.OutgoingMessage;

public class BattleResultMessage extends OutgoingMessage {
	public String winner;
	public String winnerUserid;
	public String correctResult;
	public int points;

	@Override
	public CastType getCastType() {
		return CastType.Multicast;
	}

	@Override
	public MessageType getType() {
		return MessageType.BattleResult;
	}
}
