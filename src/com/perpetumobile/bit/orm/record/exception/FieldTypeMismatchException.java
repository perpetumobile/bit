package com.perpetumobile.bit.orm.record.exception;

/**
 * @author Zoran Dukic
 *
 */
public class FieldTypeMismatchException extends RuntimeException {
	static final long serialVersionUID = 1L;
	
	public FieldTypeMismatchException() {
		super();
	}
	
	public FieldTypeMismatchException(String message) {
		super(message);
	}
}
