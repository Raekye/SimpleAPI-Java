package com.creatifcubed.simpleapi;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.LinkedList;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * 
 * @author raekye
 * 
 * For single thread access, one time use
 */
public class SimpleHTTPRequest2 {

	public static final Charset DEFAULT_ENCODING = Charset.forName("utf-8");
	private static final Logger log = Logger.getLogger(SimpleHTTPRequest2.class.getName());

	private String url;
	private final Map<String, List<String>> getParams;
	private byte[] postBody;
	private final Map<String, String> headers;
	private HttpURLConnection connection;
	private byte[] response;
	private boolean madeRequest;
	
	/**
	 * Constructs a SimpleHTTPRequest object with no URL (must be set with 1 or more calls to {@link SimpleHTTPRequest#setURL(String)}
	 */
	public SimpleHTTPRequest2() {
		this.url = null;
		this.getParams = new HashMap<String, List<String>>();
		this.postBody = new byte[0];
		this.headers = new HashMap<String, String>();
		this.connection = null;
		this.response = null;
		this.madeRequest = false;
	}
	/**
	 * @see SimpleHTTPRequest2#SimpleHTTPRequest2()
	 * @param url protocol (http or derivative), authority (host and port), and path
	 */
	public SimpleHTTPRequest2(String url) {
		this();
		this.setURL(url);
	}
	
	/**
	 * 
	 * @param url URL
	 * @return this
	 * @throws IllegalArgumentException if url is not http or is invalid invalid
	 */
	public SimpleHTTPRequest2 setURL(String url) {
		if (url != null) {
			if (!url.startsWith("http")) {
				throw new IllegalArgumentException("URL must be http, was {" + url + "}");
			}
			if (!isValidURL(url)) {
				throw new IllegalArgumentException("Invalid URL, got {" + url + "}");
			}
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
	public SimpleHTTPRequest2 addGet(String key, String value) {
		List<String> existing = this.getParams.get(key);
		if (existing == null) {
			existing = new LinkedList<String>();
		}
		existing.add(value);
		this.getParams.put(key, existing);
		return this;
	}
	
	/**
	 * Sets post body. Encodes string
	 * @param body Post body
	 * @return this
	 */
	public SimpleHTTPRequest2 setPostBody(String body) {
		this.postBody = body == null ? new byte[0] : body.getBytes(DEFAULT_ENCODING);
		return this;
	}
	
	/**
	 * Sets post body.
	 * @param data
	 * @return this
	 */
	public SimpleHTTPRequest2 setPostBody(byte[] data) {
		this.postBody = data == null ? new byte[0] : data;
		return this;
	}
	
	/**
	 * 
	 * @return post body
	 */
	public byte[] getPostBody() {
		return this.postBody;
	}
	
	/**
	 * Adds http request header
	 * @param key key
	 * @param value value
	 * @return this
	 */
	public SimpleHTTPRequest2 addHeader(String key, String value) {
		this.headers.put(key, value);
		return this;
	}
	
	/**
	 * 
	 * @return Current get data
	 */
	public String getGetData() {
		String bin = "";
		for (String key : this.getParams.keySet()) {
			for (String value : this.getParams.get(key)) {
				bin += this.encodeURLComponent(key) + "=" + this.encodeURLComponent(value);
			}
		}
		return bin;
	}
	
	/**
	 * 
	 * @return Current post data
	 */
	public String getPostData() {
		return new String(this.postBody, DEFAULT_ENCODING);
	}
	
	/**
	 * 
	 * @param component URL component
	 * @return encoded component with the current encoding
	 */
	public String encodeURLComponent(String component) {
		try {
			return URLEncoder.encode(component, DEFAULT_ENCODING.name());
		} catch (UnsupportedEncodingException ex) {
			throw new IllegalStateException(ex);
		}
	}
	
	/**
	 * Do get request
	 * @return this
	 */
	public SimpleHTTPRequest2 doGet() {
		if (this.madeRequest) {
			throw new IllegalStateException("Already made request");
		}
		this.madeRequest = true;
		HttpURLConnection connection = null;
		InputStream is = null;
		try {
			connection = this.getConnection();
			for (String key : this.headers.keySet()) {
				connection.setRequestProperty(key, this.headers.get(key));
			}
			connection.setRequestMethod("GET");
			is = new BufferedInputStream(connection.getInputStream());
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			pipeStreams(is, bos);
			this.response = bos.toByteArray();
			return this;
		} catch (MalformedURLException ex) {
			log.log(Level.WARNING, ex.getMessage(), ex);
			return null;
		} catch (ProtocolException ex) {
			throw new IllegalStateException(ex);
		} catch (IOException ex) {
			log.log(Level.WARNING, ex.getMessage(), ex);
			return null;
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException ex) {
					log.log(Level.WARNING, ex.getMessage(), ex);
				}
			}
			if (connection != null) {
				connection.disconnect();
			}
		}
	}
	
	/**
	 * Do post request
	 * @return this
	 */
	public SimpleHTTPRequest2 doPost() {
		if (this.madeRequest) {
			throw new IllegalStateException("Already made request");
		}
		this.madeRequest = true;
		HttpURLConnection connection = null;
		InputStream is = null;
		try {
			connection = this.getConnection();
			connection.setRequestMethod("POST");
			connection.setDoOutput(true);
			byte[] data = this.getPostBody();
			connection.setRequestProperty("Content-Length", Integer.toString(data.length));
			for (String key : this.headers.keySet()) {
				connection.setRequestProperty(key, this.headers.get(key));
			}
			connection.getOutputStream().write(data);
			is = new BufferedInputStream(connection.getInputStream());
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			pipeStreams(is, bos);
			this.response = bos.toByteArray();
			return this;
		} catch (MalformedURLException ex) {
			log.log(Level.WARNING, ex.getMessage(), ex);
			return null;
		} catch (ProtocolException ex) {
			throw new IllegalStateException(ex);
		} catch (IOException ex) {
			log.log(Level.WARNING, ex.getMessage(), ex);
			return null;
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException ex) {
					log.log(Level.WARNING, ex.getMessage(), ex);
				}
			}
			if (connection != null) {
				connection.disconnect();
			}
		}
	}
	
	/**
	 * 
	 * @return data
	 */
	public byte[] getResponse() {
		return this.response;
	}
	
	/**
	 * 
	 * @return data as string
	 */
	public String getResponseAsString() {
		return new String(this.response, SimpleHTTPRequest2.DEFAULT_ENCODING);
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
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public HttpURLConnection getConnection() throws MalformedURLException, IOException {
		if (this.connection == null) {
			URLConnection connection = this.getURLObject().openConnection();
			if (connection instanceof HttpURLConnection) {
				this.connection = (HttpURLConnection) connection;
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
	
	/**
	 * 
	 * @param is
	 * @param os
	 * @return
	 * @throws IOException
	 */
	public static int pipeStreams(InputStream is, OutputStream os) throws IOException {
		byte[] buffer = new byte[1 << 8];
		int total = 0;

		while (true) {
			try {
				int read = is.read(buffer);
				if (read == -1) {
					break;
				}
				os.write(buffer, 0, read);
				total += read;
			} catch (IOException ignore) {
				ignore.printStackTrace();
				break;
			}
		}

		os.flush();
		return total;
	}
}
