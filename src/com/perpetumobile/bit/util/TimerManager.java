package com.perpetumobile.bit.util;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * TimerManager will schedule and manage java timer tasks.
 * Create a subclass of TimerTask and implement the run()
 * method and then use one of the schedule() methods to schedule it
 * with the timer.
 *
 * @author  Zoran Dukic
 */
public class TimerManager {
	static private TimerManager instance = new TimerManager();
	static public TimerManager getInstance() { return instance; }
	
	private HashMap<String, TimerTaskWrapper> tasks = null;
	private Timer timer = null;
	
	private Object lock = new Object();
	
	private TimerManager() {
		tasks = new HashMap<String, TimerTaskWrapper>();
		timer = new Timer(true);
	}
	
	private void run(TimerTask task) {
		task.run();
	}
	
	/**
	 * Cancels a task by name.
	 *
	 * @param name  name of the task to be canceled.
	 */
	public void cancel(String name) {
		synchronized(lock) {
			TimerTaskWrapper task = tasks.get(name);
			if (task != null) {
				task.cancel();
				tasks.remove(name);
			}
		}
	}
	
	private TimerTaskWrapper addTask(String name, TimerTask task) {
		synchronized(lock) {
			TimerTaskWrapper result = tasks.get(name);
			if (result == null) {
				result = new TimerTaskWrapper(name, task);
				tasks.put(name, result);
			} else {
				throw new IllegalStateException("TimerTask " + name + " already scheduled!");
			}
			return result;
		}
	}
	
	/**
	 * Schedules the specified task for execution after the specified delay.
	 *
	 * @param name  name of the task to be scheduled.
	 * @param task  task to be scheduled.
	 * @param delay delay in milliseconds before task is to be executed.
	 * @throws IllegalArgumentException if <tt>delay</tt> is negative, or
	 *         <tt>delay + System.currentTimeMillis()</tt> is negative.
	 * @throws IllegalStateException if task was already scheduled or
	 *         cancelled, or timer was cancelled.
	 */
	public void schedule(String name, TimerTask task, long delay) {
		TimerTaskWrapper addedTask = addTask(name, task);
		if(addedTask != null) {
			timer.schedule(addedTask, delay);
		}
	}
	
	/**
	 * Schedules the specified task for execution at the specified time.  If
	 * the time is in the past, the task is scheduled for immediate execution.
	 *
	 * @param name name of the task to be scheduled.
	 * @param task task to be scheduled.
	 * @param time time at which task is to be executed.
	 * @throws IllegalArgumentException if <tt>time.getTime()</tt> is negative.
	 * @throws IllegalStateException if task was already scheduled or
	 *         cancelled, timer was cancelled, or timer thread terminated.
	 */
	public void schedule(String name, TimerTask task, Date time) {
		TimerTaskWrapper addedTask = addTask(name, task);
		if(addedTask != null) {
			timer.schedule(addedTask, time);
		}
	}
	
	/**
	 * Schedules the specified task for repeated <i>fixed-delay execution</i>,
	 * beginning after the specified delay.  Subsequent executions take place
	 * at approximately regular intervals separated by the specified period.
	 *
	 * <p>In fixed-delay execution, each execution is scheduled relative to
	 * the actual execution time of the previous execution.  If an execution
	 * is delayed for any reason (such as garbage collection or other
	 * background activity), subsequent executions will be delayed as well.
	 * In the long run, the frequency of execution will generally be slightly
	 * lower than the reciprocal of the specified period (assuming the system
	 * clock underlying <tt>Object.wait(long)</tt> is accurate).
	 *
	 * <p>Fixed-delay execution is appropriate for recurring activities
	 * that require "smoothness."  In other words, it is appropriate for
	 * activities where it is more important to keep the frequency accurate
	 * in the short run than in the long run.  This includes most animation
	 * tasks, such as blinking a cursor at regular intervals.  It also includes
	 * tasks wherein regular activity is performed in response to human
	 * input, such as automatically repeating a character as long as a key
	 * is held down.
	 *
	 * @param name   name of the task to be scheduled.
	 * @param task   task to be scheduled.
	 * @param delay  delay in milliseconds before task is to be executed.
	 * @param period time in milliseconds between successive task executions.
	 * @throws IllegalArgumentException if <tt>delay</tt> is negative, or
	 *         <tt>delay + System.currentTimeMillis()</tt> is negative.
	 * @throws IllegalStateException if task was already scheduled or
	 *         cancelled, timer was cancelled, or timer thread terminated.
	 */
	public void schedule(String name, TimerTask task, long delay, long period) {
		TimerTaskWrapper addedTask = addTask(name, task);
		if(addedTask != null) {
			addedTask.setPeriod(period);
			timer.schedule(addedTask, delay, period);
		}
	}
	
	/**
	 * Schedules the specified task for repeated <i>fixed-delay execution</i>,
	 * beginning at the specified time. Subsequent executions take place at
	 * approximately regular intervals, separated by the specified period.
	 *
	 * <p>In fixed-delay execution, each execution is scheduled relative to
	 * the actual execution time of the previous execution.  If an execution
	 * is delayed for any reason (such as garbage collection or other
	 * background activity), subsequent executions will be delayed as well.
	 * In the long run, the frequency of execution will generally be slightly
	 * lower than the reciprocal of the specified period (assuming the system
	 * clock underlying <tt>Object.wait(long)</tt> is accurate).
	 *
	 * <p>Fixed-delay execution is appropriate for recurring activities
	 * that require "smoothness."  In other words, it is appropriate for
	 * activities where it is more important to keep the frequency accurate
	 * in the short run than in the long run.  This includes most animation
	 * tasks, such as blinking a cursor at regular intervals.  It also includes
	 * tasks wherein regular activity is performed in response to human
	 * input, such as automatically repeating a character as long as a key
	 * is held down.
	 *
	 * @param name name of the task to be scheduled.
	 * @param task task to be scheduled.
	 * @param firstTime First time at which task is to be executed.
	 * @param period time in milliseconds between successive task executions.
	 * @throws IllegalArgumentException if <tt>time.getTime()</tt> is negative.
	 * @throws IllegalStateException if task was already scheduled or
	 *         cancelled, timer was cancelled, or timer thread terminated.
	 */
	public void schedule(String name, TimerTask task, Date firstTime, long period) {
		TimerTaskWrapper addedTask = addTask(name, task);
		if(addedTask != null) {
			addedTask.setPeriod(period);
			timer.schedule(addedTask, firstTime, period);
		}
	}
	
	/**
	 * Schedules the specified task for repeated <i>fixed-rate execution</i>,
	 * beginning after the specified delay.  Subsequent executions take place
	 * at approximately regular intervals, separated by the specified period.
	 *
	 * <p>In fixed-rate execution, each execution is scheduled relative to the
	 * scheduled execution time of the initial execution.  If an execution is
	 * delayed for any reason (such as garbage collection or other background
	 * activity), two or more executions will occur in rapid succession to
	 * "catch up."  In the long run, the frequency of execution will be
	 * exactly the reciprocal of the specified period (assuming the system
	 * clock underlying <tt>Object.wait(long)</tt> is accurate).
	 *
	 * <p>Fixed-rate execution is appropriate for recurring activities that
	 * are sensitive to <i>absolute</i> time, such as ringing a chime every
	 * hour on the hour, or running scheduled maintenance every day at a
	 * particular time.  It is also appropriate for for recurring activities
	 * where the total time to perform a fixed number of executions is
	 * important, such as a countdown timer that ticks once every second for
	 * ten seconds.  Finally, fixed-rate execution is appropriate for
	 * scheduling multiple repeating timer tasks that must remain synchronized
	 * with respect to one another.
	 *
	 * @param name   name of the task to be scheduled.
	 * @param task   task to be scheduled.
	 * @param delay  delay in milliseconds before task is to be executed.
	 * @param period time in milliseconds between successive task executions.
	 * @throws IllegalArgumentException if <tt>delay</tt> is negative, or
	 *         <tt>delay + System.currentTimeMillis()</tt> is negative.
	 * @throws IllegalStateException if task was already scheduled or
	 *         cancelled, timer was cancelled, or timer thread terminated.
	 */
	public void scheduleAtFixedRate(String name, TimerTask task, long delay, long period) {
		TimerTaskWrapper addedTask = addTask(name, task);
		if(addedTask != null) {
			addedTask.setPeriod(period);
			timer.schedule(addedTask, delay, period);
		}
	}
	
	/**
	 * Schedules the specified task for repeated <i>fixed-rate execution</i>,
	 * beginning at the specified time. Subsequent executions take place at
	 * approximately regular intervals, separated by the specified period.
	 *
	 * <p>In fixed-rate execution, each execution is scheduled relative to the
	 * scheduled execution time of the initial execution.  If an execution is
	 * delayed for any reason (such as garbage collection or other background
	 * activity), two or more executions will occur in rapid succession to
	 * "catch up."  In the long run, the frequency of execution will be
	 * exactly the reciprocal of the specified period (assuming the system
	 * clock underlying <tt>Object.wait(long)</tt> is accurate).
	 *
	 * <p>Fixed-rate execution is appropriate for recurring activities that
	 * are sensitive to <i>absolute</i> time, such as ringing a chime every
	 * hour on the hour, or running scheduled maintenance every day at a
	 * particular time.  It is also appropriate for for recurring activities
	 * where the total time to perform a fixed number of executions is
	 * important, such as a countdown timer that ticks once every second for
	 * ten seconds.  Finally, fixed-rate execution is appropriate for
	 * scheduling multiple repeating timer tasks that must remain synchronized
	 * with respect to one another.
	 *
	 * @param name   name of the task to be scheduled.
	 * @param task   task to be scheduled.
	 * @param firstTime First time at which task is to be executed.
	 * @param period time in milliseconds between successive task executions.
	 * @throws IllegalArgumentException if <tt>time.getTime()</tt> is negative.
	 * @throws IllegalStateException if task was already scheduled or
	 *         cancelled, timer was cancelled, or timer thread terminated.
	 */
	public void scheduleAtFixedRate(String name, TimerTask task, Date firstTime, long period) {
		TimerTaskWrapper addedTask = addTask(name, task);
		if(addedTask != null) {
			addedTask.setPeriod(period);
			timer.schedule(addedTask, firstTime, period);
		}
	}
	
	public class TimerTaskWrapper extends TimerTask {
		private String name;
		private TimerTask taskToRun;
		
		private long scheduledTime = -1;
		private long lastRunTime = -1;
		
		private long period = -1;
		
		private int numRuns = 0;
		
		public TimerTaskWrapper(String name, TimerTask task) {
			this.name = name;
			taskToRun = task;
			numRuns = 0;
			
			scheduledTime = System.currentTimeMillis();
		}
		
		public void setPeriod(long value) {
			period = value;
		}
		
		public String getName() { return name; }
		public long getPeriod() { return period; }
		public long getPeriodInSeconds() {
			return period != -1 ? period/1000 : 0;
		}
		
		public int getNumberOfRuns() { return numRuns; }
		
		public String getScheduledTime() {
			DateFormat df = new SimpleDateFormat();
			return df.format(new Date(scheduledTime));
		}
		
		public String getLastExecution() {
			if (lastRunTime == -1) {
				return "n/a";
			}
			
			DateFormat df = new SimpleDateFormat();
			return df.format(new Date(lastRunTime));
		}
		
		public String getNextExecution() {
			if (period == -1) {
				return "n/a";
			}
			
			DateFormat df = new SimpleDateFormat();
			long startTime = lastRunTime != -1 ? lastRunTime : scheduledTime;
			return df.format(new Date(startTime + period));
		}
		
		public void run() {
			numRuns++;
			lastRunTime = scheduledExecutionTime();
			TimerManager.getInstance().run(taskToRun);
		}
	}
}

