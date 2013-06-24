package com.creatifcubed.simpleapi.tests;

import static org.junit.Assert.*;

import org.junit.Test;

import com.creatifcubed.simpleapi.SimpleVersion;
import com.sun.istack.internal.logging.Logger;

public class SimpleVersionTests {

	@Test
	public void testRegular() {
		testWithString("1.2.3");
	}
	
	@Test
	public void testWithPrerelease() {
		testWithString("1.2.3-wa.fw.efw.");
	}
	
	@Test
	public void testWithMetadata() {
		testWithString("1.2.3-awe.f.wfe...awe");
	}
	
	@Test
	public void testWithPrereleaseAndMetadata() {
		testWithString("1.2.3-awef.awef.e.+ae.f.e.");
	}
	
	private void testWithString(String str) {
		SimpleVersion version = new SimpleVersion(str);
		assertEquals(version.toString(), str);
		Logger.getLogger(this.getClass()).info(String.format("Expected {%s}, got {%s}", str, version.toString()));
	}

}
