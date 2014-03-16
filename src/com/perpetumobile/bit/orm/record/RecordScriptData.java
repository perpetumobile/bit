package com.perpetumobile.bit.orm.record;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import com.perpetumobile.bit.orm.record.exception.RecordScriptDataException;
import com.perpetumobile.bit.util.Util;


/**
 * @author Zoran Dukic
 *
 */
public class RecordScriptData {
	
	public static final String NEW_LINE = "XXNEW_LINEXX";
	
	private HashMap<String, String> data = null;
	
	public RecordScriptData(String[] columnNames, String[] values) throws RecordScriptDataException {
		if(columnNames != null && values != null && columnNames.length >= values.length) {
			data = new HashMap<String, String>();
			for(int i = 0; i<columnNames.length; i++) {
				if(values.length > i && !values[i].equalsIgnoreCase("null")) {
					// remove " from start end end
					String val = values[i];
					if(val.startsWith("\"")) {
						val = val.substring(1);
					}
					if(val.endsWith("\"")) {
						val = val.substring(0, val.length()-1);
					}
					val = Util.replaceAll(val, NEW_LINE, "\n");
					data.put(columnNames[i].toLowerCase(), val);
				}
			}
		} else {
			throw new RecordScriptDataException();
		}
	}
	
	public String get(String columnName) {
		if(columnName != null) {
			return data.get(columnName.toLowerCase());
		}
		return null;
	}
	
	static public ArrayList<RecordScriptData> read(String directoryName, String fileName)
	throws IOException, RecordScriptDataException {
		ArrayList<RecordScriptData> result = new ArrayList<RecordScriptData>();
		BufferedReader in = new BufferedReader(new FileReader(new File(directoryName, fileName)));
		String[] columnNames = null;
		String line = null;
		while((line = in.readLine()) != null) {
			line = line.trim();
			if(!Util.nullOrEmptyString(line)) {
				if(columnNames == null) {
					columnNames = line.split("\t");
				} else {
					result.add(new RecordScriptData(columnNames, line.split("\t")));
				}
			}
		}
		in.close();
		return result;
	}	
}
