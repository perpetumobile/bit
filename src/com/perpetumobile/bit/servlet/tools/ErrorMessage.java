package com.perpetumobile.bit.servlet.tools;

import java.util.ArrayList;

public class ErrorMessage {
	
	private String key = null;
	private ArrayList<String> messageList = new ArrayList<String>();

	public ErrorMessage() {
	}
	
	public ErrorMessage(String key, String message) {
		this.key = key;
		messageList.add(message);
	}

	public void addMessage(String message) {
		messageList.add(message);
	}
	
	public String getKey() {
		return key;
	}

	public ArrayList<String> getMessageList() {
		return messageList;
	}
	
	public String getMessage(String delimiter) {
		StringBuffer buf = new StringBuffer();
		for(String m : messageList) {
			buf.append(m);
			buf.append(delimiter);
		}
		return buf.toString();
	}
}
