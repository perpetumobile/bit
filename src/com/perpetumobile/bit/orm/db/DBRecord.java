package com.perpetumobile.bit.orm.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.perpetumobile.bit.orm.record.ForeignKeyConfig;
import com.perpetumobile.bit.orm.record.Record;
import com.perpetumobile.bit.orm.record.RecordConnection;
import com.perpetumobile.bit.orm.record.RecordConnectionManager;
import com.perpetumobile.bit.orm.record.RelationshipConfig;
import com.perpetumobile.bit.orm.record.StatementLogger;
import com.perpetumobile.bit.orm.record.exception.FieldNotConfiguredException;
import com.perpetumobile.bit.orm.record.field.Field;
import com.perpetumobile.bit.util.Util;


/**
 * 
 * @author Zoran Dukic
 */
public class DBRecord extends Record {
	private static final long serialVersionUID = 1L;
	
	public DBRecord() {
	}
	
	public DBRecord(String configName) {
		init(configName);
	}
	
	public void init(String configName) {
		init(DBRecordConfigFactory.getInstance().getRecordConfig(configName));
	}
	
	public String getTableName() {
		return (config != null ? ((DBRecordConfig)config).getTableName() : null);
	}
	
	public int readRecord(DBConnection dbConnection, ResultSet rs, int index) 
	throws SQLException {
		int result = index;
		for(Field f : getFields()) {
			result = f.bind(rs, result);
		}
		return result;
	}
	
	protected RecordConnectionManager<? extends RecordConnection<?>> getConnectionManager() {
		return DBConnectionManager.getInstance();
	}
	
	private DBStatement<DBRecord> getReadRelationshipQuery(RelationshipConfig rc, StatementLogger stmtLogger) {
		DBStatement<DBRecord> query = null;
		ArrayList<ForeignKeyConfig> fklist= rc.getForeignKeyConfigs();
		if(!Util.nullOrEmptyList(fklist)) {
			query = new DBStatement<DBRecord>(rc.getConfigName(), stmtLogger);
			for(ForeignKeyConfig fk : fklist) {
				StringBuffer buf = new StringBuffer(fk.getFieldName());
				buf.append("=");
				buf.append(getSQLFieldValue(fk.getForeignFieldName()));
				query.addWhereClause(buf.toString());
			}
		}
		return query;
	}
	
	protected Record readRecordRelationship(String configName, RecordConnection<?> connection, StatementLogger stmtLogger) 
	throws Exception {
		DBRecord result = null;
		if(connection != null) {
			RelationshipConfig rc = config.getRelationshipConfig(configName);
			DBStatement<DBRecord> query = getReadRelationshipQuery(rc, stmtLogger);
			if(query != null) {
				result = query.readDBRecord((DBConnection)connection);
			}
		}
		return result;
	}
	
	protected ArrayList<? extends Record> readListRelationship(String configName, RecordConnection<?> connection, StatementLogger stmtLogger) 
	throws Exception {
		ArrayList<DBRecord> result = null;
		if(connection != null) {
			RelationshipConfig rc = config.getRelationshipConfig(configName);
			DBStatement<DBRecord> query = getReadRelationshipQuery(rc, stmtLogger);
			if(query != null) {
				result = query.readDBRecords((DBConnection)connection);
			} else {
				result = new ArrayList<DBRecord>();
			}
		}
		return result;
	}
	
	public String getSQLFieldValue(String fieldName) {
		Field dbField = getField(fieldName);
		if(dbField != null) {
			return dbField.getSQLFieldValue();
		}
		throw new FieldNotConfiguredException("Record Config Name: " + getConfigName() + "; Field Name: " + fieldName);
	}
}
