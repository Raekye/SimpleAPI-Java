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
import java.util.HashMap;
import java.util.Map;

public class SimpleHTTPRequest {

	public static final String DEFAULT_ENCODING = "utf-8";

	public final String url;
	private String getData;
	private String postData;
	private final Map<String, String> headers;
	private HttpURLConnection connection;

	public SimpleHTTPRequest(String url) {
		if (!url.toLowerCase().startsWith("http")) {
			throw new IllegalArgumentException("Invalid protocol for SimpleHTTPRequest (must be http)");
		}
		this.url = url;
		this.getData = "";
		this.postData = "";
		this.headers = new HashMap<String, String>();
		this.connection = null;
	}

	public SimpleHTTPRequest addGet(String key, String value) {
		try {
			this.getData += (this.getData.isEmpty() ? "" : "&") + URLEncoder.encode(key, DEFAULT_ENCODING) + "="
					+ URLEncoder.encode(value, DEFAULT_ENCODING);
			return this;
		} catch (UnsupportedEncodingException ex) {
			throw new RuntimeException(ex);
		}
	}

	public SimpleHTTPRequest addGet(String query) {
		this.getData += (this.getData.isEmpty() ? "" : "&") + query;
		return this;
	}

	public SimpleHTTPRequest addPost(String key, String value) {
		try {
			this.postData += (this.postData.isEmpty() ? "" : "&") + URLEncoder.encode(key, DEFAULT_ENCODING) + "="
					+ URLEncoder.encode(value, DEFAULT_ENCODING);
			return this;
		} catch (UnsupportedEncodingException ex) {
			throw new RuntimeException(ex);
		}
	}

	public SimpleHTTPRequest addPost(String data) {
		this.postData += (this.postData.isEmpty() ? "" : "&") + data;
		return this;
	}
	
	public SimpleHTTPRequest addHeader(String key, String value) {
		this.headers.put(key, value);
		return this;
	}

	public String getGetData() {
		return this.getData;
	}

	public String getPostData() {
		return this.postData;
	}

	public byte[] doGet() {
		return this.doGet(SimpleUtils.getCurrentHTTPProxy(Proxy.NO_PROXY));
	}

	public byte[] doGet(Proxy proxy) {
		try {
			HttpURLConnection connection = this.getConnection(proxy);
			addHeaders(connection, this.headers);
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
		return this.doPost(SimpleUtils.getCurrentHTTPProxy(Proxy.NO_PROXY));
	}

	public byte[] doPost(Proxy proxy) {
		try {;
		HttpURLConnection connection = this.getConnection(proxy);
		addHeaders(connection, this.headers);
		connection.setRequestMethod("POST");
		connection.setDoOutput(true);
		byte[] data = this.postData.getBytes(DEFAULT_ENCODING);
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
		return new URL(this.url + (this.getData.isEmpty() ? "" : ("?" + this.getData)));
	}
	
	public HttpURLConnection getConnection() throws MalformedURLException, IOException {
		return getConnection(SimpleUtils.getCurrentHTTPProxy(Proxy.NO_PROXY));
	}
	
	public HttpURLConnection getConnection(Proxy proxy) throws MalformedURLException, IOException {
		if (this.connection == null) {
			this.connection = (HttpURLConnection) this.getURL().openConnection(proxy);
		}
		return this.connection;
	}
	
	public static String[] parseURL(String urlStr) throws MalformedURLException {
		URL url = new URL(urlStr);
		return new String[] {
				url.getProtocol() + "://" + url.getAuthority() + url.getPath(),
				url.getQuery()
		};
	}
	
	public static void addHeaders(HttpURLConnection connection, Map<String, String> headers) {
		for (String key : headers.keySet()) {
			connection.setRequestProperty(key, headers.get(key));
		}
	}
}
