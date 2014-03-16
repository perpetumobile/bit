package com.perpetumobile.bit.servlet.actions;

import java.util.ArrayList;

import org.apache.velocity.context.Context;

public class FieldValidator {
	
	protected String fieldName = null;
	protected String configKey = null;
	
	protected ArrayList<ValidatorConfig> validatorConfigs = null;
	
	public FieldValidator(String fieldName, String parentConfigKey) {
		this.fieldName = fieldName;
		init(parentConfigKey);
	}
	
	protected void init(String parentConfigKey) {	
		StringBuffer buf = new StringBuffer(parentConfigKey);
		buf.append(".");
		buf.append(fieldName);
		configKey = buf.toString();
	
		validatorConfigs = new ArrayList<ValidatorConfig>();
		int i=1;
		while(true) {
			buf =  new StringBuffer();
			buf.append(i++);
			buf.append(".");
			buf.append(configKey);
			ValidatorConfig vc = new ValidatorConfig(fieldName, buf.toString());
			if(vc.isConfigValid()) {
				validatorConfigs.add(vc);
			} else {
				break;
			}
		}		
	}
	
	public boolean validate(Context context) throws Exception {
		boolean result = true;
		
		for (ValidatorConfig vc : validatorConfigs) {
			result = vc.validate(context);
			if(!result) {
				break;
			}
		}
		
		return result;
	}
}
