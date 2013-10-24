package at.jku.se.eatemup.core.model;

public class GoodiePoint {
	private Goodie goodie;
	private Position position;
	
	public Goodie getGoodie() {
		return goodie;
	}
	public void setGoodie(Goodie goodie) {
		this.goodie = goodie;
	}
	public Position getPosition() {
		return position;
	}
	public void setPosition(Position position) {
		this.position = position;
	}
}
