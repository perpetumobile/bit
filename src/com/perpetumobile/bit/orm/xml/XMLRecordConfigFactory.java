package com.perpetumobile.bit.orm.xml;

import org.apache.velocity.VelocityContext;

import com.perpetumobile.bit.orm.record.RecordConfigFactory;


/**
 * 
 * @author  Zoran Dukic
 */
final public class XMLRecordConfigFactory extends RecordConfigFactory<XMLRecordConfig> {
	static private XMLRecordConfigFactory instance = new XMLRecordConfigFactory();
	static public XMLRecordConfigFactory getInstance() {
		return instance;
	}
	private XMLRecordConfigFactory() {
	}
	
	protected XMLRecordConfig createRecordConfig(String configName, VelocityContext vc) throws Exception {
		return new XMLRecordConfig(configName, vc);
	}
}
