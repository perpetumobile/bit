package com.perpetumobile.bit.servlet;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.perpetumobile.bit.config.Config;
import com.perpetumobile.bit.util.Logger;
import com.perpetumobile.bit.util.Util;


/**
 * @author Zoran Dukic
 *
 */
public class HttpHeaderUtil {
	
	/** The default content type for the response */
	static final public String CONTENT_TYPE_DEFAULT = "text/html";

	/** Default encoding for the output stream */
	static final public String CHARACTER_ENCODING_DEFAULT = "UTF-8";
	
	static final public String CHARACTER_ENCODING_KEY = "ServletEngine.Character.Encoding";
	
	static public String getDefaultCharacterEncoding() {
		return Config.getInstance().getProperty(CHARACTER_ENCODING_KEY, CHARACTER_ENCODING_DEFAULT);
	}
	
	@SuppressWarnings("unchecked")
	static public void debugHeaders(Logger logger, HttpServletRequest request) {
		if(logger.isDebugEnabled()) {
			Enumeration<String> headerNames = request.getHeaderNames();
			while(headerNames.hasMoreElements()) {
				String header = headerNames.nextElement();
				logger.debug(header + " : " + request.getHeader(header));
			}
		}
	}
	
	static public void setRequestCharacterEncoding(HttpServletRequest request) 
	throws UnsupportedEncodingException {
		String charEncoding = request.getCharacterEncoding();
		if(Util.nullOrEmptyString(charEncoding)) {
			request.setCharacterEncoding(getDefaultCharacterEncoding());
		}
	}
	
	static public void setNoCacheResponseHeaders(HttpServletRequest request, HttpServletResponse response, HttpResource httpResource) {
		response.setHeader("Cache-Control", "private");
		response.setHeader("Pragma", "private");
		response.setDateHeader("Expires", System.currentTimeMillis());
	}
	
	static public void setCacheResponseHeaders(HttpServletRequest request, HttpServletResponse response, HttpResource httpResource) {
		response.setHeader("Cache-Control", "private");
		response.setHeader("Pragma", "private");
		response.setDateHeader("Last-Modified", httpResource.getLastModified());
		setETagResponseHeaders(request, response, httpResource);
	}
	
	static public void setETagResponseHeaders(HttpServletRequest request, HttpServletResponse response, HttpResource httpResource) {
		String ETag = httpResource.getETag();
		if(!Util.nullOrEmptyString(ETag)) {
			response.setHeader("ETag", ETag);
		}
	}
	
	static public void setContentResponseHeaders(HttpServletRequest request, HttpServletResponse response, HttpResource httpResource) {
		String templateName = httpResource.getName();
		// set cache control
		if(httpResource.isCacheable()) {
			setCacheResponseHeaders(request, response, httpResource);
		} else {
			setNoCacheResponseHeaders(request, response, httpResource);
		}
		// set content type
		response.setContentType(httpResource.getContentType());
		// set the Content-Disposition header
		if(templateName.endsWith(".xls")) {
			String fileName = request.getParameter("fileName");
			if(!Util.nullOrEmptyString(fileName)) {
				StringBuffer buf = new StringBuffer();
				buf.append("attachment; filename=\"");
				buf.append(fileName);
				buf.append(".xls\"");
				response.setHeader("Content-Disposition", buf.toString());
			}
		}
	}
	
	/**
	 * Check if the conditions specified in the optional If headers are
	 * satisfied.
	 * 
	 * @return boolean true if the resource meets all the specified conditions,
	 *         and false if any of the conditions is not satisfied, in which
	 *         case request processing is stopped
	 */
	static public boolean checkIfHeaders(HttpServletRequest request, HttpServletResponse response, HttpResource httpResource)
	throws IOException {
		return checkIfMatch(request, response, httpResource)
				&& checkIfModifiedSince(request, response, httpResource)
				&& checkIfNoneMatch(request, response, httpResource)
				&& checkIfUnmodifiedSince(request, response, httpResource);
	}

	/**
	 * Check if the if-match condition is satisfied.
	 */
	static public boolean checkIfMatch(HttpServletRequest request, HttpServletResponse response, HttpResource httpResource)
	throws IOException {
		String eTag = httpResource.getETag();
		String headerValue = request.getHeader("If-Match");
		if (headerValue != null) {
			if (headerValue.indexOf('*') == -1) {
				StringTokenizer commaTokenizer = new StringTokenizer(headerValue, ",");
				boolean conditionSatisfied = false;

				while (!conditionSatisfied && commaTokenizer.hasMoreTokens()) {
					String currentToken = commaTokenizer.nextToken();
					if (currentToken.trim().equals(eTag))
						conditionSatisfied = true;
				}

				// If none of the given ETags match, 412 Precodition failed is sent back
				if (!conditionSatisfied) {
					response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED);
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Check if the if-modified-since condition is satisfied.
	 */
	static public boolean checkIfModifiedSince(HttpServletRequest request, HttpServletResponse response, HttpResource httpResource)
	throws IOException {
		try {
			long headerValue = request.getDateHeader("If-Modified-Since");
			long lastModified = httpResource.getLastModified();
			if (headerValue != -1) {

				// If an If-None-Match header has been specified, 
				// if modified since is ignored.
				if ((request.getHeader("If-None-Match") == null)
					&& (lastModified <= headerValue + 1000)) {
					// The entity has not been modified since the date
					// specified by the client. This is not an error case.
					response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
					return false;
				}
			}
		} catch (IllegalArgumentException illegalArgument) {
			return true;
		}
		return true;

	}

	/**
	 * Check if the if-none-match condition is satisfied.
	 */
	static public boolean checkIfNoneMatch(HttpServletRequest request, HttpServletResponse response, HttpResource httpResource)
	throws IOException {
		String eTag = httpResource.getETag();
		String headerValue = request.getHeader("If-None-Match");
		if (headerValue != null) {
			boolean conditionSatisfied = false;
			if (!headerValue.equals("*")) {
				StringTokenizer commaTokenizer = new StringTokenizer(headerValue, ",");

				while (!conditionSatisfied && commaTokenizer.hasMoreTokens()) {
					String currentToken = commaTokenizer.nextToken();
					if (currentToken.trim().equals(eTag))
						conditionSatisfied = true;
				}

			} else {
				conditionSatisfied = true;
			}

			if (conditionSatisfied) {
				// For GET and HEAD, we should respond with 304 Not Modified.
				// For every other method, 412 Precondition Failed is sent back.
				if (("GET".equals(request.getMethod()))	|| ("HEAD".equals(request.getMethod()))) {
					response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
					return false;
				} else {
					response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED);
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Check if the if-unmodified-since condition is satisfied.
	 */
	static public boolean checkIfUnmodifiedSince(HttpServletRequest request, HttpServletResponse response, HttpResource httpResource)
	throws IOException {
		try {
			long lastModified = httpResource.getLastModified();
			long headerValue = request.getDateHeader("If-Unmodified-Since");
			if (headerValue != -1) {
				if (lastModified > (headerValue + 1000)) {
					// The entity has been modified since the date specified by the client
					// 412 Precodition failed is sent back
					response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED);
					return false;
				}
			}
		} catch (IllegalArgumentException illegalArgument) {
			return true;
		}
		return true;
	}
}
