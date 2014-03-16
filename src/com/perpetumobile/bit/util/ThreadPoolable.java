package com.perpetumobile.bit.util;

import org.apache.commons.pool.ObjectPool;

/**
 * @author Zoran Dukic
 *
 */
public class ThreadPoolable implements Runnable {
	static private Logger logger = new Logger(ThreadPoolable.class);
	
	private ObjectPool pool = null;
	
	private Runnable runnable = null;
	private boolean returned = false;
	private boolean exit = false;
	
	public ThreadPoolable(ObjectPool pool) { 
		this.pool = pool;
	}

	protected synchronized void exit() {
		exit = true;
		notifyAll();
	}
	
	protected synchronized boolean validate() {		
		return !exit;
	}	
	
	protected synchronized void set(Runnable runnable) {
		this.runnable = runnable;
		notify();
	}	
	
	public synchronized void run() {
		try {
			while (!exit) {
				if (runnable != null) {
					try {
						runnable.run();
					} catch (Exception e) {
						logger.error("ThreadPoolable.run runnable.run() exception", e);
					}
					runnable = null;

					try {
						pool.returnObject(this);
						returned = true;
					} catch (Exception e) {
						logger.error("ThreadPoolable.run pool.returnObject() exception", e);
					}
				}
				if (!exit) {
					try {
						wait();
						returned = false;
					} catch (InterruptedException e) {
						logger.error("ThreadPoolable.run wait() InterruptedException",e);
					}
				}
			}
		} catch (Throwable t) {
			// invalidate this ThreadPoolable in case of some outside condition (e.g. out-of-memory)
			if (!returned) {
				try { pool.invalidateObject(this);	} catch (Exception e) {}
			}
			logger.error("ThreadPoolable.run catch block", t);
		} finally {
			exit = true;
		}
	}
}
