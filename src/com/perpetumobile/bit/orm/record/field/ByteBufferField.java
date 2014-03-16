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
public class ByteBufferField extends Field {
	private ByteBuffer value = null;
	
	public ByteBufferField(String fieldName) {
		super(fieldName);
	}
	
	public ByteBufferField(String fieldName, ByteBuffer value) {
		super(fieldName);
		this.value = value;
	}
	
	public ByteBufferField(ByteBuffer fieldName) {
		super(fieldName);
	}
	
	public ByteBufferField(ByteBuffer fieldName, ByteBuffer value) {
		super(fieldName);
		this.value = value;
	}
	
	public boolean equalValue(Field f) {
		if(f instanceof ByteBufferField) {
			if(value != null) {
				return value.equals(((ByteBufferField) f).value);
			}
		}
		return false;
	}
	
	public String getFieldValue() {
		return Util.toString(value, FieldConfig.CHARSET_NAME);
	}
	
	public int getIntFieldValue() {
		if(value.remaining() > 4 || value.remaining() < 1) {
			throw new FieldTypeMismatchException("Field Name: " + getFieldName());
		}		
		return new BigInteger(Util.toBytes(value)).intValue();
	}
	
	public long getLongFieldValue() {
		if(value.remaining() != 8) {
			throw new FieldTypeMismatchException("Field Name: " + getFieldName());
		}		
		value.mark();
		long result = value.getLong();
		value.reset();
		return result;
	}
	
	public float getFloatFieldValue() {
		if(value.remaining() != 4) {
			throw new FieldTypeMismatchException("Field Name: " + getFieldName());
		}
		value.mark();
		float result = value.getFloat();
		value.reset();
		return result;
	}
	
	public double getDoubleFieldValue() {
		if(value.remaining() != 8) {
			throw new FieldTypeMismatchException("Field Name: " + getFieldName());
		}
		value.mark();
		double result = value.getDouble();
		value.reset();
		return result;
	}
	
	protected void setFieldValueImpl(String fieldValue) {
		value = Util.toByteBuffer(fieldValue, FieldConfig.CHARSET_NAME);
	}
	
	protected void setIntFieldValueImpl(int fieldValue) {
		value = Util.toByteBuffer(fieldValue, true);
	}
	
	protected void setLongFieldValueImpl(long fieldValue) {
		value = Util.toByteBuffer(fieldValue, false);
	}
	
	protected void setFloatFieldValueImpl(float fieldValue) {
		value = ByteBuffer.allocate(4);
		value.putFloat(fieldValue);
		value.flip();
	}
	
	protected void setDoubleFieldValueImpl(double fieldValue) {
		value = ByteBuffer.allocate(8);
		value.putDouble(fieldValue);
		value.flip();
	}
	
	public int bindImpl(ResultSet rs, int index) 
	throws SQLException {
		value = Util.toByteBuffer(rs.getString(index++), FieldConfig.CHARSET_NAME);
		return index;
	}
	
	public int setPreparedStatementParameter(PreparedStatement stmt, int index)
	throws SQLException {
		stmt.setString(index++, Util.toString(value, FieldConfig.CHARSET_NAME));
		return index;
	}
	
	protected void setByteBufferFieldValueImpl(ByteBuffer fieldValue) {
		value = fieldValue;
	}
	
	public ByteBuffer getByteBufferFieldValue() {
		return value;
	}
}
