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
	
	public Account getAccountByUserid(String userid);
	
	public Account getAccountByUsername(String username);
	
	public ArrayList<Account> getAccountsByUserids(ArrayList<String> userids);
	
	public ArrayList<Account> getHighscore(int topX);
	
	public void addUserPoints(String userid, int points);
	
	public String getUserPassword(String username);
	
	public ArrayList<GoodiePoint> getGoodiePoints();

	public void addAccount(Account acc);
	
	public void addGoodiePosition(Position position);
	
	public Account getFacebookAccount(String facebookId);
}
