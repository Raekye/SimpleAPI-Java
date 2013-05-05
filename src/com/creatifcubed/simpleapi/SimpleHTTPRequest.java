package com.creatifcubed.simpleapi;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLEncoder;

public class SimpleHTTPRequest {

	public static final String ENCODING = "utf-8";

	public static final boolean NO_PROXY = true;
	public static final boolean DEFAULT_PROXY = false;

	public final String url;
	private String getData;
	private String postData;

	public SimpleHTTPRequest(String url) {
		if (!url.toLowerCase().startsWith("http")) {
			throw new IllegalArgumentException("Invalid protocol for SimpleHTTPRequest (must be http!)");
		}
		this.url = url;
		this.getData = "";
		this.postData = "";
	}

	public SimpleHTTPRequest addGet(String key, String value) {
		try {
			this.getData += (this.getData.isEmpty() ? "" : "&") + URLEncoder.encode(key, ENCODING) + "="
					+ URLEncoder.encode(value, ENCODING);
			return this;
		} catch (UnsupportedEncodingException ex) {
			throw new RuntimeException(ex);
		}
	}

	public SimpleHTTPRequest addPost(String key, String value) {
		try {
			this.postData += (this.postData.isEmpty() ? "" : "&") + URLEncoder.encode(key, ENCODING) + "="
					+ URLEncoder.encode(value, ENCODING);
			return this;
		} catch (UnsupportedEncodingException ex) {
			throw new RuntimeException(ex);
		}
	}

	public String getGetData() {
		return this.getData;
	}

	public String getPostData() {
		return this.postData;
	}

	public byte[] doGet() {
		return this.doGet(DEFAULT_PROXY);
	}

	public byte[] doGet(boolean ignoreProxy) {
		try {
			HttpURLConnection connection = (HttpURLConnection) (this.getURL()
					.openConnection(ignoreProxy ? Proxy.NO_PROXY : null));
			connection.setRequestMethod("GET");
			BufferedInputStream in = new BufferedInputStream(connection.getInputStream());
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			SimpleStreams.pipeStreams(in, out);

			return out.toByteArray();
		} catch (MalformedURLException ex) {
			ex.printStackTrace();
			return null;
		} catch (ProtocolException ex) {
			throw new RuntimeException(ex);
		} catch (IOException ex) {
			ex.printStackTrace();
			return null;
		}
	}

	public byte[] doPost() {
		return this.doPost(DEFAULT_PROXY);
	}

	public byte[] doPost(boolean ignoreProxy) {
		try {
			HttpURLConnection connection = (HttpURLConnection) (ignoreProxy ? this.getURL().openConnection() : this
					.getURL().openConnection(Proxy.NO_PROXY));
			connection.setRequestMethod("POST");
			connection.setDoOutput(true);
			byte[] data = this.postData.getBytes(ENCODING);
			connection.setRequestProperty("Content-length", Integer.toString(data.length));
			connection.getOutputStream().write(data);
			BufferedInputStream in = new BufferedInputStream(connection.getInputStream());
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			SimpleStreams.pipeStreams(in, out);
			return out.toByteArray();
		} catch (MalformedURLException ex) {
			ex.printStackTrace();
			return null;
		} catch (ProtocolException ex) {
			throw new RuntimeException(ex);
		} catch (IOException ex) {
			ex.printStackTrace();
			return null;
		}
	}

	public URL getURL() throws MalformedURLException {
		return new URL(this.url + "?" + this.getData);
	}
}
