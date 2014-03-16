package com.perpetumobile.bit.orm.db;



import org.apache.velocity.VelocityContext;

import com.perpetumobile.bit.config.Config;
import com.perpetumobile.bit.orm.record.RecordConfig;
import com.perpetumobile.bit.util.Util;

/**
 * 
 * @author Zoran Dukic
 */
public class DBRecordConfig extends RecordConfig {
	
	public static final String TABLE_NAME_CONFIG_KEY = ".DBRecord.Table.Name";
	public static final String SELECT_CLAUSE_CONFIG_KEY = ".DBRecord.SelectClause";
	public static final String FROM_CLAUSE_CONFIG_KEY = ".DBRecord.FromClause";
	public static final String WHERE_CLAUSE_CONFIG_KEY = ".DBRecord.WhereClause";
	public static final String ORDER_BY_CLAUSE_CONFIG_KEY = ".DBRecord.OrderByClause";
	
	protected String tableName = null;
	protected String selectClause = null;
	protected String fromClause = null;
	protected String whereClause = null;
	protected String orderByClause = null;
	
	public DBRecordConfig(String configName)
	throws ClassNotFoundException {
		init(configName, null);
	}
	
	public DBRecordConfig(String configName, VelocityContext vc)
	throws ClassNotFoundException {
		init(configName, vc);
	}
	
	protected void init(String configName, VelocityContext vc)
	throws ClassNotFoundException {
		super.init(configName, DBRecord.class, vc);
		
		tableName = Config.getInstance().getProperty(configName+TABLE_NAME_CONFIG_KEY, "");
		selectClause = Config.getInstance().getProperty(configName+SELECT_CLAUSE_CONFIG_KEY, "");
		fromClause = Config.getInstance().getProperty(configName+FROM_CLAUSE_CONFIG_KEY, "");
		whereClause = Config.getInstance().getProperty(configName+WHERE_CLAUSE_CONFIG_KEY, "");
		orderByClause = Config.getInstance().getProperty(configName+ORDER_BY_CLAUSE_CONFIG_KEY, "");
	}
	
	protected RecordConfig getRecordConfig(String configName, VelocityContext vc) {
		return DBRecordConfigFactory.getInstance().getRecordConfig(configName, vc);
	}
	
	public String getTableName() {
		return tableName;
	}
	
	public String getFromClause() {
		return getFromClause(null);
	}
	
	public String getFromClause(String tableNameOverride) {
		if(!Util.nullOrEmptyString(fromClause)) {
			return fromClause;
		}
		if(!Util.nullOrEmptyString(tableNameOverride)) {
			return tableNameOverride;
		}
		return tableName;
	}
	
	public String getWhereClause() {
		return whereClause;
	}
	
	public String getOrderByClause() {
		return orderByClause;
	}
		
	public String getSelectList() {
		return getSelectList(null);
	}
	
	public String getSelectList(String tableNameOverride) {
		if(!Util.nullOrEmptyString(selectClause)) {
			return selectClause;
		}
		if(Util.nullOrEmptyString(tableNameOverride)) {
			tableNameOverride = tableName;
		}
		
		StringBuffer result = new StringBuffer();
		
		boolean first = true;
		for(int i=0; i<fields.size(); i++) {
			if(!first) {
				result.append(", ");
			}
			String fieldName = fields.get(i).getFieldName();
			if(fieldName.indexOf(".") == -1) {
				result.append(tableNameOverride);
				result.append(".");
			}
			result.append(fieldName);
			first = false;
		}
		
		return result.toString();
	}
}
