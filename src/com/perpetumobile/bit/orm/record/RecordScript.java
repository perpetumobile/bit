package com.perpetumobile.bit.orm.record;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import com.perpetumobile.bit.orm.cassandra.CassandraCliScript;
import com.perpetumobile.bit.orm.cassandra.CassandraCliScriptReadStatement;
import com.perpetumobile.bit.orm.cassandra.CassandraConnection;
import com.perpetumobile.bit.orm.cassandra.CassandraConnectionManager;
import com.perpetumobile.bit.orm.cassandra.CassandraRecord;
import com.perpetumobile.bit.orm.cassandra.CassandraRecordConfig;
import com.perpetumobile.bit.orm.cassandra.CassandraRecordConfigFactory;
import com.perpetumobile.bit.orm.cassandra.CassandraStatement;
import com.perpetumobile.bit.orm.db.DBConnection;
import com.perpetumobile.bit.orm.db.DBConnectionManager;
import com.perpetumobile.bit.orm.db.DBRecord;
import com.perpetumobile.bit.orm.db.DBRecordConfig;
import com.perpetumobile.bit.orm.db.DBRecordConfigFactory;
import com.perpetumobile.bit.orm.db.DBScript;
import com.perpetumobile.bit.orm.db.DBScriptReadStatement;
import com.perpetumobile.bit.orm.db.DBStatement;
import com.perpetumobile.bit.orm.db.DBUtil;
import com.perpetumobile.bit.orm.record.RecordScriptData;
import com.perpetumobile.bit.orm.record.exception.RecordScriptDataException;
import com.perpetumobile.bit.util.Logger;
import com.perpetumobile.bit.util.Util;
import com.perpetumobile.bit.util.VelocityScript;


abstract public class RecordScript extends VelocityScript {
	static private Logger logger = new Logger(RecordScript.class);
	
	public RecordScript(String scriptName, String scriptToolName)
	throws Exception {
		super(scriptName, scriptToolName);
		context.put("DBUtil", new DBUtil());
	}
	
	public RecordScript(String scriptName, String scriptToolName, VelocityEngine velocity, VelocityContext context)
	throws Exception {
		super(scriptName, scriptToolName, velocity, context);
	}

	public ArrayList<RecordScriptData> readIfExists(String scriptName) 
	throws RecordScriptDataException {
		ArrayList<RecordScriptData> result = new ArrayList<RecordScriptData>();
		try {
			result = read(scriptName);
		} catch (IOException e) {
		}
		return result;
	}
	
	static public ArrayList<RecordScriptData> read(String directoryName, String fileName)
	throws IOException, RecordScriptDataException {
		ArrayList<RecordScriptData> result = new ArrayList<RecordScriptData>();
		BufferedReader in = new BufferedReader(new FileReader(new File(directoryName, fileName)));
		String[] columnNames = null;
		String line = null;
		while((line = in.readLine()) != null) {
			line = line.trim();
			if(!Util.nullOrEmptyString(line)) {
				if(columnNames == null) {
					columnNames = line.split("\t");
				} else {
					result.add(new RecordScriptData(columnNames, line.split("\t")));
				}
			}
		}
		in.close();
		return result;
	}	
	
	public ArrayList<RecordScriptData> read(String scriptName) 
	throws IOException, RecordScriptDataException {
		return read(scriptFile.getParent(), scriptName);
	}
	
	public DBRecord createDBRecord(String dbRecordConfigName) {
		DBRecord result = null;
		DBRecordConfig dbRecordConfig = DBRecordConfigFactory.getInstance().getRecordConfig(dbRecordConfigName, getContext());
		if(dbRecordConfig != null) {
			try {
				result = (DBRecord)dbRecordConfig.createRecord();
			} catch (Exception e) {
			}
		}
		return result;
	}
	
	public DBStatement<DBRecord> createDBStatement(String dbRecordConfigName) {
		return new DBStatement<DBRecord>(dbRecordConfigName, context, new StatementLoggerImpl());
	}
	
	public ArrayList<DBRecord> readDBRecords(String dbConnectionConfigName, DBStatement<DBRecord> stmt) {
		ArrayList<DBRecord> result = null;
		DBConnection dbConnection = null;
		
		try {
			dbConnection = DBConnectionManager.getInstance().getConnection(dbConnectionConfigName);
			result = stmt.readDBRecords(dbConnection);
		} catch (Exception e) {
			logger.error("Exception at RecordScript.readDBRecords", e);
			DBConnectionManager.getInstance().invalidateConnection(dbConnection);
			dbConnection = null;
			result = null;
		} finally {
			DBConnectionManager.getInstance().returnConnection(dbConnection);
		}
		return result;
	}
	
	public DBRecord readDBRecord(String dbConnectionConfigName, DBStatement<DBRecord> stmt) {
		ArrayList<DBRecord> list = readDBRecords(dbConnectionConfigName, stmt);
		return (list.size() > 0 ? list.get(0) : null);
	}
	
	public ArrayList<DBRecord> readDBRecords(String dbConnectionConfigName, String dbRecordConfigName, String dbScriptName) {
		ArrayList<DBRecord> result = null;
		DBConnection dbConnection = null;
		
		try {
			dbConnection = DBConnectionManager.getInstance().getConnection(dbConnectionConfigName);
			if(dbScriptName.startsWith("SQL:")) {
				DBStatement<DBRecord> stmt = new DBStatement<DBRecord>(dbRecordConfigName, context);
				result = stmt.readDBRecords(dbConnection, dbScriptName.substring("SQL:".length()));
			} else {
				DBScriptReadStatement<DBRecord> stmt = new DBScriptReadStatement<DBRecord>(dbScriptName, dbRecordConfigName, velocity, context);
				result = stmt.readDBRecords(dbConnection);
			}
		} catch (Exception e) {
			logger.error("Exception at RecordScript.readDBRecords", e);
			DBConnectionManager.getInstance().invalidateConnection(dbConnection);
			dbConnection = null;
			result = null;
		} finally {
			DBConnectionManager.getInstance().returnConnection(dbConnection);
		}
		return result;
	}
	
	public DBRecord readDBRecord(String dbConnectionConfigName, String dbRecordConfigName, String dbScriptName) {
		ArrayList<DBRecord> list = readDBRecords(dbConnectionConfigName, dbRecordConfigName, dbScriptName);
		return (list.size() > 0 ? list.get(0) : null);
	}
	
	public void runDBScript(String dbConnectionConfigName, String scriptName) 
	throws SQLException, Exception {
		DBConnection dbConnection = null;
		try {
			dbConnection = DBConnectionManager.getInstance().getConnection(dbConnectionConfigName);
			DBScript dbScript = new DBScript(dbConnection, scriptName, velocity, context);
			dbScript.execute();
		} catch (SQLException e) {
			logger.error("Error running script: " + scriptName);
			logger.error("SQL Exception " + e.getLocalizedMessage());
			logger.error("SQL Code " + e.getErrorCode());
			DBConnectionManager.getInstance().invalidateConnection(dbConnection);
			dbConnection = null;
			throw e;
		} finally {
			DBConnectionManager.getInstance().returnConnection(dbConnection);
		}
	}
	
	public CassandraRecord createCassandraRecord(String cassandraRecordConfigName) {
		CassandraRecord result = null;
		CassandraRecordConfig cassandraRecordConfig = CassandraRecordConfigFactory.getInstance().getRecordConfig(cassandraRecordConfigName, getContext());
		if(cassandraRecordConfig != null) {
			try {
				result = (CassandraRecord)cassandraRecordConfig.createRecord();
			} catch (Exception e) {
			}
		}
		return result;
	}
	
	public CassandraStatement<CassandraRecord> createCassandraStatement(String cassandraRecordConfigName) {
		return new CassandraStatement<CassandraRecord>(cassandraRecordConfigName, new StatementLoggerImpl());
	}
	
	public ArrayList<CassandraRecord> readCassandraRecords(String cassandraConnectionConfigName, CassandraStatement<CassandraRecord> stmt) {
		ArrayList<CassandraRecord> result = null;
		CassandraConnection cassandraConnection = null;
		try {
			cassandraConnection = CassandraConnectionManager.getInstance().getConnection(cassandraConnectionConfigName);
			result = stmt.readCassandraRecords(cassandraConnection);
		} catch (Exception e) {
			logger.error("Exception at RecordScript.readDBRecords", e);
			CassandraConnectionManager.getInstance().invalidateConnection(cassandraConnection);
			cassandraConnection = null;
			result = null;
		} finally {
			CassandraConnectionManager.getInstance().returnConnection(cassandraConnection);
		}
		return result;
	}
	
	public CassandraRecord readCassandraRecord(String cassandraConnectionConfigName, CassandraStatement<CassandraRecord> stmt) {
		ArrayList<CassandraRecord> list = readCassandraRecords(cassandraConnectionConfigName, stmt);
		return (list.size() > 0 ? list.get(0) : null);
	}
	
	public ArrayList<CassandraRecord> readCassandraRecords(String cassandraConnectionConfigName, String cassandraRecordConfigName, String cassandraScriptName) {
		ArrayList<CassandraRecord> result = null;
		CassandraConnection cassandraConnection = null;
		
		try {
			cassandraConnection = CassandraConnectionManager.getInstance().getConnection(cassandraConnectionConfigName);
			if(cassandraScriptName.startsWith("CQL:")) {
				CassandraStatement<CassandraRecord> stmt = new CassandraStatement<CassandraRecord>(cassandraRecordConfigName);
				// TODO: fix when CQL becomes available in Cassandra 0.8
				result = stmt.readCassandraRecords(cassandraConnection); //, cassandraScriptName.substring("CQL:".length()));
			} else {
				CassandraCliScriptReadStatement<CassandraRecord> stmt = new CassandraCliScriptReadStatement<CassandraRecord>(cassandraScriptName, cassandraRecordConfigName, velocity, context);
				result = stmt.readCassandraRecords(cassandraConnection);
			}
		} catch (Exception e) {
			logger.error("Exception at RecordScript.readDBRecords", e);
			CassandraConnectionManager.getInstance().invalidateConnection(cassandraConnection);
			cassandraConnection = null;
			result = null;
		} finally {
			CassandraConnectionManager.getInstance().returnConnection(cassandraConnection);
		}
		return result;
	}
	
	public CassandraRecord readCassandraRecord(String cassandraConnectionConfigName, String cassandraRecordConfigName, String cassandraScriptName) {
		ArrayList<CassandraRecord> list = readCassandraRecords(cassandraConnectionConfigName, cassandraRecordConfigName, cassandraScriptName);
		return (list.size() > 0 ? list.get(0) : null);
	}
	
	public void runCassandraScript(String cassandraConnectionConfigName, String scriptName) 
	throws SQLException, Exception {
		CassandraConnection cassandraConnection = null;
		try {
			cassandraConnection = CassandraConnectionManager.getInstance().getConnection(cassandraConnectionConfigName);
			CassandraCliScript dbScript = new CassandraCliScript(cassandraConnection, scriptName, velocity, context);
			dbScript.execute();
		} catch (Exception e) {
			logger.error("Error running script: " + scriptName);
			logger.error("Cassandra Exception " + e.getLocalizedMessage());
			logger.error("Exception at RecordScript.runCassandraScript", e);
			CassandraConnectionManager.getInstance().invalidateConnection(cassandraConnection);
			cassandraConnection = null;
			throw e;
		} finally {
			CassandraConnectionManager.getInstance().returnConnection(cassandraConnection);
		}
	}
	
	abstract public void execute() throws Exception;

}
