package com.creatifcubed.simpleapi.misc;

public class DynamicFinalSilentPointer<T> extends DynamicFinalPointer<T> {
	public DynamicFinalSilentPointer() {
		this(null);
	}
	
	public DynamicFinalSilentPointer(T object) {
		super(object);
	}
	
	@Override
	public synchronized void set(T object) {
		try {
			super.set(object);
		} catch (IllegalStateException ex) {
			ex.printStackTrace();
		}
	}
}
