package at.jku.se.eatemup.core.model;

import java.util.concurrent.CopyOnWriteArrayList;

public class Location {
	private String name;
	private Position topLeftPos;
	private Position bottomRightPos;
	private Position centerPosition;
	private CopyOnWriteArrayList<GoodiePoint> goodiePoints;

	public Position getBottomRightPos() {
		return bottomRightPos;
	}

	public Position getCenterPosition() {
		return centerPosition;
	}

	public CopyOnWriteArrayList<GoodiePoint> getGoodiePoints() {
		return goodiePoints;
	}

	public String getName() {
		return name;
	}

	public Position getTopLeftPos() {
		return topLeftPos;
	}

	public void setBottomRightPos(Position bottomRightPos) {
		this.bottomRightPos = bottomRightPos;
	}

	public void setCenterPosition(Position centerPosition) {
		this.centerPosition = centerPosition;
	}

	public void setGoodiePoints(CopyOnWriteArrayList<GoodiePoint> goodiePoints) {
		this.goodiePoints = goodiePoints;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setTopLeftPos(Position topLeftPos) {
		this.topLeftPos = topLeftPos;
	}
}
