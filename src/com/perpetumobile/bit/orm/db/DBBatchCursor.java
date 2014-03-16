package com.perpetumobile.bit.orm.db;

import java.sql.SQLException;
import java.util.ArrayList;

/**
 * @author Zoran Dukic
 *
 */
public class DBBatchCursor<T extends DBRecord> extends DBCursor<T> {
	
	private int offset = 0;
	
	private ArrayList<T> results = null;
	private int position = 0;
	
	public DBBatchCursor(DBStatement<T> dbStmt, int fetchSize) {
		super(dbStmt, fetchSize);
		dbStatement.setLimit(fetchSize);
	}
	
	private T getResult() {
		if(results != null && results.size() > position) {
			return results.get(position++);
		}
		return null;
	}
	
	protected T readFirst(DBConnection dbConnection) throws SQLException, Exception {
		results = dbStatement.readDBRecords(dbConnection);
		position = 0;
		return getResult();
	}
	
	/**
	 * Reads next result. Returns null if there is no next result.
	 */
	public T readNext(DBConnection dbConnection) throws SQLException, Exception {
		if(results == null) {
			return readFirst(dbConnection);
		}
		
		T result = getResult();
		if(result == null) {
			offset += fetchSize;
			dbStatement.setOffset(offset);	
			result = readFirst(dbConnection);
		}
		return result;
	}
	
	/**
	 * Releses the resources. Must be called after the use.
	 */
	public void close() {
	}
}
