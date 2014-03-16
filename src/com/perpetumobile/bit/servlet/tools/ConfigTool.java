package com.perpetumobile.bit.servlet.tools;

import java.util.ArrayList;

import com.perpetumobile.bit.config.Config;
import com.perpetumobile.bit.config.ConfigFormOption;


/**
 *
 * @author  Zoran Dukic
 */
public class ConfigTool extends BaseTool {
	
	/** Creates a new instance of ConfigTool */
	public ConfigTool() {
	}
	
	/**
	 * Returns a property value for a given key.
	 */
	public String getProperty(String key) {
		return Config.getInstance().getProperty(key, "");
	}
	
	
	/**
	 * Returns a property value for a given key. Returns the defaultValue if the 
	 * key is not specified.
	 */
	public String getProperty(String key, String defaultValue) {
		return Config.getInstance().getProperty(key, defaultValue);
	}
	
	/**
	 * @see getProperty(String key, String defaultValue)
	 */
	public int getIntProperty(String key, int defaultValue) {
		return Config.getInstance().getIntProperty(key, defaultValue);
	}
	
	/**
	 * @see getProperty(String key, String defaultValue)
	 */
	public long getLongProperty(String key, long defaultValue) {
		return Config.getInstance().getLongProperty(key, defaultValue);
	}
	
	/**
	 * @see getProperty(String key, String defaultValue)
	 */
	public float getFloatProperty(String key, float defaultValue) {
		return Config.getInstance().getFloatProperty(key, defaultValue);
	}
	
	/**
	 * @see getProperty(String key, String defaultValue)
	 */
	public double getDoubleProperty(String key, double defaultValue) {
		return Config.getInstance().getDoubleProperty(key, defaultValue);
	}
	
	/**
	 * @see getProperty(String key, String defaultValue)
	 */
	public boolean getBooleanProperty(String key, boolean defaultValue) {
		return Config.getInstance().getBooleanProperty(key, defaultValue);
	}

	/**
	 * Returns a property value for a given classKey.key from the local properties.
	 * Returns a property value for a given key from the local properties.
	 * Returns a property value for a given classKey.key from the global properties.
	 * Returns a property value for a given key from the global properties.
	 * Returns defaultValue otherwise.
	 */
	public String getClassProperty(String classKey, String key, String defaultValue) {
		return Config.getInstance().getClassProperty(classKey, key, defaultValue);
	}
	
	/**
	 * @see getClassProperty(String classKey, String key, String defaultValue)
	 */
	public int getIntClassProperty(String classKey, String key, int defaultValue) {
		return Config.getInstance().getIntClassProperty(classKey, key, defaultValue);
	}
	
	/**
	 * @see getClassProperty(String classKey, String key, String defaultValue)
	 */
	public long getLongClassProperty(String classKey, String key, long defaultValue) {
		return Config.getInstance().getLongClassProperty(classKey, key, defaultValue);
	}
	
	/**
	 * @see getClassProperty(String classKey, String key, String defaultValue)
	 */
	public float getFloatClassProperty(String classKey, String key, float defaultValue) {
		return Config.getInstance().getFloatClassProperty(classKey, key, defaultValue);
	}
	
	/**
	 * @see getClassProperty(String classKey, String key, String defaultValue)
	 */
	public double getDoubleClassProperty(String classKey, String key, double defaultValue) {
		return Config.getInstance().getDoubleClassProperty(classKey, key, defaultValue);
	}
	
	/**
	 * @see getClassProperty(String classKey, String key, String defaultValue)
	 */
	public boolean getBooleanClassProperty(String classKey, String key, boolean defaultValue) {
		return Config.getInstance().getBooleanClassProperty(classKey, key, defaultValue);
	}

	/**
	 * Returns a list of ConfigFormOption items.
	 */
	public ArrayList<ConfigFormOption> getOptionArray(String configName) {
		return Config.getInstance().getOptionArray(configName);
	}
}
