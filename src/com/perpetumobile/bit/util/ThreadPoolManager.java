package com.perpetumobile.bit.util;

import java.util.HashMap;
import java.util.Set;

import com.perpetumobile.bit.config.Config;

/**
 * @author Zoran Dukic
 *
 */
public class ThreadPoolManager {
	static private Logger logger = new Logger(ThreadPoolManager.class);
	
	static public ThreadPoolManager instance = new ThreadPoolManager();
	static public ThreadPoolManager getInstance() { return instance; }
	
	public final static String THREAD_POOL_MANAGER_ACTIVE_MAX_KEY = "ThreadPoolManager.Active.Max";
	public final static int THREAD_POOL_MANAGER_ACTIVE_MAX_DEFAULT = 20;
	
	public final static String THREAD_POOL_MANAGER_IDLE_MAX_KEY = "ThreadPoolManager.Idle.Max";
	
	private HashMap<String, ThreadPool> poolMap = new HashMap<String, ThreadPool>();
	private Object lock = new Object();
	
	private ThreadPoolManager() {
	}
	
	static public ThreadPool createPool(String taskName) {
		int maxActive = Config.getInstance().getIntClassProperty(taskName, THREAD_POOL_MANAGER_ACTIVE_MAX_KEY, THREAD_POOL_MANAGER_ACTIVE_MAX_DEFAULT);
		int maxIdle = Config.getInstance().getIntClassProperty(taskName, THREAD_POOL_MANAGER_IDLE_MAX_KEY, maxActive);
		return new ThreadPool(taskName, maxActive, maxIdle);
	}
	
	static public void run(ThreadPool pool, Runnable runnable)
	throws Exception {
		if (runnable != null) {
			if(pool != null) {
				ThreadPoolable threadPoolable = (ThreadPoolable)pool.borrowObject();
				threadPoolable.set(runnable);
			} else {
				runnable.run();
			}
		}
	}
	
	static public void join(ThreadPool pool) {
		synchronized(pool) {
			while(pool.getNumActive() > 0) {
				try {
					pool.wait(100);
				} catch (InterruptedException e) {
					logger.error("ThreadPoolManager.join exception", e);
				}
			}
		}
	}
	
	static public void exit(ThreadPool pool) {
		join(pool);
		pool.clear();
	}
	
	private ThreadPool getPool(String taskName) {
		ThreadPool pool = null;
		synchronized(lock) {
			pool = poolMap.get(taskName);
			if(pool == null) {
				pool = createPool(taskName);
				poolMap.put(taskName, pool);
			}
		}
		return pool;
	}
	
	public void run(String taskName, Runnable runnable) 
	throws Exception {
		run(getPool(taskName), runnable);
	}
	
	public void join(String taskName) {
		join(getPool(taskName));
	}
	
	public void exit(String taskName) {
		exit(getPool(taskName));
	}
	
	public void exit() {
		Set<String> tasks = poolMap.keySet();
		for(String t : tasks) {
			exit(getPool(t));
		}
	}
}
