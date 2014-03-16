package com.perpetumobile.bit.servlet;

import javax.servlet.http.HttpServletRequest;

import com.perpetumobile.bit.config.Config;
import com.perpetumobile.bit.util.Util;


/**
 * @author Zoran Dukic
 *
 */
public class HttpResource {
	protected String name = null;
	protected long lastModified = 0;
	protected long contentLength = 0;
	protected String contentType = null;
	protected String ETag = null;
	protected boolean cacheable = false;
	
	public HttpResource(HttpServletRequest request, String name, long lastModified, long contentLength, String ETag) {
		this.name = name;
		this.lastModified = lastModified;
		this.contentLength = contentLength;
		this.ETag = ETag;
		setContentType(request);
	}
	
	protected void setContentType(HttpServletRequest request) {
		boolean isQueryString = !Util.nullOrEmptyString(request.getQueryString());
		boolean disableCashing = Config.getInstance().getBooleanProperty("HttpResource.Cashing.Disable", false);
		boolean appendCharacterEncoding = true;

		StringBuffer contentTypeBuf = new StringBuffer();
		if(name.endsWith(".js")) {
			// set the content type
			contentTypeBuf.append("text/javascript");
			// allow cache if no query string
			if(!isQueryString && !disableCashing) {
				cacheable = true;
			}
		} else if(name.endsWith(".css")) {
			// set the content type
			contentTypeBuf.append("text/css");
			// allow cache if no query string
			if(!isQueryString && !disableCashing) {
				cacheable = true;
			}
		} else if(name.endsWith(".xml")) {
			// set the content type
			contentTypeBuf.append("text/xml");
		} else if(name.endsWith(".xls")) {
			// set the content type
			contentTypeBuf.append("application/vnd.ms-excel");
		} else {
			// set the content type
			contentTypeBuf.append("text/html");
		}
		if(appendCharacterEncoding) {
			contentTypeBuf.append("; charset=");
			String charEncoding = request.getCharacterEncoding();
			if(Util.nullOrEmptyString(charEncoding)) {
				charEncoding = HttpHeaderUtil.getDefaultCharacterEncoding();
			}
			contentTypeBuf.append(charEncoding);
		}
		contentType = contentTypeBuf.toString();
	}

	/**
	 * @return the cacheable
	 */
	public boolean isCacheable() {
		return cacheable;
	}

	/**
	 * @param cacheable the cacheable to set
	 */
	public void setCacheable(boolean cacheable) {
		this.cacheable = cacheable;
	}

	/**
	 * @return the contentLength
	 */
	public long getContentLength() {
		return contentLength;
	}

	/**
	 * @param contentLength the contentLength to set
	 */
	public void setContentLength(long contentLength) {
		this.contentLength = contentLength;
	}

	/**
	 * @return the contentType
	 */
	public String getContentType() {
		return contentType;
	}

	/**
	 * @param contentType the contentType to set
	 */
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	/**
	 * @return the eTag
	 */
	public String getETag() {
		if(ETag == null) {
			StringBuffer buf = new StringBuffer("W/\"");
			buf.append(contentLength);
			buf.append("-");
			buf.append(lastModified);
			buf.append("\"");
			return buf.toString();
		}
		return ETag;
	}

	/**
	 * @param tag the eTag to set
	 */
	public void setETag(String tag) {
		ETag = tag;
	}

	/**
	 * @return the lastModified
	 */
	public long getLastModified() {
		return lastModified;
	}

	/**
	 * @param lastModified the lastModified to set
	 */
	public void setLastModified(long lastModified) {
		this.lastModified = lastModified;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
}
