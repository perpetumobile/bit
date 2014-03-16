package com.perpetumobile.bit.orm.xml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

import org.xml.sax.Attributes;

import com.perpetumobile.bit.orm.record.Record;
import com.perpetumobile.bit.orm.record.RecordConnection;
import com.perpetumobile.bit.orm.record.RecordConnectionManager;
import com.perpetumobile.bit.orm.record.StatementLogger;
import com.perpetumobile.bit.orm.record.field.Field;
import com.perpetumobile.bit.orm.record.field.FieldConfig;
import com.perpetumobile.bit.util.Util;


/**
 * 
 * @author Zoran Dukic
 */
public class XMLRecord extends Record {
	
	HashMap<String, ArrayList<XMLRecord>> map = new HashMap<String, ArrayList<XMLRecord>>();

	public XMLRecord() {
	}
	
	public XMLRecord(String configName) {
		init(configName);
	}
	
	public void init(String configName) {
		init(XMLRecordConfigFactory.getInstance().getRecordConfig(configName));
	}
	
	public String getLocalName() {
		return (config != null ? ((XMLRecordConfig)config).getLocalName() : null);
	}
	
	public boolean isParseAll() {
		return (config != null ? ((XMLRecordConfig)config).isParseAll() : true);
	}
	
	public void readRecord(Attributes attr) {
		if(isConfigFields()) {
			for(Field f : getFields()) {
				f.setFieldValue(attr.getValue(f.getFieldName()));
			}
		} else if(isParseAll()) {
			for(int i=0; i < attr.getLength(); i++) {
				FieldConfig fc = new FieldConfig(attr.getQName(i), "varchar");
				Field f = fc.createField();
				f.setFieldValue(attr.getValue(i));
				addField(f);
			}
		}
	}
	
	public void setContent(String content) {
		String value = content.trim();
		if(!Util.nullOrEmptyString(value)) {
			Field f = getField(getLocalName());
			if(f != null) {
				f.setFieldValue(value);
			} else if(isParseAll()) {
				FieldConfig fc = new FieldConfig(getLocalName(), "varchar");
				f = fc.createField();
				f.setFieldValue(value);
				addField(f);
			}
		}
	}
	
	public void aggregate(XMLRecord rec) {
		Field f = getField(rec.getLocalName());
		if(f != null && !f.isSet()) {
			f.setFieldValue(rec.getFieldValue(rec.getLocalName()));
		} else {
			String key = rec.getConfigName();
			ArrayList<XMLRecord> list = map.get(key);
			if(list == null) {
				list = new ArrayList<XMLRecord>();
				map.put(key, list);
			}
			list.add(rec);
		}
	}
	
	public void getXMLRecords(String configNamePrefix, String elementName, ArrayList<XMLRecord> result) {
		StringBuffer buf = new StringBuffer(configNamePrefix);
		buf.append(XMLRecordConfig.CONFIG_NAME_DELIMITER);
		buf.append(elementName);
		getXMLRecords(buf.toString(), result);
	}
	
	public void getXMLRecords(String configName, ArrayList<XMLRecord> result) {
		if(configName.startsWith(getConfigName())) {
			int index = configName.indexOf(XMLRecordConfig.CONFIG_NAME_DELIMITER, getConfigName().length()+1);
			if(index != -1) {
				String key = configName.substring(0, index);
				ArrayList<XMLRecord> list = map.get(key);
				if(list != null) {
					for(XMLRecord rec : list) {
						rec.getXMLRecords(configName, result);
					}
				}
			} else {
				ArrayList<XMLRecord> list = map.get(configName);
				if(list != null) {
					result.addAll(list);
				}
			}
		} 
	}
	
	protected RecordConnectionManager<? extends RecordConnection<?>> getConnectionManager() {
		// lazy relationship loading not supported for XMLRecord
		return null;
	}

	protected Record readRecordRelationship(String configName, RecordConnection<?> connection, StatementLogger stmtLogger)
	throws Exception {
		// lazy relationship loading not supported for XMLRecord
		return null;
	}

	protected ArrayList<? extends Record> readListRelationship(String configName, RecordConnection<?> connection, StatementLogger stmtLogger)
	throws Exception {
		// lazy relationship loading not supported for XMLRecord
		return null;
	}
	
	public void print(boolean printLabel) {
		super.print(printLabel);
				
		Set<Entry<String, ArrayList<XMLRecord>>> set = map.entrySet();
		for(Entry<String, ArrayList<XMLRecord>> e : set) {
			ArrayList<XMLRecord> list = e.getValue();
			for(XMLRecord rec : list) {
				rec.print(printLabel);
			}
		}
	}
}
