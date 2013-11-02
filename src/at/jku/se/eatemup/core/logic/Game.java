package at.jku.se.eatemup.core.logic;

import java.util.ArrayList;
import java.util.UUID;

import at.jku.se.eatemup.core.model.Location;
import at.jku.se.eatemup.core.model.Player;
import at.jku.se.eatemup.core.model.Team;

public class Game {
	private String id;
	private int playtime;
	private Location location;
	private Team[] teams;
	private ArrayList<String> readyToGoPlayers;
	private boolean startSurveySent;

	public Game() {
		teams = new Team[2];
		id = UUID.randomUUID().toString();
		readyToGoPlayers = new ArrayList<>();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getPlaytime() {
		return playtime;
	}

	public void setPlaytime(int playtime) {
		this.playtime = playtime;
	}

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public Team[] getTeams() {
		return teams;
	}

	public void setTeams(Team[] teams) {
		this.teams = teams;
	}

	public boolean AddPlayer(Player player) {
		if (teams[1].getPlayers().size() < teams[0].getPlayers().size()) {
			teams[1].getPlayers().add(player);
		} else {
			teams[0].getPlayers().add(player);
		}
		if (teams[0].getPlayers().size() >= 1
				&& teams[0].getPlayers().size() == teams[1].getPlayers().size()) {
			return true;
		}
		return false;
	}

	public boolean isInRedTeam(Player player) {
		return teams[0].hasPlayer(player);
	}

	public ArrayList<Player> getPlayers() {
		ArrayList<Player> list = new ArrayList<>();
		list.addAll(teams[0].getPlayers());
		list.addAll(teams[1].getPlayers());
		return list;
	}

	public boolean isFull() {
		return getPlayers().size() == 6;
	}

	public boolean isStartSurveySent() {
		return startSurveySent;
	}

	public void setStartSurveySent(boolean startSurveySent) {
		this.startSurveySent = startSurveySent;
	}

	public boolean allPlayersReady() {
		return readyToGoPlayers.size() == getPlayers().size();
	}

	public void setPlayerReady(String username) {
		if (playerIsInGame(username)) {
			if (!readyToGoPlayers.contains(username)) {
				readyToGoPlayers.add(username);
			}
		}
	}
	
	public ArrayList<String> getNotReadyPlayers(){
		ArrayList<String> list = new ArrayList<>();
		for (Player p : getPlayers()){
			if (!readyToGoPlayers.contains(p.getName())){
				list.add(p.getName());
			}
		}
		return list;
	}

	private boolean playerIsInGame(String username) {
		return teams[0].hasPlayer(username) || teams[1].hasPlayer(username);
	}
}
