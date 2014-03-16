package com.perpetumobile.bit.orm.xml;

import javax.xml.parsers.SAXParser;

import org.apache.commons.pool.PoolableObjectFactory;

/**
 * @author Zoran Dukic
 *
 */
public class SAXParserFactory implements PoolableObjectFactory {
	
	private static javax.xml.parsers.SAXParserFactory factory = null;
	static {
		 // Make sure we do NOT use the external xerces parser, 
		 // which creates problems when used with JDK1.5
		System.setProperty("javax.xml.parsers.SAXParserFactory", 
				"com.sun.org.apache.xerces.internal.jaxp.SAXParserFactoryImpl");
		System.setProperty("javax.xml.parsers.DocumentBuilderFactory", 
				"com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl");
		
		// Obtain a new instance of a SAXParserFactory.
		factory = javax.xml.parsers.SAXParserFactory.newInstance();
		// Specifies that the parser produced by this code will provide
		// support for XML namespaces.
		factory.setNamespaceAware(true);
	}
	
	public SAXParserFactory() {
	}	
	
	public void activateObject(Object obj) throws Exception {
	}
	
	public void destroyObject(Object obj) throws Exception {
	}
	
	public Object makeObject() throws Exception {
		return factory.newSAXParser();
	}
	
	public void passivateObject(Object obj) throws Exception {
		((SAXParser)obj).reset();
	}
	
	public boolean validateObject(Object obj) {
		return true;
	}
}
