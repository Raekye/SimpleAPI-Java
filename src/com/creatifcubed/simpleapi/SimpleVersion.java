/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.creatifcubed.simpleapi;

/**
 * 
 * @author Adrian
 */
public class SimpleVersion implements Comparable<SimpleVersion> {
	private int major;
	private int minor;
	private int fix;
	private int[] all;

	public SimpleVersion(String verString) {
		String[] verArray = verString.split("[.]");
		this.major = Integer.parseInt(verArray[0]);
		this.minor = Integer.parseInt(verArray[1]);
		this.fix = Integer.parseInt(verArray[2]);
		this.all = new int[verArray.length];
		for (int i = 0; i < verArray.length; i++) {
			this.all[i] = Integer.parseInt(verArray[i]);
		}
	}

	public boolean shouldUpdateTo(SimpleVersion newerVersion) {
		return this.shouldUpdateTo(newerVersion, 100);
	}

	public boolean shouldUpdateTo(SimpleVersion newerVersion, int diff) {
		return this.compareTo(newerVersion) <= -diff;
	}

	@Override
	public boolean equals(Object o) {
		if (o != null) {
			if (o instanceof SimpleVersion) {
				SimpleVersion other = (SimpleVersion) o;
				return this.major == other.major && this.minor == other.minor && this.fix == other.fix;
			}
		}
		return false;
	}

	@Override
	public int hashCode() {
		return ((this.major * 100) + this.minor) * 100 + this.fix;
	}

	@Override
	public int compareTo(SimpleVersion other) {
		return this.hashCode() - other.hashCode();
	}

	@Override
	public String toString() {
		String bin = String.valueOf(this.major);
		for (int i = 1; i < this.all.length; i++) {
			bin += "." + this.all[i];
		}
		return bin;
	}
}
