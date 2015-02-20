package com.perpetumobile.bit.http;

import java.util.EnumSet;
import java.util.HashMap;

/**
 * @author Zoran Dukic
 *
 */
public enum HttpMethod {
	// response is a text
	GET("GET"),
	POST("POST"),
	MULTI_PART("MULTI_PART"),
	// response is an image
	GET_IMAGE("GET_IMAGE"),
	POST_IMAGE("POST_IMAGE"),
	MULTI_PART_IMAGE("MULTI_PART_IMAGE");
	
	private static final HashMap<String,HttpMethod> map = new HashMap<String,HttpMethod>();
	static {
		for(HttpMethod rt : EnumSet.allOf(HttpMethod.class))
			map.put(rt.getMethod(), rt);
	}

	private String method;

	private HttpMethod(String method) {
		this.method = method.toUpperCase();
	}

	public String getMethod(){
		return method; 
	}

	static public HttpMethod get(String type) { 
		return map.get(type.toUpperCase()); 
	}
}
