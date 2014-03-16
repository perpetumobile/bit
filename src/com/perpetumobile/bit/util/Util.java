package com.perpetumobile.bit.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.nio.ByteBuffer;

import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.cassandra.utils.UUIDGen;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.json.JSONException;
import org.json.JSONObject;
import org.safehaus.uuid.UUIDGenerator;

/**
 * Misc utility methods.
 */
final public class Util {
	static private Logger logger = new Logger(Util.class);
	
	static final public long MILLIS_DAY = 24 * 3600 * 1000L;
	
	static private Random randomGenerator = new Random();
	
	static public int random(int n) {
		return randomGenerator.nextInt(n);
	}
	
	static public StringBuffer replaceAll(StringBuffer src, String pattern, String replacement) {
		StringBuffer buf = new StringBuffer();
		int startIndex = 0; 
		int endIndex = src.indexOf(pattern);
		while (endIndex != -1) {
			buf.append(src.substring(startIndex, endIndex));
			buf.append(replacement);
			startIndex = endIndex+pattern.length();
			endIndex = src.indexOf(pattern, startIndex);
		}
		buf.append(src.substring(startIndex));
		return buf;
	}
	
	static public String replaceAll(String src, String pattern, String replacement) {
		StringBuffer buf = new StringBuffer();
		int startIndex = 0; 
		int endIndex = src.indexOf(pattern);
		while (endIndex != -1) {
			buf.append(src.substring(startIndex, endIndex));
			buf.append(replacement);
			startIndex = endIndex+pattern.length();
			endIndex = src.indexOf(pattern, startIndex);
		}
		buf.append(src.substring(startIndex));
		return buf.toString();
	}
	
	static public int count(String src, String pattern) {
		int count = 0;
		int startIndex = 0; 
		int endIndex = src.indexOf(pattern);
		while (endIndex != -1) {
			count++;
			startIndex = endIndex+pattern.length();
			endIndex = src.indexOf(pattern, startIndex);
		}
		
		return count;
	}
	
	/**
	 * convert the string to an integer, and return the default value if
	 * the string is null or does not contain a valid int value
	 *
	 * @param value string value
	 * @param defaultValue default value
	 *
	 * @return int
	 */
	static public int toInt(String value, int defaultValue) {
		if (value != null) {
			try {
				return Integer.parseInt(value);
			} catch (NumberFormatException n) {
			}
		}
		return defaultValue;
	}
	
	/**
	 * convert the string to an integer, and return 0 if
	 * the string is null or does not contain a valid int value
	 *
	 * @param value string value
	 *
	 * @return int
	 */
	static public int toInt(String value) {
		if (value != null) {
			try {
				return Integer.parseInt(value);
			} catch (NumberFormatException n) {
			}
		}
		return 0;
	}
	
	/**
	 * convert the string to an long, and return the default value if
	 * the string is null or does not contain a valid int value
	 *
	 * @param value string value
	 * @param defaultValue default value
	 *
	 * @return long
	 */
	static public long toLong(String value, long defaultValue) {
		if (value != null) {
			try {
				return Long.parseLong(value);
			} catch (NumberFormatException n) {
			}
		}
		return defaultValue;
	}
	
	/**
	 * convert the string to an long, and return 0 if
	 * the string is null or does not contain a valid int value
	 *
	 * @param value string value
	 *
	 * @return long
	 */
	static public long toLong(String value) {
		if (value != null) {
			try {
				return Long.parseLong(value);
			} catch (NumberFormatException n) {
			}
		}
		return 0;
	}
	
	/**
	 * convert the string to an short, and return the default value if
	 * the string is null or does not contain a valid int value
	 *
	 * @param value string value
	 * @param defaultValue default value
	 *
	 * @return short
	 */
	static public short toShort(String value, short defaultValue) {
		if (value != null) {
			try {
				return Short.parseShort(value);
			} catch (NumberFormatException n) {
			}
		}
		return defaultValue;
	}
	
	/**
	 * convert the string to an short, and return 0 if
	 * the string is null or does not contain a valid int value
	 *
	 * @param value string value
	 *
	 * @return short
	 */
	static public short toShort(String value) {
		if (value != null) {
			try {
				return Short.parseShort(value);
			} catch (NumberFormatException n) {
			}
		}
		return 0;
	}
	
	/**
	 * convert the string to an float, and return the default value if
	 * the string is null or does not contain a valid int value
	 *
	 * @param value string value
	 * @param defaultValue default value
	 *
	 * @return float
	 */
	static public float toFloat(String value, float defaultValue) {
		if (value != null) {
			try {
				return Float.parseFloat(value);
			} catch (NumberFormatException n) {
			}
		}
		return defaultValue;
	}
	
	/**
	 * convert the string to an float, and return 0 if
	 * the string is null or does not contain a valid int value
	 *
	 * @param value string value
	 *
	 * @return float
	 */
	static public float toFloat(String value) {
		if (value != null) {
			try {
				return Float.parseFloat(value);
			} catch (NumberFormatException n) {
			}
		}
		return 0;
	}
	
	/**
	 * convert the string to an double, and return the default value if
	 * the string is null or does not contain a valid int value
	 *
	 * @param value string value
	 * @param defaultValue default value
	 *
	 * @return double
	 */
	static public double toDouble(String value, double defaultValue) {
		if (value != null) {
			try {
				return Double.parseDouble(value);
			} catch (NumberFormatException n) {
			}
		}
		return defaultValue;
	}
	
	/**
	 * convert the string to an double, and return 0 if
	 * the string is null or does not contain a valid int value
	 *
	 * @param value string value
	 *
	 * @return double
	 */
	static public double toDouble(String value) {
		if (value != null) {
			try {
				return Double.parseDouble(value);
			} catch (NumberFormatException n) {
			}
		}
		return 0;
	}
	
	/**
	 * convert the string to a boolean, returning the default value if null
	 *
	 * @param value string value
	 * @param defaultValue default value
	 *
	 * @return boolean
	 */
	static public boolean toBoolean(String value, boolean defaultValue) {
		if (value == null) {
			return defaultValue;
		}
		return Boolean.valueOf(value).booleanValue();
	}
	
	/**
	 * convert the string to a boolean
	 *
	 * @param value string value
	 *
	 * @return boolean
	 */
	static public boolean toBoolean(String value) {
		return Boolean.valueOf(value).booleanValue();
	}
	
	/**
	 * convert the string to a Timestam
	 *
	 * @param value string value
	 * @param value boolean isUnix
	 *
	 * @return Timestamp
	 */
	static public Timestamp toTimestamp(String value, boolean isUnix) {
		Timestamp result = null;
		try {
			long millis = Util.toLong(value, -1);
			if(isUnix) {
				millis *= 1000;
			}
			if(millis >= 0) {
				result = new Timestamp(millis);
			} else {
				result = Timestamp.valueOf(value);
			}
		} catch (IllegalArgumentException e) {
		}
		return result;
	}
	
	/**
	 * Convert a ByteBuffer to a byte[]
	 * ByteBuffer is reset to its original state.
	 */
	static public byte[] toBytes(ByteBuffer value) {
		byte[] result = null;
		if(value != null && value.remaining() > 0) {
			result = new byte[value.remaining()];
			value.mark();
			value.get(result);
			value.reset();
		}
		return result;
	}
	
	/**
	 * convert a ByteBuffer to a String
	 * ByteBuffer is reset to its original state.
	 */
	static public String toString(ByteBuffer value, String charsetName) {
		String result = null;
		try {
			result = new String(toBytes(value), charsetName);
		} catch (Exception e) {
		}
		return result;
	}
	
	static public String toString(int value, int radix, int minChars) {
		String result = Integer.toString(value, radix);
		int leadZeroes = minChars - result.length();
		if(leadZeroes > 0) {
			StringBuffer buf = new StringBuffer();
			for(int i=0; i<leadZeroes; i++) {
				buf.append("0");
			}
			buf.append(result);
			result = buf.toString();
		}
		return result;
	}
	
	/**
	 * convert a String to a ByteBuffer
	 */
	static public ByteBuffer toByteBuffer(String value, String charsetName) {
		ByteBuffer result = null;
		try {
			result = ByteBuffer.wrap(value.getBytes(charsetName));
		} catch (Exception e) {
		}
		return result;
	}
	
	/**
	 * convert an int to a ByteBuffer
	 */
	static public ByteBuffer toByteBuffer(int value, boolean variableLen) {
		ByteBuffer result = ByteBuffer.allocate(4);
		result.putInt(value);
		result.flip();
		if(variableLen) {
			int position = 0;
			int b = 0;
			for(int i=0; i<4; i++) {
				b = result.get();
				if(value > 0) {
					if(b > 0) {
						position = i;
						break;
					} else if(b < 0){
						position = i-1;
						break;
					}
				} else if(value == -1) {
					position = 3;
				} else {
					if(b < -1) {
						position = i;
						break;
					} else if(b > -1) {
						position = i-1;
						break;
					}
				}
			}
			result.rewind();
			result.position(position);
			// System.out.println("position:" + result.position() + " limit: " + result.limit() + " remaining: " + result.remaining());
		}
		return result;
	}
	
	/**
	 * convert a long to a ByteBuffer
	 */
	static public ByteBuffer toByteBuffer(long value, boolean variableLen) {
		ByteBuffer result = ByteBuffer.allocate(8);
		result.putLong(value);
		result.flip();
		if(variableLen) {
			int position = 0;
			int b = 0;
			for(int i=0; i<8; i++) {
				b = result.get();
				if(value > 0) {
					if(b > 0) {
						position = i;
						break;
					} else if(b < 0){
						position = i-1;
						break;
					}
				} else if(value == -1) {
					position = 7;
				} else {
					if(b < -1) {
						position = i;
						break;
					} else if(b > -1) {
						position = i-1;
						break;
					}
				}
			}
			result.rewind();
			result.position(position);
			// System.out.println("position:" + result.position() + " limit: " + result.limit() + " remaining: " + result.remaining());
		}
		return result;
	}
	
	/**
	 * convert a ByteBuffer to an Object
	 * ByteBuffer is reset to its original state.
	 */
	static public Object toObject(ByteBuffer value) {
		Object result = null;
		try {
			ByteArrayInputStream bis = new ByteArrayInputStream(toBytes(value));
			ObjectInputStream is = new ObjectInputStream(bis);
			result = is.readObject();
		} catch (Exception e) {
		}
		return result;
	}
	
	/**
	 * convert an Object to a ByteBuffer
	 */
	static public ByteBuffer toByteBuffer(Object value) {
		ByteBuffer result = null;
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream os = new ObjectOutputStream(bos);
			os.writeObject(value);
			result = ByteBuffer.wrap(bos.toByteArray());
		} catch (Exception e) {
		}
		return result;
	}
	
	/**
	 * get version 1 UUID 
	 */
	static public ByteBuffer getTimeUUID() {
		return ByteBuffer.wrap(UUIDGenerator.getInstance().generateTimeBasedUUID().asByteArray());
	}
	
	/**
	 * get version 4 UUID 
	 */
	static public ByteBuffer getLexicalUUID() {
		return ByteBuffer.wrap(UUIDGen.decompose(UUID.randomUUID()));
	}
	
	static public long currentTimeMicros() {
		return System.currentTimeMillis() * 1000;
	}
	
	static public StringBuffer readFile(String fileName) 
	throws IOException {
		StringBuffer result = new StringBuffer();
		BufferedReader in = new BufferedReader(new FileReader(fileName));
		char[] buf = new char[1024];
		while (in.read(buf) != -1) {
			result.append(buf);
		}
		in.close();
		return result;
	}
	
	/**
	 * save to file
	 *
	 * @param buf content to be saved in file
	 * @param fileName file path and name 
	 * @param delete delete file if exists
	 * @param append append bytes at the end else at the begining of file
	 *
	 * @throws IOException
	 */    	
	static public void saveToFile(StringBuffer buf, String fileName, boolean append)
	throws IOException {				
		File file = new File(fileName);
		
		if (!append && file.exists()) {
			file.delete();
		}
		
		OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(file, append), "UTF8");
		
		out.write(buf.toString());
		out.flush();
		out.close(); 
	}

	static public boolean deleteDirectory(File dir) {
		if(dir.exists()) {
			File[] files = dir.listFiles();
			for(File f : files) {
				if(f.isDirectory()) {
					deleteDirectory(f);
				} else {
					f.delete();
				}
			}
		}
		return dir.delete();
	}

	static public void fixEndOfLine(String fileName, boolean isUnix)
	throws IOException {
		StringBuffer buf = readFile(fileName);
		buf = replaceAll(buf, "\r\n", "\n");
		if(!isUnix) {
			buf = replaceAll(buf, "\n", "\r\n");
		}
		saveToFile(buf, fileName, false);
	}
	
	static public String encodeHtml(String str) {
		if (str != null) {
			str = str.replaceAll("&", "&amp;");
			str = str.replaceAll("\"", "&quot;");
			str = str.replaceAll("<", "&lt;");
			str = str.replaceAll(">", "&gt;");
			str = str.replaceAll("'", "&apos;");
			return str;
		}
		return "";
	}
	
	static public String getHttpQueryValue(String key, String query) {
		String result = null;
		int valueStartIndex = key.length() + 1;
		int valueEndIndex = -1;
		
		if(query.startsWith(key)) {
			valueEndIndex = query.indexOf("&");
			if(valueEndIndex != -1) {
				result = query.substring(valueStartIndex, valueEndIndex);
			} else {
				result = query.substring(valueStartIndex);
			}
		} else {
			int startIndex = query.indexOf("&" + key);
			if(startIndex != -1) {
				valueStartIndex += startIndex + 1;
				valueEndIndex = query.indexOf("&", valueStartIndex);
				if(valueEndIndex != -1) {
					result = query.substring(valueStartIndex, valueEndIndex);
				} else {
					result = query.substring(valueStartIndex);
				}
			}	
		}
		
		return result;
	}
	
	static public String getNewLine() {
		return "\n";
	}
	
	@SuppressWarnings("rawtypes")
	static public ArrayList getArrayList() {
		return new ArrayList();
	}
	
	static public Object[] addToArray(Object[] src, Object obj) {
		if(obj == null) {
			return src;
		}
		Object[] result = null;
		if(src != null && src.length > 0) {
			result = (Object[])java.lang.reflect.Array.newInstance(obj.getClass(), src.length+1);
			for(int i=0; i<src.length; i++) {
				result[i] = src[i];
			}
			result[result.length-1] = obj;
		} else {
			result = (Object[])java.lang.reflect.Array.newInstance(obj.getClass(), 1);
			result[0] = obj;
		}
		return result;
	}
	
	static public Object[] addToArray(Object[] src, Object[] objs) {
		if(objs == null || objs.length == 0) {
			return src;
		}

		Object[] result = null;
		if(src != null && src.length > 0) {
			result = (Object[])java.lang.reflect.Array.newInstance(objs.getClass().getComponentType(), src.length+objs.length);
			for(int i=0; i<src.length; i++) {
				result[i] = src[i];
			}
			for(int i=0; i<objs.length; i++) {
				result[i+src.length] = objs[i];
			}
		} else {
			result = objs;
		}
		
		return result;
	}
	
	static public final String ELLIPSES = " ...";
	
	static public String formatLimit(String src, int limit) {
		String result = src;
		limit -= ELLIPSES.length();
		if(src != null && src.length() > limit) {
			int index = src.lastIndexOf(" ", limit);
			if(index != -1) {
				result = src.substring(0, index) + ELLIPSES;
			} else {
				result = src.substring(0, limit) + ELLIPSES;
			}
		}
		return result;
	}
	
	/**
	 * Returns md5 hash as 32 hex-digit (lower case) string.
	 */
	public static String getMD5(String str) {
		String result = null;
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(str.getBytes());
			byte[] hash = md.digest();
			result = toHex(hash);
		} catch (NoSuchAlgorithmException e) {
			logger.error("Util.getMD5 exception", e);
		}
		return result;
	}
	
	/**
	 * Returns Hmac as 32 hex-digit (lower case) string. algorithm
	 */
	public static String getHmac(String algorithm, String key, String msg) {
		String result = null;
		try {
			Mac mac = Mac.getInstance(algorithm);
			mac.init(new SecretKeySpec(key.getBytes(), algorithm));
			mac.update(msg.getBytes());
			byte[] hash = mac.doFinal();
			result = toHex(hash);
		} catch (NoSuchAlgorithmException e) {
			logger.error("Util.getHmac exception", e);
		} catch (InvalidKeyException e) {
			logger.error("Util.getHmac exception", e);
		}
		return result;
	}

	/**
	 * Returns hex-digit (lower case) string.
	 */
	public static String toHex(byte[] bytes) {
		StringBuffer buf = new StringBuffer(bytes.length * 2);
		for (int i=0; i<bytes.length; i++) {
			int value = (int)bytes[i] & 0xff; 
			if (value < 0x10) {
				buf.append("0");
			}
			buf.append(Integer.toHexString(value));
		}
		return buf.toString();
	}
	
	static public boolean nullOrEmptyString(String str) {
		return (str==null || str.equals(""));
	}
	
	static public boolean nullOrEmptyList(List<?> list) {
		return (list==null || list.size() == 0);
	}
	
	static public String trimToLength(String src, int len) {
		if(src.length() > len) {
			return src.substring(0, len);
		}
		return src;
	}
	
	static public ArrayList<String> getDateList(String startdate, String enddate) 
	throws ParseException {
		ArrayList<String> results = new ArrayList<String>();
		
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
		Calendar date = Calendar.getInstance();
		date.setTime(df.parse(startdate));
		Calendar end = Calendar.getInstance();
		end.setTime(df.parse(enddate));
		
		while (end.compareTo(date) >= 0) {
			results.add(df.format(date.getTime()));
	        date.add(Calendar.DAY_OF_MONTH, 1);
		}
		return results;
	}
	
	static public String formatDate(String inputDate, String inFormat, String outFormat) 
	throws ParseException {
		SimpleDateFormat indf = new SimpleDateFormat(inFormat);
		SimpleDateFormat outdf = new SimpleDateFormat(outFormat);
		Date date = indf.parse(inputDate);
		return outdf.format(date);
	}
	
	static public String formatCurrency(float input, boolean isInputInCents) {
		if(isInputInCents) {
			input = Math.round(input) / (float)100;
		}
		return "$" + input;
	}
	
	static public float formatFloat(float input, int numDecimals) {
		float m = (float)Math.pow(10, numDecimals);
		return Math.round(input*m)/m;
	}
	
	static public String formatInteger(int input) {
		NumberFormat formatter = NumberFormat.getInstance(new Locale("en_US"));
		return formatter.format(input);
	}
	
	static public String formatLong(long input) {
		NumberFormat formatter = NumberFormat.getInstance(new Locale("en_US"));
		return formatter.format(input);
	}
	
	static public int round(float input) {
		return Math.round(input);
	}
	
	static public String getParamFromJSON(String src, String paramName) {
		String result = null;
		try {
			JSONObject json = new JSONObject(src);
			result = json.getString(paramName);
		} catch (JSONException e) {
			logger.error("Util.getParamFromJSON exception", e);
		}
		return result;
	}
	
	static public CommandLine processCommandLine(String[] args)
	throws org.apache.commons.cli.ParseException {
		CommandLineParser parser = new PosixParser();
		Options options = new Options();
		options.addOption("nlf", "newlinefix", true, "set the new line for the given filename");
		options.addOption("nlu", "newlineunix", false, "set unix style new line");
		options.addOption("nlw", "newlinewindows", false, "set windows style new line");
		return parser.parse(options, args);	
	}
	
	public static void main(String[] args) {		
		try {
			CommandLine cmd = processCommandLine(args);
			if(cmd.hasOption("nlf")) {
				String fileName = cmd.getOptionValue("nlf");
				if(cmd.hasOption("nlw")) {
					fixEndOfLine(fileName, false);
				} else {
					fixEndOfLine(fileName, true);
				}
				System.exit(0);
			} else {
				System.out.println("Usage: Util -nlf <filename> ");
		        System.exit(1);
			}
		} catch (Exception e){
			logger.error("Util.main exception", e);
		}
	}
	
}
