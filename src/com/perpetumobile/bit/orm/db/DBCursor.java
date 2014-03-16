package com.perpetumobile.bit.orm.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author Zoran Dukic
 *
 */
public class DBCursor<T extends DBRecord> {

	protected DBStatement<T> dbStatement = null;
	protected int fetchSize = 0;
	
	private boolean isFirstRead = false;
	
	private Statement stmt = null;
	private ResultSet rs = null;
	private T nextRecord = null;
	
	private int logId = -1;
	
	public DBCursor(DBStatement<T> dbStmt, int fetchSize) {
		this.dbStatement = dbStmt;
		this.fetchSize = fetchSize;
	}
	
	private T readDBRecord(DBConnection dbConnection)
	throws SQLException, Exception {
		T dbRecord = null;
		if(dbStatement != null) {
			T[] result = dbStatement.streamDBRecord(dbConnection, rs, nextRecord);
			if(result != null) {
				dbRecord = result[0];
				nextRecord = result[1];
			}
		}
		return dbRecord;
	}
	
	protected T readFirst(DBConnection dbConnection) throws SQLException, Exception {
		isFirstRead = true;
		if(dbStatement != null) {
			stmt = dbConnection.getConnection().createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY, java.sql.ResultSet.CONCUR_READ_ONLY);
			stmt.setFetchSize(Integer.MIN_VALUE);
			String strSQL = dbStatement.generateSelectSQL();
			logId = dbStatement.startSQL(strSQL);
			rs = stmt.executeQuery(strSQL);
			return readNext(dbConnection);
		}
		return null;
	}
	
	/**
	 * Reads next result. Returns null if there is no next result.
	 */
	public T readNext(DBConnection dbConnection) throws SQLException, Exception {
		if(!isFirstRead) {
			return readFirst(dbConnection);
		}
		
		if (stmt == null || rs == null) {
			return null;
		}
			
		T record = null;
		if(rs.next()) {
			record = readDBRecord(dbConnection);
		}
		return record;
	}
	
	/**
	 * Releses the resources. Must be called after the use.
	 */
	public void close() {
		dbStatement.endSQL(logId);
		DBUtil.close(rs);
		DBUtil.close(stmt);
	}	
}
