package com.perpetumobile.bit.config;

import com.perpetumobile.bit.util.Option;
import com.perpetumobile.bit.util.Util;


/**
 * @author Zoran Dukic
 */
public class ConfigFormOption implements Option {

	private String optionValue = null;
	private String optionText = null;
	
	public ConfigFormOption(String configName) {
		init(configName);
	}
	
	private void init(String configName) {
		optionValue = Config.getInstance().getProperty(configName+".Value", "");
		optionText = Config.getInstance().getProperty(configName+".Text", "");
		
		if(Util.nullOrEmptyString(optionValue)) {
			optionValue = optionText;
		}
		
		if(Util.nullOrEmptyString(optionText)) {
			optionText = optionValue;
		}
	}
	
	public boolean isValid() {
		return !optionValue.equals("");
	}
	
	public String getOptionValue() {
		return optionValue;
	}

	public String getOptionText() {
		return optionText;
	}

}
