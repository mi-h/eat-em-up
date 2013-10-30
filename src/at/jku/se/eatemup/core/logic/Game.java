package at.jku.se.eatemup.core.logic;

import at.jku.se.eatemup.core.model.Location;
import at.jku.se.eatemup.core.model.Player;
import at.jku.se.eatemup.core.model.Team;

public class Game {
	private long id;
	private int playtime;
	private Location location;
	private Team[] teams;
	
	public Game(){
		teams = new Team[2];
	}
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
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
		if (teams[1].getPlayers().size()<teams[0].getPlayers().size()){
			teams[1].getPlayers().add(player);
		} else {
			teams[0].getPlayers().add(player);
		}
		if (teams[0].getPlayers().size() >= 1 && teams[0].getPlayers().size() == teams[1].getPlayers().size()){
			return true;
		}
		return false;
	}
}
