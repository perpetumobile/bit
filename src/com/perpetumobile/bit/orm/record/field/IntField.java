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
public class IntField extends Field {
	private int value = 0;
	private boolean autoIncrement = false;
	
	public IntField(String fieldName) {
		super(fieldName);
	}
	
	public IntField(String fieldName, boolean autoIncrement) {
		super(fieldName);
		this.autoIncrement = autoIncrement;
	}
	
	public IntField(String fieldName, int value) {
		super(fieldName);
		this.value = value;
	}
	
	public IntField(String fieldName, int value, boolean autoIncrement) {
		super(fieldName);
		this.value = value;
		this.autoIncrement = autoIncrement;
	}
	
	public IntField(ByteBuffer fieldName) {
		super(fieldName);
	}
	
	public IntField(ByteBuffer fieldName, boolean autoIncrement) {
		super(fieldName);
		this.autoIncrement = autoIncrement;
	}
	
	public IntField(ByteBuffer fieldName, int value) {
		super(fieldName);
		this.value = value;
	}
	
	public IntField(ByteBuffer fieldName, int value, boolean autoIncrement) {
		super(fieldName);
		this.value = value;
		this.autoIncrement = autoIncrement;
	}
	
	public boolean equalValue(Field f) {
		if(f instanceof IntField) {
			return value == ((IntField) f).value;
		}
		return false;
	}
	
	public String getFieldValue() {
		return Integer.toString(value);
	}
	
	public int getIntFieldValue() {
		return value;
	}
	
	public long getLongFieldValue() {
		return value;
	}
	
	public float getFloatFieldValue() {
		return value;
	}
	
	public double getDoubleFieldValue() {
		return value;
	}
	
	protected void setFieldValueImpl(String fieldValue) {
		value = Util.toInt(fieldValue);
	}
	
	protected void setIntFieldValueImpl(int fieldValue) {
		value = fieldValue;
	}
	
	public int bindImpl(ResultSet rs, int index) 
	throws SQLException {
		value = rs.getInt(index++);
		return index;
	}

	public int setPreparedStatementParameter(PreparedStatement stmt, int index)
	throws SQLException {
		stmt.setInt(index++, value);
		return index;
	}
	
	public boolean isAutoIncrement() {
		return autoIncrement;
	}
	
	protected void setByteBufferFieldValueImpl(ByteBuffer fieldValue) {
		if(fieldValue.remaining() > 4 || fieldValue.remaining() < 1) {
			throw new FieldTypeMismatchException("Field Name: " + getFieldName());
		}
		value = new BigInteger(Util.toBytes(fieldValue)).intValue();
	}
	
	public ByteBuffer getByteBufferFieldValue() {		
		return Util.toByteBuffer(value, true);
	}
}
