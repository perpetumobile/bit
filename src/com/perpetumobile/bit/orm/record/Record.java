package com.perpetumobile.bit.orm.record;

import java.nio.ByteBuffer;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;

import com.perpetumobile.bit.orm.record.RelationshipConfig.RelationshipType;
import com.perpetumobile.bit.orm.record.exception.FieldNotConfiguredException;
import com.perpetumobile.bit.orm.record.exception.KeyFieldNotConfiguredException;
import com.perpetumobile.bit.orm.record.exception.RelationshipTypeMismatchException;
import com.perpetumobile.bit.orm.record.field.Field;
import com.perpetumobile.bit.util.Logger;
import com.perpetumobile.bit.util.Option;
import com.perpetumobile.bit.util.Util;



/**
 * 
 * @author Zoran Dukic
 */
abstract public class Record implements Option {
	static private Logger logger = new Logger(Record.class);

	protected RecordConfig config = null;
	private ArrayList<Field> fields = null;
	private HashMap<String, Field> fieldMap = null;
	
	protected HashMap<String, Record> recordRelationshipMap = new HashMap<String, Record>();
	protected HashMap<String, ArrayList<Record>> listRelationshipMap = new HashMap<String, ArrayList<Record>>();
	protected HashMap<String, HashMap<String, Record>> mapRelationshipMap = new HashMap<String, HashMap<String, Record>>();
	
	public Record() {
	}
	
	abstract public void init(String configName);
	
	public void init(RecordConfig config) {
		this.config = config;
		fields = config.createRecordFields();
		fieldMap = new HashMap<String, Field>();
		for(Field field : fields) {
			fieldMap.put(field.getFieldName(), field);
		}
	}
	
	public String getConfigName() {
		return (config != null ? config.getConfigName() : null);
	}
	
	public String getConnectionConfigName() {
		return (config != null ? config.getConnectionConfigName() : null);
	}
	
	protected boolean isConfigFields() {
		return (config != null ? config.isConfigFields() : false);
	}
	
	protected boolean doThrowFieldNotConfiguredException() {
		return (config != null ? config.doThrowFieldNotConfiguredException() : false);
	}
	
	public void addField(Field field) {
		fields.add(field);
		fieldMap.put(field.getFieldName(), field);
	}
	
	public Field getField(int index) {
		return fields.get(index);
	}
	
	public ArrayList<Field> getFields() {
		return fields;
	}
	
	public Field getField(String fieldName) {
		return fieldMap.get(fieldName);
	}
	
	public Field getKeyField() {
		return (config.getKeyFieldName() != null ? getField(config.getKeyFieldName()) : null);
	}
	
	public void addRelationshipRecord(String configName, Record rec) {
		RelationshipConfig rc = config.getRelationshipConfig(configName);
		
		switch(rc.getRelationshipType()) {
		case Record:
			recordRelationshipMap.put(configName, rec);
			break;
		case List:
			ArrayList<Record> list = listRelationshipMap.get(configName);
			if(list == null) {
				list = new ArrayList<Record>();
				listRelationshipMap.put(configName, list);
			}
			list.add(rec);
			break;
		case Map:
			Field keyField = rec.getKeyField();
			if(keyField == null) {
				throw new KeyFieldNotConfiguredException("Record Config Name: " + configName);
			}
			HashMap<String, Record> map = mapRelationshipMap.get(configName);
			if(map == null) {
				map = new HashMap<String, Record>();
				mapRelationshipMap.put(configName, map);
			}
			map.put(keyField.getFieldValue(), rec);
			break;
		}
	}
	
	abstract protected RecordConnectionManager<? extends RecordConnection<?>> getConnectionManager();
	
	abstract protected Record readRecordRelationship(String configName, RecordConnection<?> connection, StatementLogger stmtLogger) throws Exception;
	
	abstract protected ArrayList<? extends Record> readListRelationship(String configName, RecordConnection<?> connection, StatementLogger stmtLogger) throws Exception;
	
	protected HashMap<String, ? extends Record> readMapRelationship(String configName, RecordConnection<?> connection, StatementLogger stmtLogger) 
	throws Exception {
		HashMap<String, Record> result = new HashMap<String, Record>();
		ArrayList<? extends Record> list = readListRelationship(configName, connection, stmtLogger);
		if(list != null) {
			for(Record r : list) {
				Field keyField = r.getKeyField();
				if(keyField != null) {
					result.put(keyField.getFieldValue(), r);
				}
			}
		}
		return result;
	}
	
	/**
	 * Get a record defined by RelationshipType.Record (1:1) relationship.  
	 * @param configName relationship configName
	 * @param connection
	 * @param stmtLogger
	 */
	public Record getRelationshipRecord(String configName, RecordConnection<?> connection, StatementLogger stmtLogger) {
		RelationshipConfig rc = config.getRelationshipConfig(configName);
		if(rc.getRelationshipType() != RelationshipType.Record) {
			throw new RelationshipTypeMismatchException();
		}
		Record result = recordRelationshipMap.get(configName);
		if(result == null) {
			RecordConnectionManager<? extends RecordConnection<?>> connectionManager = getConnectionManager(); 
			RecordConnection<?> c = connection;
			try {			
				if(connection == null && connectionManager != null) {
					String connectionConfigName = rc.getConnectionConfigName();
					if(!Util.nullOrEmptyString(connectionConfigName)) {
						c = connectionManager.getConnection(rc.getConnectionConfigName());
					} else {
						logger.error(configName + ".Record.Connection.Config.Key needs to be configured to support lazy loading.");
					}
				}
				if(c != null) {
					result = readRecordRelationship(configName, c, stmtLogger);
				}
			} catch (Exception e) {
				logger.error("Exception at Record.getRelationshipRecord for " + configName, e);
				if(connection == null) {
					getConnectionManager().invalidateConnectionImpl(c);
				}
			} finally {
				if(connection == null) {
					getConnectionManager().returnConnectionImpl(c);
				}
			}
			if(result == null) {
				result = new NullRecord();
			}
			recordRelationshipMap.put(configName, result);
		}
		if(result instanceof NullRecord) {
			return null;
		}
		return result;
	}
	
	/**
	 * Get a record defined by RelationshipType.Record (1:1) relationship.  
	 * @param configName relationship configName
	 */
	public Record getRelationshipRecord(String configName) {
		return getRelationshipRecord(configName, null, null);
	}
	
	/**
	 * Get a record list defined by RelationshipType.List (1:n) relationship.  
	 * @param configName relationship configName
	 * @param connection
	 * @param stmtLogger
	 */
	@SuppressWarnings("unchecked")
	public ArrayList<? extends Record> getRelationshipRecordList(String configName, RecordConnection<?> connection, StatementLogger stmtLogger) {
		RelationshipConfig rc = config.getRelationshipConfig(configName);
		if(rc.getRelationshipType() != RelationshipType.List) {
			throw new RelationshipTypeMismatchException();
		}
		ArrayList<? extends Record> result = listRelationshipMap.get(configName);
		if(result == null) {
			RecordConnectionManager<? extends RecordConnection<?>> connectionManager = getConnectionManager(); 
			RecordConnection<?> c = connection;
			try {			
				if(connection == null && connectionManager != null) {
					String connectionConfigName = rc.getConnectionConfigName();
					if(!Util.nullOrEmptyString(connectionConfigName)) {
						c = connectionManager.getConnection(rc.getConnectionConfigName());
					} else {
						logger.error(configName + ".Record.Connection.Config.Key needs to be configured to support lazy loading.");
					}
				}
				if(c != null) {
					result = readListRelationship(configName, c, stmtLogger);
				}
			} catch (Exception e) {
				logger.error("Exception at Record.getRelationshipRecordList for " + configName, e);
				if(connection == null) {
					getConnectionManager().invalidateConnectionImpl(c);
				}
			} finally {
				if(connection == null) {
					getConnectionManager().returnConnectionImpl(c);
				}
			}
			if(result == null) {
				result = new ArrayList<Record>();
			}
			listRelationshipMap.put(configName, (ArrayList<Record>)result);
		}
		return result;
	}
	
	/**
	 * Get a record list defined by RelationshipType.List (1:n) relationship.  
	 * @param configName relationship configName
	 */
	public ArrayList<? extends Record> getRelationshipRecordList(String configName) {
		return getRelationshipRecordList(configName, null, null);
	}
	
	/**
	 * Get a record map defined by RelationshipType.Map (1:n) relationship.  
	 * @param configName relationship configName
	 * @param connection
	 * @param stmtLogger
	 */
	@SuppressWarnings("unchecked")
	public HashMap<String, ? extends Record> getRelationshipRecordMap(String configName, RecordConnection<?> connection, StatementLogger stmtLogger) {
		RelationshipConfig rc = config.getRelationshipConfig(configName);
		if(rc.getRelationshipType() != RelationshipType.Map) {
			throw new RelationshipTypeMismatchException();
		}
		HashMap<String, ? extends Record> result = mapRelationshipMap.get(configName);
		if(result == null) {
			RecordConnectionManager<? extends RecordConnection<?>> connectionManager = getConnectionManager(); 
			RecordConnection<?> c = connection;
			try {			
				if(connection == null && connectionManager != null) {
					String connectionConfigName = rc.getConnectionConfigName();
					if(!Util.nullOrEmptyString(connectionConfigName)) {
						c = connectionManager.getConnection(rc.getConnectionConfigName());
					} else {
						logger.error(configName + ".Record.Connection.Config.Key needs to be configured to support lazy loading.");
					}
				}
				if(c != null) {
					result = readMapRelationship(configName, c, stmtLogger);
				}
			} catch (Exception e) {
				logger.error("Exception at Record.getRelationshipRecordMap for " + configName, e);
				if(connection == null) {
					getConnectionManager().invalidateConnectionImpl(c);
				}
			} finally {
				if(connection == null) {
					getConnectionManager().returnConnectionImpl(c);
				}
			}
			if(result == null) {
				result = new HashMap<String, Record>();
			}
			mapRelationshipMap.put(configName, (HashMap<String, Record>)result);
		}
		return result;
	}
	
	/**
	 * Get a record map defined by RelationshipType.Map (1:n) relationship.  
	 * @param configName relationship configName
	 */
	public HashMap<String, ? extends Record> getRelationshipRecordMap(String configName) {
		return getRelationshipRecordMap(configName, null, null);
	}
	
	/**
	 * Get a record from a record map defined by RelationshipType.Map (1:n) relationship.  
	 * @param configName relationship configName
	 * @param keyField
	 */
	public Record getRelationshipRecord(String configName, Field keyField, RecordConnection<?> connection, StatementLogger stmtLogger) {
		HashMap<String, ? extends Record> map = getRelationshipRecordMap(configName, connection, stmtLogger);
		if(map != null) {
			return map.get(keyField.getFieldValue());
		}
		return null;
	}
	
	/**
	 * Get a record from a record map defined by RelationshipType.Map (1:n) relationship.  
	 * @param configName relationship configName
	 * @param keyField
	 */
	public Record getRelationshipRecord(String configName, Field keyField) {
		return getRelationshipRecord(configName, keyField, null, null);
	}
	
	public String getSQLFieldValue(String fieldName) {
		Field field = getField(fieldName);
		if(field != null) {
			return field.getSQLFieldValue();
		}
		if(doThrowFieldNotConfiguredException()) {
			throw new FieldNotConfiguredException("Record Config Name: " + getConfigName() + "; Field Name: " + fieldName);
		}
		return null;
	}
	
	public String getFieldValue(String fieldName) {
		Field field = getField(fieldName);
		if(field != null) {
			return field.getFieldValue();
		}
		if(doThrowFieldNotConfiguredException()) {
			throw new FieldNotConfiguredException("Record Config Name: " + getConfigName() + "; Field Name: " + fieldName);
		}
		return null;
	}
	
	public ByteBuffer getByteBufferFieldValue(String fieldName) {
		Field field = getField(fieldName);
		if(field != null) {
			return field.getByteBufferFieldValue();
		}
		if(doThrowFieldNotConfiguredException()) {
			throw new FieldNotConfiguredException("Record Config Name: " + getConfigName() + "; Field Name: " + fieldName);
		}
		return null;
	}
	
	public int getIntFieldValue(String fieldName) {
		Field field = getField(fieldName);
		if(field != null) {
			return field.getIntFieldValue();
		}
		if(doThrowFieldNotConfiguredException()) {
			throw new FieldNotConfiguredException("Record Config Name: " + getConfigName() + "; Field Name: " + fieldName);
		}
		return 0;
	}
	
	public long getLongFieldValue(String fieldName) {
		Field field = getField(fieldName);
		if(field != null) {
			return field.getLongFieldValue();
		}
		if(doThrowFieldNotConfiguredException()) {
			throw new FieldNotConfiguredException("Record Config Name: " + getConfigName() + "; Field Name: " + fieldName);
		}
		return 0;
	}
	
	public float getFloatFieldValue(String fieldName) {
		Field field = getField(fieldName);
		if(field != null) {
			return field.getFloatFieldValue();
		}
		if(doThrowFieldNotConfiguredException()) {
			throw new FieldNotConfiguredException("Record Config Name: " + getConfigName() + "; Field Name: " + fieldName);
		}
		return 0;
	}
	
	public double getDoubleFieldValue(String fieldName) {
		Field field = getField(fieldName);
		if(field != null) {
			return field.getDoubleFieldValue();
		}
		if(doThrowFieldNotConfiguredException()) {
			throw new FieldNotConfiguredException("Record Config Name: " + getConfigName() + "; Field Name: " + fieldName);
		}
		return 0;
	}
	
	public String getMD5FieldValue(String fieldName) {
		Field field = getField(fieldName);
		if(field != null) {
			return field.getMD5FieldValue();
		}
		if(doThrowFieldNotConfiguredException()) {
			throw new FieldNotConfiguredException("Record Config Name: " + getConfigName() + "; Field Name: " + fieldName);
		}
		return null;
	}
	
	public Timestamp getTimestampFieldValue(String fieldName) {
		Field field = getField(fieldName);
		if(field != null) {
			return field.getTimestampFieldValue();
		}
		if(doThrowFieldNotConfiguredException()) {
			throw new FieldNotConfiguredException("Record Config Name: " + getConfigName() + "; Field Name: " + fieldName);
		}
		return null;
	}
	
	public void setFieldValue(String fieldName, String fieldValue) {
		Field field = getField(fieldName);
		if(field != null) {
			field.setFieldValue(fieldValue);
		} else if(doThrowFieldNotConfiguredException()) {
			throw new FieldNotConfiguredException("Record Config Name: " + getConfigName() + "; Field Name: " + fieldName);
		}
	}
	
	public void setByteBufferFieldValue(String fieldName, ByteBuffer fieldValue) {
		Field field = getField(fieldName);
		if(field != null) {
			field.setByteBufferFieldValue(fieldValue);
		} else if(doThrowFieldNotConfiguredException()) {
			throw new FieldNotConfiguredException("Record Config Name: " + getConfigName() + "; Field Name: " + fieldName);
		}
	}
	
	public void setIntFieldValue(String fieldName, int fieldValue) {
		Field field = getField(fieldName);
		if(field != null) {
			field.setIntFieldValue(fieldValue);
		} else if(doThrowFieldNotConfiguredException()) {
			throw new FieldNotConfiguredException("Record Config Name: " + getConfigName() + "; Field Name: " + fieldName);
		}
	}
	
	public void setLongFieldValue(String fieldName, long fieldValue) {
		Field field = getField(fieldName);
		if(field != null) {
			field.setLongFieldValue(fieldValue);
		} else if(doThrowFieldNotConfiguredException()) {
			throw new FieldNotConfiguredException("Record Config Name: " + getConfigName() + "; Field Name: " + fieldName);
		}
	}
	
	public void setFloatFieldValue(String fieldName, float fieldValue) {
		Field field = getField(fieldName);
		if(field != null) {
			field.setFloatFieldValue(fieldValue);
		} else if(doThrowFieldNotConfiguredException()) {
			throw new FieldNotConfiguredException("Record Config Name: " + getConfigName() + "; Field Name: " + fieldName);
		}
	}
	
	public void setDoubleFieldValue(String fieldName, double fieldValue) {
		Field field = getField(fieldName);
		if(field != null) {
			field.setDoubleFieldValue(fieldValue);
		} else if(doThrowFieldNotConfiguredException()) {
			throw new FieldNotConfiguredException("Record Config Name: " + getConfigName() + "; Field Name: " + fieldName);
		}
	}
	
	public void setMD5FieldValue(String fieldName, String fieldValue) {
		Field field = getField(fieldName);
		if(field != null) {
			field.setMD5FieldValue(fieldValue);
		} else if(doThrowFieldNotConfiguredException()) {
			throw new FieldNotConfiguredException("Record Config Name: " + getConfigName() + "; Field Name: " + fieldName);
		}
	}
	
	public void setTimestampFieldValue(String fieldName, Timestamp fieldValue) {
		Field field = getField(fieldName);
		if(field != null) {
			field.setTimestampFieldValue(fieldValue);
		} else if(doThrowFieldNotConfiguredException()) {
			throw new FieldNotConfiguredException("Record Config Name: " + getConfigName() + "; Field Name: " + fieldName);
		}
	}
	
	public String getOptionValue() {
		return getFieldValue(config.getKeyFieldName());
	}
	
	public String getOptionText() {
		return getFieldValue(config.getLabelFieldName());
	}
	
	public void print(boolean printLabel) {
		if(printLabel) {
			System.out.println("Config: " + getConfigName());
		}
		StringBuilder buf = new StringBuilder();
		for(Field f : getFields()) {
			if(printLabel) {
				buf.append(f.getFieldName());
				buf.append(": ");
			}
			buf.append(f.getFieldValue());
			if(printLabel) {
				buf.append("\n");
			} else {
				buf.append("\t");
			}
		}
		System.out.println(buf.toString());
	}
	
	public boolean equals(Object obj) {
		if(obj instanceof Record) {
			Record r = (Record)obj;
			for(Field f1 : fields) {
				Field f2 = r.getField(f1.getFieldName());
				if(f2 == null) {
					return false;
				}
				if(!f1.equalValue(f2)) {
					return false;
				}
			}
			return true;
		}
		return false;
	}
}

class NullRecord extends Record {
	public void init(String configName) {
	}

	protected RecordConnectionManager<? extends RecordConnection<?>> getConnectionManager() {
		return null;
	}

	protected Record readRecordRelationship(String configName, RecordConnection<?> connection, StatementLogger stmtLogger)
	throws Exception {
		return null;
	}

	protected ArrayList<? extends Record> readListRelationship(String configName, RecordConnection<?> connection, StatementLogger stmtLogger)
	throws Exception {
		return null;
	}
}