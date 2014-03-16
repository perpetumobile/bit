package com.perpetumobile.bit.orm.cassandra.exception;

/**
 * @author Zoran Dukic
 *
 */
public class CassandraUnsupportedOperationException extends RuntimeException {
	static final long serialVersionUID = 1L;
	
	public CassandraUnsupportedOperationException() {
		super();
	}
	
	public CassandraUnsupportedOperationException(String message) {
		super(message);
	}
}
