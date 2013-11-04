package at.jku.se.eatemup.core.database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;

import at.jku.se.eatemup.core.logging.LogEntry;
import at.jku.se.eatemup.core.model.Account;
import at.jku.se.eatemup.core.model.GoodiePoint;

public class DbOperations {

	DataStore ds;
	Statement stmt;
	String sqlQuery;

	public DbOperations() {
		this.ds = new DataStore();
	}
	
	public void saveLogEntry(LogEntry e){
		//TODO
	}
	
	public ArrayList<LogEntry> getLogEntries(Date start, Date end){
		//TODO
		return null;
	}
	
	public Account getAccountByUsername(String username){
		//TODO
		return null;
	}
	
	public ArrayList<Account> getAccountsByUsernames(ArrayList<String> usernames){
		//TODO
		return null;
	}
	
	public ArrayList<Account> getHighscore(int topX){
		//TODO
		return null;
	}
	
	public void addUserPoints(String username, int points){
		//TODO
	}
	
	public String getUserPassword(String username){
		//TODO
		return null;
	}
	
	public ArrayList<GoodiePoint> getGoodiePoints(){
		//TODO
		return null;
	}

	public int selectUserPoints(String _user) {
		int userPoints = 0;
		if (this.ds.connectToDB()) {
			this.sqlQuery = "SELECT points FROM [sepr].[sepruser].[user] WHERE nickname LIKE'"
					+ _user + "'";
			try {
				this.stmt = this.ds.con.createStatement();
				ResultSet rs = this.stmt.executeQuery(this.sqlQuery);
				while (rs.next()) {
					userPoints = rs.getInt("points");
				}
				this.stmt.close();
				this.ds.closeDbConnection();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return userPoints;
	}

	public boolean updateUserPoints(String _user, int points) {
		if (this.ds.connectToDB()) {
			this.sqlQuery = "UPDATE [sepruser].[user] SET [points] = " + points
					+ "where [user].[nickname] LIKE'" + _user + "'";
			try {
				this.stmt = this.ds.con.createStatement();
				this.stmt.executeUpdate(this.sqlQuery);
				this.stmt.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return true;
	}

	public boolean insertUser(String _user) {

		if (this.ds.connectToDB()) {
			this.sqlQuery = "INSERT INTO [sepruser].[user]([nickname],[points]) values('"
					+ _user + "',0)";
			try {
				this.stmt = this.ds.con.createStatement();
				this.stmt.executeUpdate(this.sqlQuery);
				this.stmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return true;
	}

	public int selectGoodiePoints(String _goodieTyp) {
		int goodiePoints = 0;
		if (this.ds.connectToDB()) {
			this.sqlQuery = "SELECT points FROM [sepr].[sepruser].[GoodiePoints] WHERE [type] LIKE'"
					+ _goodieTyp + "'";
			try {
				this.stmt = this.ds.con.createStatement();
				ResultSet rs = this.stmt.executeQuery(this.sqlQuery);
				while (rs.next()) {
					goodiePoints = rs.getInt("points");
				}
				this.stmt.close();
				this.ds.closeDbConnection();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return goodiePoints;
	}

	/*
	 * public boolean deleteUser(String _user){
	 * 
	 * return true; }
	 */
}
