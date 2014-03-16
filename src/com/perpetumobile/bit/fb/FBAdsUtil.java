package com.perpetumobile.bit.fb;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.perpetumobile.bit.util.Util;

public class FBAdsUtil {
	private String accessToken;
	private long accountId;
	private String accountIdStr;

	public enum Action {
		Users("users"), AdAccountGroups("adaccountgroups"), AdCampaigns("adcampaigns"),
		AdCreatives("adcreatives"), CustomAudience("broadtargetingcategories"),
		AdGroups("adgroups"), AdImages("adimages"), Stats("stats");

		private String code;

		private Action(String code) {
			this.code = code;
		}

		public String getCode(){
			return code; 
		}
	}

	public enum Parameter {
		Name("name"), UID("uid"), Role("role"), DailyBudget("daily_budget"), 
		LifetimeBudget("lifetime_budget"), CampaignStatus("campaign_status"), 
		EndTime("end_time"), IncludeDeleted("include_deleted"), Type("type"), 
		Title("title"), Body("body"), LinkUrl("link_url"), ImageFile("image_file"),
		ImageHash("image_hash"), Hashes("hashes"), Id("id"), CampaignId("campaign_id"), 
		BidType("bid_type"), AdgroupStatus("adgroup_status"), MaxBid("max_bid"), 
		CreativeId("creative_id"), Creative("creative"), Targeting("targeting");

		private String code;

		private Parameter(String code) {
			this.code = code;
		}

		public String getCode(){
			return code; 
		}
	}

	public String getAccountIdAsString() {
		return "act_" + accountIdStr;
	}

	public FBAdsUtil(long accountId, String accessToken) {
		this.accountId = accountId;
		accountIdStr = String.valueOf(this.accountId);
		this.accessToken = accessToken;
	}

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String token) {
		accessToken = token;
	}

	/**
	 * route a get request
	 */
	private String getRequest(String accountId, String what, Object params) {
		return getRequest(accountId, what, accessToken, params);
	}
	
	/**
	 * route a get request
	 */
	private String getRequest(String accountId, String what, String accessToken, Object params) {
		try {
			String out = FBUtil.getGraphAPI(accountId, what, accessToken, params);
			return out;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * route a post request
	 */
	private String postRequest(String accountId, String what, Object params) {
		try {
			String out = FBUtil.postGraphAPI(accountId, what, accessToken, params);
			return out;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * route a post image request
	 */
	private String postImageRequest(String accountId, String what, Object params, File file) {
		try {
			String out = FBUtil.postImageGraphAPI(accountId, what, accessToken, params, file);
			return out;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * route a delete request
	 */
	private String deleteRequest(String accountId, String what, Object params) {
		try {
			String out = FBUtil.deleteGraphAPI(accountId, what, accessToken, params);
			return out;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	//Date functions
  public static long now() {
    Calendar cal = Calendar.getInstance(TimeZone
        .getTimeZone("America/Los_Angeles"));
    return cal.getTime().getTime();
  }
  
  public static long getStartOfToday() {
    Calendar cal = Calendar.getInstance(TimeZone
        .getTimeZone("America/Los_Angeles"));
    cal.set(Calendar.HOUR_OF_DAY, 0);
    cal.set(Calendar.MINUTE, 0);
    cal.set(Calendar.SECOND, 0);
    return cal.getTime().getTime();
  }
  
  /**
   * Get access token from FB
   */
  public String fetchAccessTokenFromFB(String secret, String tempToken) {
  	String accessToken = null;
  	HashMap<String, String> params = new HashMap<String, String>();
  	params.put("client_id", "324910570946355");
  	params.put("client_secret", secret);
  	params.put("grant_type", "fb_exchange_token");
  	params.put("fb_exchange_token", tempToken);
  	return getRequest(null, "oauth/access_token", null, params);
  	
  	//return accessToken; //TODO parse and get access token and save it in the class variable 
  }

	/**
	 * Adds an ad user to the account
	 */
	public String addAdUser(String userId, int role) {
		HashMap<String, String> params = new HashMap<String, String>();
		params.put(Parameter.UID.getCode(), userId);
		params.put(Parameter.Role.getCode(), String.valueOf(role));

		return postRequest(getAccountIdAsString(), Action.Users.getCode(), params);
	}

	/**
	 * Retrieves an ad user 
	 */
	public String getAdUsersForAccount() {
		return getRequest(getAccountIdAsString(), Action.Users.getCode(), null);
	}

	/**
	 * Delete an user from an ad account
	 */
	public String deleteAdUser(String adUserId) {
		//HACK this is an exception case. this is the only request that has 
		//two sub-directories in "what"
		String what = Action.Users.getCode() + "/" + adUserId; 

		return deleteRequest(getAccountIdAsString(), what, null);
	}

	/**
	 * Creates an ad account group and returns the id
	 */
	public String createAdAccountGroup(String accountName) {
		String fbid = "me";
		HashMap<String, String> params = new HashMap<String, String>();
		params.put(Parameter.Name.getCode(), accountName);

		return postRequest(fbid, Action.AdAccountGroups.getCode(), params);
	}

	/**
	 * Retrieves an ad account group 
	 */
	public String getAdAccountGroup() {
		return getRequest(accountIdStr, null, null);
	}

	/**
	 * Rename an ad account group
	 */
	public String renameAdAccountGroup(String newAccountName) {
		HashMap<String, String> params = new HashMap<String, String>();
		params.put(Parameter.Name.getCode(), newAccountName);

		return postRequest(accountIdStr, null, params);
	}

	/**
	 * Delete an ad account group
	 */
	public String deleteAdAccountGroup(String adAccountId) {
		return deleteRequest(adAccountId, null, null);
	}

	/**
	 * Creates an ad campaign and returns the id 
	 */
	public String createAdCampaign(String campaignName, long dailyBudget, 
			long lifetimeBudget, Date endDate) {
		String what = Action.AdCampaigns.getCode();
		HashMap<String, String> params = new HashMap<String, String>();
		params.put(Parameter.Name.getCode(), campaignName);
		params.put(Parameter.DailyBudget.getCode(), String.valueOf(dailyBudget));
		params.put(Parameter.CampaignStatus.getCode(), String.valueOf(1)); //1 is active
		if(endDate != null && endDate.getTime() > System.currentTimeMillis() &&
				lifetimeBudget >= dailyBudget) {
			params.put(Parameter.EndTime.getCode(), String.valueOf(endDate.getTime()/1000L));
			params.put(Parameter.LifetimeBudget.getCode(), String.valueOf(lifetimeBudget));			
		}

		return postRequest(getAccountIdAsString(), what, params);
	}

	/**
	 * Retrieves an ad campaign 
	 */
	public String getAdCampaign(String campaignId) {
		return getRequest(campaignId, null, null);
	}
	
	/**
	 * Retrieves an ad campaign stats
	 */
	public String getAdCampaignStats(String campaignId) {
		return getAdCampaignStats(campaignId, getStartOfToday()/1000, now()/1000);
	}

	public String getAdCampaignStats(String campaignId, long start, long end) {
		String what = Action.Stats.getCode() + "/" + start + "/" + end;
		return getRequest(campaignId, what, null);
	}
	
	/**
	 * Retrieves all campaigns of one account
	 */
	public String getAdCampaignsForAccount(boolean includeDeleted) {
		HashMap<String, String> params = null;
		if(includeDeleted) {
			params = new HashMap<String, String>(1);
			params.put(Parameter.IncludeDeleted.getCode(), String.valueOf(includeDeleted));
		}

		return getRequest(getAccountIdAsString(), Action.AdCampaigns.getCode(), params);
	}

	/**
	 * Retrieves all campaigns of one account
	 */  
	public String getAdCampaignsForAccount() {
		return getAdCampaignsForAccount(false);
	}

	/**
	 * update an ad campaign
	 * 
	 * FB does not support changing end date
	 */
	public String updateAdCampaign(String campaignId, Map<String, String> params) {
		return postRequest(campaignId, null, params);
	}

	/**
	 * deletes an ad campaign 
	 */
	public String deleteAdCampaign(String campaignId) {
		return deleteRequest(campaignId, null, null);
	}

	/**
	 * upload an ad image and return the image_hash
	 */
	public String uploadAdImage(String imageFile) throws UnsupportedEncodingException {
		HashMap<String, String> params = new HashMap<String, String>();

		File file = new File(imageFile);
		if(!file.isFile())
			return null;

		imageFile = "@" + imageFile;
		params.put(file.getName(), imageFile);

		return postImageRequest(getAccountIdAsString(), Action.AdImages.getCode(), params, file);
	}

	public String getAdImage(List<String> imageHashes) {
		HashMap<String, JSONArray> params = null;
		if(!Util.nullOrEmptyList(imageHashes)) {
			params = new HashMap<String, JSONArray>();
			JSONArray arr = new JSONArray();
			for(String hash : imageHashes)
				arr.put(hash);
					params.put(Parameter.Hashes.getCode(), arr);
		}

		return getRequest(getAccountIdAsString(), Action.AdImages.getCode(), params);
	}

	public String getFileName(String fullPath) {
		if(fullPath != null)
			return fullPath.substring(fullPath.lastIndexOf('/') + 1);
		else 
			return null;
	}

	/**
	 * create an ad creative
	 */
	public String createAdCreative(String creativeName, int adType, String title, String body,
			String url, String imageFile, String imageHash) {
		HashMap<String, String> params = new HashMap<String, String>();
		if(!Util.nullOrEmptyString(creativeName))
			params.put(Parameter.Name.getCode(), creativeName);
		params.put(Parameter.Type.getCode(), String.valueOf(adType));

		if(title.length() > 25)
			title = title.substring(0, 25);
		params.put(Parameter.Title.getCode(), title);

		if(body.length() > 90)
			body = body.substring(0, 90);
		params.put(Parameter.Body.getCode(), body);

		if(url.length() > 1024)
			url = url.substring(0, 1024);
		params.put(Parameter.LinkUrl.getCode(), url);

		if(!Util.nullOrEmptyString(imageFile)) {
			//facebook does not like the full image file path adcreative call. It wants only the filename
			String fileName = imageFile.substring(imageFile.lastIndexOf(File.separatorChar) + 1);
			params.put(Parameter.ImageFile.getCode(), fileName);

			File file = new File(imageFile);
			return postImageRequest(getAccountIdAsString(), Action.AdCreatives.getCode(), params, file);

		} else if(!Util.nullOrEmptyString(imageHash)) {
			params.put(Parameter.ImageHash.getCode(), imageHash);
			return postRequest(getAccountIdAsString(), Action.AdCreatives.getCode(), params);

		} else
			return null;
	}

	/**
	 * updates an ad creative
	 * TODO complete this
	 */
	public String updateAdCreative(String creativeName, int adType, String title, String body,
			String url, String imageFile, String imageHash) {
		String accountId = getAccountIdAsString();
		String what = Action.AdCreatives.getCode();

		return null;
	}

	/**
	 * retrieves all ad creatives
	 */
	public String getAdCreatives() {
		return getRequest(getAccountIdAsString(), Action.AdCreatives.getCode(), null);
	}

	/**
	 * creates an empty custom audience object
	 */
	public String createCustomAudience(String customAudienceName) {
		HashMap<String, String> params = new HashMap<String, String>();
		params.put(Parameter.Name.getCode(), customAudienceName);

		return postRequest(getAccountIdAsString(), Action.CustomAudience.getCode(), params);
	}

	/**
	 * adds users to custom audience object
	 */
	public String addToCustomAudience(String audienceId, List<String> ids) throws JSONException {
		HashMap<String, JSONArray> params = new HashMap<String, JSONArray>();
		JSONArray arr = new JSONArray();
		for(String id : ids) {
			JSONObject obj = new JSONObject();
			try {
				obj.put(Parameter.Id.getCode(), id);
			} catch (JSONException e) {
				throw new JSONException("Unable to add id " + id + " to list\n" + e.getMessage());
			}
		}
		params.put(Action.Users.getCode(), arr);

		return postRequest(getAccountIdAsString(), Action.CustomAudience.getCode(), params);
	}

	public String addToCustomAudience(Long audienceId, List<Long> ids) throws JSONException {
		ArrayList<String> idsStr = new ArrayList<String>(ids.size());
		for(Long id : ids)
			idsStr.add(Long.toString(id));

		return addToCustomAudience(Long.toString(audienceId), idsStr);   
	}

	/**
	 * gets users in custom audience objects
	 */
	public String getCustomAudience() {
		return getRequest(getAccountIdAsString(), Action.CustomAudience.getCode(), null);
	}

	/**
	 * deletes an ad campaign 
	 */
	public String deleteFromCustomAudience(String audienceId, List<String> ids) throws JSONException {
		HashMap<String, JSONArray> params = new HashMap<String, JSONArray>();
		JSONArray arr = new JSONArray();
		for(String id : ids) {
			JSONObject obj = new JSONObject();
			try {
				obj.put("id", id);
			} catch (JSONException e) {
				throw new JSONException("Unable to add id " + id + " to list\n" + e.getMessage());
			}
		}
		params.put(Action.Users.getCode(), arr);

		return deleteRequest(audienceId, Action.Users.getCode(), params);
	}

	/**
	 * creates an ad group
	 */
	public String createAdGroup(String groupName, String campaignId, String creativeId, JSONObject targetingSpec,
			long maxBid, Date endTime) {
		HashMap<String, String> params = new HashMap<String, String>();
		params.put(Parameter.Name.getCode(), groupName);
		params.put(Parameter.CampaignId.getCode(), campaignId);
		params.put(Parameter.AdgroupStatus.getCode(), "1");
		params.put(Parameter.BidType.getCode(), "1");
		params.put(Parameter.MaxBid.getCode(), Long.toString(maxBid));

		JSONObject creative = null;
		try {
			creative = new JSONObject();
			creative.put(Parameter.CreativeId.getCode(), creativeId);
		}
		catch (JSONException e) {
			e.printStackTrace();
			return null;
		}

		params.put(Parameter.Creative.getCode(), creative.toString());
		params.put(Parameter.Targeting.getCode(), targetingSpec.toString());
			if(endTime != null && endTime.getTime() > System.currentTimeMillis())
				params.put(Parameter.EndTime.getCode(), Long.toString(endTime.getTime()));

		return postRequest(getAccountIdAsString(), Action.AdGroups.getCode(), params);
	}
	
	public String updateAdGroup(String adGroupId, Map<String, String> params) {
		return postRequest(adGroupId, null, params);
	}

	/**
	 * gets all ad groups for an account
	 */
	public String getAllAdGroupsForAccount() {

		return getRequest(getAccountIdAsString(), Action.AdGroups.getCode(), null);
	}

	/**
	 * gets all ad groups for an account
	 */
	public String getAdGroup(String adGroupId) {
		return getRequest(adGroupId, null, null);
	}


	/**
	 * gets all ad groups for a campaign
	 */
	public String getAllAdGroupsForCampaign(String campaignId) {
		return getRequest(campaignId, Action.AdGroups.getCode(), null);
	}

	/**
	 * deletes an ad group 
	 */
	public String deleteAdGroup(String adGroupId) {
		return deleteRequest(adGroupId, null, null);
	}
}
