package com.perpetumobile.bit.orm.cassandra;

import org.apache.velocity.VelocityContext;

import com.perpetumobile.bit.orm.cassandra.CassandraRecordConfig;
import com.perpetumobile.bit.orm.record.RecordConfigFactory;


/**
 * 
 * @author  Zoran Dukic
 */
final public class CassandraRecordConfigFactory extends RecordConfigFactory<CassandraRecordConfig> {
	static private CassandraRecordConfigFactory instance = new CassandraRecordConfigFactory();
	static public CassandraRecordConfigFactory getInstance() {
		return instance;
	}
	private CassandraRecordConfigFactory() {
	}
	
	protected CassandraRecordConfig createRecordConfig(String configName, VelocityContext vc) throws Exception {
		return new CassandraRecordConfig(configName, vc);
	}
}
