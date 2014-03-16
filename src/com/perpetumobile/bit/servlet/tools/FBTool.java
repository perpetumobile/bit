package com.perpetumobile.bit.servlet.tools;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import javax.servlet.http.Cookie;

import org.apache.commons.fileupload.FileItem;

import com.perpetumobile.bit.config.Config;
import com.perpetumobile.bit.fb.FBUtil;
import com.perpetumobile.bit.orm.xml.XMLRecord;
import com.perpetumobile.bit.util.Util;


/**
 *
 * @author  Zoran Dukic
 */
public class FBTool extends BaseTool {
	
	protected String appName = null;
	protected long fbid = -1;
	protected String fbidStr = null;
	protected String accessToken = null;
	
	public FBTool() {
	}
	
	public String getAppName() {
		if(Util.nullOrEmptyString(appName)) {
			appName = getParams().getString("fb_app", "");
			if(Util.nullOrEmptyString(appName)) {
				appName = Config.getInstance().getProperty("FB.AppName.Default", "");
			}
		}
		return appName;
	}
	
	public String getFBAppID() {
		return FBUtil.getFBAppID(getAppName());
	}
	
	public String getFBAppSecret() {
		return FBUtil.getFBAppSecret(getAppName());
	}
	
	public String parseFBSRCookie() {	
		Cookie fbsrCookie = getCookies().get("fbsr_" + getFBAppID());
		if(fbsrCookie != null) {
			String fbsrCookieStr = fbsrCookie.getValue();
			return FBUtil.parseFBSRCookie(getAppName(), fbsrCookieStr);
		}
		return null;
	}
	
	protected void readFBSRCookie() {
		Cookie fbsCookie = getCookies().get("fbs_" + getFBAppID());
		if(fbsCookie != null) {
			String fbsCookieStr = fbsCookie.getValue();
			fbid = getParams().getLong("fbid", -1);
			if(fbid < 0) {
				fbid = Util.toLong(Util.getHttpQueryValue("uid", fbsCookieStr));
			}
			fbidStr = Long.toString(fbid);
			accessToken = Util.getHttpQueryValue("access_token", fbsCookieStr);
		}
	}
	
	public long getFBID() {
		if(fbid < 0) {
			readFBSRCookie();
		}
		return fbid;	
	}
	
	public String getFBIDAsString() {
	  if(Util.nullOrEmptyString(fbidStr)) {
		  readFBSRCookie();
	  }
	  return fbidStr;
	}
	
	public String getAccessToken() {
		if(Util.nullOrEmptyString(accessToken)) {
			readFBSRCookie();
		}
		return accessToken;	
	}
	
	public boolean isConnected() throws UnsupportedEncodingException {
		return FBUtil.isConnected(getFBIDAsString(), getAccessToken());
	}
	
	public String getGraphAPIUrl(String what) throws UnsupportedEncodingException {
		return FBUtil.getGraphAPIUrl(getFBIDAsString(), what, getAccessToken());
	}
	
	public String getGraphAPI(String what) throws UnsupportedEncodingException {
		return FBUtil.getGraphAPI(getFBIDAsString(), what, getAccessToken());
	}
	
	public String getGraphObject(String what) throws UnsupportedEncodingException {
		return FBUtil.getGraphObject(what, getAccessToken());
	}
	
	public String postGraphAPI(String what) throws UnsupportedEncodingException {
		return FBUtil.postGraphAPI(getFBIDAsString(), what, getAccessToken());
	}
	
	public String postGraphAPI(String what, String fieldName, FileItem fileItem) throws UnsupportedEncodingException {
		return FBUtil.postGraphAPI(getFBIDAsString(), what, fieldName, fileItem, getAccessToken());
	}
	
	public String postGraphObject(String what) throws UnsupportedEncodingException {
		return FBUtil.postGraphObject(what, getAccessToken());
	}
	
	public String getFQLUrl(String query) {
		return FBUtil.getFQLUrl(query, getAccessToken());
	}
	
	public ArrayList<XMLRecord> getFQL(String tableName, String query) {
		return FBUtil.getFQL(tableName, query, getAccessToken());
	}
}
