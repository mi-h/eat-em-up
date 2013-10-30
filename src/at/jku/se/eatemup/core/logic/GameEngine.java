package at.jku.se.eatemup.core.logic;

import java.util.HashMap;
import java.util.Map;

import at.jku.se.eatemup.core.model.Player;

public class GameEngine {
	private static Map<Long,Game> runningGames = new HashMap<>();
	private static Game standbyGame;
	
	public static long JoinGame(String name){
		if (standbyGame != null){
			addPlayerToGame(name,standbyGame);
			return 0l;
			//TODO!!
		}
		return 0l;
	}

	private static void addPlayerToGame(String name, Game standbyGame) {
		Player player = new Player(name);
		if(standbyGame.AddPlayer(player)){
			
		}
		
	}
}
