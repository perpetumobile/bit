package com.perpetumobile.bit.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.tools.view.ServletUtils;
import org.apache.velocity.tools.view.VelocityView;

import com.perpetumobile.bit.config.Config;
import com.perpetumobile.bit.servlet.tools.AbstractAuthTool;
import com.perpetumobile.bit.util.Logger;
import com.perpetumobile.bit.util.Util;



class AuthConfig {
	
	public static final String PAGE_SERVLET_AUTH_TOOL_KEY = ".PageServlet.Auth.Tool";
	public static final String PAGE_SERVLET_AUTH_LOGIN_TEMPLATE_KEY = ".PageServlet.Auth.Login.Template";
	public static final String PAGE_SERVLET_AUTH_TEMPLATE_PATHS_KEY = ".PageServlet.Auth.TemplatePaths";
	
	protected String authToolName = null;
	protected String authLoginTemplate = null;
	protected String[] authTemplatePaths = null;
	
	public AuthConfig(String appName) {
		authToolName = Config.getInstance().getProperty(appName + PAGE_SERVLET_AUTH_TOOL_KEY, null);
		authLoginTemplate = Config.getInstance().getProperty(appName + PAGE_SERVLET_AUTH_LOGIN_TEMPLATE_KEY, null);
		String paths = Config.getInstance().getProperty(appName + PAGE_SERVLET_AUTH_TEMPLATE_PATHS_KEY, null);
		if(!Util.nullOrEmptyString(paths)) {
			authTemplatePaths = paths.split(PageServlet.AUTH_TEMPLATE_PATH_DELIMITER);
		}
	}
	
	public boolean isValid() {
		return !(Util.nullOrEmptyString(authToolName) && Util.nullOrEmptyString(authLoginTemplate) && authTemplatePaths != null);
	}	
} 

class Auth {
	protected AuthConfig ac = null;
	protected String templatePath = null;
	
	public Auth(AuthConfig ac, String templatePath) {
		this.ac = ac;
		this.templatePath = templatePath;
	}
	
	public String getAuthLoginTemplate() {
		return ac.authLoginTemplate;
	}
	
	public boolean auth(Context context) 
	throws Exception {
		AbstractAuthTool authTool = (AbstractAuthTool)context.get(ac.authToolName);
		return authTool.authenticate(templatePath);
	}
}

/**
 * @author Zoran Dukic
 *
 */
public class PageServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	static private Logger logger = new Logger(PageServlet.class);
	
	static final public String SERVLET_CONTEXT_KEY = ServletContext.class.getName();
	
	public static final String VELOCITY_PROPERTIES_FILE = "velocity.properties";
	public static final String VELOCITY_TOOLBOX_FILE = "/WEB-INF/toolbox.xml";
	
	static final public String PAGE_SERVLET_AUTH_APP_LIST_KEY = "PageServlet.Auth.App.List";
	static final public String PAGE_SERVLET_AUTH_APP_LIST_DELIMITER = " ";
	static final public String AUTH_TEMPLATE_PATH_DELIMITER = ";";
	static final public String AUTH_TEMPLATE_PATH_ALL = "*";
	
	public static final String PAGE_SERVLET_ERROR_404_TEMPLATE_KEY = "PageServlet.Error.404.Template";
	public static final String PAGE_SERVLET_ERROR_MSG_TEMPLATE_KEY = "PageServlet.Error.Msg.Template";
	
	public static final String PAGE_SERVLET_ERROR_MSG_CONTEXT_KEY = "PageServlet-ErrorMsg";
	public static final String PAGE_SERVLET_ERROR_STACK_TRACE_CONTEXT_KEY = "PageServlet-ErrorStackTrace";
	
	protected ArrayList<AuthConfig> authConfigList = new ArrayList<AuthConfig>();
	
	protected String error404Template = null;
	protected String errorMsgTemplate = null;
	
	protected VelocityView velocityView = null;	
	
	/**
	 * Initializes servlet, toolbox and Velocity template engine. 
	 * Called by the servlet container on loading.
	 */
	public void init(ServletConfig config)
	throws ServletException {
		
		super.init(config);
		
		velocityView = new VelocityView(getServletContext());
		
		String authApps = Config.getInstance().getProperty(PAGE_SERVLET_AUTH_APP_LIST_KEY, null);
		if(!Util.nullOrEmptyString(authApps)) {
			String[] authAppList = authApps.split(PAGE_SERVLET_AUTH_APP_LIST_DELIMITER);
			for(String aa : authAppList) {
				AuthConfig ac = new AuthConfig(aa.trim());
				if(ac.isValid()) {
					authConfigList.add(ac);
				}
			}
		}
		
		error404Template = Config.getInstance().getProperty(PAGE_SERVLET_ERROR_404_TEMPLATE_KEY, null);
		errorMsgTemplate = Config.getInstance().getProperty(PAGE_SERVLET_ERROR_MSG_TEMPLATE_KEY, null);
	}
	
	public void doGet(HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException {
		doRequest(request, response);
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException {
		doRequest(request, response);
	}

	protected void doRequest(HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException {
		Context context = null;
		String templatePath = ServletUtils.getPath(request);
		try {
			HttpHeaderUtil.debugHeaders(logger, request);
					
			// set request character encoding
			HttpHeaderUtil.setRequestCharacterEncoding(request);
			// first, get a context
			context = velocityView.createContext(request, response);
			// authenticate
			Auth auth = getAuth(templatePath);
			if(auth != null && !auth.auth(context)) {
				templatePath = auth.getAuthLoginTemplate();
			}
			// process template
			if(!processTemplate(request, response, context, templatePath)) {
				templatePath = error404Template;
				processTemplate(request, response, context, error404Template);
			}
		} catch (Exception e) {
			processErrorMsgTemplate(request, response, context, templatePath, e);
		}
	}
	
	private Auth getAuth(String templatePath) {
		for(AuthConfig ac : authConfigList) {
			for(String path : ac.authTemplatePaths) {
				if(templatePath.startsWith(path)) {
					return new Auth(ac, path);
				}
			}
		}
		return null;
	}
	
	private boolean processTemplate(HttpServletRequest request, HttpServletResponse response, Context context, String templateName)
	throws Exception {
		Template template = null;
		try {
			template = velocityView.getTemplate(templateName);
			if (template != null) {
				HttpResource httpResource = new HttpResource(request, template.getName(), template.getLastModified(), 0, null);
				if(!httpResource.isCacheable() || HttpHeaderUtil.checkIfHeaders(request, response, httpResource)) {
					// set content response header
					HttpHeaderUtil.setContentResponseHeaders(request, response, httpResource);
					velocityView.merge(template, context, response.getWriter());
				} else {
					// only etag needs to be set if 304 (Not Modified)
					HttpHeaderUtil.setETagResponseHeaders(request, response, httpResource);
				}
				return true;
			}
		} catch (ResourceNotFoundException e) {
			logger.warn("PageServlet.processTemplate: Resource not found: " + templateName);
		}
		return false;
	}

	private String getErrorStackTrace(Throwable e) {	
		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));
		return sw.toString().replace("\n", "<br>");
	}
	
	protected void processErrorMsgTemplate(HttpServletRequest request, HttpServletResponse response, Context context, String templatePath, Exception e)
	throws ServletException {
		Throwable cause = e;
		// if it's an MIE, i want the real stack trace!
		if (cause instanceof MethodInvocationException) {
			// get the real cause
			cause = ((MethodInvocationException) cause).getWrappedThrowable();
		}
		
		logger.error("PageServlet: Exception processing template: "+templatePath, cause);
		
		try {
			context.put(PAGE_SERVLET_ERROR_MSG_CONTEXT_KEY, e.getMessage());
			context.put(PAGE_SERVLET_ERROR_STACK_TRACE_CONTEXT_KEY, getErrorStackTrace(cause));
			processTemplate(request, response, context, errorMsgTemplate);
		} catch (Exception e2) {
			logger.error("PageServlet: Exception processing error msg template: "+errorMsgTemplate, e2);
			throw new ServletException(e);
		}
	}
}
