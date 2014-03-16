package com.perpetumobile.bit.orm.db;
	


import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import com.perpetumobile.bit.config.Config;
import com.perpetumobile.bit.orm.record.StatementLog;
import com.perpetumobile.bit.orm.record.StatementLogger;
import com.perpetumobile.bit.util.Logger;
import com.perpetumobile.bit.util.Util;
import com.perpetumobile.bit.util.VelocityScript;

/**
 * 
 * @author Zoran Dukic
 */
public class DBScriptReadStatement<T extends DBRecord> {
	static private Logger logger = new Logger(DBScriptReadStatement.class);
	
	static final public String DB_SCRIPTS_PATH_CONFIG_KEY = "DB.Scripts.Path";
	
	private String dbScriptsPath = "";
	protected VelocityScript sqlScript = null;
	
	protected String configName = null;
	protected DBRecordConfig dbRecordConfig = null;
	protected StatementLogger sqlLogger = null;
	
	public DBScriptReadStatement(String scriptName, String configName) 
	throws Exception {
		dbScriptsPath = Config.getInstance().getProperty(DB_SCRIPTS_PATH_CONFIG_KEY, "scripts/db/");
		sqlScript = new VelocityScript(dbScriptsPath+scriptName, "SQLScript");
		sqlScript.getContext().put("DBUtil", new DBUtil());
		this.configName = configName;
	}
	
	public DBScriptReadStatement(String scriptName, String configName, StatementLogger sqlLogger) 
	throws Exception {
		dbScriptsPath = Config.getInstance().getProperty(DB_SCRIPTS_PATH_CONFIG_KEY, "scripts/db/");
		sqlScript = new VelocityScript(dbScriptsPath+scriptName, "SQLScript");
		sqlScript.getContext().put("DBUtil", new DBUtil());
		this.configName = configName;
		this.sqlLogger = sqlLogger;
	}
	
	public DBScriptReadStatement(String scriptName, String configName, VelocityEngine velocity, VelocityContext context) 
	throws Exception {
		dbScriptsPath = Config.getInstance().getProperty(DB_SCRIPTS_PATH_CONFIG_KEY, "scripts/db/");
		sqlScript = new VelocityScript(scriptName, "SQLScript", velocity, context);
		sqlScript.getContext().put("DBUtil", new DBUtil());
		this.configName = configName;
	}
	
	public DBScriptReadStatement(String scriptName, String configName, VelocityEngine velocity, VelocityContext context, StatementLogger sqlLogger) 
	throws Exception {
		dbScriptsPath = Config.getInstance().getProperty(DB_SCRIPTS_PATH_CONFIG_KEY, "scripts/db/");
		sqlScript = new VelocityScript(scriptName, "SQLScript", velocity, context);
		sqlScript.getContext().put("DBUtil", new DBUtil());
		this.configName = configName;
		this.sqlLogger = sqlLogger;
	}
	
	@SuppressWarnings("unchecked")
	public T createDBRecord() throws Exception {
		return (dbRecordConfig != null ? (T)dbRecordConfig.createRecord() : null);
	}
	
	public VelocityEngine getVelocityEngine() {
		if(sqlScript != null) {
			return sqlScript.getVelocityEngine();
		}
		return null;
	}
	
	public VelocityContext getContext() {
		if(sqlScript != null) {
			return sqlScript.getContext();
		}
		return null;
	}
			
	public ArrayList<T> readDBRecords(DBConnection dbConnection)
	throws Exception {
		ArrayList<T> result = new ArrayList<T>();
		
		Statement stmt = null;
		ResultSet rs = null;
		try {
			String allSQL = sqlScript.generate();
			
			String delimiter = (String)getContext().get("StatementDelimiter");
			if(Util.nullOrEmptyString(delimiter)) {
				delimiter = ";";
			}
			
			String[] tmp = allSQL.split(delimiter);
			
			// need to remove empty statements
			// execute all but last statements
			// read last statement
			ArrayList<String> sqls = new ArrayList<String>();
			for(String s : tmp) {
				s = s.trim();
				if (!Util.nullOrEmptyString(s)) {
					sqls.add(s);
				}	
			}

			String sql = null;
			int sqlLogIndex = 0; 
				
			stmt = dbConnection.getConnection().createStatement();
			
			for (int i=0; i<sqls.size()-1; i++) {
				sql = sqls.get(i); 
				sqlLogIndex = startSQL(sql);
				if(logger.isInfoEnabled()) {
					System.out.print(sql);
					System.out.println(";");
					System.out.println();
				}
				stmt.execute(sql);
				endSQL(sqlLogIndex);
			}
			
			sql = sqls.get(sqls.size()-1);
			sqlLogIndex = startSQL(sql);
			if(logger.isInfoEnabled()) {
				System.out.print(sql);
				System.out.println(";");
				System.out.println();
			}
			rs = stmt.executeQuery(sql);
			
			dbRecordConfig = DBRecordConfigFactory.getInstance().getRecordConfig(configName, sqlScript.getContext());
			
			while(rs.next()) {
				T record = createDBRecord();
				record.readRecord(dbConnection, rs, 1);
				result.add(record);
			}
			endSQL(sqlLogIndex);
		} finally {
			DBUtil.close(rs);
			DBUtil.close(stmt);
		}
		return result;
	}
		
	public T readDBRecord(DBConnection dbConnection)
	throws Exception {
		ArrayList<T> list = readDBRecords(dbConnection);
		return (list.size() > 0 ? list.get(0) : null);
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
}
