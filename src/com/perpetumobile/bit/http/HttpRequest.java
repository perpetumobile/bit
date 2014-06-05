package com.perpetumobile.bit.http;

import java.net.HttpURLConnection;

import com.perpetumobile.bit.util.Util;

/**
 * @author Zoran Dukic
 *
 */
public class HttpRequest {
	
	static final public String MIME_TYPE_DEFAULT = "text/plain";
	static final public String CHARSET_DEFAULT = "UTF-8";
	
	protected HttpMethod method = HttpMethod.GET;
	protected String url = null;
	protected String content = null;
	protected String mimeType = MIME_TYPE_DEFAULT;
	protected String charset = CHARSET_DEFAULT;
		
	public HttpRequest() {
	}
	
	public HttpRequest(String url) {
		this.url = url;
	}
	
	public HttpRequest(HttpMethod method, String url) {
		this.url = url;
		this.method = method;
	}
	
	public HttpRequest(HttpMethod method, String url, String content, String mimeType, String charset) {
		this.url = url;
		this.method = method;
		this.content = content;
		this.mimeType = mimeType;
		this.charset = charset;
	}
	
	public void prepareConnection(HttpURLConnection connection) {
	}
	
	public HttpMethod getMethod() {
		return method;
	}

	public void setMethod(HttpMethod method) {
		this.method = method;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getMimeType() {
		return mimeType;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	public String getCharset() {
		return charset;
	}

	public void setCharset(String charset) {
		this.charset = charset;
	}
	
	public String getContentType() {
		StringBuilder result = new StringBuilder();
		if(!Util.nullOrEmptyString(mimeType)) {
			result.append(mimeType);
			if(!Util.nullOrEmptyString(charset)) {
				result.append("; charset=");
				result.append(charset);
			}
		}
		return result.toString();
	}
}
