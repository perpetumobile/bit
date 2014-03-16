package com.perpetumobile.bit.orm.cassandra;

import java.util.ArrayList;

import org.apache.cassandra.thrift.IndexOperator;

import com.perpetumobile.bit.orm.cassandra.CassandraCliScript;
import com.perpetumobile.bit.orm.cassandra.CassandraCliScriptReadStatement;
import com.perpetumobile.bit.orm.cassandra.CassandraColumnCursor;
import com.perpetumobile.bit.orm.cassandra.CassandraConnection;
import com.perpetumobile.bit.orm.cassandra.CassandraConnectionManager;
import com.perpetumobile.bit.orm.cassandra.CassandraCursor;
import com.perpetumobile.bit.orm.cassandra.CassandraKey;
import com.perpetumobile.bit.orm.cassandra.CassandraRecord;
import com.perpetumobile.bit.orm.cassandra.CassandraStatement;
import com.perpetumobile.bit.orm.record.field.ByteBufferField;
import com.perpetumobile.bit.util.Util;

import junit.framework.TestCase;
import junit.framework.TestSuite;


public class CassandraTestSuite extends TestCase {
	
	static public final String TEST_CASSANDRA_CONFIG_NAME = "Test.Cassandra";

	public static TestSuite suite() {
		TestSuite suite = new TestSuite();
		suite.addTest(new CassandraTestSuite("testScript"));
		suite.addTest(new CassandraTestSuite("testReadScript"));
		suite.addTest(new CassandraTestSuite("testReadScript2"));
		suite.addTest(new CassandraTestSuite("testInsertReadDelete"));
		suite.addTest(new CassandraTestSuite("testBatchInsertReadDelete"));
		suite.addTest(new CassandraTestSuite("testCount"));
		suite.addTest(new CassandraTestSuite("testRead"));
		suite.addTest(new CassandraTestSuite("testReadSuperColumn"));
		suite.addTest(new CassandraTestSuite("testCursor"));
		suite.addTest(new CassandraTestSuite("testCursorSuperColumn"));
		suite.addTest(new CassandraTestSuite("testColumnCursor"));
		return suite;
	}
	
	private CassandraConnection connection = null;
	
	public CassandraTestSuite() {
	}
	
	public CassandraTestSuite(String testName) {
		super(testName);
	}
	
	protected void setUp() throws Exception {
		connection = CassandraConnectionManager.getInstance().getConnection(TEST_CASSANDRA_CONFIG_NAME);
	}
	
	protected void tearDown() {
		CassandraConnectionManager.getInstance().invalidateConnection(connection);
	}
	
	public void testScript() {
		System.out.println("####################################");
		System.out.println("Test Script");
		System.out.println("####################################");
		CassandraConnection cassandraConnection = null;
		try {
			cassandraConnection = CassandraConnectionManager.getInstance().getConnection(TEST_CASSANDRA_CONFIG_NAME);
			CassandraCliScript script = new CassandraCliScript(cassandraConnection, "scripts/cassandra/test.cli");
			script.execute();
		} catch (Exception e) {
			CassandraConnectionManager.getInstance().invalidateConnection(cassandraConnection);
			cassandraConnection = null;
			fail("Exception at CassandraTest.testScript: " + e.getLocalizedMessage());
		} finally {
			CassandraConnectionManager.getInstance().returnConnection(cassandraConnection);
		}
	}
	
	private void testReadScript(CassandraConnection cassandraConnection) throws Exception {
		CassandraCliScriptReadStatement<CassandraRecord> stmt = new CassandraCliScriptReadStatement<CassandraRecord>("test_read.cli", "TestStandard.Cassandra");
		ArrayList<CassandraRecord> list = stmt.readCassandraRecords(cassandraConnection);
		assertEquals(4, list.size());
		for(CassandraRecord r : list) {
			r.print(true);
		}
	}
	
	public void testReadScript() {
		System.out.println("####################################");
		System.out.println("Test ReadScript");
		System.out.println("####################################");
		CassandraConnection cassandraConnection = null;
		try {
			cassandraConnection = CassandraConnectionManager.getInstance().getConnection(TEST_CASSANDRA_CONFIG_NAME);
			testReadScript(cassandraConnection);
		} catch (Exception e) {
			CassandraConnectionManager.getInstance().invalidateConnection(cassandraConnection);
			cassandraConnection = null;
			fail("Exception at CassandraTest.testReadScript: " + e.getLocalizedMessage());
		} finally {
			CassandraConnectionManager.getInstance().returnConnection(cassandraConnection);
		}
	}
	
	public void testReadScript2() {
		System.out.println("####################################");
		System.out.println("Test ReadScript2");
		System.out.println("####################################");
		try {
			testReadScript(connection);
		} catch (Exception e) {
			fail("Exception at CassandraTest.testReadScript2: " + e.getLocalizedMessage());
		}
	}
	
	protected CassandraRecord createCassandraRecord(String configName, String key) { 
		CassandraRecord rec = new CassandraRecord(configName);
		rec.setKey(CassandraKey.create(key));
		rec.setLongFieldValue("id", Long.parseLong(key));
		rec.setFieldValue("first", "zoran");
		rec.setFieldValue("last", "dukic");
		rec.setFieldValue("email", "zoran@email.com");
		rec.setIntFieldValue("age", -256);
		rec.setByteBufferFieldValue("tuuid", Util.getTimeUUID());
		rec.setByteBufferFieldValue("luuid", Util.getLexicalUUID());
		return rec;
	}
	
	@SuppressWarnings("deprecation")
	public void testInsertReadDelete() {
		System.out.println("####################################");
		System.out.println("Test Insert/Read/Delete");
		System.out.println("####################################");
		CassandraConnection cassandraConnection = null;
		CassandraRecord rec = createCassandraRecord("TestStandard.Cassandra", "100");
		try {
			cassandraConnection = CassandraConnectionManager.getInstance().getConnection(TEST_CASSANDRA_CONFIG_NAME);
			CassandraStatement<CassandraRecord> query = new CassandraStatement<CassandraRecord>("TestStandard.Cassandra");
			query.insertCassandraRecord(cassandraConnection, rec);
			
			CassandraRecord r = query.readCassandraRecord(cassandraConnection, CassandraKey.create("100"));
			assertEquals(rec.getLongFieldValue("id"), r.getLongFieldValue("id"));
			assertEquals(rec.getFieldValue("first"), r.getFieldValue("first"));
			assertEquals(rec.getIntFieldValue("age"), r.getIntFieldValue("age"));
			
			query.deleteCassandraKey(cassandraConnection, CassandraKey.create("100"));
			r = query.readCassandraRecord(cassandraConnection, CassandraKey.create("100"));
			assertEquals(null, r);
		} catch (Exception e) {
			CassandraConnectionManager.getInstance().invalidateConnection(cassandraConnection);
			cassandraConnection = null;
			fail("Exception at CassandraTest.testInsert: " + e.getLocalizedMessage());
		} finally {
			CassandraConnectionManager.getInstance().returnConnection(cassandraConnection);
		}
	}
	
	public void testBatchInsertReadDelete() {
		System.out.println("####################################");
		System.out.println("Test Batch Insert/Read/Delete");
		System.out.println("####################################");
		CassandraConnection cassandraConnection = null;
		CassandraRecord rec = createCassandraRecord("TestSuper1.Cassandra", "100");
		try {
			cassandraConnection = CassandraConnectionManager.getInstance().getConnection(TEST_CASSANDRA_CONFIG_NAME);
			CassandraStatement<CassandraRecord> query = new CassandraStatement<CassandraRecord>("TestSuper1.Cassandra");
			query.batchInsertCassandraRecord(cassandraConnection, rec);
			
			CassandraRecord r = query.readCassandraRecord(cassandraConnection, CassandraKey.create("100"));
			assertEquals(rec.getLongFieldValue("id"), r.getLongFieldValue("id"));
			assertEquals(rec.getFieldValue("first"), r.getFieldValue("first"));
			assertEquals(rec.getIntFieldValue("age"), r.getIntFieldValue("age"));
			
			query.deleteCassandraKey(cassandraConnection, CassandraKey.create("100"));
			r = query.readCassandraRecord(cassandraConnection, CassandraKey.create("100"));
			assertEquals(null, r);
		} catch (Exception e) {
			CassandraConnectionManager.getInstance().invalidateConnection(cassandraConnection);
			cassandraConnection = null;
			fail("Exception at CassandraTest.testBatchInsert: " + e.getLocalizedMessage());
		} finally {
			CassandraConnectionManager.getInstance().returnConnection(cassandraConnection);
		}
	}
	
	public void testCount() {
		System.out.println("####################################");
		System.out.println("Test Count");
		System.out.println("####################################");
		CassandraConnection cassandraConnection = null;
		try {
			cassandraConnection = CassandraConnectionManager.getInstance().getConnection(TEST_CASSANDRA_CONFIG_NAME);
			CassandraStatement<CassandraRecord> query = new CassandraStatement<CassandraRecord>("TestStandard.Cassandra");
			int count = query.getCount(cassandraConnection, CassandraKey.create("1"));
			System.out.println("Number of columns: " + count);
			assertEquals(7, count);
		} catch (Exception e) {
			CassandraConnectionManager.getInstance().invalidateConnection(cassandraConnection);
			cassandraConnection = null;
			fail("Exception at CassandraTest.testCount: " + e.getLocalizedMessage());
		} finally {
			CassandraConnectionManager.getInstance().returnConnection(cassandraConnection);
		}
	}
	
	public void testRead() {
		System.out.println("####################################");
		System.out.println("Test Read Index Expression");
		System.out.println("####################################");
		CassandraConnection cassandraConnection = null;
		try {
			cassandraConnection = CassandraConnectionManager.getInstance().getConnection(TEST_CASSANDRA_CONFIG_NAME);
			CassandraStatement<CassandraRecord> query = new CassandraStatement<CassandraRecord>("TestStandard.Cassandra");
			query.addIndexExpression("email", IndexOperator.EQ, "zoran@email.com");
			query.addIndexExpression("age", IndexOperator.GTE, 42);
			ArrayList<CassandraRecord> list = query.readCassandraRecords(cassandraConnection);
			for(CassandraRecord r : list) {
				r.print(true);
			}
			assertEquals(3, list.size());
			System.out.println("####################################");
			System.out.println("Test Read All");
			System.out.println("####################################");
			query.reset();
			list = query.readCassandraRecords(cassandraConnection);
			for(CassandraRecord r : list) {
				r.print(true);
			}
			assertEquals(4, list.size());
		} catch (Exception e) {
			CassandraConnectionManager.getInstance().invalidateConnection(cassandraConnection);
			cassandraConnection = null;
			fail("Exception at CassandraTest.testRead: " + e.getLocalizedMessage());
		} finally {
			CassandraConnectionManager.getInstance().returnConnection(cassandraConnection);
		}
	}
	
	public void testReadSuperColumn() {
		System.out.println("####################################");
		System.out.println("Test Read SuperColumn");
		System.out.println("####################################");
		CassandraConnection cassandraConnection = null;
		try {
			cassandraConnection = CassandraConnectionManager.getInstance().getConnection(TEST_CASSANDRA_CONFIG_NAME);
			CassandraStatement<CassandraRecord> query = new CassandraStatement<CassandraRecord>("TestSuper3.Cassandra");
			ArrayList<CassandraRecord> list = query.readCassandraRecords(cassandraConnection);
			for(CassandraRecord r : list) {
				r.print(true);
			}
			assertEquals(4, list.size());
		} catch (Exception e) {
			CassandraConnectionManager.getInstance().invalidateConnection(cassandraConnection);
			cassandraConnection = null;
			fail("Exception at CassandraTest.testCursorSuperColumn: " + e.getLocalizedMessage());
		} finally {
			CassandraConnectionManager.getInstance().returnConnection(cassandraConnection);
		}
	}
	
	public void testCursor() {
		System.out.println("####################################");
		System.out.println("Test Cursor");
		System.out.println("####################################");
		CassandraConnection cassandraConnection = null;
		try {
			cassandraConnection = CassandraConnectionManager.getInstance().getConnection(TEST_CASSANDRA_CONFIG_NAME);
			CassandraStatement<CassandraRecord> query = new CassandraStatement<CassandraRecord>("TestStandard.Cassandra");
			query.addIndexExpression("email", IndexOperator.EQ, "zoran@email.com");
			query.addIndexExpression("age", IndexOperator.GTE, 42);
			CassandraCursor<CassandraRecord> cursor = new CassandraCursor<CassandraRecord>(query, 1);
			CassandraRecord r = cursor.readNext(cassandraConnection);
			while(r != null) {
				r.print(true);
				r = cursor.readNext(cassandraConnection);
			}
			cursor.close();
		} catch (Exception e) {
			CassandraConnectionManager.getInstance().invalidateConnection(cassandraConnection);
			cassandraConnection = null;
			fail("Exception at CassandraTest.testCursor: " + e.getLocalizedMessage());
		} finally {
			CassandraConnectionManager.getInstance().returnConnection(cassandraConnection);
		}
	}
	
	public void testCursorSuperColumn() {
		System.out.println("####################################");
		System.out.println("Test Cursor SuperColumn");
		System.out.println("####################################");
		CassandraConnection cassandraConnection = null;
		try {
			cassandraConnection = CassandraConnectionManager.getInstance().getConnection(TEST_CASSANDRA_CONFIG_NAME);
			CassandraStatement<CassandraRecord> query = new CassandraStatement<CassandraRecord>("TestSuper3.Cassandra");
			CassandraCursor<CassandraRecord> cursor = new CassandraCursor<CassandraRecord>(query, 3);
			CassandraRecord r = cursor.readNext(cassandraConnection);
			while(r != null) {
				r.print(true);
				r = cursor.readNext(cassandraConnection);
			}
			cursor.close();
		} catch (Exception e) {
			CassandraConnectionManager.getInstance().invalidateConnection(cassandraConnection);
			cassandraConnection = null;
			fail("Exception at CassandraTest.testCursorSuperColumn: " + e.getLocalizedMessage());
		} finally {
			CassandraConnectionManager.getInstance().returnConnection(cassandraConnection);
		}
	}
	
	public void testColumnCursor() {
		System.out.println("####################################");
		System.out.println("Test Column Cursor");
		System.out.println("####################################");
		CassandraConnection cassandraConnection = null;
		try {
			cassandraConnection = CassandraConnectionManager.getInstance().getConnection(TEST_CASSANDRA_CONFIG_NAME);
			CassandraStatement<CassandraRecord> query = new CassandraStatement<CassandraRecord>("TestStandard.Cassandra");
			CassandraColumnCursor cursor = new CassandraColumnCursor(CassandraKey.create("1"), query.getColumnParent(), 2);
			ByteBufferField f = cursor.readNext(cassandraConnection);
			System.out.println("Columns:");
			while(f != null) {
				System.out.println(f.getFieldName() + " : " + f.getFieldValue());
				f = cursor.readNext(cassandraConnection);
			}
			cursor.close();
		} catch (Exception e) {
			CassandraConnectionManager.getInstance().invalidateConnection(cassandraConnection);
			cassandraConnection = null;
			fail("Exception at CassandraTest.testColumnCursor: " + e.getLocalizedMessage());
		} finally {
			CassandraConnectionManager.getInstance().returnConnection(cassandraConnection);
		}
	}
}
