package at.jku.se.eatemup.core.model;

import java.util.ArrayList;

public class Location {
	private String name;
	private Position topLeftPos;
	private Position bottomRightPos;
	private ArrayList<GoodiePoint> goodiePoints;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Position getTopLeftPos() {
		return topLeftPos;
	}

	public void setTopLeftPos(Position topLeftPos) {
		this.topLeftPos = topLeftPos;
	}

	public Position getBottomRightPos() {
		return bottomRightPos;
	}

	public void setBottomRightPos(Position bottomRightPos) {
		this.bottomRightPos = bottomRightPos;
	}

	public ArrayList<GoodiePoint> getGoodiePoints() {
		return goodiePoints;
	}

	public void setGoodiePoints(ArrayList<GoodiePoint> goodiePoints) {
		this.goodiePoints = goodiePoints;
	}
}
