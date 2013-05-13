package com.creatifcubed.simpleapi;

public class SimpleException extends RuntimeException {
	private static final long serialVersionUID = -8570666546947083808L;

	public SimpleException(Exception other) {
		super(other);
	}
}
