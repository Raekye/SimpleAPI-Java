package com.creatifcubed.simpleapi.misc;

public class MutablePointer<T> extends Pointer<T> {
	public MutablePointer() {
		this(null);
	}
	
	public MutablePointer(T object) {
		super(object);
	}
}
