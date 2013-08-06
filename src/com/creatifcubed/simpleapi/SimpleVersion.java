/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.creatifcubed.simpleapi;

/**
 * 
 * @author Adrian
 * - http://semver.org/
 */
public class SimpleVersion implements Comparable<SimpleVersion> {
	public final int major;
	public final int minor;
	public final int patch;
	public final String prerelease;
	public final String metadata;

	public SimpleVersion(String verString) {
		if (verString == null) {
			throw new NullPointerException("Parameter 0 (String verString) is null");
		}
		String[] separateMetadata = verString.split("\\+");
		String[] separatePrerelease = separateMetadata[0].split("-");
		String[] verArray = separatePrerelease[0].split("\\.");
		if (verArray.length != 3) {
			throw new IllegalArgumentException(String.format("Version string is not major.minor.patch (is {%s})", verString));
		}
		this.major = Integer.parseInt(verArray[0]);
		this.minor = Integer.parseInt(verArray[1]);
		this.patch = Integer.parseInt(verArray[2]);
		this.prerelease = separatePrerelease.length > 1 ? separatePrerelease[1] : null;
		this.metadata = separateMetadata.length > 1 ? separateMetadata[1] : null;
	}

	public boolean shouldUpdateTo(SimpleVersion newerVersion) {
		if (newerVersion.major > this.major) {
			return true;
		}
		if (newerVersion.major == this.major && newerVersion.minor > this.minor) {
			return true;
		}
		return false;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof SimpleVersion) {
			SimpleVersion other = (SimpleVersion) o;
			return this.major == other.major && this.minor == other.minor && this.patch == other.patch;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return ((this.major * 128) + this.minor) * 128 + this.patch;
	}

	@Override
	public int compareTo(SimpleVersion other) {
		if (this.major > other.major) {
			return 1;
		}
		if (this.major < other.major){
			return -1;
		}
		if (this.minor > other.minor) {
			return 1;
		}
		if (this.minor < other.minor) {
			return -1;
		}
		if (this.patch > other.patch) {
			return 1;
		}
		if (this.patch < other.patch) {
			return -1;
		}
		return 0;
	}

	@Override
	public String toString() {
		return this.major + "." + this.minor + "." + this.patch + (this.prerelease == null ? "" : "-" + this.prerelease) + (this.metadata == null ? "" : "+" + this.metadata);
	}
}
