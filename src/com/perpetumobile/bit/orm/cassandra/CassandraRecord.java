package com.perpetumobile.bit.orm.cassandra;


import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.cassandra.thrift.Column;
import org.apache.cassandra.thrift.ColumnOrSuperColumn;
import org.apache.cassandra.thrift.KeySlice;

import com.perpetumobile.bit.orm.record.Record;
import com.perpetumobile.bit.orm.record.RecordConnection;
import com.perpetumobile.bit.orm.record.RecordConnectionManager;
import com.perpetumobile.bit.orm.record.StatementLogger;
import com.perpetumobile.bit.orm.record.field.ByteBufferField;
import com.perpetumobile.bit.orm.record.field.Field;
import com.perpetumobile.bit.orm.record.field.FieldConfig;
import com.perpetumobile.bit.util.Util;


/**
 * 
 * @author Zoran Dukic
 */
public class CassandraRecord extends Record {
	
	protected CassandraKey key = null;
	protected CassandraKey superColumn = null;
	
	protected ArrayList<CassandraRecord> superColumnRecordList = null;
	protected HashMap<CassandraKey, CassandraRecord> superColumnRecordMap = null;
	
	public CassandraRecord() {
	}
	
	public CassandraRecord(String configName) {
		init(configName);
	}
	
	public void init(String configName) {
		init(CassandraRecordConfigFactory.getInstance().getRecordConfig(configName));
	} 
	
	public CassandraKey getKey() {
		return key;
	}
	
	// override Record.getKeyField()
	public Field getKeyField() {
		return key;
	}
	
	public void setKey(CassandraKey key) {
		this.key = key;
	}
	
	public ByteBuffer getByteBufferKey() {
		if(key != null && key.isSet()) {
			return key.getByteBufferFieldValue();
		}
		return null;
	}
	
	public CassandraKey getSuperColumn() {
		return superColumn;
	}
	
	public void setSuperColumn(CassandraKey superColumn) {
		this.superColumn = superColumn;
	}
	
	public ByteBuffer getByteBufferSuperColumn() {
		if(superColumn != null && superColumn.isSet()) {
			return superColumn.getByteBufferFieldValue();
		}
		return null;
	}

	public boolean isReadAll() {
		return (config != null ? ((CassandraRecordConfig)config).isReadAll() : true);
	}
	
	public boolean isSuperColumnRecordList() {
		return (superColumnRecordList != null ? superColumnRecordList.size() > 0 : false);
	}
	
	public ArrayList<CassandraRecord> getSuperColumnRecordList() {
		return superColumnRecordList;
	}
	
	public CassandraRecord getSuperColumnRecord(CassandraKey superColumn) {
		if(superColumnRecordMap != null) {
			return superColumnRecordMap.get(superColumn);
		}
		return null;
	}
	
	public int readSimpleRecord(CassandraConnection cassandraConnection, CassandraKey superColumn, CassandraKey key, List<Column> rs, int index) 
	throws Exception {
		this.key = key;
		this.superColumn = superColumn;
		int result = index;
		for(Column column : rs) {
			if(isConfigFields()) {
				String fieldName = Util.toString(column.name, FieldConfig.CHARSET_NAME);
				Field f = getField(fieldName);
				if(f != null) {
					f.bind(column);
				}
			} else if (isReadAll()) {
				ByteBufferField f = new ByteBufferField(column.name);
				f.bind(column);
				addField(f);
			}
			result++;
		}
		return result;
	}
	
	public int readRecord(CassandraConnection cassandraConnection, CassandraKey superColumn, CassandraKey key, List<ColumnOrSuperColumn> rs, int index) 
	throws Exception {
		this.key = key;
		this.superColumn = superColumn;
		int result = index;
		
		for(ColumnOrSuperColumn cosc : rs) {
			if(cosc.super_column != null) {
				if(superColumnRecordList == null) {
					superColumnRecordList = new ArrayList<CassandraRecord>();
				}
				if(superColumnRecordMap == null) {
					superColumnRecordMap = new HashMap<CassandraKey, CassandraRecord>();
				}
				CassandraRecord rec = (CassandraRecord)config.createRecord();
				CassandraKey sc = CassandraKey.create(cosc.super_column.name);
				rec.readSimpleRecord(cassandraConnection, sc, key, cosc.super_column.columns, index);
				superColumnRecordList.add(rec);
				superColumnRecordMap.put(sc, rec);
			} else {
				if(isConfigFields()) {
					String fieldName = Util.toString(cosc.column.name, FieldConfig.CHARSET_NAME);
					Field f = getField(fieldName);
					if(f != null) {
						f.bind(cosc.column);
					}
				} else if (isReadAll()) {
					ByteBufferField f = new ByteBufferField(cosc.column.name);
					f.bind(cosc.column);
					addField(f);
				}
				result++;
			}
		}
		return result;
	}
	
	public int readRecord(CassandraConnection cassandraConnection, CassandraKey superColumn, KeySlice keySlice, int index) 
	throws Exception {
		return readRecord(cassandraConnection, superColumn, CassandraKey.create(keySlice.key), keySlice.getColumns(), index);
	}
	
	protected RecordConnectionManager<? extends RecordConnection<?>> getConnectionManager() {
		return CassandraConnectionManager.getInstance();
	}

	protected Record readRecordRelationship(String configName, RecordConnection<?> connection, StatementLogger stmtLogger)
	throws Exception {
		//TODO: Finish support for relationships
		return null;
	}

	protected ArrayList<? extends Record> readListRelationship(String configName, RecordConnection<?> connection, StatementLogger stmtLogger)
	throws Exception {
		//TODO: Finish support for relationships
		return null;
	}
	
	public void print(boolean printLabel) {
		if(isSuperColumnRecordList()) {
			for(CassandraRecord rec : superColumnRecordList) {
				rec.print(printLabel);
			}
		} else {
			System.out.println("Key: " + key.getFieldValue());
			if(superColumn != null) {
				System.out.println("Super Column: " + superColumn.getFieldValue());
			}
			super.print(printLabel);
		}
	}
}
