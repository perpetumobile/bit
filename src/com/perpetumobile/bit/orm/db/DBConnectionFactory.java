package com.perpetumobile.bit.orm.db;

import com.perpetumobile.bit.orm.record.RecordConnectionFactory;

/**
 * 
 * @author  Zoran Dukic
 */
public class DBConnectionFactory  extends RecordConnectionFactory<DBConnection> {
	
	public DBConnectionFactory(String configName) {
		super(configName);
	}
	
	public Object makeObject() throws java.lang.Exception {
		DBConnection result = new DBConnection(configName);
		result.connect();
		return result;
	}
}
