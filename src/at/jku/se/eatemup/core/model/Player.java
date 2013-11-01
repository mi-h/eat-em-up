package at.jku.se.eatemup.core.model;

import at.jku.se.eatemup.core.model.specialaction.NoAction;
import at.jku.se.eatemup.core.model.specialaction.SpecialAction;

public class Player {
	private String name;
	private int points;
	private SpecialAction specialAction;

	public Player(String name) {
		this.name = name;
		points = 0;
		specialAction = new NoAction();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getPoints() {
		return points;
	}

	public void setPoints(int points) {
		this.points = points;
	}

	public SpecialAction getSpecialAction() {
		return specialAction;
	}

	public void setSpecialAction(SpecialAction specialAction) {
		this.specialAction = specialAction;
	}
}
