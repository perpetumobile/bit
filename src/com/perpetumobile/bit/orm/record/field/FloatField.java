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
public class FloatField extends Field {
	private float value = 0;
	
	public FloatField(String fieldName) {
		super(fieldName);
	}
	
	public FloatField(String fieldName, float value) {
		super(fieldName);
		this.value = value;
	}
	
	public FloatField(ByteBuffer fieldName) {
		super(fieldName);
	}
	
	public FloatField(ByteBuffer fieldName, float value) {
		super(fieldName);
		this.value = value;
	}
	
	public boolean equalValue(Field f) {
		if(f instanceof FloatField) {
			return value == ((FloatField) f).value;
		}
		return false;
	}
	
	public String getFieldValue() {
		return Float.toString(value);
	}
	
	public String getJSONFieldValue() {
		return getFieldValue();
	}
			
	public float getFloatFieldValue() {
		return value;
	}
	
	public double getDoubleFieldValue() {
		return value;
	}
	
	protected void setFieldValueImpl(String fieldValue) {
		value = Util.toFloat(fieldValue);
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
	
	public int bindImpl(ResultSet rs, int index) 
	throws SQLException {
		value = rs.getFloat(index++);
		return index;
	}
	
	public int setPreparedStatementParameter(PreparedStatement stmt, int index)
	throws SQLException {
		stmt.setFloat(index++, value);
		return index;
	}
	
	protected void setByteBufferFieldValueImpl(ByteBuffer fieldValue) {
		if(fieldValue.remaining() != 4) {
			throw new FieldTypeMismatchException("Field Name: " + getFieldName());
		}
		fieldValue.mark();
		value = fieldValue.getFloat();
		fieldValue.reset();
	}
	
	public ByteBuffer getByteBufferFieldValue() {
		ByteBuffer buf = ByteBuffer.allocate(4);
		buf.putFloat(value);
		buf.flip();
		return buf;
	}
}
