package com.perpetumobile.bit.servlet.actions;

import org.apache.velocity.context.Context;

import com.perpetumobile.bit.servlet.tools.CookieTool;
import com.perpetumobile.bit.servlet.tools.ErrorTool;
import com.perpetumobile.bit.servlet.tools.ParamTool;


abstract public class Validator {
	
	protected Context context = null;
	
	public Validator() {
	}
	
	public void init(Context context) {
		this.context = context;
	}
	
	abstract public boolean validate(ValidatorConfig vc) throws Exception;
	
	protected ParamTool getParamTool() {
		return (ParamTool)context.get("Params");
	}
	
	protected CookieTool getCookieTool() {
		return (CookieTool)context.get("Cookies");
	}
	
	protected ErrorTool getErrorTool() {
		return (ErrorTool)context.get("Errors");
	}
	
	protected void addError(String fieldName, String message) {
		getErrorTool().addError(fieldName, message);	
	}	
}
