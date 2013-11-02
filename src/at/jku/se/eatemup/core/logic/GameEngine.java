package at.jku.se.eatemup.core.logic;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import at.jku.se.eatemup.core.json.messages.BattleAnswerMessage;
import at.jku.se.eatemup.core.json.messages.ExitMessage;
import at.jku.se.eatemup.core.json.messages.LoginMessage;
import at.jku.se.eatemup.core.json.messages.PositionMessage;
import at.jku.se.eatemup.core.json.messages.RequestForGameStartMessage;

public class GameEngine {
	private static ConcurrentHashMap<Long, Game> runningGames = new ConcurrentHashMap<>();
	private static Game standbyGame;
	private static UserSession userSessionMap = new UserSession();
	private static ConcurrentHashMap<String, Long> userGameMap = new ConcurrentHashMap<>();

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

	public synchronized static boolean acceptLogin(LoginMessage message, String sender) {
		// TODO Auto-generated method stub
		return false;
	}

	public static boolean acceptRequestForGameStart(
			RequestForGameStartMessage message, String sender) {
		// TODO Auto-generated method stub
		return false;
	}

	public static boolean acceptPosition(PositionMessage message, String sender) {
		// TODO Auto-generated method stub
		return false;
	}

	public static boolean acceptBattleAnswer(BattleAnswerMessage message,
			String sender) {
		// TODO Auto-generated method stub
		return false;
	}

	public static boolean acceptExit(ExitMessage message, String sender) {
		// TODO Auto-generated method stub
		return false;
	}
}
