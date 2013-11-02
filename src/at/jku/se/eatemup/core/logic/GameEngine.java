package at.jku.se.eatemup.core.logic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.*;

import at.jku.se.eatemup.core.MessageCreator;
import at.jku.se.eatemup.core.MessageHandler;
import at.jku.se.eatemup.core.PasswordHashManager;
import at.jku.se.eatemup.core.database.DbOperations;
import at.jku.se.eatemup.core.json.MessageContainer;
import at.jku.se.eatemup.core.json.messages.*;
import at.jku.se.eatemup.core.model.Player;

public class GameEngine {
	private static ConcurrentHashMap<String, Game> runningGames = new ConcurrentHashMap<>();
	private static ConcurrentHashMap<String, Game> standbyGames = new ConcurrentHashMap<>();
	private static UserSession userSessionMap = new UserSession();
	private static ConcurrentHashMap<String, String> userGameMap = new ConcurrentHashMap<>();
	private static ConcurrentHashMap<String, String> userStandbyGameMap = new ConcurrentHashMap<>();
	private static ExecutorService service = Executors.newCachedThreadPool();
	private static GameEngine instance = new GameEngine();

	private static void removePlayerFromGame(String name, Game game) {

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

	public static boolean acceptLogin(LoginMessage message, String sender) {
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

	private static boolean checkLoginCredentials(String username,
			String password) {
		DbOperations db = new DbOperations();
		// TODO get passwordhash for username and check
		try {
			PasswordHashManager.check(password, "fromdb");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}

	public static boolean acceptRequestForGameStart(
			RequestForGameStartMessage message, String sender) {
		if (sessionExists(sender)) {
			service.submit(instance.new RequestForGameStartTask(message, sender));
		}
		return false;
	}

	public static boolean acceptPosition(PositionMessage message, String sender) {
		if (sessionExists(sender)) {
			service.submit(instance.new PositionTask(message, sender));
		}
		return false;
	}

	public static boolean acceptBattleAnswer(BattleAnswerMessage message,
			String sender) {
		if (sessionExists(sender)) {
			service.submit(instance.new BattleAnswerTask(message, sender));
		}
		return false;
	}

	public static boolean acceptExit(ExitMessage message, String sender) {
		if (sessionExists(sender)) {
			service.submit(instance.new ExitTask(message, sender));
		}
		return false;
	}

	private static boolean sessionExists(String sender) {
		return userSessionMap.userExistsBySession(sender);
	}

	private abstract class GameTask<T> implements Runnable {
		protected T message;
		protected String sender;

		public GameTask(T message, String sender) {
			this.message = message;
			this.sender = sender;
		}
	}

	private class LoginTask extends GameTask<LoginMessage> {

		public LoginTask(LoginMessage message, String sender) {
			super(message, sender);
		}

		@Override
		public void run() {
			userSessionMap.addUser(sender, message.username);
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

		public PositionTask(PositionMessage message, String sender) {
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
				String sender) {
			super(message, sender);
		}

		@Override
		public void run() {
			Game game = getPlayerStandbyGame(message.username);
			game.setPlayerReady(message.username);
			if (game.allPlayersReady()) {
				// TODO send GameStart
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

	private class BattleAnswerTask extends GameTask<BattleAnswerMessage> {

		public BattleAnswerTask(BattleAnswerMessage message, String sender) {
			super(message, sender);
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
		}
	}

	private class ExitTask extends GameTask<ExitMessage> {

		public ExitTask(ExitMessage message, String sender) {
			super(message, sender);
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
		}
	}

	private static class UserSession {
		private ConcurrentHashMap<String, String> usernameSessionMap = new ConcurrentHashMap<>();
		private ConcurrentHashMap<String, String> sessionUsernameMap = new ConcurrentHashMap<>();

		public String getSessionByUsername(String username) {
			return usernameSessionMap.get(username);
		}

		public String getUsernameBySession(String sessionid) {
			return sessionUsernameMap.get(sessionid);
		}

		public void addUser(String sessionid, String username) {
			usernameSessionMap.put(username, sessionid);
			sessionUsernameMap.put(sessionid, username);
		}

		public void removeUser(String sessionid) {
			String u = getUsernameBySession(sessionid);
			usernameSessionMap.remove(u);
			sessionUsernameMap.remove(sessionid);
		}

		public boolean userExists(String username) {
			return usernameSessionMap.containsKey(username);
		}

		public boolean userExistsBySession(String session) {
			return sessionUsernameMap.containsKey(session);
		}
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
}