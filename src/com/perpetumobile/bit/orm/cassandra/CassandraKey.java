package com.perpetumobile.bit.orm.cassandra;

import java.nio.ByteBuffer;

import com.perpetumobile.bit.orm.record.field.ByteBufferField;


public class CassandraKey extends ByteBufferField {
	private static final long serialVersionUID = 1L;
	
	static public final String FIELD_NAME = "CassandraKey";
	
	private CassandraKey() {
		super(FIELD_NAME);
	}
	
	private CassandraKey(ByteBuffer value) {
		super(FIELD_NAME, value);
	}
	
	static public CassandraKey empty() {
		return new CassandraKey(ByteBuffer.allocate(0));
	}
	
	static public CassandraKey create(ByteBuffer k) {
		CassandraKey key = new CassandraKey();
		key.setByteBufferFieldValue(k);
		return key;
	}
	
	static public CassandraKey create(String k) {
		CassandraKey key = new CassandraKey();
		key.setFieldValue(k);
		return key;
	}
	
	static public CassandraKey create(int k) {
		CassandraKey key = new CassandraKey();
		key.setIntFieldValue(k);
		return key;
	}
	
	static public CassandraKey create(long k) {
		CassandraKey key = new CassandraKey();
		key.setLongFieldValue(k);
		return key;
	}
	
	static public CassandraKey create(float k) {
		CassandraKey key = new CassandraKey();
		key.setFloatFieldValue(k);
		return key;
	}
	
	static public CassandraKey create(double k) {
		CassandraKey key = new CassandraKey();
		key.setDoubleFieldValue(k);
		return key;
	}
}
