package at.jku.se.eatemup.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.*;

import at.jku.se.eatemup.core.database.DataStore2;
import at.jku.se.eatemup.core.json.messages.*;
import at.jku.se.eatemup.core.logging.Logger;
import at.jku.se.eatemup.core.model.Account;
import at.jku.se.eatemup.core.model.Battle;
import at.jku.se.eatemup.core.model.GoodiePoint;
import at.jku.se.eatemup.core.model.Location;
import at.jku.se.eatemup.core.model.Player;
import at.jku.se.eatemup.core.model.Position;
import at.jku.se.eatemup.core.model.specialaction.SpecialAction;
import at.jku.se.eatemup.sockets.SessionStore;

public class Engine {
	private class BattleAnswerTask extends GameTask<BattleAnswerMessage> {

		public BattleAnswerTask(BattleAnswerMessage message, Sender sender) {
			super(message, sender);
		}

		@Override
		public void run() {
			Game g = getPlayerGame(sender.userid);
			if (g != null) {
				Battle b = g.addBattleAnswer(message.userid,
						Integer.parseInt(message.answer), message.duration);
				if (b == null)
					return;
				BattleResultMessage message = new BattleResultMessage();
				message.correctResult = b.getResult()[0] + "";
				BattleWinner bw = b.getWinner();
				int points = -1;
				switch (bw) {
				case User1: {
					message.winnerUserid = b.getUserid1();
					message.winner = userManager.getUsernameByUserid(b.getUserid1());
					points = g.getBattleWinPoints(b.getUserid1(),
							b.getUserid2());
					g.addPlayerPoints(b.getUserid1(), points);
					g.addPlayerPoints(b.getUserid2(), -points);
				}
					break;
				case User2: {
					message.winnerUserid = b.getUserid2();
					message.winner = userManager.getUsernameByUserid(b.getUserid2());
					points = g.getBattleWinPoints(b.getUserid2(),
							b.getUserid1());
					g.addPlayerPoints(b.getUserid2(), points);
					g.addPlayerPoints(b.getUserid1(), -points);
				}
					break;
				case Draw: {
					message.winner = "Draw";
				}
					break;
				default:
					return;
				}
				ArrayList<String> recs = new ArrayList<>();
				recs.add(b.getUserid1());
				recs.add(b.getUserid2());
				MessageContainer container = MessageCreator.createMsgContainer(
						message,
						userManager.convertIdListToSessionList(recs));
				MessageHandler.PushMessage(container);
			}
		}
	}

	private static class DbManager {
		private static boolean firstCall = true;

		public static DataStore2 getDataStore() {
			DataStore2 ds = new DataStore2();
			if (firstCall) {
				ds.createTables();
				firstCall = false;
				try {
					Thread.sleep(1500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			return ds;
		}
	}

	private class ExitTask extends GameTask<ExitMessage> {

		public ExitTask(ExitMessage message, Sender sender) {
			super(message, sender);
		}

		private void removeUserFromAllAudiences(String userid) {
			if (userGameAudienceMap.containsKey(userid)) {
				try {
					Game g = runningGames
							.get(userGameAudienceMap.get(userid));
					g.removeAudienceUser(userid);
				} catch (Exception ex) {
					Logger.log("failed to remove player from standby game."
							+ Logger.stringifyException(ex));
				}
			}
		}

		private void removeUserFromAllRunningGames(String userid) {
			if (userGameMap.containsKey(userid)) {
				try {
					Game g = runningGames.get(userGameMap.get(userid));
					g.removePlayer(userid);
				} catch (Exception ex) {
					Logger.log("failed to remove player from running game."
							+ Logger.stringifyException(ex));
				}
			}
		}

		private void removeUserFromAllStandbyGames(String userid) {
			if (userStandbyGameMap.containsKey(userid)) {
				try {
					Game g = standbyGames.get(userStandbyGameMap.get(userid));
					g.removePlayer(userid);
				} catch (Exception ex) {
					Logger.log("failed to remove player from standby game."
							+ Logger.stringifyException(ex));
				}
			}
		}

		@Override
		public void run() {
			sendLogoutMessage(sender.session,
					"accepting exit action, user logout", sender.username);
			removeUserFromAllRunningGames(sender.username);
			removeUserFromAllStandbyGames(sender.username);
			removeUserFromAllAudiences(sender.username);
			userManager.removeUser(sender.session);
			SessionStore.removeSession(sender.session);
		}
	}

	private class FollowGameRequestTask extends
			GameTask<FollowGameRequestMessage> {

		public FollowGameRequestTask(FollowGameRequestMessage message,
				Sender sender) {
			super(message, sender);
		}

		private Game getARunningGame() {
			Random rand = new Random();
			try {
				int idx = rand.nextInt(runningGames.size());
				Game g = runningGames.get((new ArrayList<String>(runningGames
						.keySet()).get(idx)));
				return g;
			} catch (Exception ex) {
				Logger.log("failed retrieving a running game for audience viewing.</br>"
						+ Logger.stringifyException(ex));
				return null;
			}
		}

		private boolean isAGameRunning() {
			return runningGames.size() >= 1;
		}

		@Override
		public void run() {
			if (isAGameRunning()) {
				Game g = getARunningGame();
				if (g != null) {
					try {
						g.addUserToAudience(sender.username);
						userGameAudienceMap.put(sender.username, g.getId());
					} catch (Exception ex) {
						Logger.log("failed adding a viewer to a running game.</br>"
								+ Logger.stringifyException(ex));
					}
				}
			}
		}
	}

	private abstract class GameTask<T> implements Runnable {
		protected T message;
		protected Sender sender;

		public GameTask(T message, Sender sender) {
			this.message = message;
			this.sender = sender;
		}
	}

	private class HighscoreRequestTask extends
			GameTask<HighscoreRequestMessage> {

		public HighscoreRequestTask(HighscoreRequestMessage message,
				Sender sender) {
			super(message, sender);
		}

		private ArrayList<HashMap<String, Object>> createHighscore(
				ArrayList<Account> list) {
			ArrayList<HashMap<String, Object>> ret = new ArrayList<>();
			for (Account a : list) {
				HashMap<String, Object> map = new HashMap<>();
				map.put("username", a.getName());
				map.put("points", a.getPoints());
				ret.add(map);
			}
			return ret;
		}

		@Override
		public void run() {
			DataStore2 ds = DbManager.getDataStore();
			try {
				ArrayList<Account> list = ds.getHighscore(message.topx);
				HighscoreMessage msg = new HighscoreMessage();
				msg.topx = message.topx > list.size() ? list.size()
						: message.topx;
				msg.highscore = createHighscore(list);
				MessageContainer container = MessageCreator.createMsgContainer(
						msg, sender.session);
				MessageHandler.PushMessage(container);
			} catch (Exception e) {
				Logger.log("retrieving highscore failed. "
						+ Logger.stringifyException(e));
			} finally {
				ds.closeConnection();
			}
		}
	}

	private class LoginTask extends GameTask<LoginMessage> {

		private boolean credentialResult;

		public LoginTask(LoginMessage message, Sender sender) {
			super(message, sender);
		}

		public LoginTask(LoginMessage message, Sender sender, boolean credResult) {
			super(message, sender);
			credentialResult = credResult;
		}

		@Override
		public void run() {
			if (!credentialResult) {
				ReadyForGameMessage message = new ReadyForGameMessage();
				message.adCode = "";
				message.loginSuccess = credentialResult;
				message.points = -1;
				MessageContainer container = MessageCreator.createMsgContainer(
						message, sender.session);
				MessageHandler.PushMessage(container);
			} else {
				userManager.addUser(sender);
				DataStore2 ds = DbManager.getDataStore();
				Account acc = ds.getAccountByUsername(message.username);
				if (acc != null) {
					ReadyForGameMessage message = new ReadyForGameMessage();
					message.adCode = UUID.randomUUID().toString()
							.substring(0, 8);
					message.loginSuccess = credentialResult;
					message.points = acc.getPoints();
					MessageContainer container = MessageCreator
							.createMsgContainer(message, sender.session);
					MessageHandler.PushMessage(container);
				}
			}
		}
	}

	private class PlayTask extends GameTask<PlayMessage> {

		public PlayTask(PlayMessage message, Sender sender) {
			super(message, sender);
		}

		@Override
		public void run() {
			GameStandbyUpdateMessage msg = new GameStandbyUpdateMessage();
			msg.readyForStart = addPlayerToStandbyGame(message.username);
			msg.players = new ArrayList<>();
			Game game = getPlayerStandbyGame(message.username);
			for (Player p : game.getPlayers()) {
				HashMap<String, Object> map = new HashMap<>();
				map.put("username", p.getName());
				map.put("teamRed", game.isInRedTeam(p));
				msg.players.add(map);
			}
			MessageContainer container = MessageCreator.createMsgContainer(msg,
					getReceiverList(game.getPlayers()));
			MessageHandler.PushMessage(container);
		}
	}

	private class PositionTask extends GameTask<PositionMessage> {

		public PositionTask(PositionMessage message, Sender sender) {
			super(message, sender);
		}

		private boolean forStandbyGame(String uid) {
			return userStandbyGameMap.containsKey(uid);
		}

		@Override
		public void run() {
			String uid = message.username;
			Position p = new Position();
			p.setLatitude(message.latitude);
			p.setLongitude(message.longitude);
			Game g;
			if (forStandbyGame(uid)) {
				g = getPlayerStandbyGame(uid);
				g.setPlayerPosition(uid, p, message.timestamp);
			} else {
				g = getPlayerGame(uid);
				g.processPlayerPositionChange(uid, p, message.timestamp);
			}
		}
	}

	private class RequestForGameStartTask extends
			GameTask<RequestForGameStartMessage> {

		public RequestForGameStartTask(RequestForGameStartMessage message,
				Sender sender) {
			super(message, sender);
		}

		@Override
		public void run() {
			Game game = getPlayerStandbyGame(message.username);
			game.setPlayerReady(message.username);
			if (game.allPlayersReady()) {
				scheduleFullGameUpdate(game, null);
			} else {
				if (!game.isStartSurveySent()) {
					GameStartSurveyMessage msg = new GameStartSurveyMessage();
					msg.requestingUser = message.username;
					MessageContainer container = MessageCreator
							.createMsgContainer(msg, getReceiverListById(game
									.getNotReadyPlayers()));
					MessageHandler.PushMessage(container);
					game.setStartSurveySent(true);
				}
			}
		}
	}

	private class SpecialActionDeactivationTask implements Runnable {

		private String actionName;
		private long delay;
		private String username;
		private String userid;
		private ArrayList<String> receivers;

		public SpecialActionDeactivationTask(String userid,
				SpecialAction specialAction, ArrayList<String> receivers) {
			this.actionName = specialAction.getName();
			this.delay = specialAction.getDuration() * 1000l;
			this.username = userManager.getUsernameByUserid(userid);
			this.receivers = receivers;
			this.userid = userid;
		}

		@Override
		public void run() {
			try {
				Thread.sleep(this.delay);
			} catch (InterruptedException e) {
				Logger.log("special action deactivation task delay has been interrupted."
						+ Logger.stringifyException(e));
			} finally {
				Game g = getPlayerGame(userid);
				if (g != null) {
					g.disableSpecialAction(userid);
				}
				SpecialActionDeactivatedMessage message = new SpecialActionDeactivatedMessage();
				message.specialAction = this.actionName;
				message.username = this.username;
				message.userid = this.userid;
				MessageContainer container = MessageCreator.createMsgContainer(
						message, this.receivers);
				MessageHandler.PushMessage(container);
			}
		}
	}

	private class UpdatePointsTask implements Runnable {

		private ArrayList<Player> players;
		private DataStore2 datastore;

		public UpdatePointsTask(ArrayList<Player> players, DataStore2 datastore) {
			this.players = players;
			this.datastore = datastore;
		}

		@Override
		public void run() {
			try {
				for (Player p : this.players) {
					this.datastore.addUserPoints(p.getName(), p.getPoints());
				}
			} catch (Exception ex) {
				Logger.log("updating player points failed."
						+ Logger.stringifyException(ex));
			} finally {
				this.datastore.closeConnection();
			}
		}
	}

	public static class UserManager {
		private ConcurrentHashMap<String, String> useridSessionMap = new ConcurrentHashMap<>();
		private ConcurrentHashMap<String, String> sessionUseridMap = new ConcurrentHashMap<>();
		private ConcurrentHashMap<String, String> useridUsernameMap = new ConcurrentHashMap<>();
		private ConcurrentHashMap<String, String> sessionUsernameMap = new ConcurrentHashMap<>();

		public void addUser(Sender sender) {
			String userid = sender.userid;
			String username = sender.username;
			String sessionid = sender.session;
			useridSessionMap.put(userid, sessionid);
			sessionUseridMap.put(sessionid, userid);
			useridUsernameMap.put(userid, username);
			sessionUsernameMap.put(sessionid, username);
		}

		public ArrayList<String> convertIdListToSessionList(
				ArrayList<String> idList) {
			ArrayList<String> list = new ArrayList<>();
			for (String id : idList) {
				String ses = getSessionByUserid(id);
				if (ses != null && !ses.equals("")) {
					list.add(ses);
				}
			}
			return list;
		}

		public String getSessionByUserid(String userid) {
			return useridSessionMap.get(userid);
		}

		public String getUseridBySession(String sessionid) {
			return sessionUseridMap.get(sessionid);
		}

		public void removeAllInvolvedUsers(Game game) {
			for (String id : game.getBroadcastReceiverIds()) {
				try {
					removeUser(getSessionByUserid(id));
				} catch (Exception ex) {
					Logger.log("trying to remove non-existing session."
							+ Logger.stringifyException(ex));
				}
			}
		}

		public void removeUser(String sessionid) {
			String uid = getUseridBySession(sessionid);
			useridSessionMap.remove(uid);
			sessionUseridMap.remove(sessionid);
			useridUsernameMap.remove(uid);
			sessionUsernameMap.remove(sessionid);
		}

		public void updateUserSession(String userid, String newSessionId,
				String oldSessionId) {
			useridSessionMap.put(userid, newSessionId);
			sessionUseridMap.remove(oldSessionId);
			sessionUseridMap.put(newSessionId,userid);
			sessionUsernameMap.put(newSessionId, sessionUsernameMap.get(oldSessionId));
			sessionUsernameMap.remove(oldSessionId);			
		}

		public boolean userExists(String userid) {
			return useridSessionMap.containsKey(userid);
		}

		public boolean userExistsBySession(String session) {
			return sessionUseridMap.containsKey(session);
		}

		public String getUsernameByUserid(String userid) {
			String temp = useridUsernameMap.get(userid);
			if (temp != null){
				return temp;
			}
			return "";
		}
	}

	private static ConcurrentHashMap<String, Game> runningGames = new ConcurrentHashMap<>();
	private static ConcurrentHashMap<String, Game> standbyGames = new ConcurrentHashMap<>();
	public static UserManager userManager = new UserManager();
	private static ConcurrentHashMap<String, String> userGameMap = new ConcurrentHashMap<>();
	private static ConcurrentHashMap<String, String> userGameAudienceMap = new ConcurrentHashMap<>();
	private static ConcurrentHashMap<String, String> userStandbyGameMap = new ConcurrentHashMap<>();
	private static ExecutorService service = Executors.newCachedThreadPool();
	private static Engine instance = new Engine();

	private static final double jkuCenterLat = 48.337050;

	private static final double jkuCenterLong = 14.319600;

	private static final int defaultGameTimeSeconds = 600;

	public static boolean acceptBattleAnswer(BattleAnswerMessage message,
			Sender sender) {
		if (sessionExists(sender)) {
			service.execute(instance.new BattleAnswerTask(message, sender));
			return true;
		}
		return false;
	}

	public static boolean acceptExit(ExitMessage message, Sender sender) {
		if (sessionExists(sender)) {
			service.execute(instance.new ExitTask(message, sender));
			return true;
		}
		return false;
	}

	public static boolean acceptFollowGameRequest(
			FollowGameRequestMessage message, Sender sender) {
		if (sessionExists(sender)) {
			service.execute(instance.new FollowGameRequestTask(message, sender));
			return true;
		}
		return false;
	}

	public static boolean acceptHighscoreRequest(
			HighscoreRequestMessage message, Sender sender) {
		service.execute(instance.new HighscoreRequestTask(message, sender));
		return true;
	}

	public static boolean acceptLogin(LoginMessage message, Sender sender) {
		boolean check = checkLoginCredentials(message.username,
				message.password);
		LoginTask task = instance.new LoginTask(message, sender, check);
		service.execute(task);

		return check;
	}

	public static boolean acceptPlay(PlayMessage message, Sender sender) {
		if (sessionExists(sender)) {
			service.execute(instance.new PlayTask(message, sender));
			return true;
		}
		return false;
	}

	public static boolean acceptPosition(PositionMessage message, Sender sender) {
		if (sessionExists(sender)) {
			service.execute(instance.new PositionTask(message, sender));
			return true;
		}
		return false;
	}

	public static boolean acceptRequestForGameStart(
			RequestForGameStartMessage message, Sender sender) {
		if (sessionExists(sender)) {
			service.execute(instance.new RequestForGameStartTask(message,
					sender));
			return true;
		}
		return false;
	}

	private synchronized static boolean addPlayerToStandbyGame(String username) {
		Player player = new Player(username);
		Game g = getEmptyStandbyGame();
		userStandbyGameMap.put(username, g.getId());
		return g.AddPlayer(player);
	}

	private static boolean checkLoginCredentials(String username,
			String password) {
		DataStore2 ds = DbManager.getDataStore();
		/*
		 * passwordhash for username and check try {
		 * PasswordHashManager.check(password, "fromdb"); } catch (Exception e)
		 * { e.printStackTrace(); }
		 */
		String storedPw = ds.getUserPassword(username);
		ds.closeConnection();
		try {
			return storedPw.equals(password);
		} catch (Exception ex) {
			return false;
		}
	}

	private static Location createNewLocation(DataStore2 db) {
		Location loc = new Location();
		CopyOnWriteArrayList<GoodiePoint> points = new CopyOnWriteArrayList<GoodiePoint>();
		points.addAll(db.getGoodiePoints());
		loc.setGoodiePoints(points);
		Position center = new Position();
		center.setLatitude(jkuCenterLat);
		center.setLongitude(jkuCenterLong);
		loc.setCenterPosition(center);
		return loc;
	}

	public static void endGame(Game game) {
		runningGames.remove(game);
		try {
			standbyGames.remove(game);
		} catch (Exception ex) {
			Logger.log("failed to end standby game, may have been running already");
		}
		try {
			userSessionMap.removeAllInvolvedUsers(game);
		} catch (Exception ex) {
			Logger.log("failed to remove user session");
		}
		try {
			removeGameFromMap(game.getId(), userGameMap);
		} catch (Exception ex) {
			Logger.log("failed to remove user from game map");
		}
		try {
			removeGameFromMap(game.getId(), userGameAudienceMap);
		} catch (Exception ex) {
			Logger.log("failed to remove user from audience map");
		}
		try {
			removeGameFromMap(game.getId(), userStandbyGameMap);
		} catch (Exception ex) {
			Logger.log("failed to remove user from standby game map");
		}
	}

	private synchronized static Game getEmptyStandbyGame() {
		for (Game g : standbyGames.values()) {
			if (!g.isFull()) {
				return g;
			}
		}
		Game g = new Game(defaultGameTimeSeconds);
		setupGame(g);
		standbyGames.put(g.getId(), g);
		return g;
	}

	private synchronized static Game getPlayerGame(String userid) {
		try {
			return runningGames.get(userGameMap.get(userid));
		} catch (Exception ex) {
			Logger.log("trying to get player-game which is not playing a running game."
					+ Logger.stringifyException(ex));
			return null;
		}
	}

	private synchronized static Game getPlayerStandbyGame(String userid) {
		try {
			return standbyGames.get(userStandbyGameMap.get(userid));
		} catch (Exception ex) {
			Logger.log("trying to get player-game which is not playing a running game."
					+ Logger.stringifyException(ex));
			return null;
		}
	}

	private static ArrayList<String> getReceiverList(ArrayList<Player> players) {
		ArrayList<String> list = new ArrayList<>();
		for (Player p : players) {
			if (userSessionMap.userExists(p.getName())) {
				list.add(userSessionMap.getSessionByUsername(p.getName()));
			}
		}
		return list;
	}

	private static ArrayList<String> getReceiverListById(
			ArrayList<String> players) {
		ArrayList<String> list = new ArrayList<>();
		for (String p : players) {
			if (userSessionMap.userExists(p)) {
				list.add(userSessionMap.getSessionByUsername(p));
			}
		}
		return list;
	}

	private static void removeGameFromMap(String gameId, Map<String, String> map) {
		ArrayList<String> remList = new ArrayList<>();
		for (Entry<String, String> e : map.entrySet()) {
			if (e.getValue().equals(gameId)) {
				remList.add(e.getKey());
			}
		}
		for (String key : remList) {
			map.remove(key);
		}
	}

	public static void removeLostPlayers(ArrayList<String> invalidSessionIds) {
		for (String id : invalidSessionIds) {
			String username = userSessionMap.getUsernameBySession(id);
			String gameId = userGameMap.get(username);
			Game g = runningGames.get(gameId);
			removePlayerFromGame(username, g);
			userSessionMap.removeUser(id);
		}
	}

	private static void removePlayerFromGame(String name, Game game) {
		game.removePlayer(name);
	}

	public static void scheduleSpecialActionDeactivation(String uid,
			SpecialAction specialAction, ArrayList<String> receivers) {
		service.execute(instance.new SpecialActionDeactivationTask(uid,
				specialAction, receivers));
	}
	
	public static void scheduleFullGameUpdate(Game game, String receiverName){
		service.execute(instance.new FullGameUpdateTask(game, receiverName));
	}
	
	private class FullGameUpdateTask implements Runnable{

		private Game game;
		private String receiverName;
		
		public FullGameUpdateTask(Game game, String receiverName){
			this.game = game;
		}
		
		@Override
		public void run() {
			GameStateMessage message = this.game.createGameStateMessage();
			ArrayList<String> recs;
			if (receiverName == null){
				recs = userSessionMap.convertNameListToSessionList(this.game.getBroadcastReceiverNames());
			} else {
				recs = new ArrayList<>();
				recs.add(userSessionMap.getSessionByUsername(receiverName));
			}
			MessageContainer container = MessageCreator.createMsgContainer(message, recs);
			MessageHandler.PushMessage(container);
		}
	}

	public static void sendLogoutMessage(String session, String reason,
			String userid) {
		try {
			LogoutMessage message = new LogoutMessage();
			message.reason = reason;
			message.username = userManager.getUsernameByUserid(userid);
			message.userid = userid;
			MessageHandler.PushMessage(MessageCreator.createMsgContainer(
					message, userManager.getSessionByUserid(userid)));
		} catch (Exception ex) {
			Logger.log("failed sending logout message."
					+ Logger.stringifyException(ex));
		}
	}

	private static boolean sessionExists(Sender sender) {
		if (userManager.userExistsBySession(sender.session)) {
			return true;
		}
		return tryRecoverUserSession(sender);
	}

	private static void setupGame(Game g) {
		DataStore2 db = DbManager.getDataStore();
		try{
		g.setLocation(createNewLocation(db));		
		g.createGoodies(true);
		} finally {
			db.closeConnection();
		}
	}

	private static boolean tryRecoverUserSession(Sender sender) {
		String ses = userManager.getSessionByUserid(sender.userid);
		if (ses != null && ses != "") {
			userManager.updateUserSession(sender.userid, sender.session,
					ses);
			SessionStore.removeSession(ses);
			return true;
		}
		return false;
	}

	public static void updateAccountPoints(ArrayList<Player> players) {
		UpdatePointsTask task = instance.new UpdatePointsTask(players,
				DbManager.getDataStore());
		service.execute(task);
	}

	public static boolean acceptGameStateRequest(
			GameStateRequestMessage message, Sender sender) {
		if (sessionExists(sender)) {
			service.execute(instance.new GameStateRequestTask(message,
					sender));
			return true;
		}
		return false;
	}
	
	private class GameStateRequestTask extends GameTask<GameStateRequestMessage>{
		
		private String receiver;

		public GameStateRequestTask(GameStateRequestMessage message,
				Sender sender) {
			super(message, sender);
			receiver = sender.userid;
		}

		@Override
		public void run() {
			Game g = getPlayerGame(this.receiver);
			if (g != null){
				scheduleFullGameUpdate(g,this.receiver);
			}
		}		
	}

	public static boolean acceptPong(PongMessage message, Sender sender) {
		// TODO Auto-generated method stub
		return false;
	}
}
