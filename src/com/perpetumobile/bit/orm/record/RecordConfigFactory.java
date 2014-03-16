package com.perpetumobile.bit.orm.record;

import java.util.HashMap;

import org.apache.velocity.VelocityContext;

import com.perpetumobile.bit.util.Logger;

/**
 * 
 * @author  Zoran Dukic
 */
abstract public class RecordConfigFactory<T extends RecordConfig> {
	static private Logger logger = new Logger(RecordConfigFactory.class);
	
	protected HashMap<String, T> recordConfigs = new HashMap<String, T>(); 
	private Object lock = new Object();
	
	public RecordConfigFactory() {
	}
	
	abstract protected T createRecordConfig(String configName, VelocityContext vc) throws Exception;
	
	public T getRecordConfig(String configName) {	
		return getRecordConfig(configName, null);
	}
	
	public T getRecordConfig(String configName, VelocityContext vc) {	
		T result = null;
		
		synchronized(lock) {
			result = recordConfigs.get(configName);
			if (result == null) {
				try {
					result = createRecordConfig(configName, vc);
					recordConfigs.put(configName, result);
				} catch (Exception e) {
					logger.error("RecordConfigFactory.getRecordConfig exception for " + configName, e);
					result = null;
				}	
			}
		}
		
		return result;
	}
}

