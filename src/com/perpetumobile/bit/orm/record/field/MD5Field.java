package com.perpetumobile.bit.orm.record.field;

import java.nio.ByteBuffer;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.perpetumobile.bit.orm.db.DBUtil;
import com.perpetumobile.bit.util.Util;


public class MD5Field extends Field {
	private String value = null;
	private String md5Value = null;
	
	public MD5Field(String fieldName) {
		super(fieldName);
	}
	
	public MD5Field(String fieldName, String value) {
		super(fieldName);
		this.value = value;
	}
	
	public MD5Field(ByteBuffer fieldName) {
		super(fieldName);
	}
	
	public MD5Field(ByteBuffer fieldName, String value) {
		super(fieldName);
		this.value = value;
	}
	
	public boolean equalValue(Field f) {
		if(f instanceof MD5Field) {
			if(value != null) {
				return value.equals(((MD5Field) f).value);
			}
			if(md5Value != null) {
				return md5Value.equals(((MD5Field) f).md5Value);
			}
		}
		return false;
	}
	
	public String getFieldValue() {
		return value;
	}
	
	protected void setFieldValueImpl(String fieldValue) {
		value = fieldValue;
	}
	
	public String getMD5FieldValue() {
		return md5Value;
	}
	
	protected void setMD5FieldValueImpl(String fieldValue) {
		md5Value = fieldValue;
	}
	
	public String getSQLFieldValue() {
		if(value != null) {
			StringBuffer buf = new StringBuffer("MD5(");
			buf.append(DBUtil.encodeSQLString(value));
			buf.append(")");
			return buf.toString();
		}
		return DBUtil.encodeSQLString(md5Value);
	}
	
	public int bindImpl(ResultSet rs, int index) 
	throws SQLException {
		md5Value = rs.getString(index++);
		return index;
	}
	
	public int setPreparedStatementParameter(PreparedStatement stmt, int index)
	throws SQLException {
		stmt.setString(index++, md5Value);
		return index;
	}
	
	protected void setByteBufferFieldValueImpl(ByteBuffer fieldValue) {
		value = Util.toString(fieldValue, FieldConfig.CHARSET_NAME);
	}
	
	public ByteBuffer getByteBufferFieldValue() {
		return Util.toByteBuffer(md5Value, FieldConfig.CHARSET_NAME);
	}
}
