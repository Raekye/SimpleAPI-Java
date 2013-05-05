/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.creatifcubed.simpleapi;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 
 * @author Adrian
 */
public class SimpleStreams {

	public static int pipeStreams(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[8 * 1024];
		int total = 0;

		while (true) {
			try {
				int read = in.read(buffer);
				if (read == -1) {
					break;
				}
				out.write(buffer, 0, read);
				total += read;
			} catch (Exception ignore) {
				break;
			}
		}

		out.flush();
		return total;
	}

	public static void pipeStreamsConcurrent(final InputStream in, final OutputStream out) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					SimpleStreams.pipeStreams(in, out);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}, "SimpleStreams - Concurrent Stream Pipe").start();
	}
}
