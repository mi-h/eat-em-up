package at.jku.se.eatemup.core.model;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Team {
	private int points;
	private TeamType type;
	private CopyOnWriteArrayList<Player> players;

	public Team(TeamType type) {
		players = new CopyOnWriteArrayList<Player>();
		this.type = type;
	}

	public int calcTotalPoints() {
		int sum = 0;
		for (Player p : players){
			sum += p.getPoints();
		}
		return sum;
	}

	public List<Player> getPlayers() {
		return players;
	}

	public int getPoints() {
		return points;
	}

	public TeamType getType() {
		return type;
	}

	public boolean hasPlayer(Player player) {
		for (Player p : players) {
			if (p.getUserid().equals(player.getUserid())) {
				return true;
			}
		}
		return false;
	}

	public boolean hasPlayer(String userid) {
		for (Player p : players) {
			if (p.getUserid().equals(userid)) {
				return true;
			}
		}
		return false;
	}

	public void removePlayer(String userid) {
		Player rem = null;
		for (Player p : players) {
			if (p.getUserid().equals(userid)) {
				rem = p;
				break;
			}
		}
		if (rem != null) {
			players.remove(rem);
		}
	}

	public void setPlayers(CopyOnWriteArrayList<Player> players) {
		this.players = players;
	}

	public void setPoints(int points) {
		this.points = points;
	}

	public void setType(TeamType type) {
		this.type = type;
	}
}
