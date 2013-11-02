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
	private static ConcurrentHashMap<Long, Game> runningGames = new ConcurrentHashMap<>();
	private static Game standbyGame;
	private static UserSession userSessionMap = new UserSession();
	private static ConcurrentHashMap<String, Long> userGameMap = new ConcurrentHashMap<>();
	private static ExecutorService service = Executors.newCachedThreadPool();
	private static GameEngine instance = new GameEngine();

	private static void removePlayerFromGame(String name, Game game) {

	}

	public static void removeLostPlayers(ArrayList<String> invalidSessionIds) {
		for (String id : invalidSessionIds) {
			String username = userSessionMap.getUsernameBySession(id);
			long gameId = userGameMap.get(username);
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
			for (Player p : standbyGame.getPlayers()) {
				HashMap<String, Object> map = new HashMap<>();
				map.put("username", p.getName());
				map.put("teamRed", standbyGame.isInRedTeam(p));
				msg.players.add(map);
			}
			MessageContainer container = MessageCreator.createMsgContainer(msg,
					getReceiverList(standbyGame.getPlayers()));
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
			// TODO Auto-generated method stub
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
		if (standbyGame == null) {
			standbyGame = new Game();
		}
		Player player = new Player(username);
		return standbyGame.AddPlayer(player);
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
}