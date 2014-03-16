package com.perpetumobile.bit.util;

import org.apache.log4j.Priority;

/**
 * @author Zoran Dukic
 */
final public class Logger {
	private org.apache.log4j.Logger logger = null;
	
	public Logger(String name) {
		logger = org.apache.log4j.Logger.getLogger(name);
	}
	
	public Logger(Class<?> clazz) {
		logger = org.apache.log4j.Logger.getLogger(clazz);
	}
	
	public boolean isEnabledFor(Priority priority) {
		return logger.isEnabledFor(priority);
	}
	
	public boolean isInfoEnabled() {
		return logger.isInfoEnabled();
	}
	
	public boolean isDebugEnabled() {
		return logger.isDebugEnabled();
	}
	
	/*
	 * Log a message with the FATAL Level. 
	 */
	public void fatal(String message) { 
		logger.fatal(message);
	}
	
	/*
	 * Log a message with the FATAL level including the stack trace of the Throwable t 
	 * passed as parameter. 
	 */
	public void fatal(String message, Throwable t) {
		logger.fatal(message, t);
	}
	
	/*
	 * Log a message with the ERROR Level. 
	 */
	public void error(String message) { 
		logger.error(message);
	}
	
	/*
	 * Log a message with the ERROR level including the stack trace of the Throwable t 
	 * passed as parameter. 
	 */
	public void error(String message, Throwable t) {
		logger.error(message, t);
	}
	
	/*
	 * Log a message with the WARN Level. 
	 */
	public void warn(String message) { 
		logger.warn(message);
	}
	
	/*
	 * Log a message with the WARN level including the stack trace of the Throwable t 
	 * passed as parameter. 
	 */
	public void warn(String message, Throwable t) {
		logger.warn(message, t);
	}
	
	/*
	 * Log a message with the INFO Level. 
	 */
	public void info(String message) { 
		logger.info(message);
	}
	
	/*
	 * Log a message with the INFO level including the stack trace of the Throwable t 
	 * passed as parameter. 
	 */
	public void info(String message, Throwable t) {
		logger.info(message, t);
	}
	
	/*
	 * Log a message with the DEBUG Level. 
	 */
	public void debug(String message) { 
		logger.debug(message);
	}
	
	/*
	 * Log a message with the DEBUG level including the stack trace of the Throwable t 
	 * passed as parameter. 
	 */
	public void debug(String message, Throwable t) {
		logger.debug(message, t);
	}
}
