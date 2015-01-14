package com.perpetumobile.bit.orm.xml;

import java.util.ArrayList;
import java.util.Set;
import java.util.Map.Entry;

import org.xml.sax.Attributes;

import com.perpetumobile.bit.orm.record.Record;
import com.perpetumobile.bit.orm.record.RecordConnection;
import com.perpetumobile.bit.orm.record.RecordConnectionManager;
import com.perpetumobile.bit.orm.record.StatementLogger;
import com.perpetumobile.bit.orm.record.exception.RecordConfigMismatchException;
import com.perpetumobile.bit.orm.record.field.Field;
import com.perpetumobile.bit.orm.record.field.FieldConfig;
import com.perpetumobile.bit.util.Util;


/**
 * 
 * @author Zoran Dukic
 */
public class XMLRecord extends Record {
	private static final long serialVersionUID = 1L;
	
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
	
	@SuppressWarnings("unchecked")
	public void aggregate(XMLRecord rec) {
		Field f = getField(rec.getLocalName());
		if(f != null && !f.isSet()) {
			f.setFieldValue(rec.getFieldValue(rec.getLocalName()));
		} else {
			String key = rec.getConfigName();
			ArrayList<XMLRecord> list = (ArrayList<XMLRecord>)listRelationshipMap.get(key);
			if(list == null) {
				list = new ArrayList<XMLRecord>();
				listRelationshipMap.put(key, list);
			}
			list.add(rec);
		}
	}
	
	/**
	 * Set first level XMLRecord for a given key.  
	 */
	public void setFirstLevelXMLRecord(String localName, XMLRecord rec) {
		String relationshipConfigName = getRelationshipConfigName(localName);
		if(!relationshipConfigName.equals(rec.getConfigName())) {
			StringBuilder msg = new StringBuilder(rec.getConfigName());
			msg.append(" != ");
			msg.append(relationshipConfigName);
			throw new RecordConfigMismatchException(msg.toString());
		}
		aggregate(rec);
	}
	
	/**
	 * Get first level aggregated XMLRecords.
	 */
	@SuppressWarnings("unchecked")
	public ArrayList<? extends XMLRecord> getFirstLevelXMLRecords() {
		ArrayList<XMLRecord> result = new ArrayList<XMLRecord>();
			
		// add records from listRelationshipMap
		Set<Entry<String, ArrayList<? extends Record>>> listSet = listRelationshipMap.entrySet();
		for(Entry<String, ArrayList<? extends Record>> e : listSet) {
			ArrayList<XMLRecord> list = (ArrayList<XMLRecord>)e.getValue();
			result.addAll(list);
		}
		
		return result;
	}
	
	/**
	 * Get first level aggregated XMLRecord for a given local name.
	 */
	public XMLRecord getFirstLevelXMLRecord(String localName) {
		ArrayList<? extends XMLRecord> list = getFirstLevelXMLRecords(localName);
		if(!Util.nullOrEmptyList(list)) {
			return list.get(0);
		}
		return null;
	}
		
	/**
	 * Get first level aggregated XMLRecord array for a given local name.
	 */
	@SuppressWarnings("unchecked")
	public ArrayList<? extends XMLRecord> getFirstLevelXMLRecords(String localName) {
		return (ArrayList<XMLRecord>)getRelationshipRecordList(getRelationshipConfigName(localName));
	}
	
	/**
	 * Get deep level aggregated XMLRecords by walking down the 
	 * relationship maps.
	 */
	public ArrayList<? extends XMLRecord> getXMLRecords(String... configNameArray) {
		ArrayList<XMLRecord> result = new ArrayList<XMLRecord>(); 
		
		StringBuilder buf = new StringBuilder();
		boolean isFirst = true;
		for(String s : configNameArray) {
			if(!isFirst) {
				buf.append(getConfigNameDelimiter());
			}
			buf.append(s);
			isFirst = false;
		}
		getXMLRecords(result, buf.toString());
		
		return result;
	}
	
	@SuppressWarnings("unchecked")
	protected void getXMLRecords(ArrayList<XMLRecord> result, String configName) {
		if(configName.startsWith(getConfigName())) {
			int index = configName.indexOf(getConfigNameDelimiter(), getConfigName().length()+1);
			if(index != -1) {
				// need to walk down relationship maps
				String key = configName.substring(0, index);
				
				// add records from listRelationshipMap
				ArrayList<XMLRecord> list = (ArrayList<XMLRecord>)listRelationshipMap.get(key);
				if(list != null) {
					for(XMLRecord rec : list) {
						rec.getXMLRecords(result, configName);
					}
				}
			} else {
				// add records from listRelationshipMap
				ArrayList<XMLRecord> list = (ArrayList<XMLRecord>)listRelationshipMap.get(configName);
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
}
