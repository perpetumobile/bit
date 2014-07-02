package com.perpetumobile.bit.orm.record.field;

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
public class LongField extends Field {
	private static final long serialVersionUID = 1L;
	
	protected long value = 0;
	
	public LongField(String fieldName) {
		super(fieldName);
	}
	
	public LongField(String fieldName, long value) {
		super(fieldName);
		this.value = value;
	}
	
	public LongField(ByteBuffer fieldName) {
		super(fieldName);
	}
	
	public LongField(ByteBuffer fieldName, long value) {
		super(fieldName);
		this.value = value;
	}
	
	public boolean equalValue(Field f) {
		if(f instanceof LongField) {
			return value == ((LongField) f).value;
		}
		return false;
	}
	
	public String getFieldValue() {
		return Long.toString(value);
	}
	
	public String getJSONFieldValue() {
		return getFieldValue();
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
		value = Util.toLong(fieldValue);
	}
	
	protected void setIntFieldValueImpl(int fieldValue) {
		value = fieldValue;
	}
	
	protected void setLongFieldValueImpl(long fieldValue) {
		value = fieldValue;
	}
	
	public int bindImpl(ResultSet rs, int index) 
	throws SQLException {
		value = rs.getLong(index++);
		return index;
	}
	
	public int setPreparedStatementParameter(PreparedStatement stmt, int index)
	throws SQLException {
		stmt.setLong(index++, value);
		return index;
	}
	
	protected void setByteBufferFieldValueImpl(ByteBuffer fieldValue) {
		if(fieldValue.remaining() != 8) {
			throw new FieldTypeMismatchException("Field Name: " + getFieldName());
		}
		fieldValue.mark();
		value = fieldValue.getLong();
		fieldValue.reset();
	}
	
	public ByteBuffer getByteBufferFieldValue() {
		return Util.toByteBuffer(value, false);
	}
}
