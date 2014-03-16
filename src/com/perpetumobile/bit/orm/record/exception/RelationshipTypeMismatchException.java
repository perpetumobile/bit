package com.perpetumobile.bit.orm.record.exception;

/**
 * @author Zoran Dukic
 *
 */
public class RelationshipTypeMismatchException extends RuntimeException {
	static final long serialVersionUID = 1L;
	
	public RelationshipTypeMismatchException() {
		super();
	}
	
	public RelationshipTypeMismatchException(String message) {
		super(message);
	}
}
