package com.perpetumobile.bit.orm.cassandra;

import com.perpetumobile.bit.orm.record.RecordConnectionFactory;
import com.perpetumobile.bit.orm.record.RecordConnectionManager;

/**
 * Database pool manager.
 *
 * @author  Zoran Dukic
 */
final public class CassandraConnectionManager extends RecordConnectionManager<CassandraConnection> {
	static private CassandraConnectionManager instance = new CassandraConnectionManager();
	static public CassandraConnectionManager getInstance() {
		return instance;
	}
	
	private CassandraConnectionManager() {
	}
	
	protected RecordConnectionFactory<CassandraConnection> createConnectionFactory(String configName){
		return new CassandraConnectionFactory(configName);
	}	
}
