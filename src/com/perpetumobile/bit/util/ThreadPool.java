package com.perpetumobile.bit.util;

import org.apache.commons.pool.impl.GenericObjectPool;

public class ThreadPool extends GenericObjectPool {

	public ThreadPool(String threadName, int maxActive, int maxIdle) {
		setFactory(new ThreadPoolableFactory(threadName, this));
		setMaxActive(maxActive);
		setMaxIdle(maxIdle);
		setTestOnBorrow(true);
	}
}
