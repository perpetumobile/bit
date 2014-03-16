package com.perpetumobile.bit.orm.json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

import com.perpetumobile.bit.orm.record.Record;
import com.perpetumobile.bit.orm.record.RecordConnection;
import com.perpetumobile.bit.orm.record.RecordConnectionManager;
import com.perpetumobile.bit.orm.record.StatementLogger;
import com.perpetumobile.bit.orm.record.field.Field;
import com.perpetumobile.bit.orm.record.field.FieldConfig;


/**
 * 
 * @author Zoran Dukic
 */
public class JSONRecord extends Record {
	
	HashMap<String, ArrayList<JSONRecord>> map = new HashMap<String, ArrayList<JSONRecord>>();

	public JSONRecord() {
	}
	
	public JSONRecord(String configName) {
		init(configName);
	}
	
	public void init(String configName) {
		init(JSONRecordConfigFactory.getInstance().getRecordConfig(configName));
	}
	
	public boolean isParseAll() {
		return (config != null ? ((JSONRecordConfig)config).isParseAll() : true);
	}
	
	public void setField(String key, String value) {
		if(isConfigFields()) {
			Field f = getField(key);
			if(f != null) {
				f.setFieldValue(value);
			}
		} else if(isParseAll()) {
			FieldConfig fc = new FieldConfig(key, "varchar");
			Field f = fc.createField();
			f.setFieldValue(value);
			addField(f);
		}
	}
	
	public void aggregate(JSONRecord rec) {
		String key = rec.getConfigName();
		ArrayList<JSONRecord> list = map.get(key);
		if(list == null) {
			list = new ArrayList<JSONRecord>();
			map.put(key, list);
		}
		list.add(rec);
	}
	
	public void getJSONRecords(String configNamePrefix, String elementName, ArrayList<JSONRecord> result) {
		StringBuffer buf = new StringBuffer(configNamePrefix);
		buf.append(JSONRecordConfig.CONFIG_NAME_DELIMITER);
		buf.append(elementName);
		getJSONRecords(buf.toString(), result);
	}
	
	public void getJSONRecords(String configName, ArrayList<JSONRecord> result) {
		if(configName.startsWith(getConfigName())) {
			int index = configName.indexOf(JSONRecordConfig.CONFIG_NAME_DELIMITER, getConfigName().length()+1);
			if(index != -1) {
				String key = configName.substring(0, index);
				ArrayList<JSONRecord> list = map.get(key);
				if(list != null) {
					for(JSONRecord rec : list) {
						rec.getJSONRecords(configName, result);
					}
				}
			} else {
				ArrayList<JSONRecord> list = map.get(configName);
				if(list != null) {
					result.addAll(list);
				}
			}
		} 
	}
	
	protected RecordConnectionManager<? extends RecordConnection<?>> getConnectionManager() {
		// lazy relationship loading not supported for JSONRecord
		return null;
	}

	protected Record readRecordRelationship(String configName, RecordConnection<?> connection, StatementLogger stmtLogger)
	throws Exception {
		// lazy relationship loading not supported for JSONRecord
		return null;
	}

	protected ArrayList<? extends Record> readListRelationship(String configName, RecordConnection<?> connection, StatementLogger stmtLogger)
	throws Exception {
		// lazy relationship loading not supported for JSONRecord
		return null;
	}
	
	public void print(boolean printLabel) {
		super.print(printLabel);
				
		Set<Entry<String, ArrayList<JSONRecord>>> set = map.entrySet();
		for(Entry<String, ArrayList<JSONRecord>> e : set) {
			ArrayList<JSONRecord> list = e.getValue();
			for(JSONRecord rec : list) {
				rec.print(printLabel);
			}
		}
	}
}
