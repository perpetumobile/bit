package com.perpetumobile.bit.http;

import com.perpetumobile.bit.http.HttpMethod;
import com.perpetumobile.bit.http.HttpRequest;

import junit.framework.TestCase;
import junit.framework.TestSuite;


public class HttpManagerTest extends TestCase {
	
	public static TestSuite suite() {
		TestSuite suite = new TestSuite();
		suite.addTest(new HttpManagerTest("test1"));
		suite.addTest(new HttpManagerTest("test2"));
		return suite;
	}
	
	public HttpManagerTest() {
	}
	
	public HttpManagerTest(String testName) {
		super(testName);
	}
	
	protected void setUp() {
	}
	
	protected void tearDown() {
	}
	
	public void test1() {
		System.out.println("####################################");
		System.out.println("HttpManager Test 1");
		System.out.println("####################################");
		try {			
			HttpRequest httpRequest = new HttpRequest(HttpMethod.GET, "http://www.thefind.com/search?query=espresso+machines");
			String result = HttpManager.getInstance().executeImpl(httpRequest).getPageSource();
			System.out.println(result);
		} catch (Exception e) {
			fail("Exception at HttpManagerTest.test1: " + e.getLocalizedMessage());
		}
	}
	
	public void test2() {
		System.out.println("####################################");
		System.out.println("HttpManager Test 2");
		System.out.println("####################################");
		try {			
			HttpRequest httpRequest = new HttpRequest(HttpMethod.POST, "http://www.thefind.com/search");
			httpRequest.setContent("query=espresso+machines");
			httpRequest.setMimeType("application/x-www-form-urlencoded");
			httpRequest.setCharset("UTF-8");
			String result = HttpManager.getInstance().executeImpl(httpRequest).getPageSource();
			System.out.println(result);
		} catch (Exception e) {
			fail("Exception at HttpManagerTest.test2: " + e.getLocalizedMessage());
		}
	}
}
