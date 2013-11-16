package at.jku.se.eatemup.core.json;

public abstract class IncomingMessage extends Message {
	
	public String username;
	public String userid;

	@Override
	public CastType getCastType() {
		return CastType.None;
	}

	@Override
	public DirectionType getDirection() {
		return DirectionType.Incoming;
	}

}
