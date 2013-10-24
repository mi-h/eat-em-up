package at.jku.se.eatemup.core.model;

import at.jku.se.eatemup.core.model.specialaction.SpecialAction;

public class Goodie {
	private SpecialAction specialAction;
	private String name;
	private int points;
	
	public SpecialAction getSpecialAction() {
		return specialAction;
	}
	public void setSpecialAction(SpecialAction specialAction) {
		this.specialAction = specialAction;
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
}
