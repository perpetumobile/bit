package com.perpetumobile.bit.orm.record.field;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import org.apache.cassandra.thrift.Column;
import org.json.simple.JSONObject;

import com.perpetumobile.bit.orm.record.exception.FieldUnsupportedOperationException;
import com.perpetumobile.bit.util.Util;


/**
 * 
 *  @author Zoran Dukic
 */
abstract public class Field implements Serializable {
	private static final long serialVersionUID = 1L;

	public static final String NAME_SPACE_DELIMITER = ".";
	
	protected String fieldName = null;
	private ByteBuffer bbFieldName = null;
	protected boolean isSet = false;
	protected long timestamp = 0;
	
	public Field(String fieldName) {
		this.fieldName = fieldName;
	}
	
	public Field(ByteBuffer fieldName) {
		this.bbFieldName = fieldName;
	}
	
	abstract public boolean equalValue(Field f);
	public boolean equals(Object obj) {
		Field f = (Field)obj;
		if(getFieldName().equals(f.getFieldName())) {
			return equalValue(f);
		}
		return false;
	}
	
	public String getFieldName() {
		if(fieldName == null && bbFieldName != null) {
			fieldName = Util.toString(bbFieldName, FieldConfig.CHARSET_NAME);
		}
		return fieldName;
	}
	
	public ByteBuffer getByteBufferFieldName() {
		if(bbFieldName == null && fieldName != null) {
			bbFieldName = Util.toByteBuffer(fieldName, FieldConfig.CHARSET_NAME);
		}
		return bbFieldName;
	}
	
	public long getTimestamp() {
		return timestamp;
	}
	
	void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}
	
	abstract protected int bindImpl(ResultSet rs, int index) throws SQLException;
	public int bind(ResultSet rs, int index) throws SQLException {
		isSet = true;
		timestamp = Util.currentTimeMicros();
		return bindImpl(rs, index);
	}
	
	protected void bindImpl(Column column) throws Exception {
		setByteBufferFieldValueImpl(column.value);
	}
	public void bind(Column column) throws Exception {
		isSet = true;
		timestamp = column.timestamp;
		bindImpl(column);
	}
	
	abstract public int setPreparedStatementParameter(PreparedStatement stmt, int index) throws SQLException;
	
	public boolean isSet() {
		return isSet;
	}
	
	public boolean isSet(String nameSpace) {
		boolean result = false;
		if(isSet) {
			int index = fieldName.indexOf(NAME_SPACE_DELIMITER);
			if(index == -1) {
				result = true;
			} else {
				String fieldNameSpace = fieldName.substring(0, index);
				if(fieldNameSpace.equals(nameSpace)) {
					result = true;
				}
			}
		}
		return result;
	}
	
	public boolean isAutoIncrement() {
		return false;
	}
	
	public String getSQLFieldValue() {
		return getFieldValue();
	}
	
	public String getJSONFieldValue() {
		StringBuilder buf = new StringBuilder();
		String value = getFieldValue();
		if(value != null) {
			buf.append("\"");
			buf.append(JSONObject.escape(getFieldValue()));
			buf.append("\"");
		} else {
			buf.append("null");
		}	
		return buf.toString();
	}
	
	// ByteBufferField interface
	abstract public ByteBuffer getByteBufferFieldValue(); 
	abstract void setByteBufferFieldValueImpl(ByteBuffer fieldValue); 
	public void setByteBufferFieldValue(ByteBuffer fieldValue) {
		if(fieldValue != null) {
			setByteBufferFieldValueImpl(fieldValue);
			isSet = true;
			timestamp = Util.currentTimeMicros();
		}
	}
	
	// StringField interface
	abstract public String getFieldValue();
	abstract protected void setFieldValueImpl(String fieldValue);
	public void setFieldValue(String fieldValue) {
		if(fieldValue != null) {
			setFieldValueImpl(fieldValue);
			isSet = true;
			timestamp = Util.currentTimeMicros();
		}
	}
	
	// BooleanField interface
	public boolean getBooleanFieldValue() {
		throw new FieldUnsupportedOperationException("Field Name: " + getFieldName());
	}
	protected void setBooleanFieldValueImpl(boolean fieldValue) {
		throw new FieldUnsupportedOperationException("Field Name: " + getFieldName());
	}
	public void setBooleanFieldValue(boolean fieldValue) {
		setBooleanFieldValueImpl(fieldValue);
		isSet = true;
		timestamp = Util.currentTimeMicros();
	}
	
	// IntField interface
	public int getIntFieldValue() {
		throw new FieldUnsupportedOperationException("Field Name: " + getFieldName());
	}
	protected void setIntFieldValueImpl(int fieldValue) {
		throw new FieldUnsupportedOperationException("Field Name: " + getFieldName());
	}
	public void setIntFieldValue(int fieldValue) {
		setIntFieldValueImpl(fieldValue);
		isSet = true;
		timestamp = Util.currentTimeMicros();
	}
	
	// LongField interface
	public long getLongFieldValue() {
		throw new FieldUnsupportedOperationException("Field Name: " + getFieldName());
	}
	protected void setLongFieldValueImpl(long fieldValue) {
		throw new FieldUnsupportedOperationException("Field Name: " + getFieldName());
	}
	public void setLongFieldValue(long fieldValue) {
		setLongFieldValueImpl(fieldValue);
		isSet = true;
		timestamp = Util.currentTimeMicros();
	}
	
	// FloatField interface
	public float getFloatFieldValue() {
		throw new FieldUnsupportedOperationException("Field Name: " + getFieldName());
	}
	protected void setFloatFieldValueImpl(float fieldValue) {
		throw new FieldUnsupportedOperationException("Field Name: " + getFieldName());
	}
	public void setFloatFieldValue(float fieldValue) {
		setFloatFieldValueImpl(fieldValue);
		isSet = true;
		timestamp = Util.currentTimeMicros();
	}
	
	// DoubleField interface
	public double getDoubleFieldValue() {
		throw new FieldUnsupportedOperationException("Field Name: " + getFieldName());
	}
	protected void setDoubleFieldValueImpl(double fieldValue) {
		throw new FieldUnsupportedOperationException("Field Name: " + getFieldName());
	}
	public void setDoubleFieldValue(double fieldValue) {
		setDoubleFieldValueImpl(fieldValue);
		isSet = true;
		timestamp = Util.currentTimeMicros();
	}
	
	// MD5Field interface
	public String getMD5FieldValue() {
		throw new FieldUnsupportedOperationException("Field Name: " + getFieldName());
	}
	protected void setMD5FieldValueImpl(String fieldValue) {
		throw new FieldUnsupportedOperationException("Field Name: " + getFieldName());
	}
	public void setMD5FieldValue(String fieldValue) {
		if(fieldValue != null) {
			setMD5FieldValueImpl(fieldValue);
			isSet = true;
			timestamp = Util.currentTimeMicros();
		}
	}
	
	// TimestampField interface
	public Timestamp getTimestampFieldValue() {
		throw new FieldUnsupportedOperationException("Field Name: " + getFieldName());
	}
	protected void setTimestampFieldValueImpl(Timestamp fieldValue) {
		throw new FieldUnsupportedOperationException("Field Name: " + getFieldName());
	}
	public void setTimestampFieldValue(Timestamp fieldValue) {
		if(fieldValue != null) {
			setTimestampFieldValueImpl(fieldValue);
			isSet = true;
			timestamp = Util.currentTimeMicros();
		}
	}
}