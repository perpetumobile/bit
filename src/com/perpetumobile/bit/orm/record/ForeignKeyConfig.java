package com.perpetumobile.bit.orm.record;

/**
 * 
 * @author Zoran Dukic
 */

public class ForeignKeyConfig {
	
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
