package com.perpetumobile.bit.orm.db;


import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;

import com.perpetumobile.bit.config.Config;
import com.perpetumobile.bit.orm.record.RecordConnection;
import com.perpetumobile.bit.util.Logger;

/**
 * 
 * @author  Zoran Dukic
 */
public class DBConnection extends RecordConnection<Connection> {
	static private Logger logger = new Logger(DBConnection.class);
	
	//config file keys  
	final static public String DB_DRIVER_KEY = "Database.Driver";
	final static public String DB_RETRIES_KEY = "Database.Retries";
	final static public String DB_LOGIN_TIMEOUT_KEY = "Database.LoginTimeout";
	
	final static public String DB_URL_KEY = "Database.Url";
	final static public String DB_DATABASE_KEY = "Database.Database";
	
	//default config values
	final static public int DB_RETRIES_DEFAULT = 0;
	final static public int DB_LOGIN_TIMEOUT_DEFAULT = 30;
	
	private String driver = null;
	private int maxRetries = 0;
	private int loginTimeout = 30;
	
	protected String url = null;
	protected String database = null;
	
	/**
	 * Creates a new instance of BaseDBConnection
	 */
	public DBConnection(String configName){
		super(configName);
		driver = Config.getInstance().getClassProperty(configName, DB_DRIVER_KEY, null);
		maxRetries = Config.getInstance().getIntClassProperty(configName, DB_RETRIES_KEY, DB_RETRIES_DEFAULT);
		loginTimeout = Config.getInstance().getIntClassProperty( configName, DB_LOGIN_TIMEOUT_KEY, DB_LOGIN_TIMEOUT_DEFAULT);
		url = Config.getInstance().getClassProperty(configName, DB_URL_KEY, "");
		database = Config.getInstance().getClassProperty(configName, DB_DATABASE_KEY, "");
	}
	
	public void connect() {
		if(connection == null) {         
			connection = connect(url+database);
		}
	}
	
	/**
	 * Initiates a connection to the database.
	 * @return	Connection object
	 */
	protected Connection connect(String connectURL) {
		Connection result = null;
		
		// load driver class
		try {
			Class.forName(driver);
		} catch(ClassNotFoundException e) {
			logger.error("JDBC Driver is missing: " + driver, e);
			return null;
		}
		
		// set the login timeout
		DriverManager.setLoginTimeout(loginTimeout);
		
		int retries = 0;
		while (result == null && retries < maxRetries) {
			try {
				// get JDBC connection
				result = DriverManager.getConnection(connectURL, user, password);
			} catch(SQLException e) {
				result = null;
				logger.error("Cannot open connection: " + connectURL, e);
			}
			retries++;
		}
		
		return result;
	}
	
	/**
	 * Frees up connection resources.
	 * You should call this method before your DB object goes out of scope.
	 * This method explicitly frees up resources rather than waiting for
	 * garbage collection to do so.
	 *
	 * @param con Connection object
	 */
	public void disconnect() {
		try {
			if (connection != null) {
				connection.close();
			}
		} catch(SQLException e) {
			logger.error("Cannot close database connection", e);
		}
		connection = null;
	}
	
	public boolean validate() throws Exception {
		Statement stmt = null;
		try {
			stmt = connection.createStatement();
			stmt.executeQuery("SELECT 1");
		} finally {
			if (stmt!=null) stmt.close();
		}
		return true;
	}
	
	public String getUrl() {
		return url;
	}
	
	public String getDatabaseName(){
		return database;
	}
}
