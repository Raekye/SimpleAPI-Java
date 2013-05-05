/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.creatifcubed.simpleapi;

import java.io.InputStream;
import java.net.URL;

/**
 * 
 * @author Adrian
 */
public class SimpleResources {

	public static InputStream loadAsStream(String path) {
		InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
		return in;
	}

	public static URL loadAsURL(String path) {
		return Thread.currentThread().getContextClassLoader().getResource(path);
	}
}
