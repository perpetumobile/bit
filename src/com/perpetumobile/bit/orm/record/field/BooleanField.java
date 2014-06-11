package com.perpetumobile.bit.orm.record.field;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.perpetumobile.bit.orm.record.exception.FieldTypeMismatchException;
import com.perpetumobile.bit.util.Util;


/**
 * @author Zoran Dukic
 *
 */
public class BooleanField extends Field {
	private boolean value = false;
	
	public BooleanField(String fieldName) {
		super(fieldName);
	}
	
	public BooleanField(String fieldName, boolean value) {
		super(fieldName);
		this.value = value;
	}
	
	public BooleanField(ByteBuffer fieldName) {
		super(fieldName);
	}
	
	public BooleanField(ByteBuffer fieldName, boolean value) {
		super(fieldName);
		this.value = value;
	}
	
	public boolean equalValue(Field f) {
		if(f instanceof BooleanField) {
			return value == ((BooleanField) f).value;
		}
		return false;
	}
	
	public String getFieldValue() {
		return Boolean.toString(value);
	}
	
	public String getJSONFieldValue() {
		return getFieldValue();
	}
	
	public boolean getBooleanFieldValue() {
		return value;
	}
	
	protected void setFieldValueImpl(String fieldValue) {
		value = Util.toBoolean(fieldValue);
	}
	
	protected void setBooleanFieldValueImpl(boolean fieldValue) {
		value = fieldValue;
	}
	
	public int bindImpl(ResultSet rs, int index) 
	throws SQLException {
		value = rs.getInt(index++) > 0;
		return index;
	}

	public int setPreparedStatementParameter(PreparedStatement stmt, int index)
	throws SQLException {
		if(value) {
			stmt.setInt(index++, 1);
		} else {
			stmt.setInt(index++, 0);
		}
		return index;
	}
	
	protected void setByteBufferFieldValueImpl(ByteBuffer fieldValue) {
		if(fieldValue.remaining() > 4 || fieldValue.remaining() < 1) {
			throw new FieldTypeMismatchException("Field Name: " + getFieldName());
		}
		value = new BigInteger(Util.toBytes(fieldValue)).intValue() > 0;
	}
	
	public ByteBuffer getByteBufferFieldValue() {		
		return Util.toByteBuffer(value);
	}
}
