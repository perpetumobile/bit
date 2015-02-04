package com.perpetumobile.bit.fb;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.fileupload.FileItem;

import com.perpetumobile.bit.config.Config;
import com.perpetumobile.bit.http.HttpManager;
import com.perpetumobile.bit.http.HttpRequest;
import com.perpetumobile.bit.orm.xml.SAXParserManager;
import com.perpetumobile.bit.orm.xml.XMLRecord;
import com.perpetumobile.bit.servlet.tools.URLTool;
import com.perpetumobile.bit.util.Logger;
import com.perpetumobile.bit.util.Util;


/**
 *
 * @author  Zoran Dukic
 */
public class FBUtil {
	static private Logger logger = new Logger(FBUtil.class);

	static public String getFBAppID(String appName) {
		String result = null;
		if(!Util.nullOrEmptyString(appName)) {
			result = Config.getInstance().getClassProperty(appName, "FB.AppID", "");
		} else {
			result = Config.getInstance().getProperty("FB.AppID", "");
		}
		return result;
	}

	static public String getFBAppSecret(String appName) {
		String result = null;
		if(!Util.nullOrEmptyString(appName)) {
			result = Config.getInstance().getClassProperty(appName, "FB.AppSecret", "");
		}
		return result;
	}

	static public String parseFBSRCookie(String appName, String fbsrCookieStr) {
		String result = null;

		if(!Util.nullOrEmptyString(fbsrCookieStr)) {
			String[] val = fbsrCookieStr.split("\\.");
			if(val != null && val.length == 2) {
				try {
					String key = getFBAppSecret(appName);
					if(!Util.nullOrEmptyString(key)) {
						String sig = Util.toHex(Base64.decodeBase64(val[0]));
						if(!Util.nullOrEmptyString(sig)) {
							String md = Util.getHmac("HmacSHA256", key, val[1]);
							if(sig.equals(md)) {
								result = new String(Base64.decodeBase64(val[1]), "UTF8");
							}
						}
					} else {
						result = new String(Base64.decodeBase64(val[1]), "UTF8");
					}
				} catch (UnsupportedEncodingException e) {
					logger.error("FBUtil.parseFBSRCookie exception", e);
				}
			}
		}
		return result;
	}

	static public boolean isConnected(String fbid, String accessToken) throws UnsupportedEncodingException {
		String fbResponse = getGraphAPI(fbid, "", accessToken);
		return !Util.nullOrEmptyString(fbResponse) && fbResponse.indexOf("error") == -1;
	}

	static public String getGraphAPIUrl(String fbid, String what, String accessToken, Object params) throws UnsupportedEncodingException {	
		StringBuffer buf = new StringBuffer("https://graph.facebook.com/");

		boolean fbidAdded = false;
		if(!Util.nullOrEmptyString(fbid)) {
			buf.append(fbid);
			fbidAdded = true;
		}
		if (!Util.nullOrEmptyString(what)) {
			if(fbidAdded)
				buf.append("/");
			buf.append(what);
		}
		if(!Util.nullOrEmptyString(accessToken)) {
			int index = buf.indexOf("?");
			if(index != -1) {
				buf.append("&access_token=");
			} else {
				buf.append("?access_token=");
			}
			buf.append(accessToken);
		}

		if(params instanceof Map) {
			@SuppressWarnings("unchecked")
			Map<String, Object> p = (Map<String, Object>) params;
			String par = "";
			int index = buf.indexOf("?");
			if(index == -1)
				par = "?";
			
			for (Map.Entry<String, Object> me : p.entrySet()) {
				if(me.getValue() != null) {
					if(par.length() != 1)
						par += "&";
					par += me.getKey() + "=" + URLEncoder.encode(me.getValue().toString(), "UTF-8"); //works for String, JSONObject, JSONArray
				}
			}
			buf.append(par);
		}

		return buf.toString();
	}

	static public String getGraphAPIUrl(String fbid, String what, String accessToken) throws UnsupportedEncodingException {
		return getGraphAPIUrl(fbid, what, accessToken, null);
	}

	static public String getGraphAPI(String fbid, String what, String accessToken, Object params) throws UnsupportedEncodingException {
		String result = null;

		String url = getGraphAPIUrl(fbid, what, accessToken, params);
		if(!Util.nullOrEmptyString(url)) {
			result = HttpManager.getInstance().get(url).getPageSource();
		}
		return result;
	}

	static public String getGraphAPI(String fbid, String what, String accessToken) throws UnsupportedEncodingException {
		return getGraphAPI(fbid, what, accessToken, null);
	}

	static public String getGraphObject(String what, String accessToken) throws UnsupportedEncodingException {
		return getGraphAPI(null, what, accessToken);
	}

	static public String postGraphAPI(String fbid, String what, String accessToken, Object params) throws UnsupportedEncodingException {
		String result = null;
		String url = getGraphAPIUrl(fbid, what, accessToken, params);
		if(!Util.nullOrEmptyString(url)) {
			result = HttpManager.getInstance().post(url).getPageSource();
		}
		return result;
	}

	static public String postImageGraphAPI(String fbid, String what, String accessToken, Object params, File imageFile) throws UnsupportedEncodingException {
		String result = null;
		String url = getGraphAPIUrl(fbid, what, accessToken, params);
		String ext = imageFile.getName().substring(imageFile.getName().lastIndexOf('.') + 1);
		String type = "";
		if("jpeg".equals(ext) || "jpg".equals(ext))
			type = "image/jpeg";
		else if ("png".equals(ext))
			type = "image/png";
		else if ("bmp".equals(ext))
			type = "image/bmp";
		else if ("gif".equals(ext))
			type = "image/gif";
		else if ("zip".equals(ext))
			type = "application/zip";
		else
			return null; //TODO throw an exception

		if(!Util.nullOrEmptyString(url)) {
			result = HttpManager.getInstance().post(url, "image", imageFile, type).getPageSource();
		}
		return result;
	}

	static public String postGraphAPI(String fbid, String what, String accessToken) throws UnsupportedEncodingException {
		return postGraphAPI(fbid, what, accessToken, null);
	}

	static public String postGraphAPI(String fbid, String what, String fieldName, FileItem fileItem, String accessToken) throws UnsupportedEncodingException {
		String result = null;
		String url = getGraphAPIUrl(fbid, what, accessToken);
		if(!Util.nullOrEmptyString(url)) {
			result = HttpManager.getInstance().post(url, fieldName, fileItem.get(), fileItem.getName(), fileItem.getContentType()).getPageSource();
		}
		return result;
	}

	static public String postGraphObject(String what, String accessToken) throws UnsupportedEncodingException {
		return postGraphAPI(null, what, accessToken);
	}

	static public String deleteGraphAPI(String fbid, String what, String accessToken, Object params) throws UnsupportedEncodingException {
		String result = null;
		String url = getGraphAPIUrl(fbid, what, accessToken, params);
		if(!Util.nullOrEmptyString(url)) {
			result = HttpManager.getInstance().delete(url).getPageSource();
		}
		return result;
	}

	static public String getFQLUrl(String query, String accessToken) {	
		StringBuffer buf = new StringBuffer("https://api.facebook.com/method/fql.query?query=");
		buf.append(URLTool.encode(query, "UTF8"));

		if(!Util.nullOrEmptyString(accessToken)) {	
			buf.append("&access_token=");
			buf.append(accessToken);
		}
		return buf.toString();
	}

	static public ArrayList<? extends XMLRecord> getFQL(String tableName, String query, String accessToken) {
		ArrayList<? extends XMLRecord> result = new ArrayList<XMLRecord>();
		String url = getFQLUrl(query, accessToken);
		if(!Util.nullOrEmptyString(url)) {
			try {
				XMLRecord root = SAXParserManager.getInstance().parseImpl(new HttpRequest(url), false, "FQL", "fql_query_response");
				if(root != null) {
					result = root.getXMLRecords("FQL", "fql_query_response", tableName);
				}
			} catch (Exception e) {
				logger.error("FBUtil.getFQL exception", e);
			}
		}
		return result;
	}
}
