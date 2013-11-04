package at.jku.se.eatemup.core.json.messages;

import at.jku.se.eatemup.core.json.CastType;
import at.jku.se.eatemup.core.json.MessageType;
import at.jku.se.eatemup.core.json.OutgoingMessage;

public class BattleResultMessage extends OutgoingMessage {
	public String winner;
	public String correctResult;

	@Override
	public MessageType getType() {
		return MessageType.BattleResult;
	}

	@Override
	public CastType getCastType() {
		return CastType.Multicast;
	}
}
