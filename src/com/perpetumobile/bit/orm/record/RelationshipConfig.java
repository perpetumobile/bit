package com.perpetumobile.bit.orm.record;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;

import com.perpetumobile.bit.util.Util;


/**
 * 
 * @author Zoran Dukic
 */
public class RelationshipConfig {
		
	protected RecordConfig recordConfig = null;
	protected RelationshipType relationshipType = null;
	protected ArrayList<ForeignKeyConfig> foreignKeyConfigs = new ArrayList<ForeignKeyConfig>();
	
	public RelationshipConfig(RecordConfig recordConfig, String type, String foreignKeyConfig) {
		this.recordConfig = recordConfig;
		this.relationshipType = RelationshipType.get(type);
		if(!Util.nullOrEmptyString(foreignKeyConfig)) {
			String[] keys = foreignKeyConfig.split(";");
			for(String k : keys) {
				String[] val = k.split("=>");
				if(val.length == 2) {
					foreignKeyConfigs.add(new ForeignKeyConfig(val[0], val[1]));
				}
			}
		}
	}
	
	public String getConfigName() {
		return recordConfig.getConfigName();
	}
	
	public String getConnectionConfigName() {
		return recordConfig.getConnectionConfigName();
	}
	
	public RecordConfig getRecordConfig() {
		return recordConfig;
	}
	
	public RelationshipType getRelationshipType() {
		return relationshipType;
	}
	
	public ArrayList<ForeignKeyConfig> getForeignKeyConfigs() {
		return foreignKeyConfigs;
	}
	
	public boolean isValid() {
		return (recordConfig != null && relationshipType != null);
	}

	public enum RelationshipType {
		Record("record"),
		List("list"),
		Map("map");
		
		private static final HashMap<String,RelationshipType> map = new HashMap<String,RelationshipType>();
		static {
			for(RelationshipType rt : EnumSet.allOf(RelationshipType.class))
				map.put(rt.getType(), rt);
		}

		private String type;

		private RelationshipType(String type) {
			this.type = type.toLowerCase();
		}

		public String getType(){
			return type; 
		}

		static public RelationshipType get(String type) { 
			return map.get(type.toLowerCase()); 
		}
	}
}
