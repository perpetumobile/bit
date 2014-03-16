package com.perpetumobile.bit.servlet.tools;

import com.perpetumobile.bit.servlet.PageServlet;

/**
 * @author Zoran Dukic
 */
abstract public class AbstractAuthTool extends BaseTool {
	
	public AbstractAuthTool() {
	}
	
	protected boolean authenticate(String templatePathList, String templatePath) {
		String[] templatePathArray = templatePathList.split(PageServlet.AUTH_TEMPLATE_PATH_DELIMITER);
		if(templatePathArray != null) {
			for (int i=0; i<templatePathArray.length; i++) {
				if (templatePathArray[i].equals(templatePath) || templatePathArray[i].equals(PageServlet.AUTH_TEMPLATE_PATH_ALL)) {
					return true;
				}
			}
		}
		return false;
	}
	
	abstract public boolean authenticate(String templatePath) throws Exception;
}
