package com.perpetumobile.bit.orm.db;

import com.perpetumobile.bit.orm.record.RecordConnectionFactory;
import com.perpetumobile.bit.orm.record.RecordConnectionManager;

/**
 * Database pool manager.
 *
 * @author  Zoran Dukic
 */
final public class DBConnectionManager extends RecordConnectionManager<DBConnection> {
	static private DBConnectionManager instance = new DBConnectionManager();
	static public DBConnectionManager getInstance() {
		return instance;
	}
	
	private DBConnectionManager() {
	}
	
	protected RecordConnectionFactory<DBConnection> createConnectionFactory(String configName){
		return new DBConnectionFactory(configName);
	}	
}