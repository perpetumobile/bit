package com.perpetumobile.bit.servlet.actions;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.context.Context;

import com.perpetumobile.bit.config.Config;
import com.perpetumobile.bit.servlet.tools.CookieTool;
import com.perpetumobile.bit.servlet.tools.ErrorTool;
import com.perpetumobile.bit.servlet.tools.LogTool;
import com.perpetumobile.bit.servlet.tools.ParamTool;
import com.perpetumobile.bit.util.Util;


abstract public class ActionControler {
	static final public String ACTION_SOURCE_TEMPLATE_CONFIG_KEY = ".Action.Template.Source";
	
	static final public String ACTION_PREVIOUS_URL_CONFIG_KEY = ".Action.URL.Previous";
	static final public String ACTION_NEXT_URL_CONFIG_KEY = ".Action.URL.Next";
	static final public String ACTION_CANCEL_URL_CONFIG_KEY = ".Action.URL.Cancel";
	
	static final public String ACTION_PREVIOUS_URL_PARAM_KEY = "action_previous";
	static final public String ACTION_CANCEL_URL_PARAM_KEY = "action_cancel";
	
	static final public String ACTION_VALIDATOR_FIELDS_CONFIG_KEY = ".Action.Validator.Fields";
	static final public String ACTION_CONFIG_VALUES_DELIMITER = ",";
	
	protected String name = null;
	
	protected Context context = null;
	protected HttpServletRequest request = null;
	protected HttpServletResponse response = null;
	
	protected ArrayList<FieldValidator> fieldValidators = null;
	
	public ActionControler() {
	}
	
	public void init(String name, Context context, HttpServletRequest request, HttpServletResponse response) {
		this.name = name;
		fieldValidators = new ArrayList<FieldValidator>();
		
		String fieldsConfigKey = name+ACTION_VALIDATOR_FIELDS_CONFIG_KEY;
		String strFieldNames = Config.getInstance().getProperty(fieldsConfigKey, null);
		if( ! Util.nullOrEmptyString(strFieldNames)) {
			String[] fieldNames = strFieldNames.split(ACTION_CONFIG_VALUES_DELIMITER);
			for(String fieldName : fieldNames) {
				FieldValidator fv = new FieldValidator(fieldName, fieldsConfigKey);
				fieldValidators.add(fv);
			}
		}
	
		this.context = context;
		this.request = request;
		this.response = response;
	}
		
	protected ParamTool getParamsTool() {
		return (ParamTool)context.get("Params");
	}
	
	protected CookieTool getCookiesTool() {
		return (CookieTool)context.get("Cookies");
	}
	
	protected ErrorTool getErrorTool() {
		return (ErrorTool)context.get("Errors");
	}
	
	protected LogTool getLogTool() {
		return (LogTool)context.get("Log");
	}
	
	public boolean validate() throws Exception {
		boolean result = true;

		// we want to validate all fields
		// even if we can early decide that the validation is failing
		for (FieldValidator fv : fieldValidators) {
			boolean isValid = fv.validate(context);
			if(!isValid) {
				result = false;
			}
		}
		return result;
	}
	
	abstract public boolean execute() throws Exception;
	
	public boolean run() throws Exception {
		String cancel = getParamsTool().getString(ACTION_CANCEL_URL_PARAM_KEY);
		if(!Util.nullOrEmptyString(cancel)) {
			response.sendRedirect(getCancelURL());
			return true;
		}
		String previous = getParamsTool().getString(ACTION_PREVIOUS_URL_PARAM_KEY);
		if(!Util.nullOrEmptyString(previous)) {
			response.sendRedirect(getPreviousURL());
			return true;
		} 
		if(validate() && execute()) {
			response.sendRedirect(getNextURL());
			return true;
		} 
		return false;
	}
	
	public String getSourceTemplate() {
		return Config.getInstance().getProperty(name+ACTION_SOURCE_TEMPLATE_CONFIG_KEY, null);
	}
	
	public String getPreviousURL() {
		return Config.getInstance().getProperty(name+ACTION_PREVIOUS_URL_CONFIG_KEY, null);
	}
	
	public String getNextURL() {
		return Config.getInstance().getProperty(name+ACTION_NEXT_URL_CONFIG_KEY, null);
	}
	
	public String getCancelURL() {
		return Config.getInstance().getProperty(name+ACTION_CANCEL_URL_CONFIG_KEY, null);
	}
}
