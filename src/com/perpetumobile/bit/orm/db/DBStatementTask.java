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
		if(method == DBStatementMethod.SELECT) {
			if(Util.nullOrEmptyString(strSQL)) {
				resultList = DBStatementManager.getInstance().selectImpl(dbConfigName, stmt);
			} else {
				resultList = DBStatementManager.getInstance().selectImpl(dbConfigName, stmt, strSQL);
			}
		} else if(method == DBStatementMethod.INSERT || method == DBStatementMethod.INSERT_IGNORE || method == DBStatementMethod.REPLACE) {
			if(record != null) {
				result = DBStatementManager.getInstance().insertImpl(dbConfigName, method, stmt, record);
			} else if(!Util.nullOrEmptyList(recordList)) {
				DBStatementManager.getInstance().insertImpl(dbConfigName, method, stmt, recordList);
			}
		} else if(method == DBStatementMethod.UPDATE) {
			result = DBStatementManager.getInstance().updateImpl(dbConfigName, stmt, record);
		} else if(method == DBStatementMethod.DELETE) {
			result = DBStatementManager.getInstance().deleteImpl(dbConfigName, stmt);
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
