package com.perpetumobile.bit.http;

import java.awt.image.BufferedImage;

public class HttpImage {
	
	protected String url = null;
	protected int width = -1;
	protected int height = -1;
	protected String altText = null;
	protected int statusCode = -1;
	protected BufferedImage image = null;
	
	
	public HttpImage() {	
	}

	public HttpImage(String url) {
		this.url = url;
	}
	
	/**
	 * @return Returns the altText.
	 */
	public String getAltText() {
		return altText;
	}

	/**
	 * @param altText The altText to set.
	 */
	public void setAltText(String altText) {
		this.altText = altText;
	}

	/**
	 * @return Returns the height.
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * @param height The height to set.
	 */
	public void setHeight(int height) {
		this.height = height;
	}

	/**
	 * @return Returns the url.
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * @param url The url to set.
	 */
	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 * @return Returns the width.
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * @param width The width to set.
	 */
	public void setWidth(int width) {
		this.width = width;
	}

	/**
	 * @return the statusCode
	 */
	public int getStatusCode() {
		return statusCode;
	}

	/**
	 * @param statusCode the statusCode to set
	 */
	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}

	/**
	 * @return the image
	 */
	public BufferedImage getImage() {
		return image;
	}

	/**
	 * @param image the image to set
	 */
	public void setImage(BufferedImage image) {
		this.image = image;
	}
}
