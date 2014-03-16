package com.perpetumobile.bit.orm.cassandra;

import com.perpetumobile.bit.orm.record.RecordConnectionFactory;

/**
 * 
 * @author  Zoran Dukic
 */
public class CassandraConnectionFactory extends RecordConnectionFactory<CassandraConnection> {
		
	public CassandraConnectionFactory(String configName) {
		super(configName);
	}
	
	public Object makeObject() throws java.lang.Exception {
		CassandraConnection result = new CassandraConnection(configName);
		result.connect();
		return result;
	}
}
