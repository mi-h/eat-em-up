package at.jku.se.eatemup.core.model;

import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

public class Location {
	private String name;
	private Position topLeftPos;
	private Position bottomRightPos;
	private Position centerPosition;
	private CopyOnWriteArrayList<GoodiePoint> goodiePoints;

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

	public CopyOnWriteArrayList<GoodiePoint> getGoodiePoints() {
		return goodiePoints;
	}

	public void setGoodiePoints(CopyOnWriteArrayList<GoodiePoint> goodiePoints) {
		this.goodiePoints = goodiePoints;
	}

	public Position getCenterPosition() {
		return centerPosition;
	}

	public void setCenterPosition(Position centerPosition) {
		this.centerPosition = centerPosition;
	}
}
