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
	public static final int DEFAULT_BUFFER_SIZE = 8 * 1024;

	public static int pipeStreams(InputStream in, OutputStream out) throws IOException {
		return pipeStreams(in ,out , DEFAULT_BUFFER_SIZE);
	}

	public static int pipeStreams(InputStream in, OutputStream out, int bufferSize) throws IOException {
		byte[] buffer = new byte[bufferSize];
		int total = 0;

		while (true) {
			try {
				int read = in.read(buffer);
				if (read == -1) {
					break;
				}
				out.write(buffer, 0, read);
				total += read;
			} catch (IOException ignore) {
				ignore.printStackTrace();
				break;
			}
		}

		out.flush();
		return total;
	}

	public static void pipeStreamsConcurrently(InputStream in, OutputStream out) {
		pipeStreamsConcurrently(in, out, DEFAULT_BUFFER_SIZE, null);
	}

	public static void pipeStreamsConcurrently(InputStream in, OutputStream out, int bufferSize) {
		pipeStreamsConcurrently(in, out, bufferSize, null);
	}

	public static void pipeStreamsConcurrently(InputStream in, OutputStream out, PipeStreamDoneListener onDone) {
		pipeStreamsConcurrently(in, out, DEFAULT_BUFFER_SIZE, onDone);
	}

	public static void pipeStreamsConcurrently(final InputStream in, final OutputStream out, final int bufferSize, final PipeStreamDoneListener onDone) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				int bytesPiped = 0;
				try {
					bytesPiped = SimpleStreams.pipeStreams(in, out, bufferSize);
				} catch (IOException ex) {
					ex.printStackTrace();
				} finally {
					if (onDone != null) {
						onDone.onDone(bytesPiped);
					}
				}
			}
		}, "SimpleStreams - Concurrent Stream Pipe").start();
	}

	public static interface PipeStreamDoneListener {
		public void onDone(int bytesPiped);
	}
}
