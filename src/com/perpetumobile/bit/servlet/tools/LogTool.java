package com.perpetumobile.bit.servlet.tools;


import java.util.ArrayList;

import com.perpetumobile.bit.config.Config;
import com.perpetumobile.bit.orm.record.StatementLog;
import com.perpetumobile.bit.orm.record.StatementLogger;

/**
 * 
 * @author Zoran Dukic
 */
public class LogTool extends BaseTool implements StatementLogger {
	
	public static final String LOG_STATEMENT_ENABLED_CONFIG_KEY = "LogTool.Log.Statement.Enabled";
	
	private boolean statementLogEnabled = false;
	private ArrayList<StatementLog> statementLog = new ArrayList<StatementLog>();
	
	private boolean paramsChecked = false;
	
	public LogTool() {
	}
	
	public void init(Object initData) {
		super.init(initData);
		statementLogEnabled = Config.getInstance().getBooleanProperty(LOG_STATEMENT_ENABLED_CONFIG_KEY, false);
	}
	
	public boolean isStatementLogEnabled() {
		if(!statementLogEnabled && !paramsChecked) {
			if (context != null) {
				ParamTool params = getParams();
				if (params != null) {
					statementLogEnabled = (params.getInt(LOG_STATEMENT_ENABLED_CONFIG_KEY, 0) > 0);
				}
			}
			paramsChecked = true;
		}
		
		return statementLogEnabled;
	}
	
	public int startStatement(StatementLog stmt) {
		if (isStatementLogEnabled()) {
			statementLog.add(stmt);
		}
		return statementLog.size() - 1;
	}
	
	public void endStatement(int index) {
		if (isStatementLogEnabled()) {
			StatementLog stmt = statementLog.get(index);
			if (stmt != null) {
				stmt.setEndTime();
			}
		}
	}
	
	public void setMsg(int index, String msg) {
		if (isStatementLogEnabled()) {
			StatementLog stmt = statementLog.get(index);
			if (stmt != null) {
				stmt.setMsg(msg);
			}
		}
	}
	
	public void setErrorMsg(int index, String errorMsg) {
		if (isStatementLogEnabled()) {
			StatementLog stmt = statementLog.get(index);
			if (stmt != null) {
				stmt.setErrorMsg(errorMsg);
			}
		}
	}
	
	public ArrayList<StatementLog> getStatementLog() {
		return statementLog;
	}
	
	public void clearStatementLog() {
		statementLog.clear();
	}
}