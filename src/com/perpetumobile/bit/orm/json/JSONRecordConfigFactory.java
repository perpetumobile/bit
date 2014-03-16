package com.perpetumobile.bit.orm.json;

import org.apache.velocity.VelocityContext;

import com.perpetumobile.bit.orm.record.RecordConfigFactory;


/**
 * 
 * @author  Zoran Dukic
 */
final public class JSONRecordConfigFactory extends RecordConfigFactory<JSONRecordConfig> {
	static private JSONRecordConfigFactory instance = new JSONRecordConfigFactory();
	static public JSONRecordConfigFactory getInstance() {
		return instance;
	}
	private JSONRecordConfigFactory() {
	}
	
	protected JSONRecordConfig createRecordConfig(String configName, VelocityContext vc) throws Exception {
		return new JSONRecordConfig(configName, vc);
	}
}
