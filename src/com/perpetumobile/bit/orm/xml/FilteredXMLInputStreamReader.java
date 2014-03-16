package com.perpetumobile.bit.orm.xml;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

/**
 * This InputStreamReader is responsible for ignoring invalid XML characters in an XML document
 * that would have caused a fatal error in the SAX parser and forced parser to terminate.
 * 
 * @author Zoran Dukic
 *
 */
public class FilteredXMLInputStreamReader extends InputStreamReader {

	static byte[] invalidXMLCharsArray = {
	//	0  1  2  3  4  5  6  7  8  9  A  B  C  D  E  F 10 11 12 13 14 15 16 17 18 19 1A 1B 1C 1D 1E 1F	
		1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 
	};

	/**
	 * @param in
	 * @param charsetName
	 * @throws UnsupportedEncodingException
	 */
	public FilteredXMLInputStreamReader(InputStream in, String charsetName)
	throws UnsupportedEncodingException {
		super(in, charsetName);
	}

	/**
	 * @param in
	 * @param cs
	 */
	public FilteredXMLInputStreamReader(InputStream in, Charset cs) {
		super(in, cs);
	}

	/**
	 * @param in
	 * @param dec
	 */
	public FilteredXMLInputStreamReader(InputStream in, CharsetDecoder dec) {
		super(in, dec);
	}

	public int read() throws IOException {
		int result = super.read();
		// return ' ' if invalid char
		if(result < invalidXMLCharsArray.length && invalidXMLCharsArray[result] == 1) {
			result = ' ';
		}
		return result; 
	}
	
	public int read(char[] cbuf, int offset, int length) throws IOException {
		int result = super.read(cbuf, offset, length);
		int numInvalidXmlChars = invalidXMLCharsArray.length;
		// replace invalid chars with ' '
		for(int i=0; i<result; i++) {
			if(cbuf[i+offset] < numInvalidXmlChars && invalidXMLCharsArray[cbuf[i+offset]] == 1) {
				cbuf[i+offset] = ' ';
			}
		}
		return result;
	}
}
