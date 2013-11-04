package at.jku.se.eatemup.core.json;

public abstract class OutgoingMessage extends Message {

	@Override
	public DirectionType getDirection() {
		return DirectionType.Outgoing;
	}
}
