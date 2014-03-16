package com.perpetumobile.bit.orm.db;
	

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import org.apache.velocity.VelocityContext;

import com.perpetumobile.bit.orm.record.StatementLog;
import com.perpetumobile.bit.orm.record.StatementLogger;
import com.perpetumobile.bit.orm.record.exception.RecordConfigMismatchException;
import com.perpetumobile.bit.orm.record.field.Field;
import com.perpetumobile.bit.util.Util;

/**
 * 
 * @author Zoran Dukic
 */
public class DBStatement<T extends DBRecord> {	
	protected DBRecordConfig dbRecordConfig = null;
	
	protected boolean distinct = false;
	protected String tableName = null;
	protected ArrayList<DBRecordConfig> joinList = new ArrayList<DBRecordConfig>();
	protected String fromClause = null;
	protected StringBuffer where = new StringBuffer();
	protected StringBuffer orderBy = new StringBuffer();
	protected String groupBy = null;
	protected int offset = 0;
	protected int limit = 0;
	
	protected boolean hasAutoIncrement = false;
	
	protected StatementLogger sqlLogger = null;
	
	public DBStatement(String configName) {
		dbRecordConfig = DBRecordConfigFactory.getInstance().getRecordConfig(configName);
	}
	
	public DBStatement(String configName, StatementLogger sqlLogger) {
		dbRecordConfig = DBRecordConfigFactory.getInstance().getRecordConfig(configName);
		this.sqlLogger = sqlLogger;
	}
	
	public DBStatement(String configName, VelocityContext context) {
		dbRecordConfig = DBRecordConfigFactory.getInstance().getRecordConfig(configName, context);
	}
	
	public DBStatement(String configName, VelocityContext context, StatementLogger sqlLogger) {
		dbRecordConfig = DBRecordConfigFactory.getInstance().getRecordConfig(configName, context);
		this.sqlLogger = sqlLogger;
	}
	
	public void reset() {
		distinct = false;
		tableName = null;
		joinList = new ArrayList<DBRecordConfig>();
		fromClause = null;
		where = new StringBuffer();
		orderBy = new StringBuffer();
		groupBy = null;
		offset = 0;
		limit = 0;
		hasAutoIncrement = false;
	}
	
	@SuppressWarnings("unchecked")
	public T createDBRecord() throws Exception {
		return (dbRecordConfig != null ? (T)dbRecordConfig.createRecord() : null);
	}
	
	public String getConfigName() {
		return (dbRecordConfig != null ? dbRecordConfig.getConfigName() : null);
	}
	
	public String getTableName() {
		if(!Util.nullOrEmptyString(tableName)) {
			return tableName;
		}
		return (dbRecordConfig != null ? dbRecordConfig.getTableName() : null);
	}
	
	public String getFromClause() {
		if(!Util.nullOrEmptyString(fromClause)) {
			return fromClause;
		}
		return (dbRecordConfig != null ? dbRecordConfig.getFromClause(tableName) : null);
	}
	
	public String getSelectList() {
		return (dbRecordConfig != null ? dbRecordConfig.getSelectList(tableName) : null);
	}
	
	public void setDistinct(boolean distinct) {
		this.distinct = distinct;
	}
	
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	
	public void addJoin(String configName, String joinClause) {
		DBRecordConfig config = DBRecordConfigFactory.getInstance().getRecordConfig(configName);
		if(config != null) {
			joinList.add(config);
			StringBuffer buf = new StringBuffer(getFromClause());
			buf.append(" ");
			buf.append(joinClause);
			setFromClause(buf.toString());
		}
	}
	
	public void setFromClause(String fromClause) {
		this.fromClause = fromClause;
	}
	
	public void addWhereClause(String whereClause) {
		if(!Util.nullOrEmptyString(whereClause)) {
			if(where.length() != 0) {
				where.append(" AND ");
			}
			where.append(whereClause);	
		}
	}
	
	public void addOrderByClause(String orderByClause) {
		if(!Util.nullOrEmptyString(orderByClause)) {
			if(orderBy.length() != 0) {
				orderBy.append(", ");
			}
			orderBy.append(orderByClause);	
		}
	}
	
	public void setGroupBy(String groupBy) {
		this.groupBy = groupBy;
	}
	
	public void setLimit(int limit) {
		this.limit = limit;
	}
	
	public void setOffset(int offset) {
		this.offset = offset;
	}
	
	protected String generateSelectSQL(boolean isCount) {
		StringBuffer sql = new StringBuffer();
		if(isCount) {
			sql.append("SELECT COUNT(*) FROM (SELECT ");
		} else {
			sql.append("SELECT ");
		}
		if(distinct) {
			sql.append("DISTINCT ");
		}
		sql.append(getSelectList());
		if(joinList.size() > 0) {
			for(DBRecordConfig config : joinList) {
				sql.append(", ");
				sql.append(config.getSelectList());
			}
		}
		sql.append(" FROM ");
		sql.append(getFromClause());
		/* addJoin method is handling from clause in other to support left join
		if(joinList.size() > 0) {
			for(DBRecordConfig config : joinList) {
				sql.append(", ");
				sql.append(config.getFromClause());
			}
		}
		*/ 
		StringBuffer whereClause = new StringBuffer();
		if(dbRecordConfig.getWhereClause().length() > 0) {
			whereClause.append(dbRecordConfig.getWhereClause());
		}
		if(where.length() > 0) {
			if(whereClause.length() > 0) {
				whereClause.append(" AND ");
			}
			whereClause.append(where);
		}
		if(whereClause.length() > 0) {
			sql.append(" WHERE ");
			sql.append(whereClause);
		}
		if(isCount) {
			sql.append(") AS cnt_table");
		} else {
			if(!Util.nullOrEmptyString(groupBy)) {
				sql.append(" GROUP BY ");
				sql.append(groupBy);
			}
			StringBuffer orderByClause = new StringBuffer();
			if(dbRecordConfig.getOrderByClause().length() > 0) {
				orderByClause.append(dbRecordConfig.getOrderByClause());
			}
			if(orderBy.length() > 0) {
				if(orderByClause.length() > 0) {
					orderByClause.append(", ");
				}
				orderByClause.append(orderBy);
			}
			if(orderByClause.length() > 0) {
				sql.append(" ORDER BY ");
				sql.append(orderByClause);
			}
			if(offset > 0 || limit > 0) {
				sql.append(" LIMIT ");
				sql.append(offset);
				sql.append(", ");
				sql.append(limit);
			}
		}
		return sql.toString();
	}
	
	protected String generateSelectSQL() {
		return generateSelectSQL(false);
	}
	
	protected String generateSelectCountSQL() {
		return generateSelectSQL(true);
	}
	
	protected enum InsertFieldOption {
		name,
		value,
		param;
	};

	private String generateInsertSQLFields(T dbRecord, InsertFieldOption insertFieldOption) {
		StringBuffer result = new StringBuffer();
		
		result.append("(");
		boolean first = true;
		ArrayList<Field> fields = dbRecord.getFields();
		for(int i=0; i<fields.size(); i++) {
			Field dbField = fields.get(i);
			if(dbField.isSet(getTableName())) {
				if(!first) {
					result.append(", ");
				}
				if(insertFieldOption == InsertFieldOption.value) {
					result.append(dbField.getSQLFieldValue());
				} else if (insertFieldOption == InsertFieldOption.param){
					result.append("?");
				} else {
					result.append(dbField.getFieldName());
				}
				first = false;
			}
			if(dbField.isAutoIncrement()) {
				hasAutoIncrement = true;
			}
		}
		result.append(")");
		return result.toString();
	}
	
	protected String generateInsertSQL(String cmd, T dbRecord) {
		if(!getConfigName().equals(dbRecord.getConfigName())) {
			throw new RecordConfigMismatchException();
		}
		
		StringBuffer sql = new StringBuffer(cmd);
		sql.append(getTableName());
		sql.append(" ");
		sql.append(generateInsertSQLFields(dbRecord, InsertFieldOption.name));
		sql.append(" VALUES ");
		sql.append(generateInsertSQLFields(dbRecord, InsertFieldOption.value));
		
		return sql.toString();
	}
	
	protected String generateInsertSQL(String cmd, ArrayList<T> dbRecords) {
		if(dbRecords == null || dbRecords.size() == 0) {
			return null;
		}
		
		T firstRec = dbRecords.get(0);
		if(!getConfigName().equals(firstRec.getConfigName())) {
			throw new RecordConfigMismatchException();
		}
		
		StringBuffer sql = new StringBuffer(cmd);
		sql.append(getTableName());
		sql.append(" ");
		sql.append(generateInsertSQLFields(firstRec, InsertFieldOption.name));
		sql.append(" VALUES ");
		boolean first = true;
		for(T rec : dbRecords) {
			if(!first) {
				sql.append(", ");
			}
			sql.append(generateInsertSQLFields(rec, InsertFieldOption.value));
			first = false;
		}
		
		return sql.toString();
	}
	
	protected String generateInsertPreparedSQL(String cmd, T dbRecord) {
		if(!getConfigName().equals(dbRecord.getConfigName())) {
			throw new RecordConfigMismatchException();
		}
	
		StringBuffer sql = new StringBuffer(cmd);
		sql.append(getTableName());
		sql.append(" ");
		sql.append(generateInsertSQLFields(dbRecord, InsertFieldOption.name));
		sql.append(" VALUES ");
		sql.append(generateInsertSQLFields(dbRecord, InsertFieldOption.param));
		
		return sql.toString();
	}
	
	protected void addDBRecordsToPreparedStatement(PreparedStatement stmt, ArrayList<T> dbRecords)
	throws SQLException {
		if(dbRecords != null) {
			for(T rec : dbRecords) {
				// stmt.clearParameters();
				ArrayList<Field> fields = rec.getFields();
				int index = 1;
				for(Field dbField : fields) {
					if(!dbField.isAutoIncrement() && dbField.isSet(getTableName())) {
						index = dbField.setPreparedStatementParameter(stmt, index);
					} 
				}
				stmt.addBatch();
			}
		}
	}
	
	protected void insertDBRecordsUsingPreparedStatement(DBConnection dbConnection, String cmd, ArrayList<T> dbRecords)
	throws SQLException {
		if(dbRecords != null && dbRecords.size() > 0) {
			PreparedStatement stmt = null;
			String strSQL = generateInsertPreparedSQL(cmd, dbRecords.get(0));
			try {
				stmt = dbConnection.getConnection().prepareStatement(strSQL);
				stmt.setEscapeProcessing(false);
				addDBRecordsToPreparedStatement(stmt, dbRecords);
				stmt.executeBatch();
			} finally {
				DBUtil.close(stmt);
			}
		}
	}
	
	protected String generateUpdateSQL(T dbRecord) {
		if(!getConfigName().equals(dbRecord.getConfigName())) {
			throw new RecordConfigMismatchException();
		}
		
		StringBuffer sql = new StringBuffer("UPDATE ");
		sql.append(getTableName());
		
		boolean first = true;
		ArrayList<Field> fields = dbRecord.getFields();
		for(int i=0; i<fields.size(); i++) {
			Field dbField = fields.get(i);
			if(dbField.isSet(getTableName())) {
				if(first) {
					sql.append(" SET ");
				} else {
					sql.append(", ");
				}
				sql.append(dbField.getFieldName());
				sql.append("=");
				sql.append(dbField.getSQLFieldValue());
				first = false;
			}
		}
		
		if(where.length() != 0) {
			sql.append(" WHERE ");
			sql.append(where);
		}
		if(limit > 0) {
			sql.append(" LIMIT ");
			sql.append(limit);
		}
		return sql.toString();
	}
	
	protected String generateDeleteSQL() {
		StringBuffer sql = new StringBuffer("DELETE FROM ");
		sql.append(getTableName());
		if(where.length() != 0) {
			sql.append(" WHERE ");
			sql.append(where);
		}
		if(limit > 0) {
			sql.append(" LIMIT ");
			sql.append(limit);
		}
		return sql.toString();
	}
	
	public boolean equals(T rec1, T rec2) {
		return rec1.equals(rec2);
	}
	
	private void readJoinedRecords(DBConnection dbConnection, ResultSet rs, T record, int index)
	throws SQLException, Exception {
		for(DBRecordConfig config : joinList) {
			DBRecord r = (DBRecord)config.createRecord();
			index = r.readRecord(dbConnection, rs, index);
			record.addRelationshipRecord(config.getConfigName(), r);
		}
	}
	
	@SuppressWarnings("unchecked")
	public T[] streamDBRecord(DBConnection dbConnection, ResultSet rs, T nextRecord)
	throws SQLException, Exception {
		DBRecord[] result = new DBRecord[2]; 
		result[0] = nextRecord;
		if(result[0] == null) {
			result[0] = createDBRecord();
		}
		int index = result[0].readRecord(dbConnection, rs, 1);
		if(joinList.size() > 0) {
			readJoinedRecords(dbConnection, rs, (T)result[0], index);
			while(rs.next()) {
				index = 1;
				result[1] = createDBRecord();
				index = result[1].readRecord(dbConnection, rs, index);
				if(equals((T)result[0], (T)result[1])) {
					readJoinedRecords(dbConnection, rs, (T)result[0], index);
				} else {
					readJoinedRecords(dbConnection, rs, (T)result[1], index);
					break;
				}
			}
		}
		return (T[])result;
	}
	
	public T readDBRecord(DBConnection dbConnection, ResultSet rs)
	throws SQLException, Exception {
		T result = createDBRecord();
		int index = result.readRecord(dbConnection, rs, 1);
		if(joinList.size() > 0) {
			readJoinedRecords(dbConnection, rs, result, index);
			while(rs.next()) {
				index = 1;
				T record = createDBRecord();
				index = record.readRecord(dbConnection, rs, index);
				if(equals(result, record)) {
					readJoinedRecords(dbConnection, rs, result, index);
				} else {
					rs.previous();
					break;
				}
			}
		}
		return result;
	}
	
	public ArrayList<T> readDBRecords(DBConnection dbConnection, String strSQL)
	throws SQLException, Exception {
		ArrayList<T> result = new ArrayList<T>();
		
		Statement stmt = null;
		ResultSet rs = null;
		int sqlLogIndex = startSQL(strSQL);
		try {
			stmt = dbConnection.getConnection().createStatement();
			rs = stmt.executeQuery(strSQL);
			while(rs.next()) {
				T record = readDBRecord(dbConnection, rs);
				result.add(record);
			}
			endSQL(sqlLogIndex);
		} catch (Exception e) {
			logErrorMsg(sqlLogIndex, e.getMessage());
			throw e;
		} finally {
			DBUtil.close(rs);
			DBUtil.close(stmt);
		}
		return result;
	}
	
	public ArrayList<T> readDBRecords(DBConnection dbConnection)
	throws SQLException, Exception {
		return readDBRecords(dbConnection, generateSelectSQL());
	}
	
	public int readCount(DBConnection dbConnection)
	throws SQLException {
		int result = -1;
		Statement stmt = null;
		ResultSet rs = null;
		String strSQL = generateSelectCountSQL();
		int sqlLogIndex = startSQL(strSQL);
		try {
			stmt = dbConnection.getConnection().createStatement();
			rs = stmt.executeQuery(strSQL);
			while(rs.next()) {
				result = rs.getInt(1);
			}
			endSQL(sqlLogIndex);
		} catch (SQLException e) {
			logErrorMsg(sqlLogIndex, e.getMessage());
			throw e;	
		} finally {
			DBUtil.close(rs);
			DBUtil.close(stmt);
		}
		return result;
	}
	
	public T readDBRecord(DBConnection dbConnection)
	throws SQLException, Exception {
		ArrayList<T> list = readDBRecords(dbConnection);
		return (list.size() > 0 ? list.get(0) : null);
	}
	
	public int executeUpdate(DBConnection dbConnection, String strSQL)
	throws SQLException {
		if(strSQL == null || strSQL.equals("")) {
			return 0;
		}
		
		int result = 0;
		Statement stmt = null;
		int sqlLogIndex = startSQL(strSQL);
		try {
			stmt = dbConnection.getConnection().createStatement();
			stmt.setEscapeProcessing(false);
			result = stmt.executeUpdate(strSQL);
			endSQL(sqlLogIndex);
		} catch (SQLException e) {
			logErrorMsg(sqlLogIndex, e.getMessage());
			throw e;		
		} finally {
			DBUtil.close(stmt);
		}
		return result;
	}
	
	public int lastInsertedId(DBConnection dbConnection)
	throws SQLException {
		int result = 0;
		Statement stmt = null;
		ResultSet rs = null;
		String strSQL = "SELECT LAST_INSERT_ID()";
		int sqlLogIndex = startSQL(strSQL);
		try {
			stmt = dbConnection.getConnection().createStatement();
			rs = stmt.executeQuery(strSQL);
			if(rs.next()) {
				result = rs.getInt(1);
			}
			endSQL(sqlLogIndex);
		} catch (SQLException e) {
			logErrorMsg(sqlLogIndex, e.getMessage());
			throw e;			
		} finally {
			DBUtil.close(rs);
			DBUtil.close(stmt);
		}
		return result;
	}
	
	public int insertDBRecord(DBConnection dbConnection, T dbRecord)
	throws SQLException {
		executeUpdate(dbConnection, generateInsertSQL("INSERT INTO ", dbRecord));
		if(hasAutoIncrement) {
			return lastInsertedId(dbConnection);
		}
		return 0;
	}
	
	public int insertIgnoreDBRecord(DBConnection dbConnection, T dbRecord)
	throws SQLException {
		executeUpdate(dbConnection, generateInsertSQL("INSERT IGNORE INTO ", dbRecord));
		if(hasAutoIncrement) {
			return lastInsertedId(dbConnection);
		}
		return 0;
	}
	
	public int replaceDBRecord(DBConnection dbConnection, T dbRecord)
	throws SQLException {
		executeUpdate(dbConnection, generateInsertSQL("REPLACE INTO ", dbRecord));
		if(hasAutoIncrement) {
			return lastInsertedId(dbConnection);
		}
		return 0;
	}
	
	public int insertDBRecords(DBConnection dbConnection, ArrayList<T> dbRecords)
	throws SQLException {
		executeUpdate(dbConnection, generateInsertSQL("INSERT INTO ", dbRecords));
		if(hasAutoIncrement) {
			return lastInsertedId(dbConnection);
		}
		return 0;
	}
	
	// lastInsertedId is not always meaningful with insert ignore
	public void insertIgnoreDBRecords(DBConnection dbConnection, ArrayList<T> dbRecords)
	throws SQLException {
		executeUpdate(dbConnection, generateInsertSQL("INSERT IGNORE INTO ", dbRecords)); 
	}
	
	public void replaceDBRecords(DBConnection dbConnection, ArrayList<T> dbRecords)
	throws SQLException {
		executeUpdate(dbConnection, generateInsertSQL("REPLACE INTO ", dbRecords));
	}
	
	public void insertDBRecords(DBConnection dbConnection, ArrayList<T> dbRecords, int batchSize)
	throws SQLException {
		ArrayList<T> batch = new ArrayList<T>();
		for(T r : dbRecords) {
			batch.add(r);
			if(batch.size() >= batchSize) {
				insertDBRecords(dbConnection, batch);
				batch.clear();
			}
		}
		if(batch.size() > 0) {
			insertDBRecords(dbConnection, batch);
			batch.clear();
		}
	}
	
	public void insertIgnoreDBRecords(DBConnection dbConnection, ArrayList<T> dbRecords, int batchSize)
	throws SQLException {
		ArrayList<T> batch = new ArrayList<T>();
		for(T r : dbRecords) {
			batch.add(r);
			if(batch.size() >= batchSize) {
				insertIgnoreDBRecords(dbConnection, batch);
				batch.clear();
			}
		}
		if(batch.size() > 0) {
			insertIgnoreDBRecords(dbConnection, batch);
			batch.clear();
		}
	}
	
	public void replaceDBRecords(DBConnection dbConnection, ArrayList<T> dbRecords, int batchSize)
	throws SQLException {
		ArrayList<T> batch = new ArrayList<T>();
		for(T r : dbRecords) {
			batch.add(r);
			if(batch.size() >= batchSize) {
				replaceDBRecords(dbConnection, batch);
				batch.clear();
			}
		}
		if(batch.size() > 0) {
			replaceDBRecords(dbConnection, batch);
			batch.clear();
		}
	}
	
	public int updateDBRecords(DBConnection dbConnection, T dbRecord)
	throws SQLException {
		return executeUpdate(dbConnection, generateUpdateSQL(dbRecord));
	}
	
	public int deleteDBRecords(DBConnection dbConnection)
	throws SQLException {
		return executeUpdate(dbConnection, generateDeleteSQL());
	}
	
	public int startSQL(String sql) {
		StatementLog sqlLog = new StatementLog(sql);
		if (sqlLogger != null) {
			return sqlLogger.startStatement(sqlLog);
		}
		return -1;
	}

	public void endSQL(int index) {
		if (sqlLogger != null) {
			sqlLogger.endStatement(index);
		}
	}
	
	public void logErrorMsg(int index, String errorMsg) {
		if (sqlLogger != null) {
			sqlLogger.setErrorMsg(index, errorMsg);
		}
	}
}
