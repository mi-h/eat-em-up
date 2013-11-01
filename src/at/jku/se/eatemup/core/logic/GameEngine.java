package at.jku.se.eatemup.core.logic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import at.jku.se.eatemup.core.model.Player;

public class GameEngine {
	private static ConcurrentHashMap<Long,Game> runningGames = new ConcurrentHashMap<>();
	private static Game standbyGame;
	private static UserSession userSessionMap = new UserSession();
	private static ConcurrentHashMap<String,Long> userGameMap = new ConcurrentHashMap<>();
	
	
	public static void JoinGame(String name, String sessionId){
		if (standbyGame != null){
			addPlayerToGame(name,standbyGame);
			//TODO!!
		}
	}

	private static void addPlayerToGame(String name, Game standbyGame) {
		Player player = new Player(name);
		if(standbyGame.AddPlayer(player)){
			
		}
		
	}
	
	private static void removePlayerFromGame(String name, Game game){
		
	}
	
	public static void removeLostPlayers(ArrayList<String> invalidSessionIds){
		for (String id : invalidSessionIds){
			String username = userSessionMap.getUsernameBySession(id);
			long gameId = userGameMap.get(username);
			Game g = runningGames.get(gameId);
			removePlayerFromGame(username,g);
			userSessionMap.removeUser(id);
		}
	}
	
	private static class UserSession{
		private ConcurrentHashMap<String,String> usernameSessionMap = new ConcurrentHashMap<>();
		private ConcurrentHashMap<String,String> sessionUsernameMap = new ConcurrentHashMap<>();
		
		public String getSessionByUsername(String username){
			return usernameSessionMap.get(username);
		}
		
		public String getUsernameBySession(String sessionid){
			return sessionUsernameMap.get(sessionid);
		}
		
		public void addUser(String sessionid, String username){
			usernameSessionMap.put(username, sessionid);
			sessionUsernameMap.put(sessionid, username);
		}
		
		public void removeUser(String sessionid){
			String u = getUsernameBySession(sessionid);
			usernameSessionMap.remove(u);
			sessionUsernameMap.remove(sessionid);
		}
		
		public boolean userExists(String username){
			return usernameSessionMap.containsKey(username);
		}
		
		public boolean userExistsBySession(String session){
			return sessionUsernameMap.containsKey(session);
		}
	}
}
