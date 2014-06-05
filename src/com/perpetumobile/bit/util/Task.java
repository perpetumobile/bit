package com.perpetumobile.bit.util;

/**
 * @author Zoran Dukic
 *
 */
abstract public class Task implements Runnable {
	static private Logger logger = new Logger(Task.class);
	
	private boolean started = false;
	private Object startedLock = new Object();
		
	private boolean done = false;
	private Object doneLock = new Object();
	
	private boolean success = true;
	
	protected TaskCallback<Task> callback = null;
	
	public Task() { 
	}

	@SuppressWarnings("unchecked")
	public void setCallback(TaskCallback<? extends Task> callback) {
		this.callback = (TaskCallback<Task>)callback;
	}

	public void reset() {
		started = false;
		done = false;
	}
	
	public boolean isStarted() {
		synchronized(startedLock) {
			while(!started) {
				try {
					startedLock.wait();
				} catch (InterruptedException e) {
					logger.error("Task.isStarted InterruptedException",e);
				}
			}
		}
		return true;
	}
	
	public boolean isDone() {
		synchronized(doneLock) {
			while(!done) {
				try {
					doneLock.wait();
				} catch (InterruptedException e) {
					logger.error("Task.isDone InterruptedException",e);
				}
			}
		}
		return true;
	}
		
	protected void start() {
		synchronized(startedLock) {
			started = true;
			startedLock.notifyAll();
		}
	}
	
	protected void done() {
		synchronized(doneLock) {
			done = true;
			doneLock.notifyAll();
		}
	}
	
	@Override
	public void run() {
		start();
		try {
			runImpl();
		} catch (Exception e) {
			success = false;
		}
		done();
		if(callback != null) callback.onTaskDone(this);
	}
	
	public boolean isSuccess() {
		return success;
	}

	abstract public void runImpl() throws Exception;
}
