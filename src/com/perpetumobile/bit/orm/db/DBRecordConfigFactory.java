package com.perpetumobile.bit.orm.db;

import org.apache.velocity.VelocityContext;

import com.perpetumobile.bit.orm.db.DBRecordConfig;
import com.perpetumobile.bit.orm.record.RecordConfigFactory;


/**
 * 
 * @author  Zoran Dukic
 */
final public class DBRecordConfigFactory extends RecordConfigFactory<DBRecordConfig> {
	static private DBRecordConfigFactory instance = new DBRecordConfigFactory();
	static public DBRecordConfigFactory getInstance() {
		return instance;
	}
	private DBRecordConfigFactory() {
	}
	
	protected DBRecordConfig createRecordConfig(String configName, VelocityContext vc) throws Exception {
		return new DBRecordConfig(configName, vc);
	}
}