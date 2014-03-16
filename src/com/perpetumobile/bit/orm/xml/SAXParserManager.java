package com.perpetumobile.bit.orm.xml;

import java.io.StringReader;

import javax.xml.parsers.SAXParser;

import org.apache.commons.pool.impl.GenericObjectPool;
import org.xml.sax.InputSource;

import com.perpetumobile.bit.http.HttpManager;
import com.perpetumobile.bit.orm.record.StatementLog;
import com.perpetumobile.bit.orm.record.StatementLogger;
import com.perpetumobile.bit.util.Logger;
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
	
	public XMLRecord parse(String uri, String configNamePrefix, String rootElementName) throws Exception {
		return parse(uri, configNamePrefix, rootElementName, null);
	}
	
	public XMLRecord parse(String uri, String configNamePrefix, String rootElementName, StatementLogger stmtLogger) throws Exception {
		SAXParser parser = null;
		XMLRecordHandler handler = null;
		XMLRecord result = null;  
		
		int stmtLogIndex = start(stmtLogger, uri);	
		try {
			parser = getSAXParser();
			handler = new XMLRecordHandler(configNamePrefix, rootElementName, parser);
			String response = HttpManager.getInstance().get(uri).getPageSource();
			// TODO: There must be a faster way to fix & :-) 
			response = Util.replaceAll(response, "&amp;", "&");
			response = Util.replaceAll(response, "&", "&amp;");
			parser.parse(new InputSource(new StringReader(response)), handler);
			// parser.parse(uri, handler);
			result = handler.getXMLRecord();
			end(stmtLogger, stmtLogIndex);
		} catch (Exception e) {
			logErrorMsg(stmtLogger, stmtLogIndex, e.getMessage());
			invalidateSAXParser(parser);
			parser = null;
			throw e;
		} finally {
			returnSAXParser(parser);
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
