package com.perpetumobile.bit.config;

import java.io.InputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.Properties;

import com.perpetumobile.bit.util.Util;

/**
 *
 * @author  Zoran Dukic
 */
public final class ConfigProperties {
	
	final static public String CONFIG_DELIMITER_DEFAULT = ";";
	final static public String CONFIG_KEY_INCLUDE = "$include";
	
	private Properties properties = null;
	
	/**
	 * Create ConfigProperties and load properties from the given file.
	 */
	public ConfigProperties(String fileName) {
		reloadProperties(fileName);
	}
	
	
	/**
	 * Reload properties from the given file.
	 */
	public void reloadProperties(String fileName) {
		Properties tmpProperties = new Properties();
		loadProperties(tmpProperties, fileName);
		
		// is this thread safe?
		properties = tmpProperties;
	}
	
	public void putAll(ConfigProperties src) {
		properties.putAll(src.properties);
	}
	
	private void loadProperties(Properties result, String fileList) {
		loadProperties(result, fileList, null);
	}
	
	private void loadProperties(Properties result, String fileList, ArrayList<String> includeArrayList) {
		if (includeArrayList == null) {
			includeArrayList = new ArrayList<String>();
		}
		
		StringTokenizer includeStringTokenizer = new StringTokenizer(fileList, CONFIG_DELIMITER_DEFAULT);
		
		while (includeStringTokenizer.hasMoreTokens()) {
			
			String configFileName = includeStringTokenizer.nextToken();
			if (!includeArrayList.contains(configFileName)) {
				includeArrayList.add(configFileName);
				
				try {
					InputStream in = ClassLoader.getSystemResourceAsStream(configFileName);
					if (in != null) {
						result.load(in);
						in.close();
					} else {
						System.out.println("Config file '" + configFileName	+ "' not found.");
					}
				} catch (IOException e) {
					System.out.println("Config.loadProperties exception for '" + configFileName + "'");
				}
				
				String include = result.getProperty(CONFIG_KEY_INCLUDE);
				if (include != null && !include.equals("")) {
					result.setProperty(CONFIG_KEY_INCLUDE, "");
					loadProperties(result, include, includeArrayList);
				}
			}
		}
	}
	
	/**
	 * Returns a property value for a given key.
	 */
	private String getPropertyImpl(String key) {
		return properties.getProperty(key);
	}
	
	/**
	 * Returns a property value for a given key. 
	 * Returns the defaultValue if the key is not specified.
	 */
	public String getProperty(String key, String defaultValue) {
		String result = getPropertyImpl(key);
		if (result == null) {
			result = defaultValue;
		}
		return result;
	}
	
	/**
	 * @see getProperty(String key, String defaultValue)
	 */
	public int getIntProperty(String key, int defaultValue) {
		return Util.toInt(getPropertyImpl(key), defaultValue);
	}
	
	/**
	 * @see getProperty(String key, String defaultValue)
	 */
	public long getLongProperty(String key, long defaultValue) {
		return Util.toLong(getPropertyImpl(key), defaultValue);
	}
	
	/**
	 * @see getProperty(String key, String defaultValue)
	 */
	public float getFloatProperty(String key, float defaultValue) {
		return Util.toFloat(getPropertyImpl(key), defaultValue);
	}
	
	/**
	 * @see getProperty(String key, String defaultValue)
	 */
	public double getDoubleProperty(String key, double defaultValue) {
		return Util.toDouble(getPropertyImpl(key), defaultValue);
	}
	
	/**
	 * @see getProperty(String key, String defaultValue)
	 */
	public boolean getBooleanProperty(String key, boolean defaultValue) {
		return Util.toBoolean(getPropertyImpl(key), defaultValue);
	}
	
	/**
	 * Returns a property value for a given classKey.key. 
	 */
	private String getClassPropertyImpl(String classKey, String key) {
		StringBuffer buf = new StringBuffer(classKey);
		buf.append('.');
		buf.append(key);
		return getPropertyImpl(buf.toString());
	}
	
	/**
	 * Returns a property value for a given classKey.key. 
	 * Returns a property value for a given key if classKey.key is not specified. 
	 * Returns the defaultValue otherwise.
	 */
	public String getClassProperty(String classKey, String key,	String defaultValue) {
		String strResult = getClassPropertyImpl(classKey, key);
		if (strResult == null) {
			strResult = getProperty(key, defaultValue);
		}
		return strResult;
	}
	
	/**
	 * @see getClassProperty(String classKey, String key, String defaultValue)
	 */
	public int getIntClassProperty(String classKey, String key, int defaultValue) {
		String strResult = getClassPropertyImpl(classKey, key);
		int result = defaultValue;
		if (strResult != null) {
			result = Util.toInt(strResult, defaultValue);
		} else {
			result = getIntProperty(key, defaultValue);
		}
		return result;
	}
	
	/**
	 * @see getClassProperty(String classKey, String key, String defaultValue)
	 */
	public long getLongClassProperty(String classKey, String key, long defaultValue) {
		String strResult = getClassPropertyImpl(classKey, key);
		long result = defaultValue;
		if (strResult != null) {
			result = Util.toLong(strResult, defaultValue);
		} else {
			result = getLongProperty(key, defaultValue);
		}
		return result;
	}
	
	/**
	 * @see getClassProperty(String classKey, String key, String defaultValue)
	 */
	public float getFloatClassProperty(String classKey, String key,	float defaultValue) {
		String strResult = getClassPropertyImpl(classKey, key);
		float result = defaultValue;
		if (strResult != null) {
			result = Util.toFloat(strResult, defaultValue);
		} else {
			result = getFloatProperty(key, defaultValue);
		}
		return result;
	}
	
	/**
	 * @see getClassProperty(String classKey, String key, String defaultValue)
	 */
	public double getDoubleClassProperty(String classKey, String key, double defaultValue) {
		String strResult = getClassPropertyImpl(classKey, key);
		double result = defaultValue;
		if (strResult != null) {
			result = Util.toDouble(strResult, defaultValue);
		} else {
			result = getDoubleProperty(key, defaultValue);
		}
		return result;
	}
	
	/**
	 * @see getClassProperty(String classKey, String key, String defaultValue)
	 */
	public boolean getBooleanClassProperty(String classKey, String key, boolean defaultValue) {
		String strResult = getClassPropertyImpl(classKey, key);
		boolean result = defaultValue;
		if (strResult != null) {
			result = Util.toBoolean(strResult, defaultValue);
		} else {
			result = getBooleanProperty(key, defaultValue);
		}
		return result;
	}
}