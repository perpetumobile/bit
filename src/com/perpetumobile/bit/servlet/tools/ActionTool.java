package com.perpetumobile.bit.servlet.tools;

import com.perpetumobile.bit.config.Config;
import com.perpetumobile.bit.servlet.actions.ActionControler;
import com.perpetumobile.bit.util.Util;


public class ActionTool extends BaseTool {
	static final public String ACTION_CONTROLER_CLASS_CONFIG_KEY = ".Action.Controler.Class";
	
	public ActionTool() {
	}
	
	@SuppressWarnings("unchecked")
	public ActionControler getControler(String action) 
	throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		if(!Util.nullOrEmptyString(action)) {
			String className = Config.getInstance().getProperty(action+ACTION_CONTROLER_CLASS_CONFIG_KEY, null);
			if(!Util.nullOrEmptyString(className)) {
				Class<ActionControler> actionControlerClass = (Class<ActionControler>)Class.forName(className);
				if(actionControlerClass != null) {
					ActionControler actionControler = actionControlerClass.newInstance();
					actionControler.init(action, context, request, response);
					return actionControler;
				}
			}
		}
		return null;
	}
}
