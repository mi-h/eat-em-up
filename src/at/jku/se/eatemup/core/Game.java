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

import at.jku.se.eatemup.core.database.DataStore2;
import at.jku.se.eatemup.core.json.messages.BattleStartMessage;
import at.jku.se.eatemup.core.json.messages.GameEndMessage;
import at.jku.se.eatemup.core.json.messages.GameStateMessage;
import at.jku.se.eatemup.core.json.messages.GoodieCreatedMessage;
import at.jku.se.eatemup.core.json.messages.PlayerHasEatenMessage;
import at.jku.se.eatemup.core.json.messages.PlayerMovedMessage;
import at.jku.se.eatemup.core.json.messages.SpecialActionActivatedMessage;
import at.jku.se.eatemup.core.json.messages.TimerUpdateMessage;
import at.jku.se.eatemup.core.model.*;
import at.jku.se.eatemup.core.model.specialaction.DoublePointsAction;
import at.jku.se.eatemup.core.model.specialaction.InvincibleAction;
import at.jku.se.eatemup.core.model.specialaction.NoAction;
import at.jku.se.eatemup.core.model.specialaction.SpecialAction;

public class Game {
	private class GameTick extends TimerTask {
		private void checkForFullUpdate() {
			tickCnt++;
			if (tickCnt >= fullUpdateTicks) {
				tickCnt = 0;
				/*
				 * disabled on Stefan's request
				 * Engine.scheduleFullGameUpdate(Game.this,null);
				 */
			}
		}

		@Override
		public void run() {
			playtime--;
			if (playtime <= 0) {
				cancelGameTicker();
				endGame();
			} else {
				sendTimerUpdate();
			}
			checkForFullUpdate();
		}
	}

	private String id;
	private int playtime;
	private Location location;
	private Team[] teams;
	private ArrayList<String> readyToGoPlayers;
	private boolean startSurveySent;
	private ConcurrentHashMap<String, Position> playerPositions;
	private ConcurrentHashMap<String, Long> playerPositionLastMessage;
	private ConcurrentHashMap<String, SpecialAction> playerActionMap;
	private boolean positionProcessingFlag;
	private static final int radius = 3;
	private CopyOnWriteArrayList<String> audience;
	private CopyOnWriteArrayList<Battle> battles;
	private static final double battleWinQuota = 0.5;
	private static final int specialGoodieQuota = 10; // percent
	private static final int minGoodieInGameQuota = 50; // percent of loc points
	private static final int smallGoodiePoints = 25;
	private static final int bigGoodiePoints = 50;
	private Timer ticker;
	private static final int fullUpdateTicks = 15;
	private int tickCnt;
	private boolean isStarted = false;

	public Game(int playtime) {
		teams = new Team[2];
		teams[0] = new Team(TeamType.RED);
		teams[1] = new Team(TeamType.BLUE);
		id = UUID.randomUUID().toString();
		readyToGoPlayers = new ArrayList<>();
		playerPositions = new ConcurrentHashMap<>();
		audience = new CopyOnWriteArrayList<>();
		battles = new CopyOnWriteArrayList<>();
		playerPositionLastMessage = new ConcurrentHashMap<>();
		playerActionMap = new ConcurrentHashMap<>();
		this.playtime = playtime;
	}

	private void activateSpecialAction(String uid, SpecialAction specialAction) {
		SpecialActionActivatedMessage message = new SpecialActionActivatedMessage();
		message.specialAction = specialAction.getName();
		message.userid = uid;
		message.username = Engine.userManager.getUsernameByUserid(uid);
		MessageContainer container = MessageCreator.createMsgContainer(message,
				Engine.userManager
						.convertIdListToSessionList(getBroadcastReceiverIds()));
		MessageHandler.PushMessage(container);
		playerActionMap.put(uid, specialAction);
		Engine.scheduleSpecialActionDeactivation(uid, specialAction,
				Engine.userManager
						.convertIdListToSessionList(getBroadcastReceiverIds()));
	}

	public synchronized void addBattle(Battle battle) {
		battles.add(battle);
	}

	public synchronized Battle addBattleAnswer(String userid, int answer,
			int duration) {
		Battle b = getUserBattle(userid);
		if (b != null) {
			if (b.setAnswer(userid, answer, duration)) {
				battles.remove(b);
				return b;
			}
		}
		return null;
	}

	public synchronized boolean addPlayer(Player player) {
		if (teams[1].getPlayers().size() < teams[0].getPlayers().size()) {
			teams[1].getPlayers().add(player);
		} else {
			teams[0].getPlayers().add(player);
		}
		return isReadyForStart();
	}

	public synchronized boolean isReadyForStart() {
		if (teams[0].getPlayers().size() >= 1
				&& teams[0].getPlayers().size() == teams[1].getPlayers().size()) {
			return true;
		}
		return false;
	}

	public synchronized void addPlayerPoints(String userid, int points) {
		Player p = getPlayerByUserid(userid);
		if (p != null) {
			if (points < 0) {
				if (Math.abs(points) > p.getPoints()) {
					p.setPoints(0);
					return;
				}
			}
			p.setPoints(p.getPoints() + points);
		}
	}

	public synchronized void addUserToAudience(String userid) {
		audience.add(userid);
	}

	public synchronized boolean allPlayersReady() {
		return readyToGoPlayers.size() == getPlayers().size();
	}

	public synchronized void cancelGameTicker() {
		ticker.cancel();
	}

	public synchronized GameStateMessage createGameStateMessage() {
		GameStateMessage message = new GameStateMessage();
		message.playerInfo = createPlayerInfoData(getPlayers(), this);
		message.goodies = createGoodieData(location.getGoodiePoints());
		message.remainingTime = playtime;
		return message;
	}

	private Goodie createGoodie(SpecialAction special, Random rand, String name) {
		Goodie g = new Goodie();
		boolean sa = !(special instanceof NoAction);
		g.setPoints(sa ? 0 : createGoodiePoints(rand));
		g.setSpecialAction(special);
		g.setName(name);
		return g;
	}

	private ArrayList<HashMap<String, Object>> createGoodieData(
			CopyOnWriteArrayList<GoodiePoint> goodies) {
		ArrayList<HashMap<String, Object>> list = new ArrayList<>();
		for (GoodiePoint gp : goodies) {
			HashMap<String, Object> map = new HashMap<>();
			map.put("latitude", gp.getPosition().getLatitude());
			map.put("longitude", gp.getPosition().getLongitude());
			map.put("specialAction", gp.getGoodie().getSpecialAction()
					.getName());
			map.put("points", gp.getGoodie().getPoints());
			list.add(map);
		}
		return list;
	}

	private int createGoodiePoints(Random rand) {
		int swap = rand.nextInt(2);
		if (swap == 0) {
			return smallGoodiePoints;
		}
		return bigGoodiePoints;
	}

	public synchronized void createGoodies(boolean start) {
		Random rand = new Random();
		ArrayList<GoodiePoint> points = new ArrayList<GoodiePoint>(
				location.getGoodiePoints());
		Collections.shuffle(points);
		int createGoodies = start ? points.size()
				: howManyGoodiesToCreate(points);
		int createdCnt = 0;
		ArrayList<GoodiePoint> filledPoints = new ArrayList<>();
		for (GoodiePoint p : points) {
			if (createdCnt < createGoodies) {
				Goodie g = createGoodie(createSpecialAction(rand), rand, UUID
						.randomUUID().toString());
				p.setGoodie(g);
				createdCnt++;
				filledPoints.add(p);
			} else {
				break;
			}
		}
		if (!start) {
			for (GoodiePoint gp : filledPoints) {
				GoodieCreatedMessage message = new GoodieCreatedMessage();
				message.latitude = gp.getPosition().getLatitude();
				message.longitude = gp.getPosition().getLongitude();
				message.points = gp.getGoodie().getPoints();
				MessageContainer container = MessageCreator
						.createMsgContainer(
								message,
								Engine.userManager
										.convertIdListToSessionList(getBroadcastReceiverIds()));
				MessageHandler.PushMessage(container);
			}
		}
	}

	private ArrayList<HashMap<String, Object>> createPlayerInfoData(
			ArrayList<Player> players, Game game) {
		ArrayList<HashMap<String, Object>> list = new ArrayList<>();
		DataStore2 ds = new DataStore2();
		try {
			for (Player p : players) {
				Account acc = ds.getAccountByUserid(p.getUserid());
				String facebookid = acc.getFacebookId();
				HashMap<String, Object> map = new HashMap<>();
				HashMap<String, Object> pMap = new HashMap<>();
				map.put("username", p.getName());
				map.put("userid", p.getUserid());
				map.put("points", p.getPoints());
				map.put("facebookuser", facebookid != null ? true : false);
				map.put("facebookimage",
						facebookid != null ? FacebookImageLoader
								.getImageForId(facebookid) : new byte[0]);
				Position pos = game.getPlayerPosition(p.getUserid());
				pMap.put("latitude", pos.getLatitude());
				pMap.put("longitude", pos.getLongitude());
				map.put("position", pMap);
				list.add(map);
			}
		} finally {
			ds.closeConnection();
		}
		return list;
	}

	private ArrayList<HashMap<String, Object>> createPlayerResults() {
		ArrayList<HashMap<String, Object>> results = new ArrayList<>();
		for (Player p : getPlayers()) {
			HashMap<String, Object> map = new HashMap<>();
			map.put("username", p.getName());
			map.put("points", p.getPoints());
			map.put("userid", p.getUserid());
			results.add(map);
		}
		return results;
	}

	private SpecialAction createSpecialAction(Random rand) {
		int i = rand.nextInt(100) + 1;
		if (i > specialGoodieQuota) {
			return new NoAction();
		}
		i = rand.nextInt(2);
		if (i == 0) {
			return new DoublePointsAction();
		}
		return new InvincibleAction();
	}

	public synchronized void disableSpecialAction(String userid) {
		try {
			playerActionMap.remove(userid);
		} catch (NullPointerException ex) {
			// fail silently
		}
	}

	public synchronized void endGame() {
		sendGameEnd();
		processGameEnd();
		Engine.endGame(this);
	}

	public List<String> getAudienceIds() {
		return audience;
	}

	public int getBattleWinPoints(String winnerId, String loserId) {
		int p1 = getPlayerPoints(winnerId);
		int p2 = getPlayerPoints(loserId);
		if (p1 != -1 && p2 != -1) {
			int changePoints = (int) Math.floor(p2 * battleWinQuota);
			return changePoints;
		}
		return -1;
	}

	public synchronized ArrayList<String> getBroadcastReceiverIds() {
		ArrayList<String> list = new ArrayList<>();
		list.addAll(getPlayerIds());
		list.addAll(audience);
		return list;
	}

	public synchronized ArrayList<GoodiePoint> getGoodiePoints() {
		return new ArrayList<GoodiePoint>(location.getGoodiePoints());
	}

	public synchronized ArrayList<GoodiePoint> getGoodiePointsInPlayerRange(
			String userid) {
		ArrayList<GoodiePoint> points = new ArrayList<>();
		Position playerPos = getPlayerPosition(userid);
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

	public String getId() {
		return id;
	}

	public Location getLocation() {
		return location;
	}

	public synchronized ArrayList<String> getNotReadyPlayers() {
		ArrayList<String> list = new ArrayList<>();
		for (Player p : getPlayers()) {
			if (!readyToGoPlayers.contains(p.getUserid())) {
				list.add(p.getUserid());
			}
		}
		return list;
	}

	public synchronized Player getPlayerByUserid(String userid) {
		for (Player p : getPlayers()) {
			if (p.getUserid().equals(userid))
				return p;
		}
		return null;
	}

	public synchronized ArrayList<String> getPlayerIds() {
		ArrayList<String> list = new ArrayList<>();
		for (Player p : getPlayers()) {
			list.add(p.getUserid());
		}
		return list;
	}

	private Player getPlayerInPlayerRange(String uid) {
		Position playerPos = playerPositions.get(uid);
		for (Player p : getPlayers()) {
			if (!p.getUserid().equals(uid)) {
				Position tempPos = playerPositions.get(p.getUserid());
				if (tempPos != null) {
					if (playerPos.calcDistance(tempPos) < radius) {
						return p;
					}
				}
			}
		}
		return null;
	}

	public synchronized int getPlayerPoints(String userid) {
		Player p = getPlayerByUserid(userid);
		if (p != null) {
			return p.getPoints();
		}
		return -1;
	}

	public synchronized Position getPlayerPosition(String userid) {
		return playerPositions.get(userid);
	}

	public synchronized ArrayList<Player> getPlayers() {
		ArrayList<Player> list = new ArrayList<>();
		list.addAll(teams[0].getPlayers());
		list.addAll(teams[1].getPlayers());
		return list;
	}

	public synchronized ArrayList<String> getPlayersInPlayerRange(String userid) {
		ArrayList<String> players = new ArrayList<>();
		Position playerPos = getPlayerPosition(userid);
		for (Player p : getPlayers()) {
			Position temp = getPlayerPosition(p.getUserid());
			if (temp != null && playerPos.distanceLessThan(temp, radius)) {
				players.add(p.getUserid());
			}
		}
		return players;
	}

	public int getPlaytime() {
		return playtime;
	}

	public Team[] getTeams() {
		return teams;
	}

	private Battle getUserBattle(String userid) {
		for (Battle b : battles) {
			if (b.isParticipant(userid))
				return b;
		}
		return null;
	}

	private boolean hasTeamRedWon() {
		int trp = teams[0].calcTotalPoints();
		int tbp = teams[1].calcTotalPoints();
		return trp > tbp;
	}

	private int howManyGoodiesToCreate(ArrayList<GoodiePoint> points) {
		int fullPoints = 0;
		for (GoodiePoint p : points) {
			if (p.getGoodie() != null) {
				fullPoints++;
			}
		}
		int quota = (int) points.size() / fullPoints * 100;
		if (quota < minGoodieInGameQuota) {
			return minGoodieInGameQuota - quota;
		}
		return 0;
	}

	public boolean isFull() {
		return getPlayers().size() == 6;
	}

	public synchronized boolean isInRedTeam(Player player) {
		return teams[0].hasPlayer(player);
	}

	public synchronized boolean isInRedTeam(String userid) {
		return teams[0].hasPlayer(userid);
	}

	public boolean isStartSurveySent() {
		return startSurveySent;
	}

	private synchronized boolean playerEatsGoodie(String userid, Goodie goodie,
			Position position) {
		SpecialAction temp = playerActionMap.get(userid);
		if (!goodie.getSpecialAction().getName().equals("NoAction")) {
			if (temp != null) {
				return false;
			} else {
				activateSpecialAction(userid, goodie.getSpecialAction());
				return true;
			}
		}
		int points = -1;
		if (temp != null && temp instanceof DoublePointsAction) {
			points = goodie.getPoints() * 2;
		} else {
			points = goodie.getPoints();
		}
		addPlayerPoints(userid, points);
		PlayerHasEatenMessage message = new PlayerHasEatenMessage();
		message.points = getPlayerPoints(userid);
		message.userid = userid;
		message.username = Engine.userManager.getUsernameByUserid(userid);
		HashMap<String, Double> posMap = new HashMap<>();
		HashMap<String, Object> teamMap = new HashMap<>();
		posMap.put("latitude", position.getLatitude());
		posMap.put("longitude", position.getLongitude());
		if (isInRedTeam(userid)) {
			teamMap.put("teamRed", true);
			teamMap.put("newTeamPoints", teams[0].getPoints());
		} else {
			teamMap.put("teamRed", false);
			teamMap.put("newTeamPoints", teams[1].getPoints());
		}
		message.goodie = posMap;
		message.team = teamMap;
		MessageContainer container = MessageCreator.createMsgContainer(message,
				Engine.userManager
						.convertIdListToSessionList(getBroadcastReceiverIds()));
		MessageHandler.PushMessage(container);
		return true;
	}

	private boolean playerIsInGame(String userid) {
		return teams[0].hasPlayer(userid) || teams[1].hasPlayer(userid);
	}

	private boolean playerIsInvincible(String userid) {
		SpecialAction action = playerActionMap.get(userid);
		if (action != null) {
			if (action instanceof InvincibleAction) {
				return true;
			}
		}
		return false;
	}

	private void processGameEnd() {
		Engine.updateAccountPoints(getPlayers());
	}

	public synchronized void processPlayerPositionChange(String userid,
			Position p, long timestamp) {
		Position oldPos = playerPositions.get(userid);
		if (oldPos.differentFrom(p)) {
			PlayerMovedMessage message = new PlayerMovedMessage();
			message.username = Engine.userManager.getUsernameByUserid(userid);
			message.userid = userid;
			HashMap<String, Double> pos = new HashMap<>();
			pos.put("latitude", p.getLatitude());
			pos.put("longitude", p.getLongitude());
			message.position = pos;
			MessageContainer container = MessageCreator
					.createMsgContainer(
							message,
							Engine.userManager
									.convertIdListToSessionList(getBroadcastReceiverIds()));
			MessageHandler.PushMessage(container);
		}
		setPlayerPosition(userid, p, timestamp);
		ArrayList<GoodiePoint> goodiePoints = getGoodiePointsInPlayerRange(userid);
		boolean hasEaten = false;
		for (GoodiePoint gp : goodiePoints) {
			Goodie temp = gp.getGoodie();
			if (temp != null) {
				if (playerEatsGoodie(userid, temp, gp.getPosition())) {
					gp.setGoodie(null);
					hasEaten = true;
				}
			}
		}
		if (hasEaten) {
			createGoodies(false);
		}
		if (!playerIsInvincible(userid)) {
			Player battleOp = getPlayerInPlayerRange(userid);
			if (battleOp != null) {
				Battle b = BattleCreator.CreateBattle(userid,
						battleOp.getUserid());
				battles.add(b);
				BattleStartMessage message = new BattleStartMessage();
				message.answers = b.getResult();
				message.question = b.getQuestion();
				message.timelimit = b.getTime();
				message.userid1 = userid;
				message.userid2 = b.getUserid2();
				message.username1 = Engine.userManager
						.getUsernameByUserid(userid);
				message.username2 = Engine.userManager.getUsernameByUserid(b
						.getUserid2());
				ArrayList<String> receivers = new ArrayList<>();
				receivers.add(userid);
				receivers.add(b.getUserid2());
				MessageContainer container = MessageCreator.createMsgContainer(
						message, Engine.userManager
								.convertIdListToSessionList(receivers));
				MessageHandler.PushMessage(container);
			}
		}
	}

	public synchronized boolean releasePositionProcLock() {
		if (!positionProcessingFlag)
			return false;
		positionProcessingFlag = false;
		return true;
	}

	public synchronized void removeAudienceUser(String userid) {
		audience.remove(userid);
	}

	public synchronized void removePlayer(String userid) {
		playerPositions.remove(userid);
		if (isInRedTeam(userid)) {
			teams[0].removePlayer(userid);
		} else {
			teams[1].removePlayer(userid);
		}
	}

	private synchronized void sendGameEnd() {
		GameEndMessage message = new GameEndMessage();
		message.teamRedWin = hasTeamRedWon();
		message.playerResults = createPlayerResults();
		MessageContainer container = MessageCreator.createMsgContainer(message,
				Engine.userManager
						.convertIdListToSessionList(getBroadcastReceiverIds()));
		MessageHandler.PushMessage(container);
	}

	public void sendTimerUpdate() {
		Date d = new Date();
		TimerUpdateMessage message = new TimerUpdateMessage();
		message.remainingTime = playtime;
		message.currentTimestamp = d.getTime();
		MessageContainer container = MessageCreator.createMsgContainer(message,
				Engine.userManager
						.convertIdListToSessionList(getBroadcastReceiverIds()));
		MessageHandler.PushMessage(container);
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public synchronized void setPlayerPosition(String userid, Position position) {
		playerPositions.put(userid, position);
	}

	public synchronized void setPlayerPosition(String userid, Position p,
			long timestamp) {
		setPlayerPosition(userid, p);
		playerPositionLastMessage.put(userid, timestamp);
	}

	public synchronized void setPlayerReady(String userid) {
		if (playerIsInGame(userid)) {
			if (!readyToGoPlayers.contains(userid)) {
				readyToGoPlayers.add(userid);
			}
		}
	}

	public void setPlaytime(int playtime) {
		this.playtime = playtime;
	}

	public synchronized boolean setPositionProcLock() {
		if (positionProcessingFlag)
			return false;
		positionProcessingFlag = true;
		return true;
	}

	public synchronized void setStartSurveySent(boolean startSurveySent) {
		this.startSurveySent = startSurveySent;
	}

	public void setTeams(Team[] teams) {
		this.teams = teams;
	}

	public synchronized void startGame() {
		if (!isStarted){
			isStarted = true;
			for (String uid : getBroadcastReceiverIds()){
				Engine.scheduleFullGameUpdate(this, uid);
			}		
			tickCnt = 0;
			ticker.scheduleAtFixedRate(new GameTick(), 0, 1000);
		}
	}

	public synchronized void kill() {
		ticker.cancel();
	}

	public boolean isPlayerReadyForGame(Player p) {
		return readyToGoPlayers.contains(p.getUserid());
	}

	public synchronized void setPlayerNotReady(String userid) {
		if (playerIsInGame(userid)) {
			if (readyToGoPlayers.contains(userid)) {
				readyToGoPlayers.remove(userid);
			}
		}	
	}
}