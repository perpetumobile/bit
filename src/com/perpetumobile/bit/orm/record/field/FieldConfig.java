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
	
	private String fieldName = null;
	private FieldType fieldType = null;
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
			
			// special case to support limiting string length
			if(strFieldType.startsWith("char") || strFieldType.startsWith("varchar")) {
				fieldType = FieldType.STRING;
				int index1 = strFieldType.indexOf('(');
				if(index1 > 0) {
					int index2 = strFieldType.indexOf(')');
					if(index2 > 0) {
						length = Util.toInt(strFieldType.substring(index1+1, index2), -1);
					}
				}
			} else {
				fieldType = FieldType.get(strFieldType);
				if(fieldType == null) {
					fieldType = FieldType.STRING;
				}
			}
	
			valid = true;
		}
	}
	
	public Field createField() {
		
		Field result = null;
		
		switch(fieldType) {
		case BOOL:
			result = new BooleanField(fieldName);
			break;
		case INT:
			result = new IntField(fieldName);
			break;
		case AUTO_INCREMENT:	
			result = new IntField(fieldName, true);
			break;
		case LONG:
			result = new LongField(fieldName);
			break;	
		case FLOAT:
			result = new FloatField(fieldName);
			break;
		case DOUBLE:
			result = new DoubleField(fieldName);
			break;
		case STRING:
			result = new StringField(fieldName);
			((StringField)result).setLength(length);
			break;
		case DATETIME:
		case TIMESTAMP:	
			result = new SQLTimestampField(fieldName);
			break;
		case UNIXTIMESTAMP:
			result = new UnixSQLTimestampField(fieldName);
			break;	
		case MD5:	
			result = new MD5Field(fieldName);
			break;
		case BYTEBUFFER:
			result = new ByteBufferField(fieldName);
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
	
	public FieldType getFieldType() {		
		return fieldType;
	}
	
	public boolean isValid() {
		return valid;
	}
}
