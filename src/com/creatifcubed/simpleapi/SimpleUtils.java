/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.creatifcubed.simpleapi;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 
 * @author Adrian
 */
public class SimpleUtils {
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

	public static String getOSFileExplorer() {
		String fileExplorer = null;
		switch (SimpleOS.getOS()) {
		case WINDOWS:
			fileExplorer = "explorer";
			break;
		case MAC:
			fileExplorer = "open";
			break;
		case UNIX:
			fileExplorer = "nautilus";
			break;
		}
		return fileExplorer;
	}

	public static boolean openFolder(String explorer, String path) {
		ProcessBuilder pb = new ProcessBuilder(explorer, path);
		try {
			Process p = pb.start();
			if (p == null) {
				throw new NullPointerException("Starting process (open folder) was null");
			}
		} catch (Exception ex) {
			return false;
		}
		return true;
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
		try {
			if (!f.exists()) {
				f.createNewFile();
			}
			FileWriter fw = new FileWriter(f.getCanonicalFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(contents);
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
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
		Socket socket = null;
		try {
			socket = new Socket(server, 80);
			return true;
		} catch (IOException ex) {
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
		if (!destFile.exists()) {
			destFile.createNewFile();
		}

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
	
	public static void closeSilently(Closeable close) {
		try {
			close.close();
		} catch (Exception ignore) {
			return;
		}
	}
}
