package com.perpetumobile.bit.orm.db;

import java.util.ArrayList;
import java.util.HashMap;

import com.perpetumobile.bit.config.Config;

class DBInstance {
	protected String configName = null;
	protected String schema = null;
	protected boolean isActive = true;
	
	public DBInstance(String configName, String schema) {
		this.configName = configName;
		this.schema = schema;
	}

	public String getConfigName() {
		return configName;
	}
	
	public String getSchema() {
		return schema;
	}

	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}
}

/**
 * @author Zoran Dukic
 *
 */
public class ShardedDBInstanceMap {
	static final public String CONFIG_NAME_DELIMITER = "_";
	
	static final public String DATABASE_SHARDS_CONFIG_KEY = "Database.Shards";
	static final public String DATABASE_WRITE_INSTANCES_CONFIG_KEY = "Database.Write.Instances";
	static final public String DATABASE_READ_INSTANCES_CONFIG_KEY = "Database.Read.Instances";
	static final public String DATABASE_SCHEMA_CONFIG_KEY = "Database.Schema";
	
	protected String schemaConfigName = null;
	protected int numShards = 1;
	
	protected HashMap<String, ArrayList<DBInstance>> readInstanceMap =  new HashMap<String, ArrayList<DBInstance>>();
	protected HashMap<String, ArrayList<DBInstance>> writeInstanceMap =  new HashMap<String, ArrayList<DBInstance>>();
	
	public ShardedDBInstanceMap(String schemaConfigName) {
		this.schemaConfigName = schemaConfigName;
		numShards = Config.getInstance().getIntClassProperty(schemaConfigName, DATABASE_SHARDS_CONFIG_KEY, 1);
		readInstanceMap = configure(false);
		writeInstanceMap = configure(true);
	}
	
	protected HashMap<String, ArrayList<DBInstance>> configure(boolean isWrite) {
		HashMap<String, ArrayList<DBInstance>> result = new HashMap<String, ArrayList<DBInstance>>();
		for(int i=0; i<numShards; i++) {
			String key = getShardConfigName(i);
			String schema = Config.getInstance().getClassProperty(key, DATABASE_SCHEMA_CONFIG_KEY, "");
			String instancesStr = "";
			if(isWrite) {
				instancesStr = Config.getInstance().getClassProperty(key, DATABASE_WRITE_INSTANCES_CONFIG_KEY, "");
			} else {
				instancesStr = Config.getInstance().getClassProperty(key, DATABASE_READ_INSTANCES_CONFIG_KEY, "");
			}
			String[] instances = instancesStr.split(",");
			ArrayList<DBInstance> list = new ArrayList<DBInstance>();
			for(String s : instances) {
				list.add(new DBInstance(s.trim(), schema));
			}
			result.put(key, list);
		}
		return result;
	}
	
	protected String getShardConfigName(int shard) {
		StringBuilder key = new StringBuilder(schemaConfigName);
		key.append(CONFIG_NAME_DELIMITER);
		key.append(shard);
		return key.toString();
	}
	
	public int getShard(String id) {
		return Math.abs(id.hashCode()) % numShards;
	}
	
	public ArrayList<DBInstance> getReadSQLInstances(String id) {
		return readInstanceMap.get(getShardConfigName(getShard(id)));
	}
	
	public ArrayList<DBInstance> getWriteSQLInstances(String id) {
		return writeInstanceMap.get(getShardConfigName(getShard(id)));
	}

	public String getSchemaConfigName() {
		return schemaConfigName;
	}

	public int getNumShards() {
		return numShards;
	}
}

