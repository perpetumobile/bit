package com.perpetumobile.bit.http;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ResponseHandler;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import com.perpetumobile.bit.util.Util;


public class HttpResponseDocumentResponseHandler implements  ResponseHandler<HttpResponseDocument> {
	
	protected String url = null;
	protected HttpContext context = null;
	
	public HttpResponseDocumentResponseHandler(String url, HttpContext context) {
		this.url = url;
		this.context = context;
	}
	
	public HttpResponseDocument handleResponse(final HttpResponse response) 
	throws IOException {
		HttpResponseDocument result = new HttpResponseDocument(url);
		result.setStatusCode(response.getStatusLine().getStatusCode());
		String lastRedirectUrl = (String)context.getAttribute(HttpManager.LAST_REDIRECT_URL);
		if(!Util.nullOrEmptyString(lastRedirectUrl)) {
			result.setDestinationUrl(lastRedirectUrl);
		}
		HttpEntity entity = response.getEntity();
		if(entity != null) {
			if(result.getStatusCode() < 300) {
				result.setPageSource(EntityUtils.toString(entity));
				result.setContentLenght(entity.getContentLength());
			} else {
				// do we need to do something different here?
				result.setPageSource(EntityUtils.toString(entity));
				result.setContentLenght(entity.getContentLength());
			}
			EntityUtils.consume(entity);
		}
		return result;
	}
}
