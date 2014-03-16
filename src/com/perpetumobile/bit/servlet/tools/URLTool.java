package com.perpetumobile.bit.servlet.tools;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import com.perpetumobile.bit.util.Logger;


/**
 *
 * @author  Zoran Dukic
 */
public class URLTool extends BaseTool {
	static private Logger logger = new Logger(URLTool.class);
	
	public URLTool() {
	}
	
	public String decode(String url) {
		return decode(url, response.getCharacterEncoding());
	}
	
	public String encode(String url) {
		return encode(url, response.getCharacterEncoding());
	}
	
	static public String decode(String url, String enc) {
		String result = "";
		if (url != null) {
			try {
				result = URLDecoder.decode(url, enc);
			} catch (UnsupportedEncodingException e) {
				logger.error("UnsupportedEncodingException at URLTool.decode", e);
				result = url;
			}
		}
		return result;
	}
	
	static public String encode(String url, String enc) {
		String result = "";
		if (url != null) {
			try {
				result = URLEncoder.encode(url, enc);
			} catch (UnsupportedEncodingException e) {
				logger.error("UnsupportedEncodingException at URLTool.encode", e);
				result = url;
			}
		}
		return result;
	}
}
