package at.jku.se.eatemup.core.json.messages;

import at.jku.se.eatemup.core.json.CastType;
import at.jku.se.eatemup.core.json.DirectionType;
import at.jku.se.eatemup.core.json.Message;
import at.jku.se.eatemup.core.json.MessageType;

public class AreaLeftMessage extends Message{
	
	public String username;

	@Override
	public MessageType getType() {
		return MessageType.AreaLeft;
	}

	@Override
	public DirectionType getDirection() {
		return DirectionType.Outgoing;
	}

	@Override
	public CastType getCastType() {
		return CastType.Unicast;
	}

}
