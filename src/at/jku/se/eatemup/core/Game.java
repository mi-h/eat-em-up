package at.jku.se.eatemup.core;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import at.jku.se.eatemup.core.model.*;

public class Game {
	private String id;
	private int playtime;
	private Location location;
	private Team[] teams;
	private ArrayList<String> readyToGoPlayers;
	private boolean startSurveySent;
	private ConcurrentHashMap<String, Position> playerPositions;
	private boolean positionProcessingFlag;
	private static final int radius = 3;
	private CopyOnWriteArrayList<String> audience;
	private CopyOnWriteArrayList<Battle> battles;
	private static final double battleWinQuota = 0.5;

	public Game() {
		teams = new Team[2];
		id = UUID.randomUUID().toString();
		readyToGoPlayers = new ArrayList<>();
		playerPositions = new ConcurrentHashMap<>();
		audience = new CopyOnWriteArrayList<>();
		battles = new CopyOnWriteArrayList<>();
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

	public boolean isInRedTeam(String username) {
		return teams[0].hasPlayer(username);
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

	public synchronized void setStartSurveySent(boolean startSurveySent) {
		this.startSurveySent = startSurveySent;
	}

	public boolean allPlayersReady() {
		return readyToGoPlayers.size() == getPlayers().size();
	}

	public synchronized void setPlayerReady(String username) {
		if (playerIsInGame(username)) {
			if (!readyToGoPlayers.contains(username)) {
				readyToGoPlayers.add(username);
			}
		}
	}

	public ArrayList<String> getNotReadyPlayers() {
		ArrayList<String> list = new ArrayList<>();
		for (Player p : getPlayers()) {
			if (!readyToGoPlayers.contains(p.getName())) {
				list.add(p.getName());
			}
		}
		return list;
	}

	private boolean playerIsInGame(String username) {
		return teams[0].hasPlayer(username) || teams[1].hasPlayer(username);
	}

	public void setPlayerPosition(String username, Position position) {
		playerPositions.put(username, position);
	}

	public synchronized boolean setPositionProcLock() {
		if (positionProcessingFlag)
			return false;
		positionProcessingFlag = true;
		return true;
	}

	public synchronized boolean releasePositionProcLock() {
		if (!positionProcessingFlag)
			return false;
		positionProcessingFlag = false;
		return true;
	}

	public ArrayList<GoodiePoint> getGoodiePointsInPlayerRange(String username) {
		ArrayList<GoodiePoint> points = new ArrayList<>();
		Position playerPos = getPlayerPosition(username);
		if (playerPos != null) {
			for (GoodiePoint gp : location.getGoodiePoints()) {
				if (gp.hasGoodie()
						&& gp.getPosition().distanceLessThan(playerPos, radius)) {
					points.add(gp);
				}
			}
		}
		return points;
	}

	public ArrayList<String> getPlayersInPlayerRange(String username) {
		ArrayList<String> players = new ArrayList<>();
		Position playerPos = getPlayerPosition(username);
		for (Player p : getPlayers()) {
			Position temp = getPlayerPosition(p.getName());
			if (temp != null && playerPos.distanceLessThan(temp, radius)) {
				players.add(p.getName());
			}
		}
		return players;
	}

	public Position getPlayerPosition(String username) {
		return playerPositions.get(username);
	}

	public void addUserToAudience(String username) {
		audience.add(username);
	}

	public List<String> getAudienceNames() {
		return audience;
	}

	public void removePlayer(String username) {
		playerPositions.remove(username);
		if (isInRedTeam(username)) {
			teams[0].removePlayer(username);
		} else {
			teams[1].removePlayer(username);
		}
	}

	public void removeAudienceUser(String username) {
		audience.remove(username);
	}

	public void addBattle(Battle battle) {
		battles.add(battle);
	}

	public Battle addBattleAnswer(String username, int answer,
			long timestamp) {
		Battle b = getUserBattle(username);
		if (b != null) {
			if (b.setAnswer(username, answer, timestamp)) {
				battles.remove(b);
				return b;
			}
		}
		return null;
	}

	private Battle getUserBattle(String username) {
		for (Battle b : battles) {
			if (b.isParticipant(username))
				return b;
		}
		return null;
	}
	
	public void addPlayerPoints(String username, int points){
		Player p = getPlayerByUsername(username);
		if (p != null){
			if (points<0){
				if (Math.abs(points)>p.getPoints()){
					p.setPoints(0);
					return;
				}
			}
			p.setPoints(p.getPoints()+points);
		}
	}

	public int getBattleWinPoints(String winnerName, String loserName) {
		int p1 = getPlayerPoints(winnerName);
		int p2 = getPlayerPoints(loserName);
		if (p1!=-1 && p2!=-1){
			int changePoints = (int) Math.floor(p2*battleWinQuota);
			return changePoints;
		}
		return -1;
	}
	
	public Player getPlayerByUsername(String username){
		for (Player p : getPlayers()) {
			if (p.getName().equals(username))
				return p;
		}
		return null;
	}

	public int getPlayerPoints(String username) {
		Player p = getPlayerByUsername(username);
		if (p != null){
			return p.getPoints();
		}
		return -1;
	}
}
