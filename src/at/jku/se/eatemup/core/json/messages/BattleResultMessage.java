package at.jku.se.eatemup.core.json.messages;

import at.jku.se.eatemup.core.json.CastType;
import at.jku.se.eatemup.core.json.DirectionType;
import at.jku.se.eatemup.core.json.Message;
import at.jku.se.eatemup.core.json.MessageType;

public class BattleResultMessage extends Message {
	public String winner;
	public String correctResult;

	@Override
	public MessageType getType() {
		return MessageType.BattleResult;
	}

	@Override
	public DirectionType getDirection() {
		return DirectionType.Outgoing;
	}

	@Override
	public CastType getCastType() {
		return CastType.Multicast;
	}
}
