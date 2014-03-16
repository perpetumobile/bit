package com.perpetumobile.bit.orm.record.exception;

/**
 * @author Zoran Dukic
 *
 */
public class KeyFieldNotConfiguredException extends RuntimeException {
	static final long serialVersionUID = 1L;
	
	public KeyFieldNotConfiguredException() {
		super();
	}
	
	public KeyFieldNotConfiguredException(String message) {
		super(message);
	}
}
