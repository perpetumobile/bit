package com.perpetumobile.bit.orm.json;

import java.io.IOException;
import java.io.Reader;

import org.json.simple.parser.ContentHandler;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.perpetumobile.bit.util.Util;


public class JSONRecordHandler implements ContentHandler {
	protected String configName = null;
	protected String key = null;
	   	
	protected JSONRecord jsonRecord = null;	
	protected String nextKey = null;
	protected boolean isArray = false;
	
	protected JSONRecordHandler parent = null;
	protected JSONParser parser = null;
	protected Reader in = null;
	
	public JSONRecordHandler(String configName, JSONParser parser, Reader in) {
		init(configName, null, parser, in);
	}
	
	public JSONRecordHandler(String key, JSONRecordHandler parent) {
		if(parent != null) {
			init(parent.configName, key, parent.parser, parent.in);
			this.parent = parent;
		}
	}
	
	protected void init(String configName, String key, JSONParser parser, Reader in) {
		this.key = key;
		this.configName = configName;
		
		if(!Util.nullOrEmptyString(key)) { 
			StringBuffer buf = new StringBuffer();
			buf.append(configName);
			buf.append(JSONRecordConfigFactory.getInstance().getRecordConfig(configName).getConfigNameDelimiter());
			buf.append(key);
			this.configName = buf.toString();
		}
		
		this.parser = parser;
		this.in = in;
	}
	
	public JSONRecord getJSONRecord() {
		return jsonRecord;
	}
	
	protected void createJSONRecord() {
		try {
			JSONRecordConfig jsonRecordConfig = JSONRecordConfigFactory.getInstance().getRecordConfig(configName);
			jsonRecord = (JSONRecord)jsonRecordConfig.createRecord();
		} catch (Exception e) {
			jsonRecord = null;
		}
	}
	
	public void handle(String key)
	throws ParseException, IOException {
		// use this as content handler
		parser.parse(in, this, true);
	}
	
	public void aggregate(JSONRecord rec, boolean isList) {
		if(jsonRecord != null) {
			jsonRecord.aggregate(rec, isList);
		}
	}

	public void startJSON() throws ParseException, IOException {
		createJSONRecord();
	}
	
	public void endJSON() throws ParseException, IOException {
	}
	
	public boolean startObjectEntry(String key) throws ParseException, IOException {
		nextKey = key;
		return true;
	}
	
	public boolean endObjectEntry() throws ParseException, IOException {
		return true;
	}

	public boolean startObject() throws ParseException, IOException {
		boolean isNewComplexObject = true;
		if(isArray) {
			if(jsonRecord == null) {
				// need to start jsonRecord for this array
				createJSONRecord();
				// this is not new complex object but an object that belongs to this array
				isNewComplexObject = false;
			}
		} 
		
		if(isNewComplexObject && !Util.nullOrEmptyString(nextKey)) {
			JSONRecordHandler handler = new JSONRecordHandler(nextKey, this);
			handler.createJSONRecord();
			handler.handle(nextKey);
		}
		return true;
	}
	
	public boolean endObject() throws ParseException, IOException {
		if(parent != null) {
			// no need to swap content handler back to parent
			// parent parse is already waiting
			// parser.parse(in, parent, true);
			parent.aggregate(jsonRecord, isArray);
			// clear jsonRecord in case we are processing an array and this handler will be used
			jsonRecord = null;
			if(!isArray) {
				return false;
			}
		}
		return true;
	}

	public boolean startArray() throws ParseException, IOException {
		if(!Util.nullOrEmptyString(nextKey)) {
			JSONRecordHandler handler = new JSONRecordHandler(nextKey, this);
			handler.isArray = true;
			handler.handle(nextKey);
		}
		return true;
	}
	
	public boolean endArray() throws ParseException, IOException {
		if(parent != null) {
			// no need to swap content handler back to parent
			// parent parse is already waiting
			// parser.parse(in, parent, true);
			return false;
		}
		return true;
	}

	protected String getStringValue(Object primitiveValue) {
		if(primitiveValue == null) {
			return null;
		}
		if(primitiveValue instanceof String) {
			return (String)primitiveValue;
		}
		return primitiveValue.toString();
	}
	
	public boolean primitive(Object value) throws ParseException, IOException {
		// support for primitive array
		boolean isPrimitiveArray = isArray && jsonRecord == null; 
		if(isPrimitiveArray) {
			createJSONRecord();
			nextKey = key;
		}
		if(!Util.nullOrEmptyString(nextKey)) {
			if(jsonRecord != null) {
				jsonRecord.setField(nextKey, getStringValue(value));
			}
		}
		if(isPrimitiveArray) {
			if(parent != null && jsonRecord != null) {
				jsonRecord.setPrimitive(true);
				parent.aggregate(jsonRecord, true);
			}
			jsonRecord = null;
		}
		return true;
	}
}
