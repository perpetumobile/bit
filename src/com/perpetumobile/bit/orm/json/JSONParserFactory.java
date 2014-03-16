package com.perpetumobile.bit.orm.json;

import org.apache.commons.pool.PoolableObjectFactory;
import org.json.simple.parser.JSONParser;

/**
 * @author Zoran Dukic
 *
 */
public class JSONParserFactory implements PoolableObjectFactory {
	
	public JSONParserFactory() {
	}	
	
	public void activateObject(Object obj) throws Exception {
	}
	
	public void destroyObject(Object obj) throws Exception {
	}
	
	public Object makeObject() throws Exception {
		return new JSONParser();
	}
	
	public void passivateObject(Object obj) throws Exception {
		((JSONParser)obj).reset();
	}
	
	public boolean validateObject(Object obj) {
		return true;
	}
}
