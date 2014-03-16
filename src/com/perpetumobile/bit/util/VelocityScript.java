package com.perpetumobile.bit.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.StringWriter;
import java.util.HashMap;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;

import com.perpetumobile.bit.config.Config;

/**
 * @author Zoran Dukic
 */
class VelocityEngineManager {
	static private Logger logger = new Logger(VelocityEngineManager.class);
	
	static private VelocityEngineManager instance = new VelocityEngineManager();
	static public VelocityEngineManager getInstance() { return instance; }
	
	private HashMap<String, VelocityEngine> engines = new HashMap<String, VelocityEngine>(); 
	private Object lock = new Object();
	
	private VelocityEngineManager() {
	}
	
	private VelocityEngine createVelocityEngine(String parentDirectory) {
		VelocityEngine ve = new VelocityEngine();
		ve.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS, VelocityLogger.class.getName());
		ve.setProperty(RuntimeConstants.FILE_RESOURCE_LOADER_PATH, parentDirectory);
		ve.setProperty(RuntimeConstants.VM_LIBRARY, "");
		ve.init();
		return ve;
	}
	
	public VelocityEngine getVelocityEngine(String parentDirectory) {	
		VelocityEngine result = null;
		synchronized(lock) {
			result = engines.get(parentDirectory);
			if (result == null) {
				try {
					result = createVelocityEngine(parentDirectory);
					engines.put(parentDirectory, result);
				} catch (Exception e) {
					logger.error("VelocityEngineManager.getVelocityEngine exception for " + parentDirectory, e);
					result = null;
				}	
			}
		}
		return result;
	}
}

public class VelocityScript {
	static private Logger logger = new Logger(VelocityScript.class);
	
	protected VelocityEngine velocity;
	protected VelocityContext context;
	protected File scriptFile = null;
	protected Template template = null;
	
	public VelocityScript(String scriptName, String scriptToolName)
	throws Exception {
		scriptFile = new File(scriptName);
	
		velocity = VelocityEngineManager.getInstance().getVelocityEngine(scriptFile.getParent());
		
		context = new VelocityContext();
		context.put("Util", new Util());
		context.put("Config", Config.getInstance());
		context.put(scriptToolName, this);
		
		template = velocity.getTemplate(scriptFile.getName());
	}
	
	public VelocityScript(String scriptName, String scriptToolName, VelocityEngine velocity, VelocityContext context)  
	throws Exception {
		if(context == null) {
			context = new VelocityContext();
			context.put("Util", new Util());
			context.put("Config", Config.getInstance());
			context.put(scriptToolName, this);
		}
				
		if(context.get(scriptToolName) == null) {
			context.put(scriptToolName, this);
		}
		
		this.velocity = velocity;
		this.context = context	;
		template = velocity.getTemplate(scriptName);
	}
	
	public VelocityEngine getVelocityEngine() { 
		return velocity; 
	}
	
	public VelocityContext getContext() { 
		return context; 
	}
	
	public void addToContext(String name, String className) {
		try {
			Class<?> clazz = Class.forName(className);
			if(clazz != null) {
				Object o = clazz.newInstance();
				context.put(name, o);
			}
		} catch (Exception e) {
			logger.error("VelocityScript.addToContext exception for " + name, e);
		}
	}
	
	public String generate() throws Exception {		
		StringWriter stringWriter = new StringWriter();
		BufferedWriter writer = new BufferedWriter(stringWriter);
		template.merge(context, writer);
		writer.flush();
		writer.close();
		return stringWriter.getBuffer().toString();
	}
}
