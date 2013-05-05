package com.creatifcubed.simpleapi;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public abstract class SimpleStringOutputStreamAdapter extends OutputStream {
	
	private final ByteArrayOutputStream buffer;
	private final String encoding;
	private final Listener listener;
	
	public SimpleStringOutputStreamAdapter(String encoding) {
		this(encoding, null);
	}
	
	public SimpleStringOutputStreamAdapter(String encoding, Listener listener) {
		this.buffer = new ByteArrayOutputStream();
		this.encoding = encoding;
		this.listener = listener;
	}
	
	@Override
	public synchronized void write(int b) throws IOException {
		this.buffer.write(b);
	}
	
	@Override
	public synchronized void flush() throws IOException {
		this.buffer.flush();
		String flushed = this.buffer.toString(this.encoding);
		this.buffer.reset();
		this.onString(flushed);
	}
	
	private void onString(String flushed) {
		if (this.listener != null) {
			this.listener.onString(flushed);
		}
	}
	
	public static interface Listener {
		public void onString(String flushed);
	}
}
