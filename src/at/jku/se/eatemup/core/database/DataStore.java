package at.jku.se.eatemup.core.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DataStore {

	String connection;
	String url;
	String dbName;
	String dbUser;
	String dbPassword;
	Connection con;
	
	public DataStore() {
		this.url = "camellia.arvixe.com:1433";
		this.dbName = "sepr";
		this.dbUser = "sepruser";
		this.dbPassword = "seprpass";
	}
	
	public boolean connectToDB(){
		try {
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}		
		connection = 	"jdbc:sqlserver://" + this.url 
						+ ";databaseName=" + this.dbName 
						+ ";user=" + this.dbUser 
						+ ";password=" + this.dbPassword +";";
		try {
			this.con = DriverManager.getConnection(connection);			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		return true;
	}
	
	public boolean closeDbConnection(){
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
