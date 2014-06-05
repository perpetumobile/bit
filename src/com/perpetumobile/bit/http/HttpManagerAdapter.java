package com.perpetumobile.bit.http;

import org.apache.http.entity.ContentType;

import com.perpetumobile.bit.util.Util;

/**
 * bit-android uses different http package that depends on built in java http connection and doesn't include org.apache.http
 * executeImpl method serves as an adapter since orm.json.JSONParserManager and orm.xml.SAXParserManager call it
 * 
 * @author Zoran Dukic
 */
public class HttpManagerAdapter {

	@SuppressWarnings("incomplete-switch")
	public HttpResponseDocument executeImpl(HttpRequest httpRequest) {
		HttpResponseDocument result = null;
		switch(httpRequest.method) {
		case GET:
			result = HttpManager.getInstance().get(httpRequest.getUrl());
			break;
		case POST:
			String content = httpRequest.getContent();
			if(!Util.nullOrEmptyString(content)) {
				result = HttpManager.getInstance().post(httpRequest.getUrl(), content, ContentType.create(httpRequest.getMimeType(), httpRequest.getCharset()));
			} else {
				result = HttpManager.getInstance().post(httpRequest.getUrl());
			}
			break;
		}
		return result;
	}
}
