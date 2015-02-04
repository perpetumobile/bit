package com.perpetumobile.bit.orm.xml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.StringReader;

import javax.xml.parsers.SAXParser;

import org.apache.commons.pool.impl.GenericObjectPool;
import org.xml.sax.InputSource;

import com.perpetumobile.bit.http.HttpManager;
import com.perpetumobile.bit.http.HttpRequest;
import com.perpetumobile.bit.http.HttpResponseDocument;
import com.perpetumobile.bit.orm.record.StatementLog;
import com.perpetumobile.bit.orm.record.StatementLogger;
import com.perpetumobile.bit.util.Logger;
import com.perpetumobile.bit.util.Task;
import com.perpetumobile.bit.util.TaskCallback;
import com.perpetumobile.bit.util.ThreadPoolManager;
import com.perpetumobile.bit.util.Util;


/**
 * @author Zoran Dukic
 *
 */
public class SAXParserManager {
	static private Logger logger = new Logger(SAXParserManager.class);
	
	static private SAXParserManager instance = new SAXParserManager();
	static public SAXParserManager getInstance() { return instance; }
	
	private GenericObjectPool pool = null;
	
	private SAXParserManager() {
		init();
	}
	
	private void init() {
		pool = new GenericObjectPool(new SAXParserFactory());
		pool.setMaxActive(-1);
		pool.setMaxIdle(10);
		pool.setTestOnBorrow(true);
	}
	
	public SAXParser getSAXParser() throws Exception {
		return (SAXParser)pool.borrowObject();
	}
	
	public void returnSAXParser(SAXParser parser) {
		if(parser != null) {
			try {
				pool.returnObject(parser);
			} catch (Exception e) {
				logger.error("SAXParserManager.returnSAXParser exception", e);
			}
		}
	}
	
	public void invalidateSAXParser(SAXParser parser) {
		if(parser != null) {
			try {
				pool.invalidateObject(parser);
			} catch (Exception e) {
				logger.error("SAXParserManager.invalidateSAXParser exception", e);
			}
		}
	}
	
	public XMLRecord parseImpl(InputSource in, String configNamePrefix, String rootElementName) 
	throws Exception { 
		SAXParser parser = null;
		XMLRecordHandler handler = null;
		XMLRecord result = null;  
		try {
			parser = getSAXParser();
			handler = new XMLRecordHandler(configNamePrefix, rootElementName, parser);
			parser.parse(in, handler);
			result = handler.getXMLRecord();
		} catch (Exception e) {
			invalidateSAXParser(parser);
			parser = null;
			throw e;
		} finally {
			returnSAXParser(parser);
		}
		return result;
	}
	
	public XMLRecord parseImpl(HttpRequest httpRequest, boolean isHeaderConfigName, String configNamePrefix, String rootElementName) {
		return parseImpl(httpRequest, isHeaderConfigName, configNamePrefix, rootElementName, null);
	}
	
	public XMLRecord parseImpl(HttpRequest httpRequest, boolean isHeaderConfigName, String configNamePrefix, String rootElementName, StatementLogger stmtLogger) {
		XMLRecord result = null;  
		int stmtLogIndex = start(stmtLogger, httpRequest.getUrl());	
		try {
			HttpResponseDocument doc = HttpManager.getInstance().executeImpl(httpRequest);
			if(isHeaderConfigName) {
				configNamePrefix = doc.getHeaderValue(configNamePrefix);
				rootElementName = doc.getHeaderValue(rootElementName);
			}
			String response = doc.getPageSource();
			if(!Util.nullOrEmptyString(response)) {
				// TODO: There must be a faster way to fix & :-) 
				response = Util.replaceAll(response, "&amp;", "&");
				response = Util.replaceAll(response, "&", "&amp;");
				result = parseImpl(new InputSource(new StringReader(response)), configNamePrefix, rootElementName);
			}
			end(stmtLogger, stmtLogIndex);
		} catch (Exception e) {
			logErrorMsg(stmtLogger, stmtLogIndex, e.getMessage());
			logger.error("SAXParserManager.parseImpl exception", e);
		}
		return result;
	}
	
	public XMLRecord parseImpl(File file, String configNamePrefix, String rootElementName) {
		return parseImpl(file, configNamePrefix, rootElementName, null);
	}
	
	public XMLRecord parseImpl(File file, String configNamePrefix, String rootElementName, StatementLogger stmtLogger) {
		XMLRecord result = null;
		int stmtLogIndex = start(stmtLogger, file.getAbsolutePath());	
		try {
			InputSource in = new InputSource(new BufferedReader(new FileReader(file)));
			result = parseImpl(in, configNamePrefix, rootElementName);
			end(stmtLogger, stmtLogIndex);
		} catch (Exception e) {
			logErrorMsg(stmtLogger, stmtLogIndex, e.getMessage());
			logger.error("JSONParserManager.parseImpl exception", e);
		}
		return result;
	}
	
	public int start(StatementLogger stmtLogger, String uri) {
		if (stmtLogger != null) {
			StatementLog stmtLog = new StatementLog(uri);
			return stmtLogger.startStatement(stmtLog);
		}
		return -1;
	}

	public void end(StatementLogger stmtLogger, int index) {
		if (stmtLogger != null) {
			stmtLogger.endStatement(index);
		}
	}
	
	public void logErrorMsg(StatementLogger stmtLogger, int index, String errorMsg) {
		if (stmtLogger != null) {
			stmtLogger.setErrorMsg(index, errorMsg);
		}
	}
	
	protected void runTask(Task task, String threadPoolManagerConfigName, boolean isSync) {
		try {
			if(Util.nullOrEmptyString(threadPoolManagerConfigName)) {
				ThreadPoolManager.getInstance().run(ThreadPoolManager.BIT_SERVICE_THREAD_POOL_MANAGER_CONFIG_NAME, task);
			} else {
				ThreadPoolManager.getInstance().run(threadPoolManagerConfigName, task);
			}
			if(isSync) {
				task.isDone();
			}
		} catch (Exception e) {
			logger.error("SAXParserManager.runTask exception", e);
		}
	}
	
	/**
	 * Parse is executed in a Bit Service Thread.
	 * Blocking mode: Current thread is waiting for operation to complete and return result.
	 */
	public XMLRecord parseSync(HttpRequest httpRequest, boolean isHeaderConfigName, String configNamePrefix, String rootElementName) {
		return parseSync(httpRequest, isHeaderConfigName, configNamePrefix, rootElementName, null, null);
	}
	
	/**
	 * Parse is executed in a Bit Service Thread if threadPoolManagerConfigName is not provided.
	 * Blocking mode: Current thread is waiting for operation to complete and return result.
	 */
	public XMLRecord parseSync(HttpRequest httpRequest, boolean isHeaderConfigName, String configNamePrefix, String rootElementName, String threadPoolManagerConfigName) {
		return parseSync(httpRequest, isHeaderConfigName, configNamePrefix, rootElementName, threadPoolManagerConfigName, null);
	}
	
	/**
	 * Parse is executed in a Bit Service Thread if threadPoolManagerConfigName is not provided.
	 * Blocking mode: Current thread is waiting for operation to complete and return result.
	 */
	public XMLRecord parseSync(HttpRequest httpRequest, boolean isHeaderConfigName, String configNamePrefix, String rootElementName, String threadPoolManagerConfigName, StatementLogger stmtLogger) {
		SAXParserTask task = new SAXParserTask();
		task.setHttpRequest(httpRequest);
		task.setIsHeaderConfigName(isHeaderConfigName);
		task.setConfigNamePrefix(configNamePrefix);
		task.setRootElementName(rootElementName);
		task.setStmtLogger(stmtLogger);
		runTask(task, threadPoolManagerConfigName, true);
		return task.getResult();
	}

	/**
	 * Parse is executed in a Bit Service Thread.
	 * Blocking mode: Current thread is waiting for operation to complete and return result.
	 */
	public XMLRecord parseSync(File file, String configNamePrefix, String rootElementName) {
		return parseSync(file, configNamePrefix, rootElementName, null, null);
	}
	
	/**
	 * Parse is executed in a Bit Service Thread if threadPoolManagerConfigName is not provided.
	 * Blocking mode: Current thread is waiting for operation to complete and return result.
	 */
	public XMLRecord parseSync(File file, String configNamePrefix, String rootElementName, String threadPoolManagerConfigName) {
		return parseSync(file, configNamePrefix, rootElementName, threadPoolManagerConfigName, null);
	}
	
	/**
	 * Parse is executed in a Bit Service Thread if threadPoolManagerConfigName is not provided.
	 * Blocking mode: Current thread is waiting for operation to complete and return result.
	 */
	public XMLRecord parseSync(File file, String configNamePrefix, String rootElementName, String threadPoolManagerConfigName, StatementLogger stmtLogger) {
		SAXParserTask task = new SAXParserTask();
		task.setFile(file);
		task.setConfigNamePrefix(configNamePrefix);
		task.setRootElementName(rootElementName);
		task.setStmtLogger(stmtLogger);
		runTask(task, threadPoolManagerConfigName, true);
		return task.getResult();
	}
	
	/**
	 * Parse is executed in a Bit Service Thread.
	 * Non-Blocking mode: Current thread is NOT waiting for operation to complete.
	 */
	public void parse(TaskCallback<SAXParserTask> callback, HttpRequest httpRequest, boolean isHeaderConfigName, String configNamePrefix, String rootElementName) {
		parse(callback, httpRequest, isHeaderConfigName, configNamePrefix, rootElementName, null, null);
	}
	
	/**
	 * Parse is executed in a Bit Service Thread if threadPoolManagerConfigName is not provided.
	 * Non-Blocking mode: Current thread is NOT waiting for operation to complete.
	 */
	public void parse(TaskCallback<SAXParserTask> callback, HttpRequest httpRequest, boolean isHeaderConfigName, String configNamePrefix, String rootElementName, String threadPoolManagerConfigName) {
		parse(callback, httpRequest, isHeaderConfigName, configNamePrefix, rootElementName, threadPoolManagerConfigName, null);
	}
	
	/**
	 * Parse is executed in a Bit Service Thread if threadPoolManagerConfigName is not provided.
	 * Non-Blocking mode: Current thread is NOT waiting for operation to complete.
	 */
	public void parse(TaskCallback<SAXParserTask> callback, HttpRequest httpRequest, boolean isHeaderConfigName, String configNamePrefix, String rootElementName, String threadPoolManagerConfigName, StatementLogger stmtLogger) {
		SAXParserTask task = new SAXParserTask();
		task.setHttpRequest(httpRequest);
		task.setIsHeaderConfigName(isHeaderConfigName);
		task.setConfigNamePrefix(configNamePrefix);
		task.setRootElementName(rootElementName);
		task.setStmtLogger(stmtLogger);
		task.setCallback(callback);
		runTask(task, threadPoolManagerConfigName, false);
	}
	
	/**
	 * Parse is executed in a Bit Service Thread.
	 * Non-Blocking mode: Current thread is NOT waiting for operation to complete.
	 */
	public void parse(TaskCallback<SAXParserTask> callback, File file, String configNamePrefix, String rootElementName) {
		parse(callback, file, configNamePrefix, rootElementName, null, null);
	}
	
	/**
	 * Parse is executed in a Bit Service Thread if threadPoolManagerConfigName is not provided.
	 * Non-Blocking mode: Current thread is NOT waiting for operation to complete.
	 */
	public void parse(TaskCallback<SAXParserTask> callback, File file, String configNamePrefix, String rootElementName, String threadPoolManagerConfigName) {
		parse(callback, file, configNamePrefix, rootElementName, threadPoolManagerConfigName, null);
	}
	
	/**
	 * Parse is executed in a Bit Service Thread if threadPoolManagerConfigName is not provided.
	 * Non-Blocking mode: Current thread is NOT waiting for operation to complete.
	 */
	public void parse(TaskCallback<SAXParserTask> callback, File file, String configNamePrefix, String rootElementName, String threadPoolManagerConfigName, StatementLogger stmtLogger) {
		SAXParserTask task = new SAXParserTask();
		task.setFile(file);
		task.setConfigNamePrefix(configNamePrefix);
		task.setRootElementName(rootElementName);
		task.setStmtLogger(stmtLogger);
		task.setCallback(callback);
		runTask(task, threadPoolManagerConfigName, false);
	}
}
