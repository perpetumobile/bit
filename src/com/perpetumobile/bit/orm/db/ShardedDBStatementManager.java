package com.perpetumobile.bit.orm.db;

import java.util.ArrayList;
import java.util.HashMap;

import com.perpetumobile.bit.config.Config;
import com.perpetumobile.bit.config.ConfigSubscriber;
import com.perpetumobile.bit.util.Logger;
import com.perpetumobile.bit.util.Util;

/**
 * @author Zoran Dukic
 *
 */
public class ShardedDBStatementManager implements ConfigSubscriber {
	static private ShardedDBStatementManager instance = new ShardedDBStatementManager();
	static public ShardedDBStatementManager getInstance() { return instance; }
	
	static private Logger logger = new Logger(ShardedDBStatementManager.class);
	
	static final public String SCHEMAS_CONFIG_NAME = "ShardedDBStatementManager.Schemas";

	protected HashMap<String, ShardedDBInstanceMap> schemaMap;
	
	protected Object configLock = new Object();
	
	private ShardedDBStatementManager() {
		onConfigReset();
		Config.getInstance().subscribe(this);
	}
	
	@Override
	public void onConfigReset() {
		synchronized(configLock) {
			schemaMap = new HashMap<String, ShardedDBInstanceMap>();
			String shemasStr = Config.getInstance().getProperty(SCHEMAS_CONFIG_NAME, "");
			String[] schemas = shemasStr.split(",");
			for(String s : schemas) {
				schemaMap.put(s, new ShardedDBInstanceMap(s));
			}
		}
	}
			
	public ArrayList<? extends DBRecord> selectImpl(String schemaConfigName, String id, DBStatement<? extends DBRecord> stmt)
	throws Exception {
		return selectImpl(schemaConfigName, id, stmt, null);
	}
	
	public int getShard(String schemaConfigName, String id) {
		int result = -1;
		ShardedDBInstanceMap map = null;
		synchronized (configLock) {
			map = schemaMap.get(schemaConfigName);
		}
		if(map != null) {
			result = map.getShard(id);
		}
		return result;
	}
	
	public boolean isSameShard(String schemaConfigName, String id1, String id2) {
		return (getShard(schemaConfigName, id1) == getShard(schemaConfigName, id2));
	}
	
	public ArrayList<DBInstance> getReadSQLInstances(String schemaConfigName, String id) {
		ArrayList<DBInstance> result = null;
		ShardedDBInstanceMap map = null;
		synchronized (configLock) {
			map = schemaMap.get(schemaConfigName);
		}
		if(map != null) {
			result = map.getReadSQLInstances(id);
		}
		return result;
	}
	
	public ArrayList<DBInstance> getWriteSQLInstances(String schemaConfigName, String id) {
		ArrayList<DBInstance> result = null;
		ShardedDBInstanceMap map = null;
		synchronized (configLock) {
			map = schemaMap.get(schemaConfigName);
		}
		if(map != null) {
			result = map.getWriteSQLInstances(id);
		}
		return result;
	}
			
	public ArrayList<? extends DBRecord> selectImpl(String schemaConfigName, String id, DBStatement<? extends DBRecord> stmt, String strSQL) 
	throws Exception {
		ArrayList<? extends DBRecord> result = null;
		
		ArrayList<DBInstance> list = getReadSQLInstances(schemaConfigName, id);
		if(!Util.nullOrEmptyList(list)) {
			DBInstance sqlInstance = list.get(Util.random(list.size()));
			if(sqlInstance != null) {
				// TODO: Add read fail-over and blacklisting
				DBConnection dbConnection = null;
				try {
					dbConnection = DBConnectionManager.getInstance().getConnection(sqlInstance.getConfigName());
					dbConnection.setSchema(sqlInstance.getSchema());
					if(Util.nullOrEmptyString(strSQL)) {
						result = stmt.readDBRecords(dbConnection);
					} else {
						result = stmt.readDBRecords(dbConnection, strSQL);
					}
				} catch (Exception e) {
					logger.error("ShardedDBStatementManager.select exception", e);
					DBConnectionManager.getInstance().invalidateConnection(dbConnection);
					dbConnection = null;
					throw e;
				} finally {
					DBConnectionManager.getInstance().returnConnection(dbConnection);
				}
			}
		}
				
		return result;
	}
	
	@SuppressWarnings({ "unchecked" })
	public int insertImpl(String schemaConfigName, String id, DBStatementMethod method, DBStatement<? extends DBRecord> stmt, DBRecord rec)
	throws Exception {
		int result = 0;
		ArrayList<DBInstance> list = getWriteSQLInstances(schemaConfigName, id);
		if(!Util.nullOrEmptyList(list)) {
			for(DBInstance sqlInstance : list) {
				// TODO: Add error handling when write to multiple instances is required and one of them fail
				DBConnection dbConnection = null;
				try {
					dbConnection = DBConnectionManager.getInstance().getConnection(sqlInstance.getConfigName());
					dbConnection.setSchema(sqlInstance.getSchema());
					if(method == DBStatementMethod.INSERT) {
						result = ((DBStatement<DBRecord>)stmt).insertDBRecord(dbConnection, rec);
					} else if(method == DBStatementMethod.INSERT_IGNORE) {
						result = ((DBStatement<DBRecord>)stmt).insertIgnoreDBRecord(dbConnection, rec);
					} else if(method == DBStatementMethod.REPLACE){
						result = ((DBStatement<DBRecord>)stmt).replaceDBRecord(dbConnection, rec);
					}
				} catch (Exception e) {
					logger.error("ShardedDBStatementManager.insert exception", e);
					DBConnectionManager.getInstance().invalidateConnection(dbConnection);
					dbConnection = null;
					throw e;
				} finally {
					DBConnectionManager.getInstance().returnConnection(dbConnection);
				}
			}
		}
		return result;
	}
	
	@SuppressWarnings({ "unchecked" })
	public void insertImpl(String schemaConfigName, String id, DBStatementMethod method, DBStatement<? extends DBRecord> stmt, ArrayList<DBRecord> recs)
	throws Exception {
		ArrayList<DBInstance> list = getWriteSQLInstances(schemaConfigName, id);
		if(!Util.nullOrEmptyList(list)) {
			for(DBInstance sqlInstance : list) {
				// TODO: Add error handling when write to multiple instances is required and one of them fail
				DBConnection dbConnection = null;
				try {
					dbConnection = DBConnectionManager.getInstance().getConnection(sqlInstance.getConfigName());
					dbConnection.setSchema(sqlInstance.getSchema());
					if(method == DBStatementMethod.INSERT) {
						((DBStatement<DBRecord>)stmt).insertDBRecords(dbConnection, recs);
					} else if(method == DBStatementMethod.INSERT_IGNORE) {
						((DBStatement<DBRecord>)stmt).insertIgnoreDBRecords(dbConnection, recs);
					} else if(method == DBStatementMethod.REPLACE){
						((DBStatement<DBRecord>)stmt).replaceDBRecords(dbConnection, recs);
					}
				} catch (Exception e) {
					logger.error("ShardedDBStatementManager.insert exception", e);
					DBConnectionManager.getInstance().invalidateConnection(dbConnection);
					dbConnection = null;
					throw e;
				} finally {
					DBConnectionManager.getInstance().returnConnection(dbConnection);
				}
			}
		}		
	}
	
	@SuppressWarnings("unchecked")
	public void updateImpl(String schemaConfigName, String id, DBStatement<? extends DBRecord> stmt, DBRecord rec)
	throws Exception {
		ArrayList<DBInstance> list = getWriteSQLInstances(schemaConfigName, id);
		if(!Util.nullOrEmptyList(list)) {
			for(DBInstance sqlInstance : list) {
				// TODO: Add error handling when write to multiple instances is required and one of them fail
				DBConnection dbConnection = null;
				try {
					dbConnection = DBConnectionManager.getInstance().getConnection(sqlInstance.getConfigName());
					dbConnection.setSchema(sqlInstance.getSchema());
					((DBStatement<DBRecord>)stmt).updateDBRecords(dbConnection, rec);
				} catch (Exception e) {
					logger.error("ShardedDBStatementManager.update exception", e);
					DBConnectionManager.getInstance().invalidateConnection(dbConnection);
					dbConnection = null;
					throw e;
				} finally {
					DBConnectionManager.getInstance().returnConnection(dbConnection);
				}
			}
		}
	}
	
	public void deleteImpl(String schemaConfigName, String id, DBStatement<? extends DBRecord> stmt)
	throws Exception {
		ArrayList<DBInstance> list = getWriteSQLInstances(schemaConfigName, id);
		if(!Util.nullOrEmptyList(list)) {
			for(DBInstance sqlInstance : list) {
				// TODO: Add error handling when write to multiple instances is required and one of them fail
				DBConnection dbConnection = null;
				try {
					dbConnection = DBConnectionManager.getInstance().getConnection(sqlInstance.getConfigName());
					dbConnection.setSchema(sqlInstance.getSchema());
					stmt.deleteDBRecords(dbConnection);
				} catch (Exception e) {
					logger.error("ShardedDBStatementManager.delete exception", e);
					DBConnectionManager.getInstance().invalidateConnection(dbConnection);
					dbConnection = null;
					throw e;
				} finally {
					DBConnectionManager.getInstance().returnConnection(dbConnection);
				}
			}
		}
	}
	
	public void executeImpl(String schemaConfigName, String id, ArrayList<DBStatementTask> taskList)
	throws Exception {
		ArrayList<DBInstance> list = getWriteSQLInstances(schemaConfigName, id);
		if(!Util.nullOrEmptyList(list)) {
			for(DBInstance sqlInstance : list) {
				// TODO: Add error handling when write to multiple instances is required and one of them fail
				DBConnection dbConnection = null;
				try {
					dbConnection = DBConnectionManager.getInstance().getConnection(sqlInstance.getConfigName());
					dbConnection.setSchema(sqlInstance.getSchema());
					dbConnection.startTransaction();
					for(DBStatementTask task : taskList) {
						task.executeImpl(dbConnection);
					}
					dbConnection.commit();
				} catch (Exception e) {
					logger.error("DBStatementManager.execute exception", e);
					if(dbConnection != null) {
						dbConnection.rollback();
						DBConnectionManager.getInstance().invalidateConnection(dbConnection);
						dbConnection = null;
					}
					throw e;
				} finally {
					DBConnectionManager.getInstance().returnConnection(dbConnection);
				}
			}
		}
	}
		
}
