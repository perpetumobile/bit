package com.perpetumobile.bit.orm.json;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.io.StringReader;

import org.apache.commons.pool.impl.GenericObjectPool;
import org.json.simple.parser.JSONParser;

import com.perpetumobile.bit.http.HttpManager;
import com.perpetumobile.bit.orm.record.StatementLog;
import com.perpetumobile.bit.orm.record.StatementLogger;
import com.perpetumobile.bit.util.Logger;


/**
 * @author Zoran Dukic
 *
 */
public class JSONParserManager {
	static private Logger logger = new Logger(JSONParserManager.class);
	
	static private JSONParserManager instance = new JSONParserManager();
	static public JSONParserManager getInstance() { return instance; }
	
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
	
	public JSONRecord parse(Reader in, String configName)
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
	
	public JSONRecord parse(String uri, String configName) throws Exception {
		return parse(uri, configName, null);
	}
	
	public JSONRecord parse(String uri, String configName, StatementLogger stmtLogger)
	throws Exception {
		JSONRecord result = null;
		int stmtLogIndex = start(stmtLogger, uri);	
		try {
			String response = HttpManager.getInstance().get(uri).getPageSource();
			StringReader in = new StringReader(response);
			result = parse(in, configName);
			end(stmtLogger, stmtLogIndex);
		} catch (Exception e) {
			logErrorMsg(stmtLogger, stmtLogIndex, e.getMessage());
			throw e;
		}
		return result;
	}
	
	public JSONRecord parse(File file, String configName) throws Exception {
		return parse(file, configName, null);
	}
	
	public JSONRecord parse(File file, String configName, StatementLogger stmtLogger)
	throws Exception {
		JSONRecord result = null;
		int stmtLogIndex = start(stmtLogger, file.getAbsolutePath());	
		try {
			BufferedReader in = new BufferedReader(new FileReader(file));
			result = parse(in, configName);
			end(stmtLogger, stmtLogIndex);
		} catch (Exception e) {
			logErrorMsg(stmtLogger, stmtLogIndex, e.getMessage());
			throw e;
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
}
