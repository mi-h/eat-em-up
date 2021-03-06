package at.jku.se.eatemup.core.model;

import java.util.ArrayList;
import java.util.List;

public class Team {
	private int points;
	private TeamType type;
	private ArrayList<Player> players;

	public Team(TeamType type) {
		players = new ArrayList<Player>();
		this.type = type;
	}

	public int calcTotalPoints() {
		int sum = 0;
		for (Player p : players) {
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

	public synchronized boolean hasPlayer(Player player) {
		for (Player p : players) {
			if (p.getUserid().equals(player.getUserid())) {
				return true;
			}
		}
		return false;
	}

	public synchronized boolean hasPlayer(String userid) {
		for (Player p : players) {
			if (p.getUserid().equals(userid)) {
				return true;
			}
		}
		return false;
	}

	public synchronized void removePlayer(String userid) {
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

	public void setPlayers(ArrayList<Player> players) {
		this.players = players;
	}

	public void setPoints(int points) {
		this.points = points;
	}

	public void setType(TeamType type) {
		this.type = type;
	}

	public synchronized void addPlayer(Player player) {
		if (!hasPlayer(player.getUserid())){
			players.add(player);
		}
	}
}
