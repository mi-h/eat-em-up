package at.jku.se.eatemup.core;

import java.util.Date;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import at.jku.se.eatemup.core.json.messages.BattleStartMessage;
import at.jku.se.eatemup.core.json.messages.GameEndMessage;
import at.jku.se.eatemup.core.json.messages.GoodieCreatedMessage;
import at.jku.se.eatemup.core.json.messages.PlayerHasEatenMessage;
import at.jku.se.eatemup.core.json.messages.PlayerMovedMessage;
import at.jku.se.eatemup.core.json.messages.SpecialActionActivatedMessage;
import at.jku.se.eatemup.core.json.messages.TimerUpdateMessage;
import at.jku.se.eatemup.core.model.*;
import at.jku.se.eatemup.core.model.specialaction.DoublePointsAction;
import at.jku.se.eatemup.core.model.specialaction.InvisibleAction;
import at.jku.se.eatemup.core.model.specialaction.NoAction;
import at.jku.se.eatemup.core.model.specialaction.SpecialAction;

public class Game {
	private String id;
	private int playtime;
	private Location location;
	private Team[] teams;
	private ArrayList<String> readyToGoPlayers;
	private boolean startSurveySent;
	private ConcurrentHashMap<String, Position> playerPositions;
	private ConcurrentHashMap<String, Long> playerPositionLastMessage;
	private boolean positionProcessingFlag;
	private static final int radius = 3;
	private CopyOnWriteArrayList<String> audience;
	private CopyOnWriteArrayList<Battle> battles;
	private static final double battleWinQuota = 0.5;
	private static final int specialGoodieQuota = 10; //percent
	private static final int minGoodieInGameQuota = 50; //percent of loc points
	private static final int smallGoodiePoints = 25;
	private static final int bigGoodiePoints = 50;
	private Timer ticker;

	public Game() {
		teams = new Team[2];
		id = UUID.randomUUID().toString();
		readyToGoPlayers = new ArrayList<>();
		playerPositions = new ConcurrentHashMap<>();
		audience = new CopyOnWriteArrayList<>();
		battles = new CopyOnWriteArrayList<>();
		playerPositionLastMessage = new ConcurrentHashMap<>();
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

	public void createGoodies(boolean start) {
		Random rand = new Random();
		ArrayList<GoodiePoint> points = new ArrayList<GoodiePoint>(location.getGoodiePoints());
		Collections.shuffle(points);
		int createGoodies = start ? points.size() : howManyGoodiesToCreate(points);
		int createdCnt = 0;
		ArrayList<GoodiePoint> filledPoints = new ArrayList<>();
		for (GoodiePoint p : points){
			if (createdCnt < createGoodies){
				Goodie g = createGoodie(createSpecialAction(rand),rand,UUID.randomUUID().toString());				
				p.setGoodie(g);
				createdCnt++;
				filledPoints.add(p);
			} else {
				break;
			}
		}
		if(!start){
			for(GoodiePoint gp : filledPoints){
				GoodieCreatedMessage message = new GoodieCreatedMessage();
				message.latitude = gp.getPosition().getLatitude();
				message.longitude = gp.getPosition().getLongitude();
				message.points = gp.getGoodie().getPoints();
				MessageContainer container = MessageCreator.createMsgContainer(message, Engine.userSessionMap.convertNameListToSessionList(getBroadcastReceiverNames()));
				MessageHandler.PushMessage(container);
			}			
		}
	}
	
	private int createGoodiePoints(Random rand){
		int swap = rand.nextInt(2);
		if (swap == 0){
			return smallGoodiePoints;
		}
		return bigGoodiePoints;
	}
	
	private int howManyGoodiesToCreate(ArrayList<GoodiePoint> points) {
		int fullPoints = 0;
		for (GoodiePoint p : points){
			if (p.getGoodie() != null){
				fullPoints++;
			}
		}
		int quota = (int) points.size()/fullPoints*100;
		if (quota < minGoodieInGameQuota){
			return minGoodieInGameQuota-quota;
		}
		return 0;
	}
	
	private SpecialAction createSpecialAction(Random rand){
		int i = rand.nextInt(100)+1;
		if (i>specialGoodieQuota){
			return new NoAction();
		}
		i = rand.nextInt(2);
		if (i == 0){
			return new DoublePointsAction();
		}
		return new InvisibleAction();
	}

	private Goodie createGoodie(SpecialAction special, Random rand, String name){
		Goodie g = new Goodie();
		boolean sa = !(special instanceof NoAction);
		g.setPoints(sa ? 0 : createGoodiePoints(rand));
		g.setSpecialAction(special);
		g.setName(name);
		return g;
	}

	public ArrayList<GoodiePoint> getGoodiePoints() {
		return new ArrayList<GoodiePoint>(location.getGoodiePoints());
	}
	
	public ArrayList<String> getPlayerNames(){
		ArrayList<String> list = new ArrayList<>();
		for (Player p : getPlayers()){
			list.add(p.getName());
		}
		return list;
	}
	
	public ArrayList<String> getBroadcastReceiverNames(){
		ArrayList<String> list = new ArrayList<>();
		list.addAll(getPlayerNames());
		list.addAll(audience);
		return list;
	}
	
	public void startGame(){
		ticker.scheduleAtFixedRate(new GameTick(), 0, 1000);
	}
	
	public void cancelGameTicker(){
		ticker.cancel();
	}
	
	private class GameTick extends TimerTask{
		@Override
		public void run() {
			playtime--;
			if (playtime <= 0){
				cancelGameTicker();
				endGame();
			} else {
				sendTimerUpdate();
			}
		}
	}

	public void endGame() {
		sendGameEnd();
		processGameEnd();
		Engine.endGame(this);
	}
	
	private void processGameEnd() {
		Engine.updateAccountPoints(getPlayers());
	}

	private boolean hasTeamRedWon(){
		int trp = teams[0].calcTotalPoints();
		int tbp = teams[1].calcTotalPoints();
		return trp>tbp;
	}

	private void sendGameEnd() {
		GameEndMessage message = new GameEndMessage();
		message.teamRedWin = hasTeamRedWon();
		message.playerResults = createPlayerResults();
		MessageContainer container = MessageCreator.createMsgContainer(message, Engine.userSessionMap.convertNameListToSessionList(getBroadcastReceiverNames()));
		MessageHandler.PushMessage(container);
	}

	private ArrayList<HashMap<String, Object>> createPlayerResults() {
		ArrayList<HashMap<String, Object>> results = new ArrayList<>();
		for (Player p : getPlayers()){
			HashMap<String, Object> map = new HashMap<>();
			map.put("username", p.getName());
			map.put("points", p.getPoints());
			results.add(map);
		}
		return results;
	}

	public void sendTimerUpdate() {
		Date d = new Date();
		TimerUpdateMessage message = new TimerUpdateMessage();
		message.remainingTime = playtime;
		message.currentTimestamp = d.getTime();
		MessageContainer container = MessageCreator.createMsgContainer(message, Engine.userSessionMap.convertNameListToSessionList(getBroadcastReceiverNames()));
		MessageHandler.PushMessage(container);
	}

	public void setPlayerPosition(String uid, Position p, long timestamp) {
		setPlayerPosition(uid,p);
		playerPositionLastMessage.put(uid, timestamp);
	}

	public void processPlayerPositionChange(String uid, Position p,
			long timestamp) {
		Position oldPos = playerPositions.get(uid);
		if (oldPos.differentFrom(p)){
			PlayerMovedMessage message = new PlayerMovedMessage();
			message.username = uid;
			HashMap<String,Double> pos = new HashMap<>();
			pos.put("latitude", p.getLatitude());
			pos.put("longitude", p.getLongitude());
			message.position = pos;
			MessageContainer container = MessageCreator.createMsgContainer(message, Engine.userSessionMap.convertNameListToSessionList(getBroadcastReceiverNames()));
			MessageHandler.PushMessage(container);
		}
		setPlayerPosition(uid,p,timestamp);
		ArrayList<GoodiePoint> goodiePoints = getGoodiePointsInPlayerRange(uid);
		boolean hasEaten = false;
		for (GoodiePoint gp : goodiePoints){
			Goodie temp = gp.getGoodie();
			if (temp != null){
				playerEatsGoodie(uid,temp,gp.getPosition());
				gp.setGoodie(null);
				hasEaten = true;
			}
		}
		if (hasEaten){
			createGoodies(false);
		}
		Player battleOp = getPlayerInPlayerRange(uid);
		if (battleOp != null){
			Battle b = BattleCreator.CreateBattle(uid, battleOp.getName());
			battles.add(b);
			BattleStartMessage message = new BattleStartMessage();
			message.answers = b.getResult();
			message.question = b.getQuestion();
			message.timelimit = b.getTime();
			message.username1 = b.getUsername1();
			message.username2 = b.getUsername2();
			ArrayList<String> receivers = new ArrayList<>();
			receivers.add(b.getUsername1());
			receivers.add(b.getUsername2());
			MessageContainer container = MessageCreator.createMsgContainer(message, Engine.userSessionMap.convertNameListToSessionList(receivers));
			MessageHandler.PushMessage(container);
		}	
	}

	private Player getPlayerInPlayerRange(String uid) {
		Position playerPos = playerPositions.get(uid);
		for (Player p : getPlayers()){
			if (!p.getName().equals(uid)){
				Position tempPos = playerPositions.get(p.getName());
				if (tempPos != null){
					if(playerPos.calcDistance(tempPos)<radius){
						return p;
					}
				}
			}
		}
		return null;
	}

	private void playerEatsGoodie(String uid, Goodie temp, Position position) {
		addPlayerPoints(uid,temp.getPoints());
		PlayerHasEatenMessage message = new PlayerHasEatenMessage();
		message.points = getPlayerPoints(uid);
		message.username = uid;
		HashMap<String,Double> posMap = new HashMap<>();
		HashMap<String,Object> teamMap = new HashMap<>();
		posMap.put("latitude", position.getLatitude());
		posMap.put("longitude", position.getLongitude());
		if (isInRedTeam(uid)){
			teamMap.put("teamRed",true);
			teamMap.put("newTeamPoints", teams[0].getPoints());
		} else {
			teamMap.put("teamRed",false);
			teamMap.put("newTeamPoints", teams[1].getPoints());
		}
		message.goodie = posMap;
		message.team = teamMap;
		MessageContainer container = MessageCreator.createMsgContainer(message, Engine.userSessionMap.convertNameListToSessionList(getBroadcastReceiverNames()));
		MessageHandler.PushMessage(container);
		if (!temp.getSpecialAction().getName().equals("NoAction")){
			activateSpecialAction(uid,temp.getSpecialAction());
		}
	}

	private void activateSpecialAction(String uid, SpecialAction specialAction) {
		SpecialActionActivatedMessage message = new SpecialActionActivatedMessage();
		message.specialAction = specialAction.getName();
		MessageContainer container = MessageCreator.createMsgContainer(message, Engine.userSessionMap.convertNameListToSessionList(getBroadcastReceiverNames()));
		MessageHandler.PushMessage(container);
		Engine.scheduleSpecialActionDeactivation(uid,specialAction,Engine.userSessionMap.convertNameListToSessionList(getBroadcastReceiverNames()));
	}	
}