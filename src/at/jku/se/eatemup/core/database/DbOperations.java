package at.jku.se.eatemup.core.database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;

import at.jku.se.eatemup.core.logging.LogEntry;
import at.jku.se.eatemup.core.model.Account;
import at.jku.se.eatemup.core.model.GoodiePoint;
import at.jku.se.eatemup.core.model.Position;

public class DbOperations {

	DataStore ds;
	Statement stmt;
	String sqlQuery;

	public DbOperations() {
		this.ds = new DataStore();
	}
	
	public void saveLogEntry(LogEntry _e){
		if (this.ds.connectToDB()) {
			this.sqlQuery = "INSERT INTO [sepruser].[LogEntry]([ID],[Date],[text]) values('"
					+ _e.id + "," + _e.created + "," + _e.text + "'";
			try {
				this.stmt = this.ds.con.createStatement();
				this.stmt.executeUpdate(this.sqlQuery);
				this.stmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	public ArrayList<LogEntry> getLogEntries(Date start, Date end){
		//TODO
		return null;
	}
	
	public Account getAccountByUsername(String username){
		Account a = new Account();
		a.setUsername(username);
		int userPoints = 0;
		String passwort = null;
		if (this.ds.connectToDB()) {
			this.sqlQuery = "SELECT points, password FROM [sepr].[sepruser].[user] WHERE nickname LIKE'"
					+ username + "'";
			try {
				this.stmt = this.ds.con.createStatement();
				ResultSet rs = this.stmt.executeQuery(this.sqlQuery);
				while (rs.next()) {
					userPoints = rs.getInt("points");
					passwort =  rs.getString(passwort);
				}
				this.stmt.close();
				this.ds.closeDbConnection();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		a.setPoints(userPoints);
		a.setPassword(passwort);
		return a;
	}
	
	public ArrayList<Account> getAccountsByUsernames(ArrayList<String> usernames){
		ArrayList<Account> alAccount = new ArrayList<Account>();
		for (String p : usernames) {
			alAccount.add(getAccountByUsername(p));			
		}
		return alAccount;
	}
	
	public ArrayList<Account> getHighscore(int topX){
		ArrayList<Account> alAccount = new ArrayList<Account>();
		if (this.ds.connectToDB()) {
			this.sqlQuery = "SELECT TOP " + topX + " [nickname],[points],[password] " +
					"FROM [sepr].[sepruser].[user]'";
			try {
				this.stmt = this.ds.con.createStatement();
				ResultSet rs = this.stmt.executeQuery(this.sqlQuery);
				while (rs.next()) {
					Account a = new Account();
					a.setUsername(rs.getString("nickname"));
					a.setPoints(rs.getInt("points"));
					a.setPassword(rs.getString("passwort"));
					alAccount.add(a);
				}
				this.stmt.close();
				this.ds.closeDbConnection();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return alAccount;
	}
	
	public void addUserPoints(String username, int _points){
		int points = 0;
		Account a = getAccountByUsername(username);
		points = a.getPoints() + _points;
		updateUserPoints(username,points);
	}
	
	public String getUserPassword(String username){
		String password = null;
		if (this.ds.connectToDB()) {
			this.sqlQuery = "SELECT password FROM [sepr].[sepruser].[user] WHERE nickname LIKE'"
					+ username + "'";
			try {
				this.stmt = this.ds.con.createStatement();
				ResultSet rs = this.stmt.executeQuery(this.sqlQuery);
				while (rs.next()) {
					password =  rs.getString(password);
				}
				this.stmt.close();
				this.ds.closeDbConnection();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return password;
	}
	
	public ArrayList<GoodiePoint> getGoodiePoints(){
		ArrayList<GoodiePoint> alGoodiePoint = new ArrayList<GoodiePoint>();
		if (this.ds.connectToDB()) {
			this.sqlQuery = "SELECT longitude,latitude FROM [sepr].[sepruser].[GoodiePoints]";
			try {
				this.stmt = this.ds.con.createStatement();
				ResultSet rs = this.stmt.executeQuery(this.sqlQuery);
				while (rs.next()) {
					double longitude = rs.getDouble("longitude");
					double latitude =  rs.getDouble("latitude");
					Position p = new Position();
					p.setLatitude(latitude);
					p.setLongitude(longitude);
					GoodiePoint gp = new GoodiePoint();
					gp.setGoodie(null);
					gp.setPosition(p);
					alGoodiePoint.add(gp);
				}
				this.stmt.close();
				this.ds.closeDbConnection();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return alGoodiePoint;
	}
	/*
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
	*/
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
	/*
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
