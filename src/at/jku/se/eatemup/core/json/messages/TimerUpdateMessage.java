package at.jku.se.eatemup.core.json.messages;

import at.jku.se.eatemup.core.json.Message;

public class TimerUpdateMessage extends Message{
	public int remainingTime;
	public long currentTimestamp;
}
