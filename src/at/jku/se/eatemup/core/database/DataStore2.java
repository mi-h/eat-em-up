package at.jku.se.eatemup.core.database;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import at.jku.se.eatemup.core.logging.LogEntry;
import at.jku.se.eatemup.core.logging.Logger;
import at.jku.se.eatemup.core.model.Account;
import at.jku.se.eatemup.core.model.AccountType;
import at.jku.se.eatemup.core.model.GoodiePoint;
import at.jku.se.eatemup.core.model.Position;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

public class DataStore2 implements IDatabaseAPI{

	private String connection;
	private String url;
	private String dbName;
	private String dbUser;
	private String dbPassword;
	private ConnectionSource connectionSource;
	private Dao<Account,String> accountDao;
	private Dao<LogEntry,String> logDao;
	private Dao<Position,Integer> positionDao;

	public DataStore2() {
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
	
	public void addAccount(Account acc) {
		try {
			Account temp;
			if (acc.getType()==AccountType.Standard){
				temp = getAccountByUsername(acc.getUsername());
			} else {
				temp = getFacebookAccount(acc.getFacebookId());
			}
			if (temp == null){
				accountDao.createIfNotExists(acc);
			}			
		} catch (SQLException e) {
			Logger.log("adding account failed. "+Logger.stringifyException(e));
		}
	}
	
	public void addGoodiePosition(Position position){
		try {
			positionDao.createIfNotExists(position);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void addUserPoints(String userid, int points){
		Account tmp = getAccountByUserid(userid);
		if (tmp != null){
			tmp.addPoints(points);
			try {
				accountDao.createOrUpdate(tmp);
			} catch (SQLException e) {
				Logger.log("account point update failed for "+userid+"."+Logger.stringifyException(e));
			}
		}
	}
	
	public void closeConnection(){
		connectionSource.closeQuietly();
	}
	
	public void createTables(){
		try {
			TableUtils.createTableIfNotExists(connectionSource, Account.class);
			Logger.log("account table created.");
		} catch (SQLException e) {
			Logger.log("account table creation failed."+Logger.stringifyException(e));
		}
		try {
			TableUtils.createTableIfNotExists(connectionSource, LogEntry.class);
			Logger.log("log table created.");
		} catch (SQLException e) {
			Logger.log("log table creation failed."+Logger.stringifyException(e));
		}
		try {
			TableUtils.createTable(connectionSource, Position.class);
			Logger.log("goodieLocation table created.");
		} catch (SQLException e) {
			Logger.log("goodieLocation table creation failed."+Logger.stringifyException(e));
		}
	}
	
	public Account getAccountByUserid(String userid){
		try {
			Account temp = accountDao.queryForId(userid);
			return temp;
		} catch (SQLException e) {
			Logger.log("account retrieval failed for "+userid+"."+Logger.stringifyException(e));
			return null;
		}
	}
	
	public Account getAccountByUsername(String username){
		try {
			QueryBuilder<Account, String> queryBuilder = accountDao.queryBuilder();
			queryBuilder.where().eq("username", username).and().eq("type", AccountType.Standard);
			Account temp = queryBuilder.queryForFirst();
			return temp;
		} catch (SQLException e) {
			Logger.log("account retrieval failed for "+username+"."+Logger.stringifyException(e));
			return null;
		}
	}
	
	public ArrayList<Account> getAccountsByUserids(ArrayList<String> userids){
		ArrayList<Account> list = new ArrayList<>();
		for (String u : userids){
			Account tmp = getAccountByUserid(u);
			if (tmp != null){
				list.add(tmp);
			}
		}
		return list;
	}
	
	public Account getFacebookAccount(String facebookId) {
		try {
			QueryBuilder<Account, String> queryBuilder = accountDao.queryBuilder();
			queryBuilder.where().eq("facebookId", facebookId).and().eq("type", AccountType.Facebook);
			Account temp = queryBuilder.queryForFirst();
			return temp;
		} catch (SQLException e) {
			Logger.log("account retrieval failed for (facebook) "+facebookId+"."+Logger.stringifyException(e));
			return null;
		}
	}
	
	public ArrayList<GoodiePoint> getGoodiePoints(){
		ArrayList<GoodiePoint> ret = new ArrayList<GoodiePoint>();
		try {
			List<Position> list = positionDao.queryForAll();
			for (Position pos : list){
				GoodiePoint tmp = new GoodiePoint();
				tmp.setPosition(pos);
				ret.add(tmp);
			}
			return ret;
		} catch (SQLException e) {
			Logger.log("retrieve goodie point positions failed."+Logger.stringifyException(e));
			return ret;
		}
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
	
	public String getUserPassword(String username){
		Account tmp = getAccountByUsername(username);
		if (tmp != null){
			return tmp.getPassword();
		}
		return null;
	}

	public void saveLogEntry(LogEntry e){
		try {
			logDao.create(e);
		} catch (SQLException e1) {
			System.out.println("saving logentry failed");
		}
	}
}
