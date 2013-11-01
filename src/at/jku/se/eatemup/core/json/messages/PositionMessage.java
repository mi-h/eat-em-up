package at.jku.se.eatemup.core.json.messages;

import at.jku.se.eatemup.core.json.Message;

public class PositionMessage extends Message{
	public String username;
	public double latitude;
	public double longitude;
	public long timestamp;
}
