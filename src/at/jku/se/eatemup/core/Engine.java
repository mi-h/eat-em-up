package at.jku.se.eatemup.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.*;

import at.jku.se.eatemup.core.database.DataStore2;
import at.jku.se.eatemup.core.json.messages.*;
import at.jku.se.eatemup.core.logging.Logger;
import at.jku.se.eatemup.core.model.Battle;
import at.jku.se.eatemup.core.model.GoodiePoint;
import at.jku.se.eatemup.core.model.Location;
import at.jku.se.eatemup.core.model.Player;
import at.jku.se.eatemup.core.model.Position;
import at.jku.se.eatemup.sockets.SessionStore;

public class Engine {
	private class BattleAnswerTask extends GameTask<BattleAnswerMessage> {

		public BattleAnswerTask(BattleAnswerMessage message, Sender sender) {
			super(message, sender);
		}

		@Override
		public void run() {
			Game g = getPlayerGame(sender.username);
			if (g != null) {
				Battle b = g.addBattleAnswer(message.username,
						Integer.parseInt(message.answer), message.timestamp);
				if (b == null)
					return;
				BattleResultMessage message = new BattleResultMessage();
				message.correctResult = b.getResult()[0] + "";
				BattleWinner bw = b.getWinner();
				int points = -1;
				switch (bw) {
				case User1: {
					message.winner = b.getUsername1();
					points = g.getBattleWinPoints(b.getUsername1(),
							b.getUsername2());
					g.addPlayerPoints(b.getUsername1(), points);
					g.addPlayerPoints(b.getUsername2(), -points);
				}
					break;
				case User2: {
					message.winner = b.getUsername2();
					points = g.getBattleWinPoints(b.getUsername2(),
							b.getUsername1());
					g.addPlayerPoints(b.getUsername2(), points);
					g.addPlayerPoints(b.getUsername1(), -points);
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
				recs.add(b.getUsername1());
				recs.add(b.getUsername2());
				MessageContainer container = MessageCreator.createMsgContainer(
						message, recs);
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
					Thread.sleep(1000);
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

		private void removeUserFromAllAudiences(String username) {
			if (userGameAudienceMap.containsKey(username)) {
				try {
					Game g = runningGames
							.get(userGameAudienceMap.get(username));
					g.removeAudienceUser(username);
				} catch (Exception ex) {
					Logger.log("failed to remove player from standby game."
							+ Logger.stringifyException(ex));
				}
			}
		}

		private void removeUserFromAllRunningGames(String username) {
			if (userGameMap.containsKey(username)) {
				try {
					Game g = runningGames.get(userGameMap.get(username));
					g.removePlayer(username);
				} catch (Exception ex) {
					Logger.log("failed to remove player from running game."
							+ Logger.stringifyException(ex));
				}
			}
		}

		private void removeUserFromAllStandbyGames(String username) {
			if (userStandbyGameMap.containsKey(username)) {
				try {
					Game g = standbyGames.get(userStandbyGameMap.get(username));
					g.removePlayer(username);
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
			userSessionMap.removeUser(sender.session);
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

	private class LoginTask extends GameTask<LoginMessage> {

		public LoginTask(LoginMessage message, Sender sender) {
			super(message, sender);
		}

		@Override
		public void run() {
			userSessionMap.addUser(sender);
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

		@Override
		public void run() {
			// TODO Auto-generated method stub
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
				ArrayList<Player> players = game.getPlayers();
				ArrayList<GoodiePoint> goodies = game.getGoodiePoints();
				GameStartMessage message = new GameStartMessage();
				message.playerInfo = createPlayerInfoData(players, game);
				message.goodies = createGoodieData(goodies);
				message.remainingTime = defaultGameTimeSeconds;
				MessageContainer container = MessageCreator.createMsgContainer(
						message, game.getBroadcastReceiverNames());
				MessageHandler.PushMessage(container);
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

		private ArrayList<HashMap<String, Object>> createGoodieData(
				ArrayList<GoodiePoint> goodies) {
			ArrayList<HashMap<String, Object>> list = new ArrayList<>();
			for (GoodiePoint gp : goodies) {
				HashMap<String, Object> map = new HashMap<>();
				map.put("latitude", gp.getPosition().getLatitude());
				map.put("longitude", gp.getPosition().getLongitude());
				map.put("specialAction", gp.getGoodie().getSpecialAction()
						.getName());
				list.add(map);
			}
			return list;
		}

		private ArrayList<HashMap<String, Object>> createPlayerInfoData(
				ArrayList<Player> players, Game game) {
			ArrayList<HashMap<String, Object>> list = new ArrayList<>();
			for (Player p : players) {
				HashMap<String, Object> map = new HashMap<>();
				HashMap<String, Object> pMap = new HashMap<>();
				map.put("username", p.getName());
				map.put("points", 0);
				Position pos = game.getPlayerPosition(p.getName());
				pMap.put("latitude", pos.getLatitude());
				pMap.put("longitude", pos.getLongitude());
				map.put("position", pMap);
				list.add(map);
			}
			return list;
		}
	}

	private static class UserSession {
		private ConcurrentHashMap<String, String> usernameSessionMap = new ConcurrentHashMap<>();
		private ConcurrentHashMap<String, String> sessionUsernameMap = new ConcurrentHashMap<>();

		public void addUser(Sender sender) {
			String username = sender.username;
			String sessionid = sender.session;
			usernameSessionMap.put(username, sessionid);
			sessionUsernameMap.put(sessionid, username);
		}

		@Deprecated
		public void addUser(String sessionid, String username) {
			usernameSessionMap.put(username, sessionid);
			sessionUsernameMap.put(sessionid, username);
		}

		public String getSessionByUsername(String username) {
			return usernameSessionMap.get(username);
		}

		public String getUsernameBySession(String sessionid) {
			return sessionUsernameMap.get(sessionid);
		}

		public void removeUser(String sessionid) {
			String u = getUsernameBySession(sessionid);
			usernameSessionMap.remove(u);
			sessionUsernameMap.remove(sessionid);
		}

		public void updateUserSession(String username, String newSessionId,
				String oldSessionId) {
			usernameSessionMap.put(username, newSessionId);
			sessionUsernameMap.remove(oldSessionId);
			sessionUsernameMap.put(newSessionId, username);
		}

		public boolean userExists(String username) {
			return usernameSessionMap.containsKey(username);
		}

		public boolean userExistsBySession(String session) {
			return sessionUsernameMap.containsKey(session);
		}

		public void removeAllInvolvedUsers(Game game) {
			for (String name : game.getBroadcastReceiverNames()) {
				try {
					removeUser(getSessionByUsername(name));
				} catch (Exception ex) {
					Logger.log("trying to remove non-existing session."
							+ Logger.stringifyException(ex));
				}
			}
		}
	}

	private static ConcurrentHashMap<String, Game> runningGames = new ConcurrentHashMap<>();

	private static ConcurrentHashMap<String, Game> standbyGames = new ConcurrentHashMap<>();

	private static UserSession userSessionMap = new UserSession();

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

	public static boolean acceptLogin(LoginMessage message, Sender sender) {
		if (sessionExists(sender)) {
			boolean check = checkLoginCredentials(message.username,
					message.password);
			if (check) {
				service.execute(instance.new LoginTask(message, sender));
			}
			return check;
		}
		return false;
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
		return storedPw.equals(password);
	}

	private synchronized static Game getEmptyStandbyGame() {
		for (Game g : standbyGames.values()) {
			if (!g.isFull()) {
				return g;
			}
		}
		Game g = new Game();
		setupGame(g);
		standbyGames.put(g.getId(), g);
		return g;
	}

	private static void setupGame(Game g) {
		DataStore2 db = DbManager.getDataStore();
		g.setLocation(createNewLocation(db));
		db.closeConnection();
		g.createGoodies(true);
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

	private synchronized static Game getPlayerStandbyGame(String username) {
		try {
			return standbyGames.get(userStandbyGameMap.get(username));
		} catch (Exception ex) {
			Logger.log("trying to get player-game which is not playing a running game."
					+ Logger.stringifyException(ex));
			return null;
		}
	}

	private synchronized static Game getPlayerGame(String username) {
		try {
			return runningGames.get(userGameMap.get(username));
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

	}

	public static void sendLogoutMessage(String session, String reason,
			String username) {
		try {
			LogoutMessage message = new LogoutMessage();
			message.reason = reason;
			message.username = username;
			MessageHandler.PushMessage(MessageCreator.createMsgContainer(
					message, username));
		} catch (Exception ex) {
			Logger.log("failed sending logout message."
					+ Logger.stringifyException(ex));
		}
	}

	private static boolean sessionExists(Sender sender) {
		if (userSessionMap.userExistsBySession(sender.session)) {
			return true;
		}
		return tryRecoverUserSession(sender);
	}

	private static boolean tryRecoverUserSession(Sender sender) {
		String ses = userSessionMap.getSessionByUsername(sender.username);
		if (ses != null && ses != "") {
			userSessionMap.updateUserSession(sender.username, sender.session,
					ses);
			SessionStore.removeSession(ses);
			return true;
		}
		return false;
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

	public static void updateAccountPoints(ArrayList<Player> players) {
		UpdatePointsTask task = instance.new UpdatePointsTask(players,
				DbManager.getDataStore());
		service.execute(task);
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
}