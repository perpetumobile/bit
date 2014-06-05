package com.perpetumobile.bit.orm.json;

import java.io.File;
import com.perpetumobile.bit.http.HttpRequest;
import com.perpetumobile.bit.orm.record.StatementLogger;
import com.perpetumobile.bit.util.Task;

public class JSONParserTask extends Task {

	protected HttpRequest httpRequest = null;
	protected File file = null;
	protected String configName = null;
	protected StatementLogger stmtLogger = null;
	
	protected JSONRecord result = null;
	
	public JSONParserTask() {
	}
	
	@Override
	public void runImpl() {	
		if(httpRequest != null) {
			result = JSONParserManager.getInstance().parseImpl(httpRequest, configName, stmtLogger);
		} else if (file != null) {
			result = JSONParserManager.getInstance().parseImpl(file, configName, stmtLogger);
		}
	}
	
	public JSONRecord getResult() {
		return result;
	}

	public void setHttpRequest(HttpRequest httpRequest) {
		this.httpRequest = httpRequest;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public void setConfigName(String configName) {
		this.configName = configName;
	}

	public void setStmtLogger(StatementLogger stmtLogger) {
		this.stmtLogger = stmtLogger;
	}
}
