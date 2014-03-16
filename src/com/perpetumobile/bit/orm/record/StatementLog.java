package com.perpetumobile.bit.orm.record;

import com.perpetumobile.bit.util.Logger;

/**
 * @author Zoran Dukic
 *
 */
public class StatementLog {
	static private Logger logger = new Logger(StatementLog.class);
			
	private String stmt = null;
	private String msg = null;
	private String errorMsg = null;
	private long startTime = 0;
	private long endTime = 0;
	
	public StatementLog(String stmt) {
		this.stmt = stmt;
		startTime = System.currentTimeMillis();
		if(logger.isDebugEnabled()) {
			logger.debug(stmt);
		}
	}
	
	public void setEndTime() {
		endTime = System.currentTimeMillis();
	}
	
	public String getStmt() {
		return stmt;
	}
	
	public void setMsg(String msg) {
		this.msg = msg;
	}
	
	public String getMsg() {
		return msg;
	}
	
	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}
	
	public String getErrorMsg() {
		return errorMsg;
	}
	
	public long getElapsedTime() {
		return (endTime > 0 ? endTime - startTime : -1);
	}
}
