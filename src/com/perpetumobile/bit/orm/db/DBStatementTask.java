package com.perpetumobile.bit.orm.db;

import java.util.ArrayList;

import com.perpetumobile.bit.util.Task;
import com.perpetumobile.bit.util.Util;

public class DBStatementTask extends Task {

	protected String dbConfigName = null;
	protected DBStatementMethod method = null;
	protected DBStatement<? extends DBRecord> stmt = null;
	protected String strSQL = null;
	protected DBRecord record = null;
	protected ArrayList<DBRecord> recordList = null;

	protected ArrayList<? extends DBRecord> resultList = null;
	protected int result = 0;

	public DBStatementTask() {
	}

	@Override
	public void runImpl() throws Exception {
		switch(method) {
		case SELECT:
			if(Util.nullOrEmptyString(strSQL)) {
				resultList = DBStatementManager.getInstance().selectImpl(dbConfigName, stmt);
			} else {
				resultList = DBStatementManager.getInstance().selectImpl(dbConfigName, stmt, strSQL);
			}
			break;
		case INSERT:
		case INSERT_IGNORE:
		case REPLACE:
			if(record != null) {
				result = DBStatementManager.getInstance().insertImpl(dbConfigName, method, stmt, record);
			} else if(!Util.nullOrEmptyList(recordList)) {
				DBStatementManager.getInstance().insertImpl(dbConfigName, method, stmt, recordList);
			}
			break;
		case UPDATE:
			if(Util.nullOrEmptyString(strSQL)) {
				result = DBStatementManager.getInstance().updateImpl(dbConfigName, stmt, record);
			} else {
				result = DBStatementManager.getInstance().executeImpl(dbConfigName, stmt, strSQL);
			}
			break;
		case DELETE:
			if(Util.nullOrEmptyString(strSQL)) {
				result = DBStatementManager.getInstance().deleteImpl(dbConfigName, stmt);
			} else {
				result = DBStatementManager.getInstance().executeImpl(dbConfigName, stmt, strSQL);
			}
			break;
		}
	}
	
	@SuppressWarnings("unchecked")
	public void executeImpl(DBConnection dbConnection) throws Exception {
		switch(method) {
		case SELECT:
			if(Util.nullOrEmptyString(strSQL)) {
				resultList = stmt.readDBRecords(dbConnection);;
			} else {
				resultList = stmt.readDBRecords(dbConnection, strSQL);
			}
			break;
		case INSERT:
			if(record != null) {
				result = ((DBStatement<DBRecord>)stmt).insertDBRecord(dbConnection, record);
			} else if(!Util.nullOrEmptyList(recordList)) {
				((DBStatement<DBRecord>)stmt).insertDBRecords(dbConnection, recordList);
			}
			break;
		case INSERT_IGNORE:
			if(record != null) {
				result = ((DBStatement<DBRecord>)stmt).insertIgnoreDBRecord(dbConnection, record);
			} else if(!Util.nullOrEmptyList(recordList)) {
				((DBStatement<DBRecord>)stmt).insertIgnoreDBRecords(dbConnection, recordList);
			}
			break;
		case REPLACE:
			if(record != null) {
				result = ((DBStatement<DBRecord>)stmt).replaceDBRecord(dbConnection, record);
			} else if(!Util.nullOrEmptyList(recordList)) {
				((DBStatement<DBRecord>)stmt).replaceDBRecords(dbConnection, recordList);
			}
			break;
		case UPDATE:
			if(Util.nullOrEmptyString(strSQL)) {
				result = ((DBStatement<DBRecord>)stmt).updateDBRecords(dbConnection, record);
			} else {
				result = ((DBStatement<DBRecord>)stmt).executeUpdate(dbConnection, strSQL);
			}
			break;
		case DELETE:
			if(Util.nullOrEmptyString(strSQL)) {
				result = stmt.deleteDBRecords(dbConnection);
			} else {
				result = ((DBStatement<DBRecord>)stmt).executeUpdate(dbConnection, strSQL);
			}
			break;
		}
	}

	public void setDBConfigName(String dbConfigName) {
		this.dbConfigName = dbConfigName;
	}

	public void setMethod(DBStatementMethod method) {
		this.method = method;
	}

	public void setStmt(DBStatement<? extends DBRecord> stmt) {
		this.stmt = stmt;
	}

	public void setSQL(String strSQL) {
		this.strSQL = strSQL;
	}

	public void setRecord(DBRecord record) {
		this.record = record;
	}

	public void setRecordList(ArrayList<DBRecord> recordList) {
		this.recordList = recordList;
	}

	public ArrayList<? extends DBRecord> getResultList() {
		return resultList;
	}

	public int getResult() {
		return result;
	}
}
