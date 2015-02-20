package com.perpetumobile.bit.servlet.tools;


import org.apache.velocity.tools.view.ParameterTool;
import org.apache.velocity.tools.view.ViewContext;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.perpetumobile.bit.util.Logger;
import com.perpetumobile.bit.util.Util;

import javax.servlet.http.HttpServletRequest;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;
import java.util.HashMap;

/**
 * TODO make sure that ParameterTool calls getValue and getValues in all other get methods
 * @author Zoran Dukic
 */
public class ParamTool extends ParameterTool {
	static private Logger logger = new Logger(ParamTool.class);
	
	protected HashMap<String, String[]> params = new HashMap<String, String[]>();
	protected HashMap<String, FileItem[]> fileItems = new HashMap<String, FileItem[]>();
	protected String postContent = null;
	
	public void init(Object initData) {
		HttpServletRequest request = ((ViewContext)initData).getRequest();
		
		if (initData instanceof ViewContext) {
			setRequest(request);
		} else {
			throw new IllegalArgumentException("Was expecting " + ViewContext.class);
		}
		
		if(isMultipartContent()) {
			try {
				parseMultipartRequest();
			} catch (Exception e){
				logger.error("Exception at ParamTool.init", e);
			}
		} else {
			String line = null;
			StringBuffer buf = new StringBuffer(); 
			BufferedReader reader;
			try {
				reader = request.getReader();
				if(reader != null) {
					while ((line = reader.readLine()) != null) {
						buf.append(line);
						buf.append("\n");
					}
					postContent = buf.toString();
				}
			} catch (IOException e) {
				logger.error("Error reading from request in ParamTool.init", e);
			}
		}
	}
	
	public HttpServletRequest getHttpServletRequest() {
		return (HttpServletRequest)getRequest();
	}
	
	public void setString(String key, String value) {
		String[] values = { value };
		params.put(key, values);
	}
	
	public void setStrings(String key, String[] values) {
		params.put(key, values);
	}
	
	public void addString(String key, String value) {
		String[] newValues = (String[])Util.addToArray(getStrings(key), value);
		params.put(key, newValues);	
	}
	
	public void addStrings(String key, String[] values) {
		String[] newValues = (String[])Util.addToArray(getStrings(key), values);
		params.put(key, newValues);
	}
	
	protected void addFileItem(String key, FileItem value) { 
		FileItem[] newValues = (FileItem[])Util.addToArray(getFileItems(key), value);
		fileItems.put(key, newValues);	
	}
	
	@Override
	public Object getValue(String key) {
		String[] values = params.get(key);
		if (values != null) {
			return values[0];
		}
		return super.getValue(key);
	}
	
	public Object[] getValues(String key) {
		String[] values = params.get(key);
		if (values != null) {
			return values;
		}
		return super.getValues(key);
	}

	public long getLong(String key, long alternate) {
		Number n = getNumber(key);
		return (n != null) ? n.longValue() : alternate;
	}

	public long[] getLongs(String key) {
		String[] strings = getStrings(key);
		if (strings == null) {
			return null;
		}

		long[] longs = new long[strings.length];
		try {
			for (int i = 0; i < longs.length; i++) {
				if (strings[i] != null && strings[i].length() > 0) {
					longs[i] = parseNumber(strings[i]).longValue();
				}
			}
		} catch (NumberFormatException nfe) {
			return null;
		}
		return longs;
	}

	public boolean isMultipartContent() {
		return ServletFileUpload.isMultipartContent(getHttpServletRequest());
	}
	
	protected void parseMultipartRequest() throws Exception {
		ServletFileUpload upload = new ServletFileUpload(new DiskFileItemFactory());
		List<FileItem> items = upload.parseRequest(getHttpServletRequest());
		for(FileItem item : items) {
			if (item.isFormField()) {
				String name = item.getFieldName();
				String value = item.getString();
				setString(name, value);
			} else {
				String fileName = item.getName();
				if(fileName != null && !fileName.equals("")) {
					String fieldName = item.getFieldName(); 
					addFileItem(fieldName, item);
					// System.out.println("Content Type: " + item.getContentType());
				}
			}
		}
	}
		
	public FileItem[] getFileItems(String key) {
		return fileItems.get(key);
	}
	
	public FileItem getFileItem(String key) {
		FileItem[] values = fileItems.get(key);
		if (values != null && values.length > 0) {
			return values[0];
		}
		return null;
	}
	
	public String getPostContent() {
		return postContent;
	}
}