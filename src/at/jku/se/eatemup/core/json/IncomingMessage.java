package at.jku.se.eatemup.core.json;

public abstract class IncomingMessage extends Message {
	
	public String username;

	@Override
	public DirectionType getDirection() {
		return DirectionType.Incoming;
	}

	@Override
	public CastType getCastType() {
		return CastType.None;
	}

}
