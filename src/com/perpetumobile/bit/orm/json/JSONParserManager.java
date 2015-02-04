package com.perpetumobile.bit.orm.json;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.io.StringReader;

import org.apache.commons.pool.impl.GenericObjectPool;
import org.json.simple.parser.JSONParser;

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
public class JSONParserManager {
	static private JSONParserManager instance = new JSONParserManager();
	static public JSONParserManager getInstance() { return instance; }
	
	static private Logger logger = new Logger(JSONParserManager.class);
	
	private GenericObjectPool pool = null;
	
	private JSONParserManager() {
		init();
	}
	
	private void init() {
		pool = new GenericObjectPool(new JSONParserFactory());
		pool.setMaxActive(-1);
		pool.setMaxIdle(10);
		pool.setTestOnBorrow(true);
	}
	
	public JSONParser getJSONParser() throws Exception {
		return (JSONParser)pool.borrowObject();
	}
	
	public void returnJSONParser(JSONParser parser) {
		if(parser != null) {
			try {
				pool.returnObject(parser);
			} catch (Exception e) {
				logger.error("JSONParserManager.returnJSONParser exception", e);
			}
		}
	}
	
	public void invalidateJSONParser(JSONParser parser) {
		if(parser != null) {
			try {
				pool.invalidateObject(parser);
			} catch (Exception e) {
				logger.error("JSONParserManager.invalidateJSONParser exception", e);
			}
		}
	}
	
	public JSONRecord parseImpl(Reader in, String configName) 
	throws Exception {
		JSONParser parser = null;
		JSONRecordHandler handler = null;
		JSONRecord result = null;  	
		try {
			parser = getJSONParser();
			handler = new JSONRecordHandler(configName, parser, in);
			parser.parse(in, handler);
			result = handler.getJSONRecord();
		} catch (Exception e) {
			invalidateJSONParser(parser);
			parser = null;
			throw e;
		} finally {
			returnJSONParser(parser);
		}
		return result;
	}
	
	public JSONRecord parseImpl(HttpRequest httpRequest, boolean isHeaderConfigName, String configName) {
		return parseImpl(httpRequest, isHeaderConfigName, configName, null);
	}
	
	public JSONRecord parseImpl(HttpRequest httpRequest, boolean isHeaderConfigName, String configName, StatementLogger stmtLogger) {
		JSONRecord result = null;
		int stmtLogIndex = start(stmtLogger, httpRequest.getUrl());	
		try {
			HttpResponseDocument doc = HttpManager.getInstance().executeImpl(httpRequest);
			if(isHeaderConfigName) {
				configName = doc.getHeaderValue(configName);
			}
			String response = doc.getPageSource();
			if(!Util.nullOrEmptyString(response)) {
				StringReader in = new StringReader(response);
				result = parseImpl(in, configName);
			}
			end(stmtLogger, stmtLogIndex);
		} catch (Exception e) {
			logErrorMsg(stmtLogger, stmtLogIndex, e.getMessage());
			logger.error("JSONParserManager.parseImpl exception", e);
		}
		return result;
	}
	
	public JSONRecord parseImpl(File file, String configName) {
		return parseImpl(file, configName, null);
	}
	
	public JSONRecord parseImpl(File file, String configName, StatementLogger stmtLogger) {
		JSONRecord result = null;
		int stmtLogIndex = start(stmtLogger, file.getAbsolutePath());	
		try {
			BufferedReader in = new BufferedReader(new FileReader(file));
			result = parseImpl(in, configName);
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
			logger.error("JSONParserManager.runTask exception", e);
		}
	}
	
	/**
	 * Parse is executed in a Bit Service Thread.
	 * Blocking mode: Current thread is waiting for operation to complete and return result.
	 */
	public JSONRecord parseSync(HttpRequest httpRequest, boolean isHeaderConfigName, String configName) {
		return parseSync(httpRequest, isHeaderConfigName, configName, null, null);
	}
	
	/**
	 * Parse is executed in a Bit Service Thread if threadPoolManagerConfigName is not provided.
	 * Blocking mode: Current thread is waiting for operation to complete and return result.
	 */
	public JSONRecord parseSync(HttpRequest httpRequest, boolean isHeaderConfigName, String configName, String threadPoolManagerConfigName) {
		return parseSync(httpRequest, isHeaderConfigName, configName, threadPoolManagerConfigName, null);
	}
	
	/**
	 * Parse is executed in a Bit Service Thread if threadPoolManagerConfigName is not provided.
	 * Blocking mode: Current thread is waiting for operation to complete and return result.
	 */
	public JSONRecord parseSync(HttpRequest httpRequest, boolean isHeaderConfigName, String configName, String threadPoolManagerConfigName, StatementLogger stmtLogger) {
		JSONParserTask task = new JSONParserTask();
		task.setHttpRequest(httpRequest);
		task.setIsHeaderConfigName(isHeaderConfigName);
		task.setConfigName(configName);
		task.setStmtLogger(stmtLogger);
		runTask(task, threadPoolManagerConfigName, true);
		return task.getResult();
	}
	
	/**
	 * Parse is executed in a Bit Service Thread.
	 * Blocking mode: Current thread is waiting for operation to complete and return result.
	 */
	public JSONRecord parseSync(File file, String configName) {
		return parseSync(file, configName, null, null);
	}
	
	/**
	 * Parse is executed in a Bit Service Thread if threadPoolManagerConfigName is not provided.
	 * Blocking mode: Current thread is waiting for operation to complete and return result.
	 */
	public JSONRecord parseSync(File file, String configName, String threadPoolManagerConfigName) {
		return parseSync(file, configName, threadPoolManagerConfigName, null);
	}
	
	/**
	 * Parse is executed in a Bit Service Thread if threadPoolManagerConfigName is not provided.
	 * Blocking mode: Current thread is waiting for operation to complete and return result.
	 */
	public JSONRecord parseSync(File file, String configName, String threadPoolManagerConfigName, StatementLogger stmtLogger) {
		JSONParserTask task = new JSONParserTask();
		task.setFile(file);
		task.setConfigName(configName);
		task.setStmtLogger(stmtLogger);
		runTask(task, threadPoolManagerConfigName, true);
		return task.getResult();
	}
	
	/**
	 * Parse is executed in a Bit Service Thread.
	 * Non-Blocking mode: Current thread is NOT waiting for operation to complete.
	 */
	public void parse(TaskCallback<JSONParserTask> callback, HttpRequest httpRequest, boolean isHeaderConfigName, String configName) {
		parse(callback, httpRequest, isHeaderConfigName, configName, null, null);
	}
	
	/**
	 * Parse is executed in a Bit Service Thread if threadPoolManagerConfigName is not provided.
	 * Non-Blocking mode: Current thread is NOT waiting for operation to complete.
	 */
	public void parse(TaskCallback<JSONParserTask> callback, HttpRequest httpRequest, boolean isHeaderConfigName, String configName, String threadPoolManagerConfigName) {
		parse(callback, httpRequest, isHeaderConfigName, configName, threadPoolManagerConfigName, null);
	}
	
	/**
	 * Parse is executed in a Bit Service Thread if threadPoolManagerConfigName is not provided.
	 * Non-Blocking mode: Current thread is NOT waiting for operation to complete.
	 */
	public void parse(TaskCallback<JSONParserTask> callback, HttpRequest httpRequest, boolean isHeaderConfigName, String configName, String threadPoolManagerConfigName, StatementLogger stmtLogger) {
		JSONParserTask task = new JSONParserTask();
		task.setHttpRequest(httpRequest);
		task.setIsHeaderConfigName(isHeaderConfigName);
		task.setConfigName(configName);
		task.setStmtLogger(stmtLogger);
		task.setCallback(callback);
		runTask(task, threadPoolManagerConfigName, false);
	}
	
	/**
	 * Parse is executed in a Bit Service Thread.
	 * Non-Blocking mode: Current thread is NOT waiting for operation to complete.
	 */
	public void parse(TaskCallback<JSONParserTask> callback, File file, String configName) {
		parse(callback, file, configName, null, null);
	}
	
	/**
	 * Parse is executed in a Bit Service Thread if threadPoolManagerConfigName is not provided.
	 * Non-Blocking mode: Current thread is NOT waiting for operation to complete.
	 */
	public void parse(TaskCallback<JSONParserTask> callback, File file, String configName, String threadPoolManagerConfigName) {
		parse(callback, file, configName, threadPoolManagerConfigName, null);
	}
	
	/**
	 * Parse is executed in a Bit Service Thread if threadPoolManagerConfigName is not provided.
	 * Non-Blocking mode: Current thread is NOT waiting for operation to complete.
	 */
	public void parse(TaskCallback<JSONParserTask> callback, File file, String configName, String threadPoolManagerConfigName, StatementLogger stmtLogger) {
		JSONParserTask task = new JSONParserTask();
		task.setFile(file);
		task.setConfigName(configName);
		task.setStmtLogger(stmtLogger);
		task.setCallback(callback);
		runTask(task, threadPoolManagerConfigName, false);
	}
}
