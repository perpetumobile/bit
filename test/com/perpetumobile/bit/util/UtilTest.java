package com.perpetumobile.bit.util;

import java.util.ArrayList;

import com.perpetumobile.bit.orm.db.DBConnection;
import com.perpetumobile.bit.orm.db.DBConnectionManager;
import com.perpetumobile.bit.orm.db.DBUtil;
import com.perpetumobile.bit.util.Logger;
import com.perpetumobile.bit.util.Util;

import junit.framework.TestCase;


public class UtilTest extends TestCase {
	static private Logger logger = new Logger(UtilTest.class);
	
	static private String[] testStrings = {
		"1234567890", "Zoran Dukic"
	};
	
	public UtilTest() {
	}
	
	protected void setUp() {
	}
	
	protected void tearDown() {
	}
	
	public void testMD5() {
		DBConnection dbConnection = null;
		try {
			logger.info("Start UtilTest.testMD5");
			dbConnection = DBConnectionManager.getInstance().getConnection("Alert");
			for(String str : testStrings) {
				String dbUtilMD5 = DBUtil.getMD5(dbConnection, str);
				String utilMD5 = Util.getMD5(str);
				logger.debug("String: " + str + " DBUtil.MD5: " + dbUtilMD5 + " Util.MD5: " + utilMD5);
				assertEquals(dbUtilMD5, utilMD5);
			}
		} catch(Exception e) {
			logger.error("UtilTest.testMD5 exception", e);
			DBConnectionManager.getInstance().invalidateConnection(dbConnection);
			dbConnection = null;
		} finally {
			DBConnectionManager.getInstance().returnConnection(dbConnection);
		}
	}
	
	public void testGetDateList() {
		ArrayList<String> result = null;
		try {
			logger.info("Start UtilTest.testGetDateList");
			result = Util.getDateList("20110901", "20111231");
			assertEquals(result.size(), 30+31+30+31);
			assertEquals("20110901", result.get(0));
			assertEquals("20111231", result.get(result.size()-1));
			result = Util.getDateList("20120101", "20120430");
			assertEquals(result.size(), 31+29+31+30);
			assertEquals("20120101", result.get(0));
			assertEquals("20120430", result.get(result.size()-1));
			result = Util.getDateList("20120430", "20120601");
			assertEquals(result.size(), 1+31+1);
			assertEquals("20120430", result.get(0));
			assertEquals("20120601", result.get(result.size()-1));
		} catch(Exception e) {
			logger.error("UtilTest.testGetDateList exception", e);
		}
	}
}
