package com.perpetumobile.bit.orm.record;

import java.io.Serializable;

/**
 * 
 * @author Zoran Dukic
 */

public class ForeignKeyConfig implements Serializable {
	private static final long serialVersionUID = 1L;
	
	protected String fieldName = null;
	protected String foreignFieldName = null;
	
	public ForeignKeyConfig(String fieldName, String foreignFieldName) {
		this.fieldName = fieldName;
		this.foreignFieldName = foreignFieldName;
	}

	/**
	 * @return the fieldName
	 */
	public String getFieldName() {
		return fieldName;
	}

	/**
	 * @return the foreignFieldName
	 */
	public String getForeignFieldName() {
		return foreignFieldName;
	}
}
