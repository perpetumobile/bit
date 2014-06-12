package com.perpetumobile.bit.orm.json;

import java.util.ArrayList;
import java.util.Set;
import java.util.Map.Entry;

import com.perpetumobile.bit.orm.record.Record;
import com.perpetumobile.bit.orm.record.RecordConnection;
import com.perpetumobile.bit.orm.record.RecordConnectionManager;
import com.perpetumobile.bit.orm.record.RelationshipConfig;
import com.perpetumobile.bit.orm.record.RelationshipConfig.RelationshipType;
import com.perpetumobile.bit.orm.record.StatementLogger;
import com.perpetumobile.bit.orm.record.field.Field;
import com.perpetumobile.bit.orm.record.field.FieldConfig;
import com.perpetumobile.bit.util.Util;


/**
 * 
 * @author Zoran Dukic
 */
public class JSONRecord extends Record {

	protected boolean isPrimitive = false;
	
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
	
	public boolean isPrimitive() {
		return isPrimitive;
	}

	public void setPrimitive(boolean isPrimitive) {
		this.isPrimitive = isPrimitive;
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
	
	@SuppressWarnings("unchecked")
	public void aggregate(JSONRecord rec, boolean isList) {
		String key = rec.getConfigName();
		RelationshipConfig rc = config.getRelationshipConfig(key);
		if(isList) {
			rc.setRelationshipType(RelationshipType.List);
			ArrayList<JSONRecord> list = (ArrayList<JSONRecord>)listRelationshipMap.get(key);
			if(list == null) {
				list = new ArrayList<JSONRecord>();
				listRelationshipMap.put(key, list);
			}
			list.add(rec);
		} else {
			rc.setRelationshipType(RelationshipType.Record);
			recordRelationshipMap.put(key, rec);
		}
	}
	
	/**
	 * Get first level aggregated JSONRecords.
	 */
	@SuppressWarnings("unchecked")
	public ArrayList<? extends JSONRecord> getJSONRecords() {
		ArrayList<JSONRecord> result = new ArrayList<JSONRecord>();
		
		// add records from recordRelationshipMap
		Set<Entry<String, Record>> recordSet = recordRelationshipMap.entrySet();
		for(Entry<String, Record> e : recordSet) {
			Record rec = e.getValue();
			if(rec != null && rec instanceof JSONRecord) {
				result.add((JSONRecord)rec);
			}
		}
		
		// add records from listRelationshipMap
		Set<Entry<String, ArrayList<? extends Record>>> listSet = listRelationshipMap.entrySet();
		for(Entry<String, ArrayList<? extends Record>> e : listSet) {
			ArrayList<JSONRecord> list = (ArrayList<JSONRecord>)e.getValue();
			result.addAll(list);
		}
		
		return result;
	}
	
	/**
	 * Get deep level aggregated JSONRecords by walking down the relationship maps.
	 */
	public ArrayList<? extends JSONRecord> getJSONRecords(String... configNameArray) {
		ArrayList<JSONRecord> result = new ArrayList<JSONRecord>();
		
		StringBuilder buf = new StringBuilder();
		boolean isFirst = true;
		for(String s : configNameArray) {
			if(!isFirst) {
				buf.append(getConfigNameDelimiter());
			}
			buf.append(s);
			isFirst = false;
		}
		getJSONRecords(result, buf.toString());
		
		return result;
	}
	
	
	@SuppressWarnings("unchecked")
	protected void getJSONRecords(ArrayList<JSONRecord> result, String configName) {
		if(configName.startsWith(getConfigName())) {
			int index = configName.indexOf(getConfigNameDelimiter(), getConfigName().length()+1);
			if(index != -1) {
				// need to walk down relationship maps
				String key = configName.substring(0, index);
				
				// add record from recordRelationshipMap
				JSONRecord rec = (JSONRecord)recordRelationshipMap.get(key);
				if(rec != null) {
					rec.getJSONRecords(result, configName);
				}
				
				// add records from listRelationshipMap
				ArrayList<JSONRecord> list = (ArrayList<JSONRecord>)listRelationshipMap.get(key);
				if(list != null) {
					for(JSONRecord r : list) {
						r.getJSONRecords(result, configName);
					}
				}
			} else {
				// add record from recordRelationshipMap
				JSONRecord rec = (JSONRecord)recordRelationshipMap.get(configName);
				if(rec != null) {
					result.add(rec);
				}
				
				// add records from listRelationshipMap
				ArrayList<JSONRecord> list = (ArrayList<JSONRecord>)listRelationshipMap.get(configName);
				if(list != null) {
					result.addAll(list);
				}
			}
		} 
	}
	
	@Override
	protected StringBuilder generateJSON(String indent, boolean readable) {
		if(isPrimitive) {
			ArrayList<Field> fields = getFields();
			if(!Util.nullOrEmptyList(fields)) {
				Field f = fields.get(0);
				if(f != null) {
					StringBuilder buf = new StringBuilder();
					buf.append(indent);
					buf.append(f.getJSONFieldValue());
					return buf;
				}
			}
		}
		return super.generateJSON(indent, readable);
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
}
