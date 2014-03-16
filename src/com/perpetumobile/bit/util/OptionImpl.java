package com.perpetumobile.bit.util;

public class OptionImpl implements Option {
	private String optionValue;
	private String optionText;
	
	public OptionImpl(String optionValue) {
		this.optionValue = optionValue;
		this.optionText = optionValue;
	}
	
	public OptionImpl(String optionValue, String optionText) {
		this.optionValue = optionValue;
		this.optionText = optionText;
	}
	
	public String getOptionValue() {
		return optionValue;
	}
	
	public String getOptionText() {
		return optionText;
	}

}
