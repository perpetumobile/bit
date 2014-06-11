package com.perpetumobile.bit.orm.record.field;

import java.util.EnumSet;
import java.util.HashMap;

/**
 * 
 * @author Zoran Dukic
 */
public enum FieldType {
	BOOL("bool", "boolean"),
	INT("int", "integer"),
	AUTO_INCREMENT("auto", "autoincrement"),
	LONG("long"),
	FLOAT("float"),
	DOUBLE("double"),
	STRING("string", "char", "varchar", "text"),
	DATETIME("datetime"),
	TIMESTAMP("timestamp", "sqltimestamp"),
	UNIXTIMESTAMP("unixtimestamp"),
	MD5("md5"),
	BYTEBUFFER("bytebuffer");
	
	private static final HashMap<String,FieldType> map = new HashMap<String,FieldType>();
	static {
		for(FieldType ft : EnumSet.allOf(FieldType.class)) {
			for(String t : ft.type) {
				map.put(t.toLowerCase(), ft);
			}
		}
	}

	private String[] type;

	private FieldType(String... type) {
		this.type = type;
	}

	static public FieldType get(String type) { 
		return map.get(type.toLowerCase()); 
	}	
}
