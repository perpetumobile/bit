package com.perpetumobile.bit.servlet.tools;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.tools.view.ViewContext;

/**
 * @author Zoran Dukic
 */
public class CookieTool { 
	
	protected HttpServletRequest request;
	protected HttpServletResponse response;

	public CookieTool() {
	}


	/**
	 * Initializes this instance for the current request.
	 *
	 * @param obj the ViewContext of the current request
	 */
	public void init(Object obj) {
		ViewContext context = (ViewContext)obj;
		this.request = context.getRequest();
		this.response = context.getResponse();
	}


	/**
	 * Expose array of Cookies for this request to the template.
	 *
	 * <p>This is equivalent to <code>$request.cookies</code>.</p>
	 *
	 * @return array of Cookie objects for this request
	 */
	public Cookie[] getAll() {
		return request.getCookies();
	}


	/**
	 * Returns the Cookie with the specified name, if it exists.
	 *
	 * <p>So, if you had a cookie named 'foo', you'd get it's value
	 * by <code>$cookies.foo.value</code> or it's max age
	 * by <code>$cookies.foo.maxAge</code></p>
	 */
	public Cookie get(String name) {
		Cookie[] all = getAll();
		if (all == null) {
			return null;
		}

		for (int i = 0; i < all.length; i++) {
			Cookie cookie = all[i];
			if (cookie.getName().equals(name)) {
				return cookie;
			}
		}
		return null;
	}


	/**
	 * Adds a new Cookie with the specified name and value
	 * to the HttpServletResponse.  This does *not* add a Cookie
	 * to the current request.
	 *
	 * @param name the name to give this cookie
	 * @param value the value to be set for this cookie
	 */
	public void add(String name, String value) {
		Cookie cookie = new Cookie(name, value);
		response.addCookie(cookie);
	}


	/**
	 * Convenience method to add a new Cookie to the response
	 * and set an expiry time for it.
	 *
	 * @param name the name to give this cookie
	 * @param value the value to be set for this cookie
	 * @param maxAge the expiry to be set for this cookie
	 */
	public void add(String name, String value, int maxAge) {
		Cookie cookie = new Cookie(name, value);
		cookie.setMaxAge(maxAge);
		response.addCookie(cookie);
	}
	
	/**
	 * Convenience method to add a new Cookie to the response
	 * and set an expiry and path time for it.
	 *
	 * @param name the name to give this cookie
	 * @param value the value to be set for this cookie
	 * @param maxAge the expiry to be set for this cookie
	 * @param path the path to be set for this cookie
	 */
	public void add(String name, String value, int maxAge, String path) {
		Cookie cookie = new Cookie(name, value);
		cookie.setMaxAge(maxAge);
		cookie.setPath(path);
		response.addCookie(cookie);
	}
}
