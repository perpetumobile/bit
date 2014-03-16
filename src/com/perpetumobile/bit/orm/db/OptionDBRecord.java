package com.perpetumobile.bit.orm.db;

import com.perpetumobile.bit.util.OptionImpl;


/**
 * @author Zoran Dukic
 *
 */
public class OptionDBRecord extends DBRecord {

	protected OptionImpl option = null;
	
	public OptionDBRecord(String optionValue) {
		this.option = new OptionImpl(optionValue);
	}
	
	public OptionDBRecord(String optionValue, String optionText) {
		this.option = new OptionImpl(optionValue, optionText);
	}
		
	public String getOptionValue() {
		return option.getOptionValue();
	}
	
	public String getOptionText() {
		return option.getOptionText();
	}
}
