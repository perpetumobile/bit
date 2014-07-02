package com.perpetumobile.bit.orm.record.field;

import java.nio.ByteBuffer;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.perpetumobile.bit.orm.db.DBUtil;
import com.perpetumobile.bit.util.Util;

/**
 * @author Zoran Dukic
 *
 */
public class StringField extends Field {
	private static final long serialVersionUID = 1L;
	
	private String value = null;
	private int length = -1;
	
	public StringField(String fieldName) {
		super(fieldName);
	}
	
	public StringField(String fieldName, String value) {
		super(fieldName);
		this.value = value;
	}
	
	public StringField(ByteBuffer fieldName) {
		super(fieldName);
	}
	
	public StringField(ByteBuffer fieldName, String value) {
		super(fieldName);
		this.value = value;
	}
	
	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public boolean equalValue(Field f) {
		if(f instanceof StringField) {
			if(value != null) {
				return value.equals(((StringField) f).value);
			} else if (((StringField) f).value == null) {
				return true;
			}
		}
		return false;
	}
	
	public String getFieldValue() {
		return value;
	}
	
	public String getSQLFieldValue() {
		if(length > 0) {
			return DBUtil.encodeSQLString(value, length);
		}
		return DBUtil.encodeSQLString(value);
	}
	
	protected void setFieldValueImpl(String fieldValue) {
		value = fieldValue;
	}
	
	public int bindImpl(ResultSet rs, int index) 
	throws SQLException {
		value = rs.getString(index++);
		return index;
	}
	
	public int setPreparedStatementParameter(PreparedStatement stmt, int index)
	throws SQLException {
		String val = value;
		if(length > 0) {
			val = value.substring(0, length);
		}
		stmt.setString(index++, val);
		return index;
	}
	
	protected void setByteBufferFieldValueImpl(ByteBuffer fieldValue) {
		value = Util.toString(fieldValue, FieldConfig.CHARSET_NAME);
	}
	
	public ByteBuffer getByteBufferFieldValue() {
		return Util.toByteBuffer(value, FieldConfig.CHARSET_NAME);
	}
}
