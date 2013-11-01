package at.jku.se.eatemup.core.json;

public abstract class Message {
	public abstract MessageType getType();

	public abstract DirectionType getDirection();

	public abstract CastType getCastType();
}
