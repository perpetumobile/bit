package com.perpetumobile.bit.orm.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import com.perpetumobile.bit.util.Logger;
import com.perpetumobile.bit.util.Util;


/**
 * @author Zoran Dukic
 * 
 */
public class DBUtil {
	static private Logger logger = new Logger(DBUtil.class);
	
	static final String SQL_STRING_NULL = "null";
	/**	
	 * Replaces all occurances of single quotes to double quotes
	 * and back slash to double back slash. 
	 * eg. "this is alex's book" becomes "this is alex''s book"
	 *
	 * @param sql string to be fixed
	 */
	public static String encodeSQLString(String sql) {
		if (sql == null) {
			return SQL_STRING_NULL;
		}

		sql = Util.replaceAll(sql, "\\", "\\\\");
		sql = Util.replaceAll(sql, "'", "''");

		StringBuffer buf = new StringBuffer();
		buf.append('\'');
		buf.append(sql);
		buf.append('\'');
		return buf.toString();
	}
	
	public static String encodeSQLString(String sql, int limit) {
		if(sql.length() > limit) {
			return encodeSQLString(sql.substring(0, limit));
		}		
		return encodeSQLString(sql);
	}

	public static void close(ResultSet rs) {
		if (rs != null) {	
			try {
				rs.close();
			} catch (Exception e) {
			}
		}
	}

	public static void close(Connection connection) {
		if (connection != null) {
			try {
				connection.close();
			} catch (Exception e) {
			}
		}
	}

	public static void close(Statement stmt) {
		if (stmt != null) {
			try {
				stmt.close();
			} catch (Exception e) {
			}
		}
	}
	
	static public String getMD5(DBConnection dbConnection, String str) 
	throws SQLException {
		StringBuffer buf = new StringBuffer("SELECT MD5(");
		buf.append(encodeSQLString(str));
		buf.append(")");
		String strSQL = buf.toString();
		
		String result = "";
		Statement stmt = null;
		ResultSet rs = null;
		try {
			stmt = dbConnection.getConnection().createStatement();
			rs = stmt.executeQuery(strSQL);
			if(rs.next()) {
				result = rs.getString(1);
			}
		} finally {
			close(rs);
			close(stmt);
		}		
		return result;
	}
	
	static public ArrayList<DBRecord> readDBRecords(String dbConnectionConfigName, String dbRecordConfigName, String dbScriptName) {
		ArrayList<DBRecord> result = null;
		DBConnection dbConnection = null;
		
		try {
			dbConnection = DBConnectionManager.getInstance().getConnection(dbConnectionConfigName);
			if(dbScriptName.startsWith("SQL:")) {
				DBStatement<DBRecord> stmt = new DBStatement<DBRecord>(dbRecordConfigName);
				result = stmt.readDBRecords(dbConnection, dbScriptName.substring("SQL:".length()));
			} else {
				DBScriptReadStatement<DBRecord> stmt = new DBScriptReadStatement<DBRecord>(dbScriptName, dbRecordConfigName);
				result = stmt.readDBRecords(dbConnection);
			}
		} catch (Exception e) {
			logger.error("Exception at DBUtil.readDBRecords", e);
			DBConnectionManager.getInstance().invalidateConnection(dbConnection);
			dbConnection = null;
			result = null;
		} finally {
			DBConnectionManager.getInstance().returnConnection(dbConnection);
		}
		return result;
	}
}
