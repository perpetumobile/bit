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
public class DoubleField extends Field {
	private double value = 0;
	
	public DoubleField(String fieldName) {
		super(fieldName);
	}
	
	public DoubleField(String fieldName, double value) {
		super(fieldName);
		this.value = value;
	}
	
	public DoubleField(ByteBuffer fieldName) {
		super(fieldName);
	}
	
	public DoubleField(ByteBuffer fieldName, double value) {
		super(fieldName);
		this.value = value;
	}

	public boolean equalValue(Field f) {
		if(f instanceof DoubleField) {
			return value == ((DoubleField) f).value;
		}
		return false;
	}
	
	public String getFieldValue() {
		return Double.toString(value);
	}
	
	public String getJSONFieldValue() {
		return getFieldValue();
	}
	
	public double getDoubleFieldValue() {
		return value;
	}
	
	protected void setFieldValueImpl(String fieldValue) {
		value = Util.toDouble(fieldValue);
	}
	
	protected void setIntFieldValueImpl(int fieldValue) {
		value = fieldValue;
	}
	
	protected void setLongFieldValueImpl(long fieldValue) {
		value = fieldValue;
	}
	
	protected void setFloatFieldValueImpl(float fieldValue) {
		value = fieldValue;
	}
	
	protected void setDoubleFieldValueImpl(double fieldValue) {
		value = fieldValue;
	}
	
	public int bindImpl(ResultSet rs, int index) 
	throws SQLException {
		value = rs.getDouble(index++);
		return index;
	}
	
	public int setPreparedStatementParameter(PreparedStatement stmt, int index)
	throws SQLException {
		stmt.setDouble(index++, value);
		return index;
	}
	
	protected void setByteBufferFieldValueImpl(ByteBuffer fieldValue) {
		if(fieldValue.remaining() != 8) {
			throw new FieldTypeMismatchException("Field Name: " + getFieldName());
		}
		fieldValue.mark();
		value = fieldValue.getDouble();
		fieldValue.reset();
	}
	
	public ByteBuffer getByteBufferFieldValue() {
		ByteBuffer buf = ByteBuffer.allocate(8);
		buf.putDouble(value);
		buf.flip();
		return buf;
	}
}
