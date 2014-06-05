package com.perpetumobile.bit.orm.record;



import java.util.ArrayList;
import java.util.HashMap;

import org.apache.velocity.VelocityContext;

import com.perpetumobile.bit.config.Config;
import com.perpetumobile.bit.orm.record.field.Field;
import com.perpetumobile.bit.orm.record.field.FieldConfig;
import com.perpetumobile.bit.util.Util;

/**
 * 
 * @author Zoran Dukic
 */
abstract public class RecordConfig {
	
	public static final String CONFIG_NAME_DELIMITER_CONFIG_KEY = "Record.ConfigNameDelimiter";
	public static final String CONFIG_NAME_DELIMITER_DEFAULT = "|";
	
	public static final String CLASS_CONFIG_KEY = ".Record.Class";
	
	public static final String CONNECTION_CONFIG_KEY_CONFIG_KEY = ".Record.Connection.Config.Key";
	
	public static final String KEY_FIELD_CONFIG_KEY = ".Record.KeyField";
	public static final String LABEL_FIELD_CONFIG_KEY = ".Record.LabelField";

	public static final String FIELDS_CONFIG_KEY_CONFIG_KEY = ".Record.Fields.Config.Key";
	public static final String FIELDS_CONFIG_KEY = ".Record.Fields";
	public static final String FIELDS_VELOCITY_CONTEXT_KEY = "_Record_Fields";
	public static final String FIELD_NUM_CONFIG_KEY = ".Record.Field.Num";
	
	public static final String RELATIONSHIPS_CONFIG_KEY = ".Record.Relationships";
	public static final String RELATIONSHIPS_VELOCITY_CONTEXT_KEY = "_Record_Relationships";
	
	protected Class<? extends Record> recordClass = null;
	
	protected String configName = null;
	protected String configNameDelimiter = null;
	protected String connectionConfigName = null;
	
	protected String keyFieldName = null;
	protected String labelFieldName = null;
	
	protected ArrayList<FieldConfig> fields = null;
	protected ArrayList<RelationshipConfig> relationships = null;
	protected HashMap<String, RelationshipConfig> relationshipMap = null;
	
	public RecordConfig() {
	}
	
	@SuppressWarnings("unchecked")
	protected void init(String configName, Class<? extends Record> defaultRecordClass, VelocityContext vc)
	throws ClassNotFoundException {
		this.configName = configName;
		// Using a global configNameDelimiter configuration since configName based configuration would add lots of confusion
		configNameDelimiter = Config.getInstance().getProperty(CONFIG_NAME_DELIMITER_CONFIG_KEY, CONFIG_NAME_DELIMITER_DEFAULT);
		connectionConfigName = Config.getInstance().getProperty(configName+CONNECTION_CONFIG_KEY_CONFIG_KEY, null);
		
		String className = Config.getInstance().getProperty(configName+CLASS_CONFIG_KEY, null);
		if(!Util.nullOrEmptyString(className)) {
			recordClass = (Class<? extends Record>)Class.forName(className);
		}
		if(recordClass == null) {
			recordClass = defaultRecordClass;
		}
		
		keyFieldName = Config.getInstance().getProperty(configName+KEY_FIELD_CONFIG_KEY, null);
		labelFieldName = Config.getInstance().getProperty(configName+LABEL_FIELD_CONFIG_KEY, null);
		
		fields = new ArrayList<FieldConfig>();
		
		String str = null;
		if(vc != null) {
			str = (String)vc.get(configName+FIELDS_VELOCITY_CONTEXT_KEY);
		}
		if(Util.nullOrEmptyString(str)) {
			String fieldsConfigKey = Config.getInstance().getProperty(configName+FIELDS_CONFIG_KEY_CONFIG_KEY, configName+FIELDS_CONFIG_KEY);
			str = Config.getInstance().getProperty(fieldsConfigKey, null);
		}
		if(!Util.nullOrEmptyString(str)) {
			String[] fieldArray = str.split(",");
			for(String f : fieldArray) {
				FieldConfig fieldConfig = null;
				String[] configVal = f.trim().split(" ");
				if(configVal.length == 2) {
					fieldConfig = new FieldConfig(configVal[0], configVal[1]);
				}
				if(fieldConfig != null && fieldConfig.isValid()) {
					fields.add(fieldConfig);
				} else {
					break;
				}
			}
		} else {
			// This config style should be deprecated
			int num = Config.getInstance().getIntProperty(configName+FIELD_NUM_CONFIG_KEY, 100);
			for(int i=1; i<=num; i++) {
				FieldConfig fieldConfig = new FieldConfig(i+"."+configName);
				if(fieldConfig.isValid()) {
					fields.add(fieldConfig);
				} else {
					break;
				}
			}
		}
		
		relationships = new ArrayList<RelationshipConfig>();
		relationshipMap = new HashMap<String, RelationshipConfig>();
		
		str = null;
		if(vc != null) {
			str = (String)vc.get(configName+RELATIONSHIPS_VELOCITY_CONTEXT_KEY);
		}
		if(Util.nullOrEmptyString(str)) {
			str = Config.getInstance().getProperty(configName+RELATIONSHIPS_CONFIG_KEY, null);
		}
		if(!Util.nullOrEmptyString(str)) {
			String[] relationshipArray = str.split(",");
			for(String r : relationshipArray) {
				RelationshipConfig relationshipConfig = null;
				String[] configVal = r.trim().split(" ");
				if(configVal.length == 2) {
					relationshipConfig = new RelationshipConfig(getRecordConfig(configVal[0], vc), configVal[1], null);
				} else if(configVal.length == 3) {
					relationshipConfig = new RelationshipConfig(getRecordConfig(configVal[0], vc), configVal[1], configVal[2]);
				}
				if(relationshipConfig != null && relationshipConfig.isValid()) {
					relationships.add(relationshipConfig);
					relationshipMap.put(relationshipConfig.getConfigName(), relationshipConfig);
				} else {
					break;
				}
			}
		}
	}
	
	abstract protected RecordConfig getRecordConfig(String configName, VelocityContext vc);
	
	public Record createRecord() throws Exception {
		Record result = recordClass.newInstance();
		result.init(this);
		return result;
	}
	
	public String getConfigName() {
		return configName;
	}
	
	public String getConfigNameDelimiter() {
		return configNameDelimiter;
	}
	
	public String getConnectionConfigName() {
		return connectionConfigName;
	}
	
	public boolean isConfigFields() {
		return (fields != null ? fields.size() > 0 : false);
	}
	
	public boolean doThrowFieldNotConfiguredException() {
		return isConfigFields();
	}
	
	public String getKeyFieldName() {
		return keyFieldName;
	}
	
	public String getLabelFieldName() {
		return labelFieldName;
	}
	
	public ArrayList<RelationshipConfig> getRelationships() {
		return relationships;
	}
	
	public RelationshipConfig getRelationshipConfig(String configName) {
	  RelationshipConfig rc =  relationshipMap.get(configName);
	  if(rc == null)
	    rc = new RelationshipConfig(getRecordConfig(configName, null), "list", null);
	  return rc;
	}
	
	public ArrayList<Field> createRecordFields() {
		ArrayList<Field> result =  new ArrayList<Field>();
		for(int i=0; i<fields.size(); i++) {
			Field field = fields.get(i).createField();
			if(field != null) { 
				result.add(field);
			}
		}
		return result;
	}
}
