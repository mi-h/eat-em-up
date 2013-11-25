package at.jku.se.eatemup.core.model;

public class GoodiePoint {
	private Goodie goodie;
	private Position position;

	public Goodie getGoodie() {
		return goodie;
	}

	public Position getPosition() {
		return position;
	}

	public boolean hasGoodie() {
		return goodie != null;
	}

	public void removeGoodie() {
		goodie = null;
	}

	public void setGoodie(Goodie goodie) {
		this.goodie = goodie;
	}

	public void setPosition(Position position) {
		this.position = position;
	}
}
