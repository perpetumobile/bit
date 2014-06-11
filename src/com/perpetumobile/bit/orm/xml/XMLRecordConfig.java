package com.perpetumobile.bit.orm.xml;


import org.apache.velocity.VelocityContext;

import com.perpetumobile.bit.config.Config;
import com.perpetumobile.bit.orm.record.RecordConfig;
import com.perpetumobile.bit.orm.record.field.FieldConfig;

/**
 * 
 * @author Zoran Dukic
 */
public class XMLRecordConfig extends RecordConfig {
	
	public static final String PARSE_ALL_ENABLE_CONFIG_KEY = "XMLRecord.ParseAll.Enable";
	
	protected String localName = null;
	protected boolean parseAll = true;
	
	public XMLRecordConfig(String configName)
	throws ClassNotFoundException {
		init(configName, null);
	}
	
	public XMLRecordConfig(String configName, VelocityContext vc)
	throws ClassNotFoundException {
		init(configName, vc);
	}
	
	protected void init(String configName, VelocityContext vc)
	throws ClassNotFoundException {
		super.init(configName, XMLRecord.class, vc);
		int index = configName.lastIndexOf(getConfigNameDelimiter());
		if(index != -1) {
			localName = configName.substring(index+1);
		} else {
			localName = configName;
		}
		parseAll = Config.getInstance().getBooleanClassProperty(configName, PARSE_ALL_ENABLE_CONFIG_KEY, true);
	}
	
	protected RecordConfig getRecordConfig(String configName, VelocityContext vc) {
		return XMLRecordConfigFactory.getInstance().getRecordConfig(configName, vc);
	}

	public String getLocalName() {
		return localName;
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
