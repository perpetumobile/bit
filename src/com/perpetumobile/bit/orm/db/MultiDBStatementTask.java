package com.perpetumobile.bit.orm.db;

import java.util.ArrayList;

import com.perpetumobile.bit.util.Task;

public class MultiDBStatementTask extends Task {

	protected String dbConfigName = null;
	protected ArrayList<DBStatementTask> taskList = null;

	public MultiDBStatementTask() {
		taskList = new ArrayList<DBStatementTask>();
	}
	
	public MultiDBStatementTask(String dbConfigName, ArrayList<DBStatementTask> taskList) {
		this.dbConfigName = dbConfigName;
		this.taskList = taskList;
	}

	@Override
	public void runImpl() throws Exception {
		DBStatementManager.getInstance().executeImpl(dbConfigName, taskList);
	}
	
	public void setDBConfigName(String dbConfigName) {
		this.dbConfigName = dbConfigName;
	}
	
	public void setTaskList(ArrayList<DBStatementTask> taskList) {
		this.taskList = taskList;
	}
	
	public void addTask(DBStatementTask task) {
		taskList.add(task);
	}
}
