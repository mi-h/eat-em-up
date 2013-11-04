package at.jku.se.eatemup.core.logging;

import java.util.Date;

public class Logger {

	public static synchronized void log(String message) {
		Date d = new Date();
		// TODO: save to db!
		consoleOut(message, d);
	}

	private static void consoleOut(String message, Date d) {
		System.out.println(d.toString() + ": " + message);
	}
	
	public static String stringifyException(Exception exception){
		StringBuilder sb = new StringBuilder(exception.getMessage());
		sb.append("</br>");
		for (StackTraceElement ste : exception.getStackTrace()){
			sb.append(ste.toString());
			sb.append("</br>");
		}
		return sb.toString();
	}
}
