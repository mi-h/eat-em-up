package at.jku.se.eatemup.core.json.messages;

import at.jku.se.eatemup.core.json.CastType;
import at.jku.se.eatemup.core.json.MessageType;
import at.jku.se.eatemup.core.json.OutgoingMessage;

public class TimerUpdateMessage extends OutgoingMessage {
	public int remainingTime;
	public long currentTimestamp;

	@Override
	public CastType getCastType() {
		return CastType.Broadcast;
	}

	@Override
	public MessageType getType() {
		return MessageType.TimerUpdate;
	}
}
