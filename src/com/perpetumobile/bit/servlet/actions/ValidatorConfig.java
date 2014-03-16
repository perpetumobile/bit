package com.perpetumobile.bit.servlet.actions;

import org.apache.velocity.context.Context;

import com.perpetumobile.bit.config.Config;
import com.perpetumobile.bit.util.Util;


public class ValidatorConfig {

	static final public String CLASS_NAME_CONFIG_KEY = ".Class";
	final static public String ERROR_MESSAGE_CONFIG_KEY = ".ErrorMessage";
	static final public String ARGS_CONFIG_KEY = ".Args";
	
	protected String className = null;
	
	protected String fieldName = null;
	protected String errorMessage = null;
	protected String[] args = null;
	
	public ValidatorConfig(String fieldName, String configKey) {
		this.fieldName = fieldName;
		init(configKey);
	}
	
	protected void init(String configKey) {
		className = Config.getInstance().getProperty(configKey + CLASS_NAME_CONFIG_KEY, null);
		errorMessage = Config.getInstance().getProperty(configKey + ERROR_MESSAGE_CONFIG_KEY, null);
		String strArgs = Config.getInstance().getProperty(configKey + ARGS_CONFIG_KEY, null);
		if (strArgs != null) {
			args = strArgs.split(ActionControler.ACTION_CONFIG_VALUES_DELIMITER);
		}
	}
	
	public String getFieldName() {
		return fieldName;
	}
	
	public String getErrorMessage() {
		return errorMessage;
	}
	
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	
	public String[] getArgs() {
		return args;
	}
	
	public void setArgs(String[] args) {
		this.args = args;
	}
	
	public boolean isConfigValid() {
		return !Util.nullOrEmptyString(className);
	}
	
	@SuppressWarnings("unchecked")
	public boolean validate(Context context) throws Exception {
		boolean result = true;
		Class<Validator> validatorClass = (Class<Validator>)Class.forName(className);
		if(validatorClass != null) {
			Validator validator = validatorClass.newInstance();
			validator.init(context);
			result = validator.validate(this);
		}
		return result;
	}
}
