package at.jku.se.eatemup.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import at.jku.se.eatemup.core.database.DataStore2;
import at.jku.se.eatemup.core.json.messages.AlreadyLoggedInMessage;
import at.jku.se.eatemup.core.json.messages.BattleAnswerMessage;
import at.jku.se.eatemup.core.json.messages.BattleResultMessage;
import at.jku.se.eatemup.core.json.messages.ExitMessage;
import at.jku.se.eatemup.core.json.messages.FollowGameRequestMessage;
import at.jku.se.eatemup.core.json.messages.GameStandbyUpdateMessage;
import at.jku.se.eatemup.core.json.messages.GameStartSurveyMessage;
import at.jku.se.eatemup.core.json.messages.GameStateMessage;
import at.jku.se.eatemup.core.json.messages.GameStateRequestMessage;
import at.jku.se.eatemup.core.json.messages.HighscoreMessage;
import at.jku.se.eatemup.core.json.messages.HighscoreRequestMessage;
import at.jku.se.eatemup.core.json.messages.LeaveGameMessage;
import at.jku.se.eatemup.core.json.messages.LoginMessage;
import at.jku.se.eatemup.core.json.messages.LogoutMessage;
import at.jku.se.eatemup.core.json.messages.PingMessage;
import at.jku.se.eatemup.core.json.messages.PlayMessage;
import at.jku.se.eatemup.core.json.messages.PongMessage;
import at.jku.se.eatemup.core.json.messages.PositionMessage;
import at.jku.se.eatemup.core.json.messages.ReadyForGameMessage;
import at.jku.se.eatemup.core.json.messages.RequestForGameStartMessage;
import at.jku.se.eatemup.core.json.messages.SpecialActionDeactivatedMessage;
import at.jku.se.eatemup.core.logging.Logger;
import at.jku.se.eatemup.core.model.Account;
import at.jku.se.eatemup.core.model.AccountType;
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
					message.winner = userManager.getUsernameByUserid(b
							.getUserid1());
					points = g.getBattleWinPoints(b.getUserid1(),
							b.getUserid2());
					g.addPlayerPoints(b.getUserid1(), points);
					g.addPlayerPoints(b.getUserid2(), -points);
				}
					break;
				case User2: {
					message.winnerUserid = b.getUserid2();
					message.winner = userManager.getUsernameByUserid(b
							.getUserid2());
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
						message, userManager.convertIdListToSessionList(recs));
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
	
	private synchronized static void removeUserFromAllAudiences(String userid) {
		if (userGameAudienceMap.containsKey(userid)) {
			try {
				Game g = runningGames.get(userGameAudienceMap.get(userid));
				g.removeAudienceUser(userid);
			} catch (Exception ex) {
				Logger.log("failed to remove player from standby game."
						+ Logger.stringifyException(ex));
			}
		}
	}

	private synchronized static void removeUserFromAllRunningGames(String userid) {
		if (userGameMap.containsKey(userid)) {
			try {
				Game g = runningGames.get(userGameMap.get(userid));
				g.removePlayer(userid);
				scheduleFullGameUpdate(g, g.getBroadcastReceiverIds());
			} catch (Exception ex) {
				Logger.log("failed to remove player from running game."
						+ Logger.stringifyException(ex));
			}
		}
	}

	private synchronized static void removeUserFromAllStandbyGames(String userid) {
		if (userStandbyGameMap.containsKey(userid)) {
			try {
				Game g = standbyGames.get(userStandbyGameMap.get(userid));
				g.removePlayer(userid);
				GameStandbyUpdateMessage msg = new GameStandbyUpdateMessage();
				msg.readyForStart = g.isReadyForStart();
				msg.players = new ArrayList<>();
				for (Player p : g.getPlayers()) {
					HashMap<String, Object> map = new HashMap<>();
					map.put("username", p.getName());
					map.put("userid", p.getUserid());
					map.put("teamRed", g.isInRedTeam(p));
					map.put("readyForStart", g.isPlayerReadyForGame(p));
					msg.players.add(map);
				}
				MessageContainer container = MessageCreator
						.createMsgContainer(msg,
								getReceiverList(g.getPlayers()));
				MessageHandler.PushMessage(container);
			} catch (Exception ex) {
				Logger.log("failed to remove player from standby game."
						+ Logger.stringifyException(ex));
			}
		}
	}

	private class ExitTask extends GameTask<ExitMessage> {

		public ExitTask(ExitMessage message, Sender sender) {
			super(message, sender);
		}

		@Override
		public void run() {
			try {
				sendLogoutMessage(sender.session,
						"accepting exit action, user logout", sender.userid);
			} catch (Exception ex) {
				Logger.log("sending logout message failed. "
						+ Logger.stringifyException(ex));
			}
			try {
				removeUserFromAllRunningGames(sender.userid);
			} catch (Exception ex) {
				Logger.log("removing player from running games failed. "
						+ Logger.stringifyException(ex));
			}
			try {
				removeUserFromAllStandbyGames(sender.userid);
			} catch (Exception ex) {
				Logger.log("removing player from standbygames failed. "
						+ Logger.stringifyException(ex));
			}
			try {
				removeUserFromAllAudiences(sender.userid);
			} catch (Exception ex) {
				Logger.log("removing player from audiences failed. "
						+ Logger.stringifyException(ex));
			}
			try {
				userManager.removeUser(sender.session);
			} catch (Exception ex) {
				Logger.log("removing player from usermanager failed. "
						+ Logger.stringifyException(ex));
			}
			try {
				SessionStore.removeSession(sender.session);
			} catch (Exception ex) {
				Logger.log("removing player from sessionstore failed. "
						+ Logger.stringifyException(ex));
			}
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
						g.addUserToAudience(sender.userid);
						userGameAudienceMap.put(sender.userid, g.getId());
					} catch (Exception ex) {
						Logger.log("failed adding a viewer to a running game.</br>"
								+ Logger.stringifyException(ex));
					}
				}
			}
		}
	}

	private class FullGameUpdateTask implements Runnable {

		private Game game;
		private String receiverId;
		private boolean multiple;
		private ArrayList<String> userIds;

		public FullGameUpdateTask(Game game, String receiverId) {
			this.game = game;
			this.receiverId = receiverId;
			this.multiple = false;
		}

		public FullGameUpdateTask(Game game, ArrayList<String> receiverIds) {
			this.game = game;
			this.multiple = true;
			this.userIds = receiverIds;
		}

		@Override
		public void run() {
			GameStateMessage message = this.game.createGameStateMessage();
			ArrayList<String> recs;
			if (multiple || receiverId == null) {
				recs = userManager.convertIdListToSessionList(this.game
						.getBroadcastReceiverIds());
			} else {
				recs = new ArrayList<>();
				recs.add(userManager.getSessionByUserid(receiverId));
			}
			MessageContainer container = MessageCreator.createMsgContainer(
					message, recs);
			MessageHandler.PushMessage(container);
		}
	}

	private class GameStateRequestTask extends
			GameTask<GameStateRequestMessage> {

		private String receiver;

		public GameStateRequestTask(GameStateRequestMessage message,
				Sender sender) {
			super(message, sender);
			receiver = sender.userid;
		}

		@Override
		public void run() {
			Game g = getPlayerGame(this.receiver);
			if (g != null) {
				scheduleFullGameUpdate(g, this.receiver);
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
				map.put("username", a.getUsername());
				map.put("userid", a.getId());
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

		public LoginTask(LoginMessage message, Sender sender) {
			super(message, sender);
		}

		private boolean checkLoginCredentials(String username, String password,
				DataStore2 ds) {
			/*
			 * passwordhash for username and check try {
			 * PasswordHashManager.check(password, "fromdb"); } catch (Exception
			 * e) { e.printStackTrace(); }
			 */
			String storedPw = ds.getUserPassword(username);
			try {
				return storedPw.equals(password);
			} catch (Exception ex) {
				return false;
			}
		}

		private AccountType parseAccountType(String type) {
			switch (type) {
			case "facebook":
				return AccountType.Facebook;
			default:
				return AccountType.Standard;
			}
		}

		@Override
		public void run() {
			AccountType type = parseAccountType(message.type);
			DataStore2 ds = DbManager.getDataStore();
			Account acc;
			boolean loginResult;
			if (type == AccountType.Facebook) {
				if (message.facebookid == null || message.facebookid.equals("")) {
					return;
				}
				acc = ds.getFacebookAccount(message.facebookid);
				if (acc == null) {
					Account temp = new Account();
					temp.setFacebookId(message.facebookid);
					temp.setUsername(message.username);
					temp.setType(AccountType.Facebook);
					temp.setId(UUID.randomUUID().toString());
					ds.addAccount(temp);
					acc = temp;
				}
				loginResult = true;
			} else {
				loginResult = checkLoginCredentials(message.username,
						message.password, ds);
				acc = loginResult ? ds.getAccountByUsername(message.username)
						: null;
			}
			if (loginResult && acc != null) {
				sender.userid = acc.getId();
				userManager.addUser(sender);
				ReadyForGameMessage message = new ReadyForGameMessage();
				message.adCode = UUID.randomUUID().toString().substring(0, 8);
				message.loginSuccess = loginResult;
				message.points = acc.getPoints();
				message.userid = acc.getId();
				MessageContainer container = MessageCreator.createMsgContainer(
						message, sender.session);
				MessageHandler.PushMessage(container);
			} else {
				ReadyForGameMessage message = new ReadyForGameMessage();
				message.adCode = "";
				message.loginSuccess = loginResult;
				message.points = -1;
				message.userid = null;
				MessageContainer container = MessageCreator.createMsgContainer(
						message, sender.session);
				MessageHandler.PushMessage(container);
			}
		}

	}

	private static class PingManager {
		private class Ping {
			public String userid;
			public boolean secondAttempt;
			public String pingId;
			public String gameid;
		}

		private class PingTask extends TimerTask {

			private void cleanupUsers() {
				ArrayList<String> lostPlayers = new ArrayList<String>();
				for (Ping p : secondAttemptPings.values()) {
					secondAttemptPings.remove(p);
					lostPlayers.add(p.userid);
				}
				removeLostPlayers(lostPlayers);
			}

			private void escalateFirstAttempts() {
				ArrayList<PingMessage> messages = new ArrayList<>();
				for (Ping p : firstAttemptPings.values()) {
					p.secondAttempt = true;
					messages.add(createPingMessage(p));
					secondAttemptPings.put(p.pingId, p);
					firstAttemptPings.remove(p);
				}
				sendPingMessages(messages);
			}

			@Override
			public void run() {
				startGamePings();
				escalateFirstAttempts();
				cleanupUsers();
			}

			private void startGamePings() {
				for (Game g : observerMap.values()) {
					service.execute(new ProcessGamePingTask(g));
				}
			}
		}

		private class ProcessGamePingTask implements Runnable {
			private Game game;

			public ProcessGamePingTask(Game game) {
				this.game = game;
			}

			@Override
			public void run() {
				ArrayList<String> ids = game.getBroadcastReceiverIds();
				ArrayList<Ping> pings = new ArrayList<>(ids.size());
				for (String uid : ids) {
					if (!userWithActivePing.contains(uid)) {
						Ping ping = new Ping();
						ping.pingId = UUID.randomUUID().toString();
						ping.secondAttempt = false;
						ping.userid = uid;
						ping.gameid = game.getId();
						pings.add(ping);
					}
				}
				ArrayList<PingMessage> messages = new ArrayList<>(pings.size());
				ArrayList<String> users = new ArrayList<>();
				for (Ping ping : pings) {
					messages.add(createPingMessage(ping));
					firstAttemptPings.put(ping.pingId, ping);
					users.add(ping.userid);
				}
				userWithActivePing.addAll(users);
				sendPingMessages(messages);
			}
		}

		private static ConcurrentHashMap<String, Game> observerMap = new ConcurrentHashMap<>();
		private static ConcurrentHashMap<String, Ping> firstAttemptPings = new ConcurrentHashMap<>();
		private static ConcurrentHashMap<String, Ping> secondAttemptPings = new ConcurrentHashMap<>();
		private static CopyOnWriteArrayList<String> userWithActivePing = new CopyOnWriteArrayList<>();
		private static Timer ticker = new Timer();

		private static PingMessage createPingMessage(Ping ping) {
			PingMessage temp = new PingMessage();
			temp.secondAttempt = ping.secondAttempt;
			temp.userid = ping.userid;
			temp.username = ping.userid;
			temp.pingid = ping.pingId;
			return temp;
		}

		public synchronized static void kill() {
			ticker.cancel();
		}

		private static void sendPingMessages(ArrayList<PingMessage> messages) {
			for (PingMessage msg : messages) {
				MessageContainer container = MessageCreator.createMsgContainer(
						msg, userManager.getSessionByUserid(msg.userid));
				MessageHandler.PushMessage(container);
			}
		}

		public PingManager() {
			ticker.scheduleAtFixedRate(new PingTask(), 30000, pingInterval);
		}

		public synchronized void acceptPong(PongMessage pong) {
			if (!pong.secondAttempt) {
				firstAttemptPings.remove(pong.pingid);
			} else {
				secondAttemptPings.remove(pong.pingid);
			}
			userWithActivePing.remove(pong.userid);
		}

		public void addGame(Game game) {
			observerMap.put(game.getId(), game);
		}

		private void cleanupAttempts(String gameid) {
			ArrayList<String> remList = new ArrayList<>();
			ArrayList<String> userList = new ArrayList<>();
			for (Ping p : secondAttemptPings.values()) {
				if (p.gameid.equals(gameid)) {
					remList.add(p.pingId);
					userList.add(p.userid);
				}
			}
			for (String s : remList) {
				secondAttemptPings.remove(s);
			}
			remList.clear();
			for (Ping p : firstAttemptPings.values()) {
				if (p.gameid.equals(gameid)) {
					remList.add(p.pingId);
					userList.add(p.userid);
				}
			}
			for (String s : remList) {
				firstAttemptPings.remove(s);
			}
			userWithActivePing.removeAll(userList);
		}

		public void removeGame(Game game) {
			observerMap.remove(game.getId());
			cleanupAttempts(game.getId());
		}
	}

	private class PlayTask extends GameTask<PlayMessage> {

		public PlayTask(PlayMessage message, Sender sender) {
			super(message, sender);
		}

		@Override
		public void run() {
			GameStandbyUpdateMessage msg = new GameStandbyUpdateMessage();
			msg.readyForStart = addPlayerToStandbyGame(message.userid);
			msg.players = new ArrayList<>();
			Game game = getPlayerStandbyGame(message.userid);
			for (Player p : game.getPlayers()) {
				HashMap<String, Object> map = new HashMap<>();
				map.put("username", p.getName());
				map.put("userid", p.getUserid());
				map.put("teamRed", game.isInRedTeam(p));
				map.put("readyForStart", game.isPlayerReadyForGame(p));
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

		private boolean forStandbyGame(String userid) {
			return userStandbyGameMap.containsKey(userid);
		}

		@Override
		public void run() {
			String uid = message.userid;
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
			Game game = getPlayerStandbyGame(message.userid);
			if (message.startGame) {
				game.setPlayerReady(message.userid);
			} else {
				game.setPlayerNotReady(message.userid);
			}
			GameStartSurveyMessage msg = new GameStartSurveyMessage();
			for (Player p : game.getPlayers()) {
				msg.players.add(createPlayerArray(p, game));
			}
			MessageContainer container = MessageCreator.createMsgContainer(msg,
					getReceiverListById(game.getPlayerIds()));
			MessageHandler.PushMessage(container);
			game.setStartSurveySent(true);
			if (game.allPlayersReady()) {
				game.startGame();
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

	private static class TaskManager {
		private LinkedBlockingQueue<Runnable> tasks;
		private Thread worker;
		private boolean started;
		private final int maxTasks = 120;
		private final int sleepTime = 5;

		public TaskManager() {
			tasks = new LinkedBlockingQueue<>(maxTasks);
			started = false;
			start();
		}

		public boolean addTask(Runnable task) {
			try {
				tasks.put(task);
				return true;
			} catch (InterruptedException e) {
				Logger.log("adding task failed. "
						+ Logger.stringifyException(e));
				return false;
			}
		}

		/**
		 * kills the worker
		 * 
		 * @return remaining tasks in the queue
		 */
		public synchronized List<Runnable> dispose() {
			ArrayList<Runnable> tmp = new ArrayList<>(maxTasks);
			tasks.drainTo(tmp);
			worker.interrupt();
			tasks = null;
			started = false;
			return tmp;
		}

		public synchronized int[] getStatus() {
			int[] ret = new int[2];
			ret[0] = tasks.size();
			ret[1] = maxTasks;
			return ret;
		}

		private synchronized void start() {
			if (!started) {
				started = true;
				if (worker != null) {
					worker.interrupt();
				}
				worker = new Thread() {
					@Override
					public void run() {
						while (true) {
							Runnable temp = tasks.poll();
							if (temp != null) {
								try {
									temp.run();
								} catch (Exception e) {
									Logger.log("task throws exception. "
											+ Logger.stringifyException(e));
								}
							} else {
								try {
									Thread.sleep(sleepTime);
								} catch (InterruptedException e) {
									Logger.log("worker interrupted. "
											+ Logger.stringifyException(e));
								}
							}
						}
					}
				};
				worker.start();
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
					this.datastore.addUserPoints(p.getUserid(), p.getPoints());
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
		private static ConcurrentHashMap<String, String> useridSessionMap = new ConcurrentHashMap<>();
		private static ConcurrentHashMap<String, String> sessionUseridMap = new ConcurrentHashMap<>();
		private static ConcurrentHashMap<String, String> useridUsernameMap = new ConcurrentHashMap<>();
		private static ConcurrentHashMap<String, String> sessionUsernameMap = new ConcurrentHashMap<>();

		public synchronized void addUser(Sender sender) {
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

		public String getUsernameByUserid(String userid) {
			String temp = useridUsernameMap.get(userid);
			if (temp != null) {
				return temp;
			}
			return "";
		}

		public synchronized boolean isUserActive(LoginMessage message,
				Sender sender) {
			DataStore2 ds = DbManager.getDataStore();
			Account acc;
			try {
				if (message.facebookid != null
						&& !message.facebookid.equals("")) {
					acc = ds.getFacebookAccount(message.facebookid);
				} else {
					acc = ds.getAccountByUsername(message.username);
				}
				if (acc != null) {
					String uid = acc.getId();
					return useridSessionMap.containsKey(uid);
				}
				return false;
			} catch (Exception ex) {
				return false;
			} finally {
				ds.closeConnection();
			}
		}

		public synchronized void removeAllInvolvedUsers(Game game) {
			for (String id : game.getBroadcastReceiverIds()) {
				try {
					removeUser(getSessionByUserid(id));
				} catch (Exception ex) {
					Logger.log("trying to remove non-existing session."
							+ Logger.stringifyException(ex));
				}
			}
		}

		public synchronized void removeUser(String sessionid) {
			String uid = getUseridBySession(sessionid);
			useridSessionMap.remove(uid);
			sessionUseridMap.remove(sessionid);
			useridUsernameMap.remove(uid);
			sessionUsernameMap.remove(sessionid);
		}

		public synchronized void updateUserSession(String userid,
				String newSessionId, String oldSessionId) {
			useridSessionMap.put(userid, newSessionId);
			sessionUseridMap.remove(oldSessionId);
			sessionUseridMap.put(newSessionId, userid);
			sessionUsernameMap.put(newSessionId,
					sessionUsernameMap.get(oldSessionId));
			sessionUsernameMap.remove(oldSessionId);
		}

		public boolean userExists(String userid) {
			return useridSessionMap.containsKey(userid);
		}

		public boolean userExistsBySession(String session) {
			return sessionUseridMap.containsKey(session);
		}
	}

	private static ConcurrentHashMap<String, Game> runningGames = new ConcurrentHashMap<>();
	private static ConcurrentHashMap<String, Game> standbyGames = new ConcurrentHashMap<>();
	private static ConcurrentHashMap<String, String> userGameMap = new ConcurrentHashMap<>();
	private static ConcurrentHashMap<String, String> userGameAudienceMap = new ConcurrentHashMap<>();
	private static ConcurrentHashMap<String, String> userStandbyGameMap = new ConcurrentHashMap<>();
	private static ExecutorService service = Executors.newCachedThreadPool();
	private static Engine instance = new Engine();
	private static final double jkuCenterLat = 48.337050;
	private static final double jkuCenterLong = 14.319600;
	private static final int defaultGameTimeSeconds = 600;
	public static UserManager userManager = new UserManager();
	private static PingManager pingManager = new PingManager();
	private static TaskManager taskManager = new TaskManager();
	private static final String flushToken = "supersecret";
	private static final int pingInterval = 30000; // millisecs

	public static boolean acceptBattleAnswer(BattleAnswerMessage message,
			Sender sender) {
		if (sessionExists(sender)) {
			// service.execute(instance.new BattleAnswerTask(message, sender));
			// return true;
			return taskManager.addTask(instance.new BattleAnswerTask(message,
					sender));
		}
		return false;
	}

	public static boolean acceptExit(ExitMessage message, Sender sender) {
		if (sessionExists(sender)) {
			// service.execute(instance.new ExitTask(message, sender));
			// return true;
			return taskManager.addTask(instance.new ExitTask(message, sender));
		}
		return false;
	}

	public static boolean acceptFollowGameRequest(
			FollowGameRequestMessage message, Sender sender) {
		if (sessionExists(sender)) {
			// service.execute(instance.new FollowGameRequestTask(message,
			// sender));
			// return true;
			return taskManager.addTask(instance.new FollowGameRequestTask(
					message, sender));
		}
		return false;
	}

	public static boolean acceptGameStateRequest(
			GameStateRequestMessage message, Sender sender) {
		if (sessionExists(sender)) {
			// service.execute(instance.new GameStateRequestTask(message,
			// sender));
			// return true;
			return taskManager.addTask(instance.new GameStateRequestTask(
					message, sender));
		}
		return false;
	}

	public static boolean acceptHighscoreRequest(
			HighscoreRequestMessage message, Sender sender) {
		// service.execute(instance.new HighscoreRequestTask(message, sender));
		// return true;
		return taskManager.addTask(instance.new HighscoreRequestTask(message,
				sender));
	}

	public static synchronized boolean acceptLogin(LoginMessage message,
			Sender sender) {
		if (userManager.isUserActive(message, sender)) {
			AlreadyLoggedInMessage msg = new AlreadyLoggedInMessage();
			MessageContainer container = MessageCreator.createMsgContainer(msg,
					sender.session);
			MessageHandler.PushMessage(container);
			return false;
		}
		if (message.type.equals("facebook")) {
			if (message.facebookid == null || message.facebookid.equals(""))
				return false;
		} else {
			if (message.username == null || message.password == null
					|| message.username.equals("")
					|| message.password.equals(""))
				return false;
		}
		// service.execute(instance.new LoginTask(message, sender));
		// return true;
		return taskManager.addTask(instance.new LoginTask(message, sender));
	}

	public static boolean acceptPlay(PlayMessage message, Sender sender) {
		if (sessionExists(sender)) {
			// service.execute(instance.new PlayTask(message, sender));
			// return true;
			return taskManager.addTask(instance.new PlayTask(message, sender));
		}
		return false;
	}

	public static boolean acceptPong(PongMessage message, Sender sender) {
		pingManager.acceptPong(message);
		return true;
	}

	public static boolean acceptPosition(PositionMessage message, Sender sender) {
		if (sessionExists(sender)) {
			// service.execute(instance.new PositionTask(message, sender));
			// return true;
			return taskManager.addTask(instance.new PositionTask(message,
					sender));
		}
		return false;
	}

	public static boolean acceptRequestForGameStart(
			RequestForGameStartMessage message, Sender sender) {
		if (sessionExists(sender)) {
			// service.execute(instance.new RequestForGameStartTask(message,
			// sender));
			// return true;
			return taskManager.addTask(instance.new RequestForGameStartTask(
					message, sender));
		}
		return false;
	}

	private synchronized static boolean addPlayerToStandbyGame(String userid) {
		Player player = new Player(userManager.getUsernameByUserid(userid),
				userid);
		Game g = getEmptyStandbyGame();
		userStandbyGameMap.put(userid, g.getId());
		return g.addPlayer(player);
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
		pingManager.removeGame(game);
		try {
			standbyGames.remove(game);
		} catch (Exception ex) {
			Logger.log("failed to end standby game, may have been running already");
		}
		try {
			userManager.removeAllInvolvedUsers(game);
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

	/**
	 * for debug/testing use only!!
	 */
	public synchronized static void flush() {
		try {
			taskManager.dispose();
		} catch (Exception ex) {

		}
		for (Game g : runningGames.values()) {
			try {
				g.kill();
			} catch (Exception ex) {

			}
		}
		for (Game g : standbyGames.values()) {
			try {
				g.kill();
			} catch (Exception ex) {

			}
		}
		try {
			PingManager.kill();
		} catch (Exception ex) {

		}
		userManager = new UserManager();
		pingManager = new PingManager();
		runningGames = new ConcurrentHashMap<>();
		standbyGames = new ConcurrentHashMap<>();
		userGameMap = new ConcurrentHashMap<>();
		userGameAudienceMap = new ConcurrentHashMap<>();
		userStandbyGameMap = new ConcurrentHashMap<>();
		service = Executors.newCachedThreadPool();
		instance = new Engine();
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
		pingManager.addGame(g);
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
			if (userManager.userExists(p.getUserid())) {
				list.add(userManager.getSessionByUserid(p.getUserid()));
			}
		}
		return list;
	}

	private static ArrayList<String> getReceiverListById(
			ArrayList<String> players) {
		ArrayList<String> list = new ArrayList<>();
		for (String p : players) {
			if (userManager.userExists(p)) {
				list.add(userManager.getSessionByUserid(p));
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

	public static synchronized void removeLostPlayers(
			ArrayList<String> invalidSessionIds) {
		for (String id : invalidSessionIds) {
			boolean standby = false;
			boolean audience = false;
			String userid = userManager.getUseridBySession(id);
			if (userid != null) {
				String gameId = userGameMap.get(userid);
				if (gameId == null) {
					gameId = userStandbyGameMap.get(userid);
					standby = gameId != null;
					if (!standby) {
						gameId = userGameAudienceMap.get(userid);
						audience = true;
					}
				}
				if (gameId != null) {
					Game g = null;
					if (standby) {
						if (standby) {
							g = standbyGames.get(gameId);
						}
					} else {
						g = runningGames.get(gameId);
					}
					if (g != null) {
						if (audience) {
							g.removeAudienceUser(userid);
						} else {
							removePlayerFromGame(userid, g);
						}
						userManager.removeUser(id);
					}
				}
			}
		}
	}

	private static void removePlayerFromGame(String userid, Game game) {
		game.removePlayer(userid);
	}

	public static void scheduleFullGameUpdate(Game game, String userid) {
		// service.execute(instance.new FullGameUpdateTask(game, userid));
		taskManager.addTask(instance.new FullGameUpdateTask(game, userid));
	}

	public static void scheduleFullGameUpdate(Game game,
			ArrayList<String> userids) {
		taskManager.addTask(instance.new FullGameUpdateTask(game, userids));
	}

	public static void scheduleSpecialActionDeactivation(String userid,
			SpecialAction specialAction, ArrayList<String> receivers) {
		service.execute(instance.new SpecialActionDeactivationTask(userid,
				specialAction, receivers));
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
		try {
			g.setLocation(createNewLocation(db));
			g.createGoodies(true);
		} finally {
			db.closeConnection();
		}
	}

	private static boolean tryRecoverUserSession(Sender sender) {
		String ses = userManager.getSessionByUserid(sender.userid);
		if (ses != null && ses != "") {
			userManager.updateUserSession(sender.userid, sender.session, ses);
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

	private HashMap<String, Object> createPlayerArray(Player p, Game game) {
		HashMap<String, Object> map = new HashMap<>();
		map.put("username", p.getName());
		map.put("userid", p.getUserid());
		map.put("ready", game.isPlayerReadyForGame(p));
		return map;
	}

	public static boolean acceptLeaveGame(LeaveGameMessage message,
			Sender sender) {
		if (sessionExists(sender)) {
			// service.execute(instance.new ExitTask(message, sender));
			// return true;
			return taskManager.addTask(instance.new LeaveGameTask(message, sender));
		}
		return false;
	}
	
	private class LeaveGameTask extends GameTask<LeaveGameMessage>{

		public LeaveGameTask(LeaveGameMessage message, Sender sender) {
			super(message, sender);
		}

		@Override
		public void run() {
			setPlayerBackInLobby(message.userid,sender.session);
		}		
	}
	
	private static synchronized void setPlayerBackInLobby(String userid, String sessionid){
		removeUserFromAllAudiences(userid);
		removeUserFromAllRunningGames(userid);
		removeUserFromAllStandbyGames(userid);
	}
}
