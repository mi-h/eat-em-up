package at.jku.se.eatemup.core.logic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.*;

import at.jku.se.eatemup.core.MessageContainer;
import at.jku.se.eatemup.core.MessageCreator;
import at.jku.se.eatemup.core.MessageHandler;
import at.jku.se.eatemup.core.PasswordHashManager;
import at.jku.se.eatemup.core.Sender;
import at.jku.se.eatemup.core.database.DataStore;
import at.jku.se.eatemup.core.json.messages.*;
import at.jku.se.eatemup.core.logging.Logger;
import at.jku.se.eatemup.core.model.Player;
import at.jku.se.eatemup.sockets.SessionStore;

public class Engine {
	private class BattleAnswerTask extends GameTask<BattleAnswerMessage> {

		public BattleAnswerTask(BattleAnswerMessage message, Sender sender) {
			super(message, sender);
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
		}
	}

	private class ExitTask extends GameTask<ExitMessage> {

		public ExitTask(ExitMessage message, Sender sender) {
			super(message, sender);
		}

		@Override
		public void run() {
			sendLogoutMessage(sender.session,"accepting exit action, user logout", sender.username);
			removeUserFromAllRunningGames(sender.username);
			removeUserFromAllStandbyGames(sender.username);
			removeUserFromAllAudiences(sender.username);
			userSessionMap.removeUser(sender.session);
			SessionStore.removeSession(sender.session);
		}

		private void removeUserFromAllAudiences(String username) {
			if(userGameAudienceMap.containsKey(username)){
				try{
				Game g = runningGames.get(userGameAudienceMap.get(username));
				g.removeAudienceUser(username);
				} catch (Exception ex){
					Logger.log("failed to remove player from standby game."+Logger.stringifyException(ex));
				}
			}
		}

		private void removeUserFromAllStandbyGames(String username) {
			if(userStandbyGameMap.containsKey(username)){
				try{
				Game g = standbyGames.get(userStandbyGameMap.get(username));
				g.removePlayer(username);
				} catch (Exception ex){
					Logger.log("failed to remove player from standby game."+Logger.stringifyException(ex));
				}
			}
		}

		private void removeUserFromAllRunningGames(String username) {
			if(userGameMap.containsKey(username)){
				try{
				Game g = runningGames.get(userGameMap.get(username));
				g.removePlayer(username);
				} catch (Exception ex){
					Logger.log("failed to remove player from running game."+Logger.stringifyException(ex));
				}
			}
		}
	}

	private class FollowGameRequestTask extends
			GameTask<FollowGameRequestMessage> {

		public FollowGameRequestTask(FollowGameRequestMessage message,
				Sender sender) {
			super(message, sender);
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

		private boolean isAGameRunning() {
			return runningGames.size() >= 1;
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
				// TODO send GameStart etc.
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
	}

	private static ConcurrentHashMap<String, Game> runningGames = new ConcurrentHashMap<>();

	private static ConcurrentHashMap<String, Game> standbyGames = new ConcurrentHashMap<>();

	private static UserSession userSessionMap = new UserSession();

	private static ConcurrentHashMap<String, String> userGameMap = new ConcurrentHashMap<>();
	
	private static ConcurrentHashMap<String, String> userGameAudienceMap = new ConcurrentHashMap<>();

	private static ConcurrentHashMap<String, String> userStandbyGameMap = new ConcurrentHashMap<>();

	private static ExecutorService service = Executors.newCachedThreadPool();

	private static Engine instance = new Engine();
	
	private static DbManager db = new DbManager();

	public static boolean acceptBattleAnswer(BattleAnswerMessage message,
			Sender sender) {
		if (sessionExists(sender)) {
			service.submit(instance.new BattleAnswerTask(message, sender));
			return true;
		}
		return false;
	}

	public void sendLogoutMessage(String session, String reason, String username) {
		try{
		LogoutMessage message = new LogoutMessage();
		message.reason = reason;
		message.username = username;
		MessageHandler.PushMessage(MessageCreator.createMsgContainer(message, username));
		} catch (Exception ex){
			Logger.log("failed sending logout message."+Logger.stringifyException(ex));
		}
	}

	public static boolean acceptExit(ExitMessage message, Sender sender) {
		if (sessionExists(sender)) {
			service.submit(instance.new ExitTask(message, sender));
			return true;
		}
		return false;
	}

	public static boolean acceptFollowGameRequest(
			FollowGameRequestMessage message, Sender sender) {
		if (sessionExists(sender)) {
			service.submit(instance.new FollowGameRequestTask(message, sender));
			return true;
		}
		return false;
	}

	public static boolean acceptLogin(LoginMessage message, Sender sender) {
		if (sessionExists(sender)) {
			boolean check = checkLoginCredentials(message.username,
					message.password);
			if (check) {
				service.submit(instance.new LoginTask(message, sender));
			}
			return check;
		}
		return false;
	}

	public static boolean acceptPlay(PlayMessage message, Sender sender) {
		if (sessionExists(sender)) {
			service.submit(instance.new PlayTask(message, sender));
			return true;
		}
		return false;
	}

	public static boolean acceptPosition(PositionMessage message, Sender sender) {
		if (sessionExists(sender)) {
			service.submit(instance.new PositionTask(message, sender));
			return true;
		}
		return false;
	}

	public static boolean acceptRequestForGameStart(
			RequestForGameStartMessage message, Sender sender) {
		if (sessionExists(sender)) {
			service.submit(instance.new RequestForGameStartTask(message, sender));
			return true;
		}
		return false;
	}

	private static boolean checkLoginCredentials(String username,
			String password) {
		DataStore ds = db.getDataStore();
		/*
		passwordhash for username and check
		try {
			PasswordHashManager.check(password, "fromdb");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
		return true;
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

	private synchronized boolean addPlayerToStandbyGame(String username) {
		Player player = new Player(username);
		Game g = getEmptyStandbyGame();
		userStandbyGameMap.put(username, g.getId());
		return g.AddPlayer(player);
	}

	private synchronized Game getEmptyStandbyGame() {
		for (Game g : standbyGames.values()) {
			if (!g.isFull()) {
				return g;
			}
		}
		Game g = new Game();
		standbyGames.put(g.getId(), g);
		return g;
	}

	private synchronized Game getPlayerStandbyGame(String username) {
		return standbyGames.get(userStandbyGameMap.get(username));
	}

	private ArrayList<String> getReceiverList(ArrayList<Player> players) {
		ArrayList<String> list = new ArrayList<>();
		for (Player p : players) {
			if (userSessionMap.userExists(p.getName())) {
				list.add(userSessionMap.getSessionByUsername(p.getName()));
			}
		}
		return list;
	}

	private ArrayList<String> getReceiverListById(ArrayList<String> players) {
		ArrayList<String> list = new ArrayList<>();
		for (String p : players) {
			if (userSessionMap.userExists(p)) {
				list.add(userSessionMap.getSessionByUsername(p));
			}
		}
		return list;
	}
	
	private static class DbManager{
		private static boolean firstCall = true;
		
		public static DataStore getDataStore(){
			DataStore ds = new DataStore();
			if (firstCall){
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
}