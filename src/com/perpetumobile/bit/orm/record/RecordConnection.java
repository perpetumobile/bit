package com.perpetumobile.bit.orm.record;

import com.perpetumobile.bit.config.Config;

/**
 * 
 * @author  Zoran Dukic
 */
abstract public class RecordConnection<T> {
	
	final static public String DB_USER_KEY = "Database.User";
	final static public String DB_PASSWORD_KEY = "Database.Password";
	
	protected String configName = null;
	protected String user = null;
	protected String password = null;
	
	protected T connection = null;
	
	public RecordConnection(String configName) {
		this.configName = configName;
		user = Config.getInstance().getClassProperty(configName, DB_USER_KEY, "");
		password = Config.getInstance().getClassProperty(configName, DB_PASSWORD_KEY, "");
	}
	
	/**
	 * Initiates a connection to the database.
	 */
	abstract public void connect();
	
	/**
	 * Frees up connection resources.
	 * You should call this method before your object goes out of scope.
	 * This method explicitly frees up resources rather than waiting for
	 * garbage collection to do so.
	 */
	abstract public void disconnect();
	
	abstract public boolean validate() throws Exception;
	
	public String getConfigName() {
		return configName;
	}
	
	public T getConnection(){
		connect();
		return connection;
	}
}
