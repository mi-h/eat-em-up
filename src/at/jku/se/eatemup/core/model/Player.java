package at.jku.se.eatemup.core.model;

import at.jku.se.eatemup.core.model.specialaction.NoAction;
import at.jku.se.eatemup.core.model.specialaction.SpecialAction;

public class Player {
	private String name;
	private String userid;
	private int points;
	private SpecialAction specialAction;

	public Player(String name, String userid) {
		this.name = name;
		this.points = 0;
		this.specialAction = new NoAction();
		this.userid = userid;
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

	public String getUserid() {
		return userid;
	}

	public void setUserid(String userid) {
		this.userid = userid;
	}
}
