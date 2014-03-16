package com.perpetumobile.bit.http;

import java.awt.Image;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.http.Header;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.HttpStatus;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.client.params.ClientPNames;

import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import com.perpetumobile.bit.config.Config;
import com.perpetumobile.bit.util.Logger;
import com.perpetumobile.bit.util.Util;


/**
 * @author Zoran Dukic
 *
 */

class RedirectLocationHttpResponseInterceptor implements HttpResponseInterceptor {

	public void process(HttpResponse response, HttpContext context) throws HttpException, IOException {
		if (response.containsHeader("Location")) {
			Header[] locations = response.getHeaders("Location");
			if (locations.length > 0) {
				context.setAttribute(HttpManager.LAST_REDIRECT_URL, locations[0].getValue());
			}
		}
	}
}

public class HttpManager {
	static private Logger logger = new Logger(HttpManager.class);
	
	private static HttpManager instance = new HttpManager();
	public static HttpManager getInstance() { return instance; }
	
	static final public String ENABLE_MULTI_THREADED_HTTP_CONNECTION_MANAGER_KEY = "HttpManager.MultiThreadedHttpConnectionManager.Enable"; 
	static final public boolean ENABLE_MULTI_THREADED_HTTP_CONNECTION_MANAGER_DEFAULT = true;
	
	static final public String CONTENT_MAX_LENGTH_KEY = "HttpManager.Content.Length.Max";
	static final public int CONTENT_MAX_LENGTH_DEFAULT = 128*1024;
	
	static final public String USER_AGENT_KEY = "HttpManager.UserAgent";
	static final public String USER_AGENT_DEFAULT = "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.2; WOW64; Trident/6.0; .NET4.0E; .NET4.0C; .NET CLR 3.5.30729; .NET CLR 2.0.50727; .NET CLR 3.0.30729; Tablet PC 2.0; InfoPath.3; MALCJS)";
	
	static final public int TIMEOUT_DEFAULT = 20000;
	
	public static final String LAST_REDIRECT_URL = "last_redirect_url";
	
	private PoolingClientConnectionManager connectionManagerInstance = null;
	private DefaultHttpClient httpClientFollowRedirectsInstance = null; 
	private DefaultHttpClient httpClientNoFollowRedirectsInstance = null;
	
	private String userAgent = null;
	
	private HttpManager() {
		init();
	}
	
	private void init() {
		if(Config.getInstance().getBooleanProperty(ENABLE_MULTI_THREADED_HTTP_CONNECTION_MANAGER_KEY, ENABLE_MULTI_THREADED_HTTP_CONNECTION_MANAGER_DEFAULT)) {	        
			// Create and initialize scheme registry 
			SchemeRegistry schemeRegistry = new SchemeRegistry();
			schemeRegistry.register(new Scheme("http", 80, PlainSocketFactory.getSocketFactory()));
			schemeRegistry.register(new Scheme("https", 443, SSLSocketFactory.getSocketFactory()));

			// This connection manager must be used if more than one thread will be using the HttpClient.
			connectionManagerInstance = new PoolingClientConnectionManager(schemeRegistry);
			connectionManagerInstance.setMaxTotal(50);
			connectionManagerInstance.setDefaultMaxPerRoute(5);

			// Create an HttpClient with the ThreadSafeClientConnManager that will follow redirects
			httpClientFollowRedirectsInstance = new DefaultHttpClient(connectionManagerInstance);
			httpClientFollowRedirectsInstance.getParams().setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, TIMEOUT_DEFAULT);
			httpClientFollowRedirectsInstance.getParams().setIntParameter(CoreConnectionPNames.SO_TIMEOUT, TIMEOUT_DEFAULT);
			httpClientFollowRedirectsInstance.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.BROWSER_COMPATIBILITY);
			httpClientFollowRedirectsInstance.setRedirectStrategy(new DefaultRedirectStrategy());
			httpClientFollowRedirectsInstance.addResponseInterceptor(new RedirectLocationHttpResponseInterceptor());

			// Create an HttpClient with the ThreadSafeClientConnManager that will not follow redirects
			httpClientNoFollowRedirectsInstance = new DefaultHttpClient(connectionManagerInstance);
			httpClientNoFollowRedirectsInstance.getParams().setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, TIMEOUT_DEFAULT);
			httpClientNoFollowRedirectsInstance.getParams().setIntParameter(CoreConnectionPNames.SO_TIMEOUT, TIMEOUT_DEFAULT);
			httpClientNoFollowRedirectsInstance.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.BROWSER_COMPATIBILITY);
			httpClientFollowRedirectsInstance.addResponseInterceptor(new RedirectLocationHttpResponseInterceptor());
			
			userAgent = Config.getInstance().getProperty(USER_AGENT_KEY, USER_AGENT_DEFAULT);
		}
	}
	
	private HttpClient getHttpClient(boolean followRedirects) {
		if(httpClientFollowRedirectsInstance != null && httpClientNoFollowRedirectsInstance != null) {
			if(followRedirects) {
				return httpClientFollowRedirectsInstance;
			}
			return httpClientNoFollowRedirectsInstance;
		}
		DefaultHttpClient result = new DefaultHttpClient();	
		result.getParams().setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, TIMEOUT_DEFAULT);
		result.getParams().setIntParameter(CoreConnectionPNames.SO_TIMEOUT, TIMEOUT_DEFAULT);
		result.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.BROWSER_COMPATIBILITY);
		if(followRedirects) {
			result.setRedirectStrategy(new DefaultRedirectStrategy());
		}
		result.addResponseInterceptor(new RedirectLocationHttpResponseInterceptor());
		
		return result;
	}
	
	private void shutdownHttpClient(HttpClient client) {
		if(httpClientFollowRedirectsInstance == null || httpClientNoFollowRedirectsInstance == null) {
			client.getConnectionManager().shutdown();
		}
	}
	
	private void setRequestHeader(HttpRequest method) {
		// fake ie6.0
		method.setHeader("accept", "image/gif, image/x-xbitmap, image/jpeg, image/pjpeg, */*");
		method.setHeader("-------", "----:-----------:----------------------------");
		method.setHeader("accept-language", "en-us");
		method.setHeader("---------------", "----- -------");
		method.setHeader("user-agent", userAgent);
	}
	
	private void printDebugInfo(HttpRequest request, HttpResponse response) {
		if(logger.isDebugEnabled()) {
			// write out the request headers
			logger.debug("*** Request ***");
			logger.debug("Request URI: " + request.getRequestLine().getUri());
			Header[] requestHeaders = request.getAllHeaders();
			for(Header header : requestHeaders) {
				String str = header.toString();
				if(str != null && !str.equals("")) {
					logger.debug(header.toString());
				}
			}
			// write out the response headers
			logger.debug("*** Response ***");
			logger.debug("Status Line: " + response.getStatusLine());
			Header[] responseHeaders = response.getAllHeaders();
			for(Header header : responseHeaders) {
				String str = header.toString();
				if(str != null && !str.equals("")) {
					logger.debug(header.toString());
				}
			}
		}
	}
	
	private String fixUrl(String url) {
		String fixedUrl = url;
	
		if(fixedUrl.startsWith("//")) {
			fixedUrl = "http:" + fixedUrl;
		}
		
		fixedUrl = Util.replaceAll(fixedUrl, " ", "%20");
		
		int idx = fixedUrl.indexOf(">");
		if(idx > 0) {
			fixedUrl = fixedUrl.substring(0, idx);				
		}
		
		return fixedUrl;
	}
	
	public HttpResponseDocument get(String url) {
	  HttpResponseDocument result = new HttpResponseDocument(url);

	  HttpClient client = getHttpClient(true);
	  HttpGet httpGet = new HttpGet(fixUrl(url));
	  HttpContext context = new BasicHttpContext(); 
	  setRequestHeader(httpGet);

	  try { 
	    ResponseHandler<HttpResponseDocument> response = new HttpResponseDocumentResponseHandler(url, context);
	    result = client.execute(httpGet, response, context);
	  } catch (Exception e) {
	    logger.error("HttpManager.get exception for: " + url, e);
	  } finally {
	    shutdownHttpClient(client);
	  }

	  return result;
	}

	public HttpResponseDocument post(String url) {
	  HttpResponseDocument result = new HttpResponseDocument(url);

	  HttpClient client = getHttpClient(true);
	  HttpPost httpPost = new HttpPost(fixUrl(url));
	  HttpContext context = new BasicHttpContext();
	  setRequestHeader(httpPost);

	  try { 
	    ResponseHandler<HttpResponseDocument> response = new HttpResponseDocumentResponseHandler(url, context);
	    result = client.execute(httpPost, response);
	  } catch (Exception e) {
	    logger.error("HttpManager.post exception for: " + url, e);
	  } finally {
	    shutdownHttpClient(client);
	  }

	  return result;
	}

	public HttpResponseDocument post(String url, String content, ContentType contentType) {
	  HttpResponseDocument result = new HttpResponseDocument(url);

	  HttpClient client = getHttpClient(true);
	  HttpPost httpPost = new HttpPost(fixUrl(url));
	  HttpContext context = new BasicHttpContext();
	  setRequestHeader(httpPost);
	  httpPost.setEntity(new StringEntity(content, contentType));

	  try { 
	    ResponseHandler<HttpResponseDocument> response = new HttpResponseDocumentResponseHandler(url, context);
	    result = client.execute(httpPost, response);
	  } catch (Exception e) {
	    logger.error("HttpManager.post exception for: " + url, e);
	  } finally {
	    shutdownHttpClient(client);
	  }

	  return result;
	}
	
	public HttpResponseDocument post(String url, MultipartEntity entity) {
	  HttpResponseDocument result = new HttpResponseDocument(url);

	  HttpClient client = getHttpClient(true);
	  HttpPost httpPost = new HttpPost(fixUrl(url));
	  HttpContext context = new BasicHttpContext();
	  setRequestHeader(httpPost);
	  httpPost.setEntity(entity);

	  try { 
	    ResponseHandler<HttpResponseDocument> response = new HttpResponseDocumentResponseHandler(url, context);
	    result = client.execute(httpPost, response);
	  } catch (Exception e) {
	    logger.error("HttpManager.post exception for: " + url, e);
	  } finally {
	    shutdownHttpClient(client);
	  }

	  return result;
	}
	
	public HttpResponseDocument post(String url, String partName, File file, String mimeType) {
		MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
		FileBody body = null;
		if(!Util.nullOrEmptyString(mimeType)) {
			body = new FileBody(file, mimeType);
		} else {
			body = new FileBody(file);
		}
		entity.addPart(partName, body);
		return post(url, entity);
	}
	
	public HttpResponseDocument post(String url, String partName, byte[] data, String fileName, String mimeType) {
		MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
		ByteArrayBody body = null;
		if(!Util.nullOrEmptyString(mimeType)) {
			body = new ByteArrayBody(data, mimeType, fileName);
		} else {
			body = new ByteArrayBody(data, fileName);
		}
		entity.addPart(partName, body);
		return post(url, entity);
	}
	
	public HttpResponseDocument delete(String url) {
	  HttpResponseDocument result = new HttpResponseDocument(url);

	  HttpClient client = getHttpClient(true);
	  HttpDelete httpDelete = new HttpDelete(fixUrl(url));
	  HttpContext context = new BasicHttpContext();
	  setRequestHeader(httpDelete);

	  try { 
	    ResponseHandler<HttpResponseDocument> response = new HttpResponseDocumentResponseHandler(url, context);
	    result = client.execute(httpDelete, response);
	  } catch (Exception e) {
	    logger.error("HttpManager.delete exception for: " + url, e);
	  } finally {
	    shutdownHttpClient(client);
	  }

	  return result;
	}
	
	public Image getImage(String url) {
		HttpImage httpImage = new HttpImage(fixUrl(url));
		return httpImage.getImage();
	}
	
	public void getImage(HttpImage httpImage) {
		String url = fixUrl(httpImage.getUrl());
		HttpClient client = getHttpClient(true);
		HttpGet httpGet = new HttpGet(url);
		setRequestHeader(httpGet);

		try { 
			HttpResponse response = client.execute(httpGet);
			printDebugInfo(httpGet, response);
			
			// update status code
			httpImage.setStatusCode(response.getStatusLine().getStatusCode());
			if(httpImage.getStatusCode() == HttpStatus.SC_OK) {
				httpImage.setImage(ImageIO.read(response.getEntity().getContent()));
			}
			EntityUtils.consume(response.getEntity());
        } catch (Exception e) {
			logger.error("HttpManager.getImage exception for: " + url, e);
        } finally {
        	shutdownHttpClient(client);
        }
	}
	
	public long getContentLength(String url) {
		long result = 0;
		
		HttpClient client = getHttpClient(true);
		HttpGet httpGet = new HttpGet(fixUrl(url));
		setRequestHeader(httpGet);

		try { 
			HttpResponse response = client.execute(httpGet);
			printDebugInfo(httpGet, response);
			
			if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				result = response.getEntity().getContentLength();
			}
			EntityUtils.consume(response.getEntity());
        } catch (Exception e) {
			logger.error("HttpManager.getContentLength exception for: " + url, e);
        } finally {
        	shutdownHttpClient(client);
        }
        
        return result;
	}
	
	static public void main(String[] args) throws IOException {
		HttpResponseDocument response = HttpManager.getInstance().get("http://www.google.com/");		
		Util.saveToFile(new StringBuffer(response.getPageSource()), "http_manager.htm", false);
	}
}
