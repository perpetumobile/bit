package com.perpetumobile.bit.orm.json;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import junit.framework.TestCase;
import junit.framework.TestSuite;


public class JSONParserManagerTest extends TestCase {
	
	public static final String JSON_DATA_FILE_NAME = "json.data";
	
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
		File file = new File(JSON_DATA_FILE_NAME);
		if(file.exists()) {
			file.delete();
		}
	}
	
	protected void write(JSONRecord rec, String fileName) {
		try {			
			ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(fileName, false));
			out.writeObject(rec);
			out.flush();
			out.close();
		} catch (Exception e) {
			fail("Exception at JSONParserManagerTest.write: " + e.getLocalizedMessage());
		}
	}
	
	protected JSONRecord read(String fileName) {
		JSONRecord result = null;
		try {			
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(fileName));
			result = (JSONRecord)in.readObject();
			in.close();
		} catch (Exception e) {
			fail("Exception at JSONParserManagerTest.write: " + e.getLocalizedMessage());
		}
		return result;
	}
	
	public void test1() {
		System.out.println("####################################");
		System.out.println("JSONParserManager Test 1");
		System.out.println("####################################");
		try {
			JSONRecord result = JSONParserManager.getInstance().parseImpl(new File("data/json/sample_1.txt"), "JSONSample1");
			StringBuilder buf = result.generateJSON(true);
			String jsonStr = buf.toString();
			System.out.println(jsonStr);
			// test serialization
			write(result, JSON_DATA_FILE_NAME);
			result = read(JSON_DATA_FILE_NAME);
			buf = result.generateJSON(true);
			assertEquals(jsonStr, buf.toString());
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
			String jsonStr = buf.toString();
			System.out.println(jsonStr);
			// test serialization
			write(result, JSON_DATA_FILE_NAME);
			result = read(JSON_DATA_FILE_NAME);
			buf = result.generateJSON(true);
			assertEquals(jsonStr, buf.toString());
		} catch (Exception e) {
			fail("Exception at JSONParserManagerTest.test2: " + e.getLocalizedMessage());
		}
	}
}
