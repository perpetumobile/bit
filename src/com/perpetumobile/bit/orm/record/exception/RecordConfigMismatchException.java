package com.perpetumobile.bit.orm.record.exception;

/**
 * @author Zoran Dukic
 *
 */
public class RecordConfigMismatchException extends RuntimeException {
	static final long serialVersionUID = 1L;
	
	public RecordConfigMismatchException() {
		super();
	}
	
	public RecordConfigMismatchException(String message) {
		super(message);
	}
}
