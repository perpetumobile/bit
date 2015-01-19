package com.perpetumobile.bit.http;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.Header;

import com.perpetumobile.bit.util.Util;

/**
 * @author Zoran Dukic
 *
 */
public class HttpResponseDocument {
	
	protected String sourceUrl = null;
	protected String destinationUrl = null;
	protected int statusCode = -1;
	protected Map<String, List<String>> headerFields = null;
	protected String contentType = null;
	protected long contentLenght = -1;
	protected String pageSource = null;
	
	public HttpResponseDocument(String url) {
		sourceUrl = url;
		destinationUrl = url;
	}
	
	protected void reset() {
		// reset all generated fields
		statusCode = -1;
		headerFields = null;
		contentType = null;
		contentLenght = -1;		
		pageSource = null;
	}
	
	/**
	 * @return Returns the sourceUrl.
	 */
	public String getSourceUrl() {
		return sourceUrl;
	}
	
	/**
	 * @param sourceUrl The sourceUrl to set.
	 */
	public void setSourceUrl(String sourceUrl) {
		this.sourceUrl = sourceUrl;
	}
	
	/**
	 * @return Returns the destinationUrl.
	 */
	public String getDestinationUrl() {
		return destinationUrl;
	}
	
	/**
	 * @param destinationUrl The destinationUrl to set.
	 */
	public void setDestinationUrl(String destinationUrl) {
		this.destinationUrl = destinationUrl;
	}
	
	public int getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}
	
	public Map<String, List<String>> getHeaderFields() {
		return headerFields;
	}
	
	public List<String> getHeaderValues(String name) {
		return (headerFields != null ? headerFields.get(name) : null);
	}
	
	public String getHeaderValue(String name) {
		List<String> list = getHeaderValues(name);
		if(!Util.nullOrEmptyList(list)) {
			return list.get(0);
		}
		return null;
	}
	
	public void setHeaderFields(Map<String, List<String>> headerFields) {
		this.headerFields = headerFields;
	}
	
	public void setHeader(String name, String value) {
		if(headerFields == null) {
			headerFields = new HashMap<String, List<String>>();
		}
		List<String> list = new ArrayList<String>();
		list.add(value);
		headerFields.put(name, list);
	}
	
	public void addHeader(String name, String value) {
		if(headerFields == null) {
			headerFields = new HashMap<String, List<String>>();
		}
		List<String> list = headerFields.get(name);
		if(list == null) {
			list = new ArrayList<String>();
			headerFields.put(name, list);
		}
		list.add(value);
	}
	
	public void setHeaderFields(Header[] headers) {
		headerFields = new HashMap<String, List<String>>();
		for(Header h : headers) {
			addHeader(h.getName(), h.getValue());
		}
	}
	
	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	/**
	 * @return the contentLenght
	 */
	public long getContentLenght() {
		return contentLenght;
	}

	/**
	 * @param contentLenght the contentLenght to set
	 */
	public void setContentLenght(long contentLenght) {
		this.contentLenght = contentLenght;
	}
	
	/**
	 * @return Returns the pageSource.
	 */
	public String getPageSource() {
		return pageSource;
	}
	
	/**
	 * @param pageSource The pageSource to set.
	 */
	public void setPageSource(String pageSource) {
		this.pageSource = pageSource;
	}
}
