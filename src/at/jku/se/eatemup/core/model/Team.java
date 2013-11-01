package at.jku.se.eatemup.core.model;

import java.util.ArrayList;
import java.util.List;

public class Team {
	private int points;
	private TeamType type;
	private List<Player> players;

	public Team() {
		players = new ArrayList<Player>();
	}

	public int getPoints() {
		return points;
	}

	public void setPoints(int points) {
		this.points = points;
	}

	public TeamType getType() {
		return type;
	}

	public void setType(TeamType type) {
		this.type = type;
	}

	public List<Player> getPlayers() {
		return players;
	}

	public void setPlayers(List<Player> players) {
		this.players = players;
	}
}
