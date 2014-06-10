package com.perpetumobile.bit.orm.json;

import java.io.File;

import junit.framework.TestCase;
import junit.framework.TestSuite;


public class JSONParserManagerTest extends TestCase {
	
	public static TestSuite suite() {
		TestSuite suite = new TestSuite();
		suite.addTest(new JSONParserManagerTest("test1"));
		suite.addTest(new JSONParserManagerTest("test2"));
		return suite;
	}
	
	public JSONParserManagerTest() {
	}
	
	public JSONParserManagerTest(String testName) {
		super(testName);
	}
	
	protected void setUp() {
	}
	
	protected void tearDown() {
	}
	
	public void test1() {
		System.out.println("####################################");
		System.out.println("JSONParserManager Test 1");
		System.out.println("####################################");
		try {
			JSONRecord result = JSONParserManager.getInstance().parseImpl(new File("data/json/sample_1.txt"), "JSONSample1");
			StringBuilder buf = result.generateJSON(true);
			System.out.println(buf.toString());
		} catch (Exception e) {
			fail("Exception at JSONParserManagerTest.test1: " + e.getLocalizedMessage());
		}
	}
	
	public void test2() {
		System.out.println("####################################");
		System.out.println("JSONParserManager Test 2");
		System.out.println("####################################");
		try {
			JSONRecord result = JSONParserManager.getInstance().parseImpl(new File("data/json/sample_2.txt"), "JSONSample2");
			StringBuilder buf = result.generateJSON(true);
			System.out.println(buf.toString());
		} catch (Exception e) {
			fail("Exception at JSONParserManagerTest.test2: " + e.getLocalizedMessage());
		}
	}
}
