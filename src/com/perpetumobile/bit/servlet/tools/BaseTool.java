package com.perpetumobile.bit.servlet.tools;

import org.apache.velocity.context.Context;
import org.apache.velocity.tools.view.ViewContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author  Zoran Dukic
 */
public class BaseTool {
	
	static public final String LOG_TOOL_CONTEXT_KEY = "Log";
	static public final String PARAMS_TOOL_CONTEXT_KEY = "Params";
	static public final String COOKIES_TOOL_CONTEXT_KEY = "Cookies";
	
	protected Context context = null;
	protected HttpServletRequest request = null;
	protected HttpServletResponse response = null;
	
	private LogTool logTool = null;
	private ParamTool params = null;
	private CookieTool cookies = null;
	
	public BaseTool() {
	}
	
	public void init(Object initData) {
		if (initData instanceof ViewContext) {
			context = ((ViewContext) initData).getVelocityContext();
			request = ((ViewContext) initData).getRequest();
			response = ((ViewContext) initData).getResponse();
		} else {
			throw new IllegalArgumentException("Was expecting " + ViewContext.class);
		}
	}
	
	public LogTool getLogTool() {
		if(logTool == null && context != null) {
			logTool = (LogTool)context.get(LOG_TOOL_CONTEXT_KEY);
		}
		return logTool;
	}
	
	public ParamTool getParams() {
		if(params == null && context != null) {
			params = (ParamTool)context.get(PARAMS_TOOL_CONTEXT_KEY);
		}
		return params;
	}
	
	public CookieTool getCookies() {
		if(cookies == null && context != null) {
			cookies = (CookieTool)context.get(COOKIES_TOOL_CONTEXT_KEY);
		}
		return cookies;
	}
}
