package com.perpetumobile.bit.servlet.tools;

import com.perpetumobile.bit.util.Util;

/**
 *
 * @author  Zoran Dukic
 */
public class URLTool extends BaseTool {
	
	public URLTool() {
	}
	
	public String decode(String url) {
		return decode(url, response.getCharacterEncoding());
	}
	
	public String encode(String url) {
		return encode(url, response.getCharacterEncoding());
	}
	
	static public String decode(String url, String enc) {
		return Util.decodeUrl(url, enc);
	}
	
	static public String encode(String url, String enc) {
		return Util.encodeUrl(url, enc);
	}
}
