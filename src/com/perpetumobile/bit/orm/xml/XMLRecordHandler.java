package com.perpetumobile.bit.orm.xml;

import java.io.CharArrayWriter;
import java.io.IOException;

import javax.xml.parsers.SAXParser;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import com.perpetumobile.bit.config.Config;
import com.perpetumobile.bit.util.Util;


public class XMLRecordHandler extends DefaultHandler {
	public static final String ATTR_NAME = "name";
	public static final String ATTR_NAME_ENABLED_KEY = ".ElementName.AttrName.Enabled";
	
	protected CharArrayWriter content = null;
	
	protected String configNamePrefix = null;
	protected String localName = null;
	protected String configName = null;
	protected boolean attrNameEnabled = true;
	
	protected XMLRecordHandler parent = null;
	protected XMLReader xmlReader = null;
   	
	protected XMLRecord xmlRecord = null;
	
	public XMLRecordHandler(String configNamePrefix, String localName, SAXParser parser) throws SAXException {
		content = new CharArrayWriter();
		this.configNamePrefix = configNamePrefix;
		this.localName = localName;
		
		StringBuffer buf = new StringBuffer();
		buf.append(configNamePrefix);
		buf.append(XMLRecordConfig.CONFIG_NAME_DELIMITER);
		buf.append(localName);
		this.configName = buf.toString();	
		
		xmlReader = parser.getXMLReader();
		xmlReader.setErrorHandler(this);
		xmlReader.setEntityResolver(this);
	}
	
	public XMLRecordHandler(String localName, XMLRecordHandler parent) {
		content = new CharArrayWriter();
		this.configNamePrefix = parent.configNamePrefix;
		this.localName = localName;
		
		StringBuffer buf = new StringBuffer();
		buf.append(parent.configName);
		buf.append(XMLRecordConfig.CONFIG_NAME_DELIMITER);
		buf.append(localName);
		configName = buf.toString();
		
		attrNameEnabled = Config.getInstance().getBooleanProperty(configName+ATTR_NAME_ENABLED_KEY, true);
		
		this.parent = parent;
		this.xmlReader = parent.xmlReader;
	}
	
	public boolean isLocalName(String localName) {
		return localName.equalsIgnoreCase(this.localName);
	}
	
	public XMLRecord getXMLRecord() {
		return xmlRecord;
	}
	
	protected void createXMLRecord() {
		if(xmlRecord == null) {
			try {
				XMLRecordConfig xmlRecordConfig = XMLRecordConfigFactory.getInstance().getRecordConfig(configName);
				xmlRecord = (XMLRecord)xmlRecordConfig.createRecord();
			} catch (Exception e) {
				xmlRecord = null;
			}
		}
	}
	
	protected void setupConfigName(String attrNameValue) {
		if(attrNameEnabled && !Util.nullOrEmptyString(attrNameValue)) {
			StringBuffer buf = new StringBuffer(configName);
			buf.append("[");
			buf.append(attrNameValue);
			buf.append("]");
			configName = buf.toString();
		}
	}
	
	public void handle(String namespaceURI, String localName, String qName, Attributes attr)
	throws SAXException {
		xmlReader.setContentHandler(this);
		xmlReader.setErrorHandler(this);
		startElement(namespaceURI, localName, qName, attr);
	}
	
	public void aggregate(XMLRecord rec) {
		if(xmlRecord != null) {
			xmlRecord.aggregate(rec);
		}
	}
	
	public void startElement(String namespaceURI, String localName, String qName, Attributes attr)
	throws SAXException {
		if(isLocalName(localName)) {
			content.reset();
			setupConfigName(attr.getValue(ATTR_NAME));
			createXMLRecord();
			if(xmlRecord != null) {
				xmlRecord.readRecord(attr);
			}
		} else {
			XMLRecordHandler handler = new XMLRecordHandler(localName, this);
			handler.handle(namespaceURI, localName, qName, attr);
		}
	}
	
	public void endElement(String namespaceURI, String localName, String qName)
	throws SAXException {
		if (isLocalName(localName)) {
			if(xmlRecord != null) {
				xmlRecord.setContent(getContent());
			}
			if(parent != null) {
				parent.aggregate(xmlRecord);
				// swap content handler back to parent
				xmlReader.setContentHandler(parent);
				xmlReader.setErrorHandler(parent);
			}
		} 
	}
	
	public String getContent() {
		return content.toString();
	}
	
	public void characters(char[] ch, int start, int length)
	throws SAXException {
		content.write(ch, start, length);
	}
	
	public void error(SAXParseException e) throws SAXException {
		super.error(e);
	}
	
	public void fatalError(SAXParseException e) throws SAXException {
		super.error(e);
	}
	
	public void warning(SAXParseException e) throws SAXException {
		super.error(e);
	}
	
	public InputSource resolveEntity(String publicId, String systemId)
	throws SAXException, IOException {
		return null;
	}
}
