package at.jku.se.eatemup.core.database;

import java.util.ArrayList;
import java.util.Date;

import at.jku.se.eatemup.core.logging.LogEntry;
import at.jku.se.eatemup.core.model.Account;
import at.jku.se.eatemup.core.model.GoodiePoint;
import at.jku.se.eatemup.core.model.Position;

public interface IDatabaseAPI {
	public void saveLogEntry(LogEntry e);
	
	public ArrayList<LogEntry> getLogEntries(Date start, Date end);
	
	public Account getAccountByUsername(String username);
	
	public ArrayList<Account> getAccountsByUsernames(ArrayList<String> usernames);
	
	public ArrayList<Account> getHighscore(int topX);
	
	public void addUserPoints(String username, int points);
	
	public String getUserPassword(String username);
	
	public ArrayList<GoodiePoint> getGoodiePoints();

	public void addAccount(Account acc);
	
	public void addGoodiePosition(Position position);
}
