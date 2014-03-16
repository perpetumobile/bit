package com.perpetumobile.bit.orm.record.field;

import java.nio.ByteBuffer;

import com.perpetumobile.bit.config.Config;
import com.perpetumobile.bit.util.Util;


/**
 * 
 * @author Zoran Dukic
 */
public class FieldConfig {
	
	public static final String CHARSET_NAME = "UTF8";
	
	public static final String FIELD_CONFIG_KEY = ".Record.Field";
	public static final String FIELD_NAME_CONFIG_KEY = ".Record.Field.Name";
	public static final String FIELD_TYPE_CONFIG_KEY = ".Record.Field.Type";
	
	public static final int FIELD_TYPE_OTHER = 0;
	public static final int FIELD_TYPE_INT = 1;
	public static final int FIELD_TYPE_LONG = 2;
	public static final int FIELD_TYPE_FLOAT = 3;
	public static final int FIELD_TYPE_DOUBLE = 4;
	public static final int FIELD_TYPE_CHAR = 5;
	public static final int FIELD_TYPE_VARCHAR = 6;
	public static final int FIELD_TYPE_TIME = 7;
	public static final int FIELD_TYPE_TIMESTAMP = 8;
	public static final int FIELD_TYPE_MD5 = 9;
	public static final int FIELD_TYPE_AUTO_INCREMENT = 10;
	public static final int FIELD_TYPE_BYTEBUFFER = 11;
	public static final int FIELD_TYPE_UNIXTIMESTAMP = 12;
	
	private String fieldName = null;
	private int fieldType = FIELD_TYPE_OTHER;
	private int length = -1;
	private boolean valid = false;
	
	public FieldConfig(String fieldName, String strFieldType) {
		this.fieldName = fieldName;
		init(strFieldType);
	}
	
	public FieldConfig(String configName) {
		String f = Config.getInstance().getProperty(configName+FIELD_CONFIG_KEY, null);
		if(!Util.nullOrEmptyString(f)) {
			String[] fieldConfig = f.trim().split(" ");
			if(fieldConfig.length == 2) {
				this.fieldName = fieldConfig[0];
				init(fieldConfig[1]);
			}
		} else {
			this.fieldName = Config.getInstance().getProperty(configName+FIELD_NAME_CONFIG_KEY, null);
			init(Config.getInstance().getProperty(configName+FIELD_TYPE_CONFIG_KEY, ""));
		}
	}
	
	protected void init(String strFieldType) {
		if(!Util.nullOrEmptyString(fieldName)) {
			strFieldType = strFieldType.toLowerCase();
			//TODO use enum
			if(strFieldType.equals("int") || strFieldType.equals("integer")) {
				fieldType = FIELD_TYPE_INT;
			} else if(strFieldType.equals("long")) {
				fieldType = FIELD_TYPE_LONG; 	
			} else if(strFieldType.equals("float")) {
				fieldType = FIELD_TYPE_FLOAT;
			} else if(strFieldType.equals("double")) {
				fieldType = FIELD_TYPE_DOUBLE;
			} else if(strFieldType.startsWith("char") || strFieldType.startsWith("varchar")) {
				fieldType = FIELD_TYPE_VARCHAR;
				int index1 = strFieldType.indexOf('(');
				if(index1 > 0) {
					int index2 = strFieldType.indexOf(')');
					if(index2 > 0) {
						length = Util.toInt(strFieldType.substring(index1+1, index2), -1);
					}
				}
			} else if(strFieldType.equals("text")) {
				fieldType = FIELD_TYPE_VARCHAR;
			} else if(strFieldType.equals("datetime")) {
				fieldType = FIELD_TYPE_TIME;
			} else if(strFieldType.equals("sqltimestamp")) {
				fieldType = FIELD_TYPE_TIMESTAMP;
			} else if(strFieldType.equals("unixtimestamp")) {
				fieldType = FIELD_TYPE_UNIXTIMESTAMP;
			} else if(strFieldType.equals("md5")) {
				fieldType = FIELD_TYPE_MD5;
			} else if(strFieldType.equals("auto")) {
				fieldType = FIELD_TYPE_AUTO_INCREMENT;
			} else if(strFieldType.equals("bytebuffer")) {
				fieldType = FIELD_TYPE_BYTEBUFFER;	
			} else {
				fieldType = FIELD_TYPE_OTHER;
			}	
			valid = true;
		}
	}
	
	public Field createField() {
		
		Field result = null;
		
		switch(fieldType) {
		case FIELD_TYPE_INT:
			result = new IntField(fieldName);
			break;
		case FIELD_TYPE_LONG:
			result = new LongField(fieldName);
			break;	
		case FIELD_TYPE_FLOAT:
			result = new FloatField(fieldName);
			break;
		case FIELD_TYPE_DOUBLE:
			result = new DoubleField(fieldName);
			break;
		case FIELD_TYPE_CHAR:
		case FIELD_TYPE_VARCHAR:
			result = new StringField(fieldName);
			((StringField)result).setLength(length);
			break;
		case FIELD_TYPE_TIME:
		case FIELD_TYPE_TIMESTAMP:	
			result = new SQLTimestampField(fieldName);
			break;
		case FIELD_TYPE_UNIXTIMESTAMP:
			result = new UnixSQLTimestampField(fieldName);
			break;	
		case FIELD_TYPE_MD5:	
			result = new MD5Field(fieldName);
			break;
		case FIELD_TYPE_AUTO_INCREMENT:	
			result = new IntField(fieldName, true);
			break;
		case FIELD_TYPE_BYTEBUFFER:
			result = new ByteBufferField(fieldName);
			break;	
		default:
			result = new StringField(fieldName);
			break;
		}
		
		return result;
	}
	
	public String getFieldName() {
		return fieldName;
	}
	
	public ByteBuffer getByteBufferFieldName() {
		return Util.toByteBuffer(fieldName, CHARSET_NAME);
	}
	
	public int getFieldType() {		
		return fieldType;
	}
	
	public boolean isValid() {
		return valid;
	}
}
