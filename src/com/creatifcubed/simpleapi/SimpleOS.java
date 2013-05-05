/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.creatifcubed.simpleapi;

/**
 * 
 * @author Adrian
 */
public enum SimpleOS {
	WINDOWS("Windows"), MAC("Mac"), UNIX("Unix (like)"), UNKNOWN("Unknown");

	public final String representation;

	private SimpleOS(String representation) {
		this.representation = representation;
	}

	public static SimpleOS getOS() {
		String os = System.getProperty("os.name").toLowerCase();
		if (os.indexOf("win") != -1) {
			return SimpleOS.WINDOWS;
		} else if (os.indexOf("mac") != -1) {
			return SimpleOS.MAC;
		} else if (os.indexOf("nix") != -1 || os.indexOf("nux") != -1) {
			return SimpleOS.UNIX;
		}
		return SimpleOS.UNKNOWN;
	}
}
