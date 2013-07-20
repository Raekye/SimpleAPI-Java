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
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.HashMap;
import java.util.Map;

public class SimpleHTTPRequest {

	public static final Charset DEFAULT_ENCODING = Charset.forName("utf-8");

	private String url;
	private String getQuery;
	private String postQuery;
	private String postBody;
	private final Map<String, String> headers;
	private HttpURLConnection connection;
	private Charset encoding;
	private volatile boolean requestMade;
	
	/**
	 * Constructs a SimpleHTTPRequest object with no URL (must be set with 1 or more calls to {@link SimpleHTTPRequest#setURL(String)}
	 */
	public SimpleHTTPRequest() {
		this.url = null;
		this.getQuery = "";
		this.postQuery = "";
		this.postBody = null;
		this.headers = new HashMap<String, String>();
		this.connection = null;
		this.requestMade = false;
	}
	/**
	 * @see SimpleHTTPRequest#SimpleHTTPRequest()
	 * @param url protocol (http or derivative), authority (host and port), and path
	 */
	public SimpleHTTPRequest(String url) {
		this();
		this.setURL(url);
	}
	
	/**
	 * 
	 * @param charset Charset, null for {@link SimpleHTTPRequest#DEFAULT_ENCODING}
	 * @return this
	 */
	public SimpleHTTPRequest setEncoding(String charset) {
		this.encoding = charset == null ? DEFAULT_ENCODING : Charset.forName(charset);
		return this;
	}
	
	/**
	 * 
	 * @return Current encoding
	 */
	public Charset getEncoding() {
		return this.encoding;
	}
	
	/**
	 * 
	 * @param url URL
	 * @return this
	 * @throws NullPointerException if url is null
	 * @throws IllegalArgumentException if url is not http or invalid
	 */
	public SimpleHTTPRequest setURL(String url) {
		if (url == null) {
			throw new NullPointerException("Parameter 0 (String url) is null");
		}
		if (!url.startsWith("http")) {
			throw new IllegalArgumentException("URL must be http, was {" + url + "}");
		}
		if (!isValidURL(url)) {
			throw new IllegalArgumentException("Invalid URL, got {" + url + "}");
		}
		this.url = url;
		return this;
	}
	
	/**
	 * 
	 * @return url
	 */
	public String getURL() {
		return this.url;
	}

	/**
	 * Adds get parameters
	 * @param key key
	 * @param value value
	 * @return this
	 */
	public SimpleHTTPRequest addGet(String key, String value) {
		this.getQuery += (this.getQuery.isEmpty() ? "" : "&") + this.encodeURLComponent(key) + "="
				+ this.encodeURLComponent(value);
		return this;
	}
	
	/**
	 * Adds raw String to get parameters
	 * @param query raw String
	 * @return this
	 */
	public SimpleHTTPRequest addGet(String query) {
		this.getQuery += (this.getQuery.isEmpty() ? "" : "&") + query;
		return this;
	}
	
	/**
	 * Adds post query
	 * @param key key
	 * @param value value
	 * @return this 
	 */
	public SimpleHTTPRequest addPost(String key, String value) {
		this.postQuery += (this.getQuery.isEmpty() ? "" : "&") + this.encodeURLComponent(key) + "="
				+ this.encodeURLComponent(value);
		return this;
	}
	
	/**
	 * Adds raw string to post query
	 * @param data raw post string
	 * @return this
	 */
	public SimpleHTTPRequest addPost(String data) {
		this.postQuery += (this.postQuery.isEmpty() ? "" : "&") + data;
		return this;
	}
	
	/**
	 * Sets post body to be used instead of post query. Use null to use postQuery
	 * @param body Post body
	 * @return this
	 */
	public SimpleHTTPRequest setPostBody(String body) {
		this.postBody = body;
		return this;
	}
	
	/**
	 * 
	 * @return post body
	 */
	public String getPostBody() {
		return this.postBody;
	}
	
	/**
	 * Adds http request header
	 * @param key key
	 * @param value value
	 * @return this
	 */
	public SimpleHTTPRequest addHeader(String key, String value) {
		this.headers.put(key, value);
		return this;
	}
	
	/**
	 * 
	 * @param key
	 * @return removed value
	 */
	public String removeHeader(String key) {
		return this.headers.remove(key);
	}
	
	/**
	 * Clears headers
	 * @return this
	 */
	public SimpleHTTPRequest clearHeaders() {
		this.headers.clear();
		return this;
	}
	
	/**
	 * 
	 * @param key key
	 * @return whether headers already contains this key
	 */
	public boolean containsHeader(String key) {
		return this.headers.containsKey(key);
	}
	
	/**
	 * 
	 * @param key key
	 * @return current value for the header
	 */
	public String getHeader(String key) {
		return this.headers.get(key);
	}
	
	/**
	 * 
	 * @return Current get data
	 */
	public String getGetData() {
		return this.getQuery;
	}
	
	/**
	 * 
	 * @return Current post data
	 */
	public String getPostData() {
		return this.postQuery;
	}
	
	/**
	 * 
	 * @param component URL component
	 * @return encoded component with the current encoding
	 */
	public String encodeURLComponent(String component) {
		try {
			return URLEncoder.encode(component, this.getEncoding().name());
		} catch (UnsupportedEncodingException ex) {
			throw new IllegalStateException(ex);
		}
	}
	
	/**
	 * Do get request
	 * @return byte data
	 */
	public byte[] doGet() {
		return this.doGet(SimpleUtils.getCurrentHTTPProxy(Proxy.NO_PROXY));
	}
	
	/**
	 * Do get request
	 * @param proxy Proxy
	 * @return byte data
	 */
	public synchronized byte[] doGet(Proxy proxy) {
		try {
			HttpURLConnection connection = this.getConnection(proxy);
			for (String key : this.headers.keySet()) {
				connection.setRequestProperty(key, this.headers.get(key));
			}
			connection.setRequestMethod("GET");
			BufferedInputStream in = new BufferedInputStream(connection.getInputStream());
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			SimpleStreams.pipeStreams(in, out);

			return out.toByteArray();
		} catch (MalformedURLException ex) {
			ex.printStackTrace();
			return null;
		} catch (ProtocolException ex) {
			throw new IllegalStateException(ex);
		} catch (IOException ex) {
			ex.printStackTrace();
			return null;
		} finally {
			this.requestMade = true;
		}
	}
	
	/**
	 * Do post request
	 * @return byte data
	 */
	public byte[] doPost() {
		return this.doPost(SimpleUtils.getCurrentHTTPProxy(Proxy.NO_PROXY));
	}
	
	/**
	 * Do post request
	 * @param proxy Proxy
	 * @return byte data
	 */
	public synchronized byte[] doPost(Proxy proxy) {
		try {
			HttpURLConnection connection = this.getConnection(proxy);
			for (String key : this.headers.keySet()) {
				connection.setRequestProperty(key, this.headers.get(key));
			}
			connection.setRequestMethod("POST");
			connection.setDoOutput(true);
			String postBody = this.getPostBody();
			String content = postBody == null ? this.getPostData() : postBody;
			byte[] data = content.getBytes(this.getEncoding());
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
			throw new IllegalStateException(ex);
		} catch (IOException ex) {
			ex.printStackTrace();
			return null;
		} finally {
			this.requestMade = true;
		}
	}
	
	/**
	 * 
	 * @return URL object with the url string + get parameters
	 * @throws MalformedURLException 
	 */
	public URL getURLObject() throws MalformedURLException {
		String urlString = this.getURL();
		if (urlString == null) {
			throw new IllegalStateException("URL not initialized (is null)");
		}
		return new URL(urlString + "?" + this.getGetData());
	}
	
	/**
	 * 
	 * @return HttpURLConnection
	 */
	public HttpURLConnection getConnectionSilently() {
		return this.getConnectionSilently(SimpleUtils.getCurrentHTTPProxy(Proxy.NO_PROXY));
	}
	
	/**
	 * 
	 * @param proxy proxy
	 * @return HttpURLConnection
	 */
	public HttpURLConnection getConnectionSilently(Proxy proxy) {
		try {
			return this.getConnection(proxy);
		} catch (MalformedURLException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 
	 * @return HttpURLConnection 
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public HttpURLConnection getConnection() throws MalformedURLException, IOException {
		return getConnection(SimpleUtils.getCurrentHTTPProxy(Proxy.NO_PROXY));
	}
	
	/**
	 * 
	 * @param proxy
	 * @return HttpURLConnection
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public synchronized HttpURLConnection getConnection(Proxy proxy) throws MalformedURLException, IOException {
		if (this.connection == null || this.requestMade) {
			URLConnection connection = this.getURLObject().openConnection(proxy);
			if (connection instanceof HttpURLConnection) {
				this.connection = (HttpURLConnection) connection;
				this.requestMade = false;
			} else {
				throw new IllegalStateException("Connection was not http");
			}
		}
		return this.connection;
	}
	
	/**
	 * 
	 * @param url url
	 * @return whether the url is valid
	 */
	public static boolean isValidURL(String url) {
		try {
			new URL(url).toURI();
			return true;
		} catch (Exception ignore) {
			return false;
		}
	}
}
