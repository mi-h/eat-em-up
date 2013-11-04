package at.jku.se.eatemup.core.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;

import at.jku.se.eatemup.core.logging.LogEntry;
import at.jku.se.eatemup.core.logging.Logger;
import at.jku.se.eatemup.core.model.Account;
import at.jku.se.eatemup.core.model.GoodiePoint;
import at.jku.se.eatemup.core.model.Position;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

public class DataStore{

	private String connection;
	private String url;
	private String dbName;
	private String dbUser;
	private String dbPassword;
	private ConnectionSource connectionSource;
	private Dao<Account,String> accountDao;
	private Dao<LogEntry,String> logDao;
	private Dao<Position,Integer> positionDao;
	Connection con;

	public DataStore() {
		this.url = "camellia.arvixe.com:1433";
		this.dbName = "sepr";
		this.dbUser = "sepruser";
		this.dbPassword = "seprpass";
		connection = "jdbc:sqlserver://" + this.url + ";databaseName="
				+ this.dbName + ";user=" + this.dbUser + ";password="
				+ this.dbPassword + ";";
		try {
			connectionSource =
			        new JdbcConnectionSource(connection);
		} catch (SQLException e) {
			Logger.log("jdbc connection failed."+Logger.stringifyException(e));
		}
		try {
			accountDao = DaoManager.createDao(connectionSource, Account.class);
		} catch (SQLException e) {
			Logger.log("account dao creation failed."+Logger.stringifyException(e));
		}
		try {
			logDao = DaoManager.createDao(connectionSource, LogEntry.class);
		} catch (SQLException e) {
			Logger.log("log dao creation failed."+Logger.stringifyException(e));
		}
		try {
			positionDao = DaoManager.createDao(connectionSource, Position.class);
		} catch (SQLException e) {
			Logger.log("position dao creation failed."+Logger.stringifyException(e));
		}		
	}
	
	public void createTables(){
		try {
			TableUtils.createTableIfNotExists(connectionSource, Account.class);
		} catch (SQLException e) {
			Logger.log("account table creation failed."+Logger.stringifyException(e));
		}
		try {
			TableUtils.createTableIfNotExists(connectionSource, LogEntry.class);
		} catch (SQLException e) {
			Logger.log("log table creation failed."+Logger.stringifyException(e));
		}
		try {
			TableUtils.createTable(connectionSource, Position.class);
		} catch (SQLException e) {
			Logger.log("position table creation failed."+Logger.stringifyException(e));
		}
	}
	
	public void closeConnection(){
		connectionSource.closeQuietly();
	}
	
	public void saveLogEntry(LogEntry e){
		try {
			logDao.create(e);
		} catch (SQLException e1) {
			System.out.println("saving logentry failed");
		}
	}
	
	public ArrayList<LogEntry> getLogEntries(Date start, Date end){
		QueryBuilder<LogEntry, String> queryBuilder = logDao.queryBuilder();
		try {
			queryBuilder.where().le("created", end);
		} catch (SQLException e1) {
			Logger.log("adding lowerthanequal to logentry query failed."+Logger.stringifyException(e1));
		}
		try {
			queryBuilder.where().ge("created", start);
		} catch (SQLException e1) {
			Logger.log("adding greaterthanequal to logentry query failed."+Logger.stringifyException(e1));
		}
		PreparedQuery<LogEntry> preparedQuery = null;
		try {
			preparedQuery = queryBuilder.prepare();
		} catch (SQLException e) {
			Logger.log("preparing logentry query failed."+Logger.stringifyException(e));
		}
		try {
			return new ArrayList<LogEntry>(logDao.query(preparedQuery));
		} catch (SQLException e) {
			Logger.log("logentry query failed."+Logger.stringifyException(e));
		}
		return new ArrayList<LogEntry>();
	}
	
	public Account getAccountByUsername(String username){
		try {
			Account temp = accountDao.queryForId(username);
			return temp;
		} catch (SQLException e) {
			Logger.log("account retrieval failed for "+username+"."+Logger.stringifyException(e));
			return null;
		}
	}
	
	public ArrayList<Account> getAccountsByUsernames(ArrayList<String> usernames){
		ArrayList<Account> list = new ArrayList<>();
		for (String u : usernames){
			Account tmp = getAccountByUsername(u);
			if (tmp != null){
				list.add(tmp);
			}
		}
		return list;
	}
	
	public ArrayList<Account> getHighscore(int topX){
		QueryBuilder<Account, String> queryBuilder = accountDao.queryBuilder();
		queryBuilder.orderBy("points", false);
		queryBuilder.limit(new Long(topX));
		PreparedQuery<Account> preparedQuery = null;
		try {
			preparedQuery = queryBuilder.prepare();
		} catch (SQLException e) {
			Logger.log("preparing highscore query failed."+Logger.stringifyException(e));
		}
		try {
			return new ArrayList<Account>(accountDao.query(preparedQuery));
		} catch (SQLException e) {
			Logger.log("highscore query failed."+Logger.stringifyException(e));
		}
		return new ArrayList<Account>();
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

	public boolean connectToDB() {
		try {
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		try {
			this.con = DriverManager.getConnection(connection);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return true;
	}

	public boolean closeDbConnection() {
		try {
			this.con.commit();
			this.con.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}
}
