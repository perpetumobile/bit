package com.perpetumobile.bit.orm.xml;

import com.perpetumobile.bit.util.OptionImpl;


/**
 * @author Zoran Dukic
 *
 */
public class OptionXMLRecord extends XMLRecord {
	private static final long serialVersionUID = 1L;

	protected OptionImpl option = null;
	
	public OptionXMLRecord(String optionValue) {
		this.option = new OptionImpl(optionValue);
	}
	
	public OptionXMLRecord(String optionValue, String optionText) {
		this.option = new OptionImpl(optionValue, optionText);
	}
		
	public String getOptionValue() {
		return option.getOptionValue();
	}
	
	public String getOptionText() {
		return option.getOptionText();
	}
}
