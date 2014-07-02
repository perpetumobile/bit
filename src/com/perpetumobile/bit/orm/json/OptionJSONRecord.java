package com.perpetumobile.bit.orm.json;

import com.perpetumobile.bit.util.OptionImpl;


/**
 * @author Zoran Dukic
 *
 */
public class OptionJSONRecord extends JSONRecord {
	private static final long serialVersionUID = 1L;

	protected OptionImpl option = null;
	
	public OptionJSONRecord(String optionValue) {
		this.option = new OptionImpl(optionValue);
	}
	
	public OptionJSONRecord(String optionValue, String optionText) {
		this.option = new OptionImpl(optionValue, optionText);
	}
		
	public String getOptionValue() {
		return option.getOptionValue();
	}
	
	public String getOptionText() {
		return option.getOptionText();
	}
}
