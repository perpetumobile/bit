package com.perpetumobile.bit.orm.record.exception;

/**
 * @author Zoran Dukic
 *
 */
public class FieldUnsupportedOperationException extends RuntimeException {
	static final long serialVersionUID = 1L;
	
	public FieldUnsupportedOperationException() {
		super();
	}
	
	public FieldUnsupportedOperationException(String message) {
		super(message);
	}
}
