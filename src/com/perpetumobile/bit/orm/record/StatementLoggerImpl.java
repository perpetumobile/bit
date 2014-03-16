package com.perpetumobile.bit.orm.record;


import java.util.ArrayList;

import com.perpetumobile.bit.config.Config;
import com.perpetumobile.bit.orm.record.StatementLog;
import com.perpetumobile.bit.orm.record.StatementLogger;
import com.perpetumobile.bit.util.Logger;
import com.perpetumobile.bit.util.Util;

/**
 * 
 * @author Zoran Dukic
 */
public class StatementLoggerImpl implements StatementLogger {
	static private Logger logger = new Logger(StatementLoggerImpl.class);
	
	public static final String LOG_STATEMENT_ENABLED_CONFIG_KEY = "Log.Statement.Enabled";
	
	private boolean statementLogEnabled = false;
	private StatementLog statementLog = null;
	
	public StatementLoggerImpl() {
		statementLogEnabled = Config.getInstance().getBooleanProperty(LOG_STATEMENT_ENABLED_CONFIG_KEY, false);
	}
	
	public boolean isStatementLogEnabled() {
		return statementLogEnabled;
	}
	
	public int startStatement(StatementLog stmt) {
		if (isStatementLogEnabled()) {
			statementLog = stmt;
		}
		return 0;
	}
	
	public void endStatement(int index) {
		if (isStatementLogEnabled() && statementLog != null) {
			statementLog.setEndTime();
			if(Util.nullOrEmptyString(statementLog.getMsg())) {
				logger.info(statementLog.getStmt() + " (Elapsed Time:" + statementLog.getElapsedTime() + "msec)");
			} else { 
				logger.info(statementLog.getStmt() + " Message: " + statementLog.getMsg() + " (Elapsed Time:" + statementLog.getElapsedTime() + "msec)");
			}
		}
		clearStatementLog();
	}
	
	public void setMsg(int index, String msg) {
		if (isStatementLogEnabled() && statementLog != null) {
			statementLog.setMsg(msg);
		}
	}
	
	public void setErrorMsg(int index, String errorMsg) {
		if (isStatementLogEnabled() && statementLog != null) {
			statementLog.setErrorMsg(errorMsg);
			logger.error(statementLog.getStmt() + " (Error Msg: " + statementLog.getErrorMsg());
		}
		clearStatementLog();
	}
	
	public ArrayList<StatementLog> getStatementLog() {
		ArrayList<StatementLog> result = new ArrayList<StatementLog>();
		if (isStatementLogEnabled() && statementLog != null) {
			result.add(statementLog);
		}
		return result;
	}
	
	public void clearStatementLog() {
		statementLog = null;
	}
}