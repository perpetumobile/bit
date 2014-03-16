package com.perpetumobile.bit.orm.record;

import org.apache.commons.pool.PoolableObjectFactory;

/**
 * 
 * @author  Zoran Dukic
 */
abstract public class RecordConnectionFactory<T extends RecordConnection<?>> implements PoolableObjectFactory {
	
	protected String configName = null;
	
	public RecordConnectionFactory(String configName) {
		this.configName = configName;
	}
	
	public void activateObject(Object obj) throws java.lang.Exception {
	}
	
	@SuppressWarnings("unchecked")
	public void destroyObject(Object obj) throws java.lang.Exception {
		((T)obj).disconnect();
	}
	
	abstract public Object makeObject() throws java.lang.Exception;
	
	public void passivateObject(Object obj) throws java.lang.Exception {
	}
	
	@SuppressWarnings("unchecked")
	public boolean validateObject(Object obj) {
		boolean result = false;
		try {
			result = ((T)obj).validate();
		} catch (Exception e) {
			result = false;
		}
		return result;
	}
}
