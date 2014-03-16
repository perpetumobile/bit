package com.perpetumobile.bit.http;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;

import org.cyberneko.html.parsers.DOMParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.perpetumobile.bit.config.Config;
import com.perpetumobile.bit.util.Logger;
import com.perpetumobile.bit.util.Util;


/**
 * @author Zoran Dukic
 *
 */
public class HttpDocument {
	static private Logger logger = new Logger(HttpDocument.class);
	
	static final public String FOLLOW_META_REFRESH = "HttpDocument.MetaRefresh.Follow.Enable";
	
	static public HashMap<String, String> filters = new HashMap<String, String>();
	static {
		filters.put("SCRIPT", "1");
		filters.put("STYLE", "1");
		// filters.put("A", "1");
		// filters.put("SELECT", "1");
	}
	
	protected HttpResponseDocument httpResponseDocument = null;
	
	protected boolean followRedirects = true;
	protected boolean followMetaRefresh = true;

	protected boolean fetchFailed = false;
	
	protected Document document = null;
	protected HashMap<String, ArrayList<String>> metaContentMap = null;
	protected String metaRefreshUrl = null;
	
	protected ArrayList<String> title = null;
	protected ArrayList<String> text = null;
	protected ArrayList<HttpImage> images = null;
	protected String base = null;
	
	public HttpDocument(String url) {
		httpResponseDocument = new HttpResponseDocument(url);
		followMetaRefresh = Config.getInstance().getBooleanProperty(FOLLOW_META_REFRESH, followRedirects);
	}
	
	public HttpDocument(String url, boolean followRedirects) {
		httpResponseDocument = new HttpResponseDocument(url);
		this.followRedirects = followRedirects;
		followMetaRefresh = Config.getInstance().getBooleanProperty(FOLLOW_META_REFRESH, followRedirects);
	}
	
	/**
	 * @return the followRedirects
	 */
	public boolean isFollowRedirects() {
		return followRedirects;
	}

	/**
	 * @param followRedirects the followRedirects to set
	 */
	public void setFollowRedirects(boolean followRedirects) {
		this.followRedirects = followRedirects;
	}
	
	public void setHttpResponseDocument(HttpResponseDocument httpResponseDocument) {
		this.httpResponseDocument = httpResponseDocument;
	}
	
	private void updateHttpResponseDocument(HttpResponseDocument httpResponseDocument) {
		String sourceUrl = getSourceUrl();
		this.httpResponseDocument = httpResponseDocument;
		if(!Util.nullOrEmptyString(sourceUrl)) {
			setSourceUrl(sourceUrl);
		}
	}
	
	/**
	 * @return Returns the destinationUrl.
	 */
	public String getDestinationUrl() {
		return httpResponseDocument.getDestinationUrl();
	}
	
	/**
	 * @param destinationUrl The destinationUrl to set.
	 */
	public void setDestinationUrl(String destinationUrl) {
		httpResponseDocument.setDestinationUrl(destinationUrl);
	}
	
	/**
	 * @return Returns the sourceUrl.
	 */
	public String getSourceUrl() {
		return httpResponseDocument.getSourceUrl();
	}
	
	/**
	 * @param sourceUrl The sourceUrl to set.
	 */
	public void setSourceUrl(String sourceUrl) {
		httpResponseDocument.setSourceUrl(sourceUrl);
	}
	
	public int getStatusCode() {
		return httpResponseDocument.getStatusCode();
	}

	public void setStatusCode(int statusCode) {
		httpResponseDocument.setStatusCode(statusCode);
	}
	
	/**
	 * @return Returns the pageSource.
	 */
	public String getPageSource() {
		if(httpResponseDocument.getPageSource() == null && !fetchFailed) {
			reset();
			updateHttpResponseDocument(HttpManager.getInstance().get(getDestinationUrl()));
			if(followMetaRefresh && httpResponseDocument.getPageSource() != null) {
				try {
					// document is required for getMetaRefreshUrl()
					document = getDocument(httpResponseDocument.getPageSource());
					String url = getMetaRefreshUrl();
					if(url != null && !url.equals("")) {
						httpResponseDocument.reset();
						httpResponseDocument.setDestinationUrl(url);
						if(logger.isDebugEnabled()) {
							logger.debug("HttpDocument.getPageSource follow meta refresh for URL: " + getSourceUrl() + " destination URL: " + url);
						}
						return getPageSource();
					}
				} catch (Exception e) {
					logger.error("HttpDocument.getPageSource exception", e);
				}
			}
		}
		if(httpResponseDocument.getPageSource() == null) {
			fetchFailed = true;
		}
		return httpResponseDocument.getPageSource();
	}
	
	/**
	 * @param pageSource The pageSource to set.
	 */
	public void setPageSource(String pageSource) {
		httpResponseDocument.setPageSource(pageSource);
	}
	
	protected void reset() {
		// reset all generated fields
		document = null;
		metaContentMap = null;
		metaRefreshUrl = null;
		title = null;
		text = null;
		images = null;
	}
	
	private Document getDocument(String src) throws SAXException, IOException {
		if(src != null) {
			DOMParser parser = new DOMParser();
			parser.parse(new InputSource(new StringReader(src)));
			return parser.getDocument();
		}
		return null;
	}
	
	protected Document getDocument() throws SAXException, IOException {
		if(document == null) {
			String src = getPageSource();
			// test if document is created by getPageSource()
			if(document == null && src != null) {
				document = getDocument(src);
			}
		}
		return document;
	}
	
	private String getNormalizedName(String name) {
		return name.toLowerCase();
	}
	
	private void appendMetaContent(String metaName, String metaContent) {
		String normalizedMetaName = getNormalizedName(metaName);
		if(metaContentMap.containsKey(normalizedMetaName)) {
			ArrayList<String> list = metaContentMap.get(normalizedMetaName);
			list.add(metaContent);
		} else {
			ArrayList<String> list = new ArrayList<String>();
			list.add(metaContent);
			metaContentMap.put(normalizedMetaName, list);
		}
	}
	
	public ArrayList<String> getMetaContent(String name) throws SAXException, IOException {
		if(metaContentMap == null) {
			Document doc = getDocument();
			metaContentMap = new HashMap<String, ArrayList<String>>();
			if(doc != null) {
				NodeList nodeList = doc.getElementsByTagName("META");
				for(int i=0; i<nodeList.getLength(); i++) {
					Element element = (Element)nodeList.item(i);
					String metaName = element.getAttribute("name");
					String metaContent = element.getAttribute("content");
					appendMetaContent(metaName, metaContent);
				}
			}
		}
		return metaContentMap.get(getNormalizedName(name));
	}
	
	public String getMetaRefreshUrl() throws SAXException, IOException {
		if(metaRefreshUrl == null) {
			Document doc = getDocument();
			if(doc != null) {
				NodeList nodeList = doc.getElementsByTagName("META");
				for(int i=0; i<nodeList.getLength(); i++) {
					Element element = (Element)nodeList.item(i);
					String httpEquiv = element.getAttribute("http-equiv");
					if (httpEquiv != null && httpEquiv.equals("refresh")) {
						String metaRefreshContent = element.getAttribute("content");
						if(metaRefreshContent != null) {
							String[] metaRefreshContentArray = metaRefreshContent.split(";");
							if ((metaRefreshContentArray.length == 2) && metaRefreshContentArray[1].startsWith("url=")) {
								metaRefreshUrl = metaRefreshContentArray[1].substring("url=".length(), metaRefreshContentArray[1].length());
							}
						}
					}
				}
			}
		}
		return metaRefreshUrl;
	}
	
	private boolean filter(Element element) {
		return filters.containsKey(element.getNodeName().toUpperCase());
	}
	
	private void appendContent(ArrayList<String> list, Element element) {
		if(!filter(element)) {
			Node child = element.getFirstChild();	
			while(child != null) {
				int nodeType = child.getNodeType();
				if(nodeType == Node.ELEMENT_NODE) {
					appendContent(list, (Element)child);
				} else if(nodeType == Node.TEXT_NODE) {
					String text = child.getNodeValue().trim();
					if(text.length() > 0) {
						list.add(text);
					}
				}
				child = child.getNextSibling();
			}
		}
	}
	
	private ArrayList<String> getContent(String tagName) throws SAXException, IOException {
		ArrayList<String> result = null;
		Document doc = getDocument();
		if(doc != null) {
			result = new ArrayList<String>();
			NodeList nodeList = doc.getElementsByTagName(tagName);
			for(int i=0; i<nodeList.getLength(); i++) {
				Element element = (Element)nodeList.item(i);
				appendContent(result, element);
			}
		}
		return result;
	}
	
	public ArrayList<String> getTitle() throws SAXException, IOException {
		if(title == null) {
			title = getContent("TITLE");
		}
		return title;
	}
	
	public ArrayList<String> getText() throws SAXException, IOException {
		if(text == null) {
			text = getContent("HTML");
		}
		return text;
	}
	
	public String getText(String fragmentDelimiter) throws SAXException, IOException {
		ArrayList<String> list = getText();
		StringBuffer buf = new StringBuffer();
		if(list != null && list.size() > 0) {
			boolean first = true;
			for(String fragment : list) {
				if(!first) {
					buf.append(fragmentDelimiter);
				}
				buf.append(fragment);
				first = false;
			}
		}
		return buf.toString();
	}
	
	public ArrayList<HttpImage> getImages() throws SAXException, IOException {
		if(images == null) {
			Document doc = getDocument();
			images = new ArrayList<HttpImage>();
			if(doc != null) {
				NodeList nodeList = doc.getElementsByTagName("IMG");
				for(int i=0; i<nodeList.getLength(); i++) {
					Element element = (Element)nodeList.item(i);
					HttpImage httpImage = new HttpImage();
					httpImage.setUrl(element.getAttribute("src"));
					httpImage.setWidth(Util.toInt(element.getAttribute("width")));
					httpImage.setHeight(Util.toInt(element.getAttribute("height")));
					httpImage.setAltText(element.getAttribute("alt"));
					images.add(httpImage);
				}
			}
		}
		return images;
	}
	
	public String getBase() throws SAXException, IOException {
		if(base == null) {
			Document doc = getDocument();
			if(doc != null) {
				NodeList nodeList = doc.getElementsByTagName("BASE");
				for(int i=0; i<nodeList.getLength(); i++) {
					Element element = (Element)nodeList.item(i);
					base = element.getAttribute("href");
				}
			}
		}
		return base;
	}
}
