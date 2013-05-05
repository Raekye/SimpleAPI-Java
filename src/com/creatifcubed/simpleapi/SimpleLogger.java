/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.creatifcubed.simpleapi;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

/**
 * 
 * @author Adrian
 */
public class SimpleLogger {

	public String logPrefix;
	private BufferedWriter logFile;
	private Map<Integer, String> logLevelDescription;

	public int fileThreshold;
	public static final int DEFAULT_FILE_THRESHOLD = 10;
	public static final int DEFAULT_LOG_LEVEL = 0;

	public SimpleLogger() {
		this("");
	}

	public SimpleLogger(String prefix) {
		this("", null);
	}

	public SimpleLogger(String prefix, File f) {
		this.logPrefix = prefix;
		this.setFile(f);
		this.fileThreshold = DEFAULT_FILE_THRESHOLD;
	}

	public void log(String str) {
		this.log(str, DEFAULT_LOG_LEVEL);
	}

	public void log(String str, int level) {
		String log = String.format(this.logPrefix, this.getLogLevelDescription(level)) + str;
		System.out.println(log);
		if (level >= this.fileThreshold) {
			if (this.logFile != null) {
				this.writeToFile(log);
			}
		}
	}

	public void writeToFile(String log) {
		try {
			this.logFile.append(log);
			this.logFile.newLine();
			this.logFile.flush();
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	public String getLogLevelDescription(int level) {
		if (this.logLevelDescription.containsKey(level)) {
			return this.logLevelDescription.get(level);
		}
		return String.valueOf(level);
	}

	public void setFile(File f) {
		if (f == null) {
			this.logFile = null;
			return;
		}
		try {
			this.logFile = new BufferedWriter(new FileWriter(f));
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}
}
