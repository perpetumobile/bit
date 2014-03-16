package com.perpetumobile.bit.util;

import org.apache.log4j.Level;

import org.apache.velocity.runtime.log.LogChute;
import org.apache.velocity.runtime.RuntimeServices;

/**
 *
 * @author  Zoran Dukic
 */
public class VelocityLogger implements LogChute {
	static private Logger logger = new Logger(VelocityLogger.class);
	public static final String PREFIX = " Velocity : ";

	public VelocityLogger() {
	}

	public void init(RuntimeServices rs) throws Exception {
	}

	public boolean isLevelEnabled(int level) {
		switch (level) {
		case LogChute.WARN_ID:
			return logger.isEnabledFor(Level.WARN);
		case LogChute.INFO_ID:
			return logger.isEnabledFor(Level.INFO);
		case LogChute.DEBUG_ID:
			return logger.isEnabledFor(Level.DEBUG);
		case LogChute.ERROR_ID:
			return logger.isEnabledFor(Level.ERROR);
		}
		return false;
	}
	
	/**
	 * Send a log message from Velocity.
	 */
	public void log(int level, String message, Throwable t) {
		switch (level) {
		case LogChute.WARN_ID:
			logger.warn(PREFIX + message, t);
			break;
		case LogChute.INFO_ID:
			logger.info(PREFIX + message, t);
			break;
		case LogChute.DEBUG_ID:
			logger.debug(PREFIX + message, t);
			break;
		case LogChute.ERROR_ID:
			logger.error(PREFIX + message, t);
			break;
		default:
			logger.info(PREFIX + message, t);
			break;
		}
	}
	
	/**
	 * Send a log message from Velocity.
	 */
	public void log(int level, String message) {
		switch (level) {
		case LogChute.WARN_ID:
			logger.warn(PREFIX + message);
			break;
		case LogChute.INFO_ID:
			logger.info(PREFIX + message);
			break;
		case LogChute.DEBUG_ID:
			logger.debug(PREFIX + message);
			break;
		case LogChute.ERROR_ID:
			logger.error(PREFIX + message);
			break;
		default:
			logger.info(PREFIX + message);
			break;
		}
	}
}

