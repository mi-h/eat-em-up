package at.jku.se.eatemup.core.logging;

import java.util.Date;
import java.util.UUID;

import at.jku.se.eatemup.core.database.DataStore2;

public class Logger {

	private static final boolean debug = true;
	private static DataStore2 ds;

	public static void closeConnection() {
		if (ds != null) {
			ds.closeConnection();
		}
	}

	private static void consoleOut(String message, Date d) {
		System.out.println(d.toString() + ": " + message);
	}

	public static synchronized void log(String message) {
		if (!debug) {
			return;
		}
		Date d = new Date();
		LogEntry le = new LogEntry();
		le.created = d;
		le.text = message;
		le.id = UUID.randomUUID().toString();
		consoleOut(message, d);
		saveLogEntry(le);
	}

	private static void saveLogEntry(LogEntry logEntry) {
		if (ds == null) {
			ds = new DataStore2();
			ds.createTables();
		}
		ds.saveLogEntry(logEntry);
	}

	public static String stringifyException(Exception exception) {
		if (!debug) {
			return "";
		}
		if (exception == null) {
			return "";
		}
		StringBuilder sb = new StringBuilder(exception.getMessage());
		sb.append("</br>");
		for (StackTraceElement ste : exception.getStackTrace()) {
			sb.append(ste.toString());
			sb.append("</br>");
		}
		return sb.toString();
	}
}
