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

	public boolean hasPlayer(Player player) {
		for (Player p : players) {
			if (p.getName().equals(player.getName())) {
				return true;
			}
		}
		return false;
	}

	public boolean hasPlayer(String username) {
		for (Player p : players) {
			if (p.getName().equals(username)) {
				return true;
			}
		}
		return false;
	}

	public void removePlayer(String username) {
		Player rem = null;
		for (Player p : players) {
			if (p.getName().equals(username)) {
				rem = p;
				break;
			}
		}
		if (rem != null) {
			players.remove(rem);
		}
	}

	public int calcTotalPoints() {
		int sum = 0;
		for (Player p : players){
			sum += p.getPoints();
		}
		return sum;
	}
}
