package at.jku.se.eatemup.core.json;

public abstract class Message {
	public abstract CastType getCastType();

	public abstract DirectionType getDirection();

	public abstract MessageType getType();
}
