package com.perpetumobile.bit.orm.db;

import java.util.EnumSet;
import java.util.HashMap;

/**
 * @author Zoran Dukic
 *
 */
public enum DBStatementMethod {
	SELECT("SELECT"),
	INSERT("INSERT"),
	INSERT_IGNORE("INSERT_IGNORE"),
	REPLACE("REPLACE"),
	UPDATE("UPDATE"),
	DELETE("DELETE");
	
	private static final HashMap<String,DBStatementMethod> map = new HashMap<String,DBStatementMethod>();
	static {
		for(DBStatementMethod rt : EnumSet.allOf(DBStatementMethod.class))
			map.put(rt.getMethod(), rt);
	}

	private String method;

	private DBStatementMethod(String method) {
		this.method = method.toUpperCase();
	}

	public String getMethod(){
		return method; 
	}

	static public DBStatementMethod get(String type) { 
		return map.get(type.toUpperCase()); 
	}
}
