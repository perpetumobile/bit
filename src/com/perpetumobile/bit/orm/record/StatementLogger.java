package com.perpetumobile.bit.orm.record;

import java.util.ArrayList;


public interface StatementLogger {
	public boolean isStatementLogEnabled();
	public int startStatement(StatementLog statement);
	public void endStatement(int index);
	public void setMsg(int index, String msg);
	public void setErrorMsg(int index, String errorMsg);
	public ArrayList<StatementLog> getStatementLog();
	public void clearStatementLog();
}
