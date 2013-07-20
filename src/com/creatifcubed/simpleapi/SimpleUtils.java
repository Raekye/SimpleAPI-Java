/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.creatifcubed.simpleapi;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.Socket;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * 
 * @author Adrian
 */
public class SimpleUtils {
	public static final Charset DEFAULT_ENCODING = Charset.forName("utf-8");
	private SimpleUtils() {
		//
	}

	public static long getRam() {
		try {
			return ((com.sun.management.OperatingSystemMXBean) java.lang.management.ManagementFactory
					.getOperatingSystemMXBean()).getTotalPhysicalMemorySize();
		} catch (ClassCastException ex) {
			return -1;
		}
	}

	public static int constrain(int num, int min, int max) {
		return Math.max(Math.min(num, max), min);
	}

	public static void downloadFile(URL url, String filename, int size) throws IOException {
		ReadableByteChannel rbc = Channels.newChannel(url.openStream());
		FileOutputStream fos = new FileOutputStream(filename);
		fos.getChannel().transferFrom(rbc, 0, size);
		fos.close();
	}

	public static void filePutContents(File f, String contents) {
		filePutContents(f, contents, DEFAULT_ENCODING);
	}
	public static void filePutContents(File f, String contents, Charset encoding) {
		try {
			if (!f.exists()) {
				f.createNewFile();
			}
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f.getCanonicalFile()), encoding));
			bw.write(contents);
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public static String fileGetContents(File f) {
		return fileGetContents(f, DEFAULT_ENCODING);
	}
	public static String fileGetContents(File f, Charset encoding) {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(f), encoding));
			String nl = System.getProperty("line.separator");
			String bin = "";
			while (true) {
				String line = reader.readLine();
				if (line == null) {
					break;
				}
				bin += line;
			}
			return bin;
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return null;
	}

	public static <T> T[] appendArrays(T[] first, T[]... rest) {
		int total = first.length;
		for (T[] each : rest) {
			total += each.length;
		}
		T[] all = Arrays.copyOf(first, total);
		int offset = first.length;

		for (T[] each : rest) {
			System.arraycopy(each, 0, all, offset, each.length);
			offset += each.length;
		}

		return all;
	}

	public static boolean httpPing(String server) {
		try {
			URL url = new URL(server);
			server = url.getHost();
		} catch (MalformedURLException ignore) {
			//
		}
		Socket socket = null;
		try {
			socket = new Socket(server, 80);
			return true;
		} catch (IOException ex) {
			ex.printStackTrace();
			return false;
		} finally {
			if (socket != null) {
				try {
					socket.close();
				} catch (IOException ignore) {
					ignore.printStackTrace();
				}
			}
		}
	}

	public static File getJarPath() {
		return getJarPath(SimpleUtils.class);
	}

	public static File getJarPath(Object o) {
		return getJarPath(o.getClass());
	}

	public static File getJarPath(Class<?> c) {
		try {
			return new File(c.getProtectionDomain().getCodeSource().getLocation().toURI());
		} catch (URISyntaxException ex) {
			throw new SimpleException(ex);
		}
	}

	public static void wait(int seconds) {
		try {
			Thread.sleep(seconds * 1000);
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		}
	}

	public static String vardump(Object o) {
		return vardump(o, "\t");
	}

	public static String vardump(Object o, String indentString) {
		return vardump(o, indentString, 0, new HashSet<Integer>(), false);
	}

	public static String vardump(Object o, String indentString, int n, Set<Integer> encountered,
			boolean ignoreFirstIndent) {
		String indent = repeat(indentString, n);
		String indentPlus = indent + indentString;
		if (o == null) {
			return (ignoreFirstIndent ? "" : indent) + "{null}";
		}
		if (isPrimitiveWrapper(o) || o instanceof String) {
			return (ignoreFirstIndent ? "" : indent) + "{" + o.getClass().getSimpleName() + ": " + String.valueOf(o)
					+ "}";
		}
		String bin = (ignoreFirstIndent ? "" : indent) + "{";
		if (o instanceof Object) {
			bin += "\n";
			bin += indentPlus + "class: " + o.getClass().getName() + ",\n";
			bin += indentPlus + "system hashcode: " + System.identityHashCode(o) + ",\n";
			bin += indentPlus + "object hashcode: " + o.hashCode() + ",\n";
			if (encountered.add(System.identityHashCode(o))) {
				if (o instanceof Object[]) {
					Object[] arr = (Object[]) o;
					bin += indentPlus + "array length: " + arr.length + ",\n";
					bin += indentPlus + "array values: [\n";
					for (int i = 0; i < arr.length; i++) {
						bin += indentPlus + indentString + i + ": "
								+ vardump(arr[i], indentString, n + 1, encountered, true) + ",\n";
					}
					bin += indentPlus + "],\n";
				} else {
					bin += indentPlus + "fields: {\n";
					Field[] fields = o.getClass().getDeclaredFields();
					for (int i = 0; i < fields.length; i++) {
						fields[i].setAccessible(true);
						bin += indentPlus + indentString + fields[i].getName() + ": ";
						try {
							bin += vardump(fields[i].get(o), indentString, n + 2, encountered, true);
						} catch (IllegalAccessException ex) {
							bin += "unable to access: " + ex.getMessage();
						}
						bin += ",\n";
					}
					bin += indentPlus + "},\n";
				}
			} else {
				bin += indentPlus + "already encountered,\n";
			}
		}
		return bin + indent + "}";
	}

	public static String repeat(String str, int n) {
		return new String(new char[n]).replaceAll("\0", str);
	}

	public static boolean isPrimitiveWrapper(Object o) {
		return o instanceof Boolean || o instanceof Character || o instanceof Byte || o instanceof Short
				|| o instanceof Integer || o instanceof Long || o instanceof Float || o instanceof Double
				|| o instanceof Void;
	}

	public static Proxy getCurrentHTTPProxy(Proxy defaultProxy) {
		String proxyHost = System.getProperty("http.proxyHost");
		String proxyPort = System.getProperty("http.proxyPort");
		if (proxyHost == null || proxyPort == null) {
			return defaultProxy;
		}
		return new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, Integer.parseInt(proxyPort)));
	}

	public static void copyFile(File sourceFile, File destFile) throws IOException {
		if (sourceFile.isDirectory()) {
			destFile.mkdir();
			File[] sourceChildren = sourceFile.listFiles();
			for (int i = 0; i < sourceChildren.length; i++) {
				copyFile(sourceChildren[i], new File(destFile, sourceChildren[i].getName()));
			}
		} else {
			destFile.createNewFile();
			FileChannel source = null;
			FileChannel destination = null;
	
			try {
				source = new FileInputStream(sourceFile).getChannel();
				destination = new FileOutputStream(destFile).getChannel();
				destination.transferFrom(source, 0, source.size());
			} finally {
				if (source != null) {
					source.close();
				}
				if (destination != null) {
					destination.close();
				}
			}
		}
	}

	public static void closeSilently(Closeable close) {
		try {
			close.close();
		} catch (Exception ignore) {
			return;
		}
	}

	public static boolean openLink(String url) {
		try {
			return openLink(new URL(url));
		} catch (MalformedURLException ignore) {
			ignore.printStackTrace();
		}
		return false;
	}
	public static boolean openLink(URL url) {
		try {
			Desktop.getDesktop().browse(url.toURI());
			return true;
		} catch (IOException ignore) {
			ignore.printStackTrace();
		} catch (URISyntaxException ignore) {
			ignore.printStackTrace();
		}
		return false;
	}

	public static String implode(String join, List<String> list) {
		return implode(join, list.toArray(new String[list.size()]));
	}
	public static String implode(String join, String[] arr) {
		if (arr.length == 0) {
			return "";
		}
		String bin = arr[0];
		for (int i = 1; i < arr.length; i++) {
			bin += join + arr[i];
		}
		return bin;
	}
	public static <T> T[] reverseArray(T[] arr) {
		T[] copy = Arrays.copyOf(arr, arr.length);
		for (int i = 0; i < copy.length / 2; i++) {
			copy[i] = copy[copy.length - (i + 1)];
		}
		return copy;
	}
}
