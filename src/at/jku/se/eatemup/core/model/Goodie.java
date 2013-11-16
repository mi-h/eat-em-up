package at.jku.se.eatemup.core.model;

import at.jku.se.eatemup.core.model.specialaction.SpecialAction;

public class Goodie {
	private SpecialAction specialAction;
	private String name;
	private int points;

	public String getName() {
		return name;
	}

	public int getPoints() {
		return points;
	}

	public SpecialAction getSpecialAction() {
		return specialAction;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setPoints(int points) {
		this.points = points;
	}

	public void setSpecialAction(SpecialAction specialAction) {
		this.specialAction = specialAction;
	}
}
