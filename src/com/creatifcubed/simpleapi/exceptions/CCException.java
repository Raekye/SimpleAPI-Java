package com.creatifcubed.simpleapi.exceptions;

public class CCException extends RuntimeException {

	private int code;

	public CCException() {
		super();
	}

	public CCException(String msg) {
		super(msg);
	}

	public CCException(Throwable cause) {
		super(cause);
	}

	public CCException(String msg, Throwable cause) {
		super(msg, cause);
	}

	public CCException(String msg, int code) {
		super(msg);
		this.code = code;
	}

	public CCException(int code) {
		this.code = code;
	}

	public int getCode() {
		return this.code;
	}
}
