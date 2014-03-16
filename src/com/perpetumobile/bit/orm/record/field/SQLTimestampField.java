package com.perpetumobile.bit.orm.record.field;

import java.nio.ByteBuffer;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import com.perpetumobile.bit.orm.db.DBUtil;
import com.perpetumobile.bit.util.Util;


/**
 * @author Zoran Dukic
 *
 */
public class SQLTimestampField extends Field {
	protected Timestamp value = null;
	protected String sqlFieldValue = null;
	
	public SQLTimestampField(String fieldName) {
		super(fieldName);
	}
	
	public SQLTimestampField(String fieldName, Timestamp value) {
		super(fieldName);
		this.value = value;
	}
	
	public SQLTimestampField(ByteBuffer fieldName) {
		super(fieldName);
	}
	
	public SQLTimestampField(ByteBuffer fieldName, Timestamp value) {
		super(fieldName);
		this.value = value;
	}
	
	public boolean equalValue(Field f) {
		if(f instanceof SQLTimestampField) {
			if(value != null) {
				return value.equals(((SQLTimestampField) f).value);
			}
		}
		return false;
	}
	
	public String getFieldValue() {
		if(value != null) {
			return value.toString();
		}
		return null;
	}
	
	public String getSQLFieldValue() {
		return sqlFieldValue;
	}
	
	protected void setFieldValueImpl(String fieldValue) {
		try {
			long millis = Util.toLong(fieldValue, -1);
			if(millis >= 0) {
				value = new Timestamp(millis);
				sqlFieldValue = DBUtil.encodeSQLString(value.toString());
			} else {
				value = Timestamp.valueOf(fieldValue);
				sqlFieldValue = DBUtil.encodeSQLString(fieldValue);
			}
		} catch (IllegalArgumentException e) {
			sqlFieldValue = DBUtil.encodeSQLString(fieldValue);
		}
	}
	
	public Timestamp getTimestampFieldValue() {
		return value;
	}
	
	protected void setTimestampFieldValueImpl(Timestamp fieldValue) {
		value = fieldValue;
		if(value!=null) {
			sqlFieldValue = DBUtil.encodeSQLString(value.toString());
		}
	}
	
	protected void setLongFieldValueImpl(long fieldValue) {
		value = new Timestamp(fieldValue);
		sqlFieldValue = DBUtil.encodeSQLString(value.toString());
	}
	
	public int bindImpl(ResultSet rs, int index) 
	throws SQLException {
		value = rs.getTimestamp(index++);
		if(value!=null) {
			sqlFieldValue = DBUtil.encodeSQLString(value.toString());
		}
		return index;
	}
	
	public int setPreparedStatementParameter(PreparedStatement stmt, int index)
	throws SQLException {
		stmt.setTimestamp(index++, value);
		return index;
	}
	
	protected void setByteBufferFieldValueImpl(ByteBuffer fieldValue) {
		value = (Timestamp)Util.toObject(fieldValue);
		if(value!=null) {
			sqlFieldValue = DBUtil.encodeSQLString(value.toString());
		}
	}
	
	public ByteBuffer getByteBufferFieldValue() {
		return Util.toByteBuffer(value);
	}
}

