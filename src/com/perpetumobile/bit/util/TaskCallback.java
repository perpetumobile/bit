package com.perpetumobile.bit.util;

public interface TaskCallback<T extends Task> {
	void onTaskDone(T task);
}
