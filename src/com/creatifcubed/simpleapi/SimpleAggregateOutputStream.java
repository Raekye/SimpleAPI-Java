package com.creatifcubed.simpleapi;

import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

public class SimpleAggregateOutputStream extends OutputStream {

	private final List<OutputStream> children;

	public SimpleAggregateOutputStream(OutputStream... children) {
		this.children = new LinkedList<OutputStream>();
		for (int i = 0; i < children.length; i++) {
			this.children.add(children[i]);
		}
	}

	public synchronized SimpleAggregateOutputStream addListener(OutputStream out) {
		this.children.add(out);
		return this;
	}
	
	public synchronized SimpleAggregateOutputStream removeListener(OutputStream out) {
		this.children.remove(out);
		return this;
	}

	@Override
	public synchronized void write(int b) throws IOException {
		for (OutputStream each : this.children) {
			each.write(b);
		}
	}
	
	@Override
	public synchronized void write(byte[] b) throws IOException {
		for (OutputStream each : this.children) {
			each.write(b);
		}
	}
	
	@Override
	public synchronized void write(byte[] b, int offset, int len) throws IOException {
		for (OutputStream each : this.children) {
			each.write(b, offset, len);
		}
	}

	@Override
	public synchronized void flush() throws IOException {
		for (OutputStream each : this.children) {
			each.flush();
		}
	}

	@Override
	public synchronized void close() throws IOException {
		for (OutputStream each : this.children) {
			each.close();
		}
	}

}
