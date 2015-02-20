package com.perpetumobile.bit.http;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.entity.mime.MultipartEntity;

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

	protected MultipartEntity multipartEntity = null;

	protected Map<String, List<String>> headerFields = null;
	
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
	
	public MultipartEntity getMultipartEntity() {
		return multipartEntity;
	}

	public void setMultipartEntity(MultipartEntity multipartEntity) {
		this.multipartEntity = multipartEntity;
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
}
