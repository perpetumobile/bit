package com.perpetumobile.bit.orm.record.field;

import java.nio.ByteBuffer;
import java.sql.Timestamp;

import com.perpetumobile.bit.util.Util;

/**
 * @author Zoran Dukic
 *
 */
public class UnixSQLTimestampField extends LongField {
	
	public UnixSQLTimestampField(String fieldName) {
		super(fieldName);
	}
	
	public UnixSQLTimestampField(String fieldName, long value) {
		super(fieldName, value);
	}
	
	public UnixSQLTimestampField(String fieldName, Timestamp value) {
		super(fieldName, value.getTime() / 1000);
	}
	
	public UnixSQLTimestampField(ByteBuffer fieldName) {
		super(fieldName);
	}
	
	public UnixSQLTimestampField(ByteBuffer fieldName, long value) {
		super(fieldName, value);
	}
	
	public UnixSQLTimestampField(ByteBuffer fieldName, Timestamp value) {
		super(fieldName, value.getTime() / 1000);
	}
	
	public String getFieldValue() {
		Timestamp t = new Timestamp(value * 1000);
		return t.toString();
	}
	
	protected void setFieldValueImpl(String fieldValue) {
		value = Util.toLong(fieldValue, -1);
		if(value < 0) {
			Timestamp t = Timestamp.valueOf(fieldValue);
			if(t != null) {
				value = t.getTime() / 1000;
			}
		}
	}
	
	public Timestamp getTimestampFieldValue() {
		return new Timestamp(value * 1000);
	}
		
	protected void setTimestampFieldValueImpl(Timestamp fieldValue) {
		value = fieldValue.getTime() / 1000;
	}
}
