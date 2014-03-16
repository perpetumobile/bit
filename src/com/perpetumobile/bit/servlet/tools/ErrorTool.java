package com.perpetumobile.bit.servlet.tools;

import java.util.HashMap;

public class ErrorTool extends BaseTool {
	
	private HashMap<String, String> errorMap = new HashMap<String, String>();
	private HashMap<String, ErrorMessage> errorMessageMap = new HashMap<String, ErrorMessage>();
	
	public ErrorTool() {
	}
	
	public void addError(String key, String message) {
		errorMap.put(key, "1");
		ErrorMessage em = errorMessageMap.get(key);
		if(em != null) {
			em.addMessage(message);
		} else {
			em = new ErrorMessage(key, message);
			errorMessageMap.put(key, em);
		}
	}
		
	public boolean isError(String key) {
		return (errorMap.get(key) != null);
	}
	
	public ErrorMessage getErrorMessage(String key) {
		return errorMessageMap.get(key);
	}
	
	public String getMessage(String key, String delimiter) {
		ErrorMessage em = errorMessageMap.get(key);
		if(em != null) {
			return em.getMessage(delimiter);
		}
		return null;
	}
}