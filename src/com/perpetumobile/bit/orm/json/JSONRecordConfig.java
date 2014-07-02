package com.perpetumobile.bit.orm.json;


import org.apache.velocity.VelocityContext;

import com.perpetumobile.bit.config.Config;
import com.perpetumobile.bit.orm.record.RecordConfig;
import com.perpetumobile.bit.orm.record.field.FieldConfig;

/**
 * 
 * @author Zoran Dukic
 */
public class JSONRecordConfig extends RecordConfig {
	private static final long serialVersionUID = 1L;
	
	public static final String PARSE_ALL_ENABLE_CONFIG_KEY = "JSONRecord.ParseAll.Enable";
	
	protected boolean parseAll = true;
	
	public JSONRecordConfig(String configName)
	throws ClassNotFoundException {
		init(configName, null);
	}
	
	public JSONRecordConfig(String configName, VelocityContext vc)
	throws ClassNotFoundException {
		init(configName, vc);
	}
	
	protected void init(String configName, VelocityContext vc)
	throws ClassNotFoundException {
		super.init(configName, JSONRecord.class, vc);
		parseAll = Config.getInstance().getBooleanClassProperty(configName, PARSE_ALL_ENABLE_CONFIG_KEY, true);
	}
	
	protected RecordConfig getRecordConfig(String configName, VelocityContext vc) {
		return JSONRecordConfigFactory.getInstance().getRecordConfig(configName, vc);
	}
	
	public boolean isParseAll() {
		return parseAll;
	}
	
	public String getSelectList() {
		StringBuffer result = new StringBuffer();
		
		boolean first = true;
		for(int i=0; i<fields.size(); i++) {
			if(!first) {
				result.append(", ");
			}
			String fieldName = ((FieldConfig)fields.get(i)).getFieldName();
			result.append(fieldName);
			first = false;
		}
		
		return result.toString();
	}
}
