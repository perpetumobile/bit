package com.perpetumobile.bit.orm.record;


import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool;

import com.perpetumobile.bit.config.Config;
import com.perpetumobile.bit.util.Logger;

import java.util.HashMap;

/**
 * Database pool manager.
 *
 * @author  Zoran Dukic
 */
abstract public class RecordConnectionManager<T extends RecordConnection<?>> {
	static private Logger logger = new Logger(RecordConnectionManager.class);
	
	protected HashMap<String, ObjectPool> connectionPools = new HashMap<String, ObjectPool>();
	protected Object lock = new Object();
	
	//config file keys
	public String DB_ACTIVE_MAX_KEY = "Database.Active.Max";
	public String DB_IDLE_MAX_KEY = "Database.Idle.Max";
	
	//default config values
	public int DB_ACTIVE_MAX_DEFAULT = 10;
	
	public RecordConnectionManager() {
	}
	
	abstract protected RecordConnectionFactory<T> createConnectionFactory(String configName);
	
	/**
	 * Get a connection object from a pool name directly.
	 *
	 * @param poolName pool name
	 * @param configRoot configuration root key
	 *
	 * @return Connection object
	 */
	@SuppressWarnings("unchecked")
	public T getConnection(String configName) 
	throws Exception {
		T connection = null;
		
		ObjectPool pool = getPool(configName);
		if (pool != null) {
			connection = (T)pool.borrowObject();
		}
		return connection;
	}
	
	// need not type checked argument for use in the package
	void returnConnectionImpl(RecordConnection<?> connection) {
		if (connection == null) {
			return;
		}
		
		try {
			ObjectPool pool = getPool(connection.getConfigName());
			if (pool != null) {
				pool.returnObject(connection);
			}
		} catch (Exception e) {
			logger.error("Exception at DBPool.returnDBConnection", e);
		}
	}
	
	/**
	 * Return a connection to pool.
	 *
	 * @param poolName pool name
	 * @param con connection to be returned to pool
	 */
	public void returnConnection(T connection) {
		returnConnectionImpl(connection);
	}
	
	// need not type checked argument for use in the package
	void invalidateConnectionImpl(RecordConnection<?>  connection) {
		if (connection == null) {
			return;
		}
		
		try {
			ObjectPool pool = getPool(connection.getConfigName());
			if (pool != null) {
				pool.invalidateObject(connection);
			}
		} catch (Exception e) {
			logger.error("Exception at DBPool.invalidateDBConnection", e);
		}
	}
	
	/**
	 * Invalidate connection.
	 *
	 * @param poolName pool name
	 * @param con connection to be returned to pool
	 */
	public void invalidateConnection(T connection) {
		invalidateConnectionImpl(connection);
	}
	
	private ObjectPool createPool(String configName) throws Exception {
		GenericObjectPool pool = null;
		
		int maxActive = Config.getInstance().getIntClassProperty(configName, DB_ACTIVE_MAX_KEY, DB_ACTIVE_MAX_DEFAULT);
		int maxIdle = Config.getInstance().getIntClassProperty(configName, DB_IDLE_MAX_KEY, maxActive);
		
		try {
			pool = new GenericObjectPool(createConnectionFactory(configName));
			pool.setMaxActive(maxActive);
			pool.setMaxIdle(maxIdle);
			pool.setTestOnBorrow(true);
		} catch (Exception e) {
			logger.error("Exception at DBPool.creatPool(\"" + configName + "\")", e);
		}
		return pool;
	}
	
	private ObjectPool getPool(String configName) throws Exception {
		ObjectPool pool = null;
		synchronized(lock) {
			pool = connectionPools.get(configName);
			if (pool == null) {
				// could not find one, try to create one
				try {
					pool = createPool(configName);
					connectionPools.put(configName, pool);
				} catch (Exception e) {
					logger.error("Exception at DBPool.getPool(\"" + configName + "\")", e);
				}
			}
		}
		return pool;
	}
}
