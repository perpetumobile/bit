package com.perpetumobile.bit.orm.xml;

import java.io.File;

import com.perpetumobile.bit.http.HttpRequest;
import com.perpetumobile.bit.orm.record.StatementLogger;
import com.perpetumobile.bit.util.Task;

public class SAXParserTask extends Task {

	protected HttpRequest httpRequest = null;
	protected File file = null;
	protected String configNamePrefix = null;
	protected String rootElementName = null;
	protected StatementLogger stmtLogger = null;
	
	protected XMLRecord result = null;
	
	public SAXParserTask() {
	}
	
	@Override
	public void runImpl() {
		if(httpRequest != null) {
			result = SAXParserManager.getInstance().parseImpl(httpRequest, configNamePrefix, rootElementName, stmtLogger);
		} else if (file != null) {
			result = SAXParserManager.getInstance().parseImpl(file, configNamePrefix, rootElementName, stmtLogger);
		}
	}
	
	public XMLRecord getResult() {
		return result;
	}

	public void setHttpRequest(HttpRequest httpRequest) {
		this.httpRequest = httpRequest;
	}

	public void setFile(File file) {
		this.file = file;
	}
	
	public void setConfigNamePrefix(String configNamePrefix) {
		this.configNamePrefix = configNamePrefix;
	}

	public void setRootElementName(String rootElementName) {
		this.rootElementName = rootElementName;
	}

	public void setStmtLogger(StatementLogger stmtLogger) {
		this.stmtLogger = stmtLogger;
	}
}
