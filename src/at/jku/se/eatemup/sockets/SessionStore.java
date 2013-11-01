package at.jku.se.eatemup.sockets;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;

import javax.websocket.Session;

public class SessionStore {

	private static ConcurrentHashMap<String,Session> sessions = new ConcurrentHashMap<>();
	
	public static synchronized boolean sessionExists(String id){
		return sessions.containsKey(id);
	}
	
	public static synchronized void saveSession(Session session){
		sessions.put(session.getId(), session);
	}
	
	public static synchronized void removeSession(String id){
		sessions.remove(id);
	}
	
	public static synchronized Session getSession(String id){
		if (sessionExists(id)) return sessions.get(id);
		return null;
	}
	
	public static synchronized ArrayList<String> getKeyList(){
		ArrayList<String> list = new ArrayList<>();
		for (String s : sessions.keySet()){
			list.add(s);
		}
		return list;
	}
}