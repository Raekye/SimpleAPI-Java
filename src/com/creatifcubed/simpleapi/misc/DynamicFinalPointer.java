package com.creatifcubed.simpleapi.misc;

public class DynamicFinalPointer<T> extends Pointer<T> {
	private boolean isSet;
	public DynamicFinalPointer() {
		this(null);
	}
	
	public DynamicFinalPointer(T object) {
		super(object);
		this.isSet = false;
	}
	
	@Override
	public synchronized void set(T object) {
		if (this.isSet) {
			throw new IllegalStateException("DynamicFinalPointer value already set");
		}
	}
}
