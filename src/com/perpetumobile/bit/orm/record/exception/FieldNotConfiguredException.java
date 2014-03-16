package com.perpetumobile.bit.orm.record.exception;

/**
 * @author Zoran Dukic
 *
 */
public class FieldNotConfiguredException extends RuntimeException {
	static final long serialVersionUID = 1L;
	
	
	public FieldNotConfiguredException() {
		super();
	}
	
	public FieldNotConfiguredException(String message) {
		super(message);
	}
}
