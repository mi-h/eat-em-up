package at.jku.se.eatemup.sockets;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import javax.websocket.Session;

public class SessionStore {

	private static ConcurrentHashMap<String, Session> sessions = new ConcurrentHashMap<>();
	private static Timer cleanupTimer = new Timer();
	private static final int cleanupInterval = 60000;

	public SessionStore() {
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				ArrayList<String> remList = new ArrayList<>();
				for (Session ses : sessions.values()) {
					if (!ses.isOpen()) {
						remList.add(ses.getId());
					}
				}
				for (String s : remList) {
					sessions.remove(s);
				}
			}
		};
		cleanupTimer.schedule(task, cleanupInterval, cleanupInterval);
	}

	public static synchronized boolean sessionExists(String id) {
		return sessions.containsKey(id);
	}

	public static synchronized void saveSession(Session session) {
		sessions.put(session.getId(), session);
	}

	public static synchronized void removeSession(String id) {
		sessions.remove(id);
	}

	public static synchronized Session getSession(String id) {
		if (sessionExists(id))
			return sessions.get(id);
		return null;
	}

	public static synchronized ArrayList<String> getKeyList() {
		ArrayList<String> list = new ArrayList<>();
		for (String s : sessions.keySet()) {
			list.add(s);
		}
		return list;
	}
}