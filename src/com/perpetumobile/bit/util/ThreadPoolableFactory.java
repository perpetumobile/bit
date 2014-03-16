package com.perpetumobile.bit.util;

import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.PoolableObjectFactory;

/**
 * @author Zoran Dukic
 *
 */
public class ThreadPoolableFactory implements PoolableObjectFactory {
	private ObjectPool pool = null;
	private String threadName = null;
	
	public ThreadPoolableFactory(String threadName, ObjectPool pool) {
		this.pool = pool;
		this.threadName = threadName;
	}
	
	public void activateObject(Object obj) throws Exception {
	}
	
	public void destroyObject(Object obj) throws Exception {
		((ThreadPoolable)obj).exit();
	}
	
	public Object makeObject() throws Exception {
		ThreadPoolable result = new ThreadPoolable(pool);
		new Thread(result, threadName).start();
		return result;
	}
	
	public void passivateObject(Object obj) throws Exception {
	}
	
	public boolean validateObject(Object obj) {
		return ((ThreadPoolable)obj).validate();
	}
}
