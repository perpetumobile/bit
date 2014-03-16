package com.perpetumobile.bit.orm.cassandra;
	

import java.util.ArrayList;
import java.util.List;

// import org.apache.cassandra.cli.CliMain;
import org.apache.cassandra.thrift.KeySlice;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import com.perpetumobile.bit.config.Config;
import com.perpetumobile.bit.orm.record.StatementLog;
import com.perpetumobile.bit.orm.record.StatementLogger;
import com.perpetumobile.bit.util.Util;
import com.perpetumobile.bit.util.VelocityScript;

/**
 * 
 * @author Zoran Dukic
 */
public class CassandraCliScriptReadStatement<T extends CassandraRecord> {
	static final public String CASSANDRA_SCRIPTS_PATH_CONFIG_KEY = "Cassandra.Scripts.Path";
	
	private String cassandraScriptsPath = "";
	protected VelocityScript cassandraScript = null;
	
	protected String configName = null;
	protected CassandraRecordConfig cassandraRecordConfig = null;
	protected StatementLogger stmtLogger = null;
	
	public CassandraCliScriptReadStatement(String scriptName, String configName) 
	throws Exception {
		cassandraScriptsPath = Config.getInstance().getProperty(CASSANDRA_SCRIPTS_PATH_CONFIG_KEY, "scripts/cassandra/");
		cassandraScript = new VelocityScript(cassandraScriptsPath+scriptName, "CQLScript");
		this.configName = configName;
	}
	
	public CassandraCliScriptReadStatement(String scriptName, String configName, StatementLogger stmtLogger) 
	throws Exception {
		cassandraScriptsPath = Config.getInstance().getProperty(CASSANDRA_SCRIPTS_PATH_CONFIG_KEY, "scripts/cassandra/");
		cassandraScript = new VelocityScript(cassandraScriptsPath+scriptName, "CQLScript");
		this.configName = configName;
		this.stmtLogger = stmtLogger;
	}
	
	public CassandraCliScriptReadStatement(String scriptName, String configName, VelocityEngine velocity, VelocityContext context) 
	throws Exception {
		cassandraScriptsPath = Config.getInstance().getProperty(CASSANDRA_SCRIPTS_PATH_CONFIG_KEY, "scripts/cassandra/");
		cassandraScript = new VelocityScript(scriptName, "CQLScript", velocity, context);
		this.configName = configName;
	}
	
	public CassandraCliScriptReadStatement(String scriptName, String configName, VelocityEngine velocity, VelocityContext context, StatementLogger stmtLogger) 
	throws Exception {
		cassandraScriptsPath = Config.getInstance().getProperty(CASSANDRA_SCRIPTS_PATH_CONFIG_KEY, "scripts/cassandra/");
		cassandraScript = new VelocityScript(scriptName, "CQLScript", velocity, context);
		this.configName = configName;
		this.stmtLogger = stmtLogger;
	}
	
	@SuppressWarnings("unchecked")
	public T createCassandraRecord() throws Exception {
		return (cassandraRecordConfig != null ? (T)cassandraRecordConfig.createRecord() : null);
	}
	
	public VelocityContext getContext() {
		if(cassandraScript != null) {
			return cassandraScript.getContext();
		}
		return null;
	}
			
	public ArrayList<T> readCassandraRecords(CassandraConnection cassandraConnection)
	throws Exception {
		ArrayList<T> result = new ArrayList<T>();
	
		try {
			String all = cassandraScript.generate();
			
			String delimiter = (String)getContext().get("StatementDelimiter");
			if(Util.nullOrEmptyString(delimiter)) {
				delimiter = ";";
			}
			
			String[] tmp = all.split(delimiter);
			
			// need to remove empty statements
			// execute all but last statements
			// read last statement
			ArrayList<String> statements = new ArrayList<String>();
			for(String s : tmp) {
				s = s.trim();
				if (!Util.nullOrEmptyString(s)) {
					statements.add(s);
				}	
			}

			// CliMain.sessionState = cassandraConnection.getCliSessionState();
			// CliMain.connect(cassandraConnection.host, cassandraConnection.port);
	
			CliClient cli = new CliClient(cassandraConnection.getCliSessionState(), cassandraConnection.getConnection());
			cli.setKeySpace(cassandraConnection.getKeyspace());
			
			String stmt = null;
			int stmtLogIndex = 0; 
			
			for (int i=0; i<statements.size()-1; i++) {
				stmt = statements.get(i); 
				stmtLogIndex = startStatement(stmt);
				cli.executeCLIStatement(stmt);
				endStatement(stmtLogIndex);
			}
			
			stmt = statements.get(statements.size()-1);
			stmtLogIndex = startStatement(stmt);
			// CliMain.processStatement(stmt);
			List<KeySlice> list = cli.executeCLIStatement(stmt);
			
			CassandraKey superColumn = null;
			String str = (String)getContext().get("ReadStatementSuperColumn");
			if(!Util.nullOrEmptyString(str)) {
				superColumn = CassandraKey.create(str);
			}
			cassandraRecordConfig = CassandraRecordConfigFactory.getInstance().getRecordConfig(configName, cassandraScript.getContext());
			if(list != null) {
				for(KeySlice rs : list) {
					T record = createCassandraRecord();
					record.readRecord(cassandraConnection, superColumn, rs, 1);
					result.add(record);
				}
			}
			endStatement(stmtLogIndex);
		} finally {
			CliMain.disconnect();
		}
		return result;
	}
		
	public T readCassandraRecord(CassandraConnection CassandraConnection)
	throws Exception {
		ArrayList<T> list = readCassandraRecords(CassandraConnection);
		return (list.size() > 0 ? list.get(0) : null);
	}
	
		
	public int startStatement(String stmt) {
		StatementLog stmtLog = new StatementLog(stmt);
		if (stmtLogger != null) {
			return stmtLogger.startStatement(stmtLog);
		}
		return -1;
	}

	public void endStatement(int index) {
		if (stmtLogger != null) {
			stmtLogger.endStatement(index);
		}
	}
}
