package com.creatifcubed.simpleapi.exceptions;

public class SimpleDocException extends CCException {

	public static final int CONSTRUCTOR_EXCEPTION = 0;
	public static final int SAVING_EXCEPTION = 1;
	public static final int LOADING_EXCEPTION = 2;

	public SimpleDocException() {
		super();
	}

	public SimpleDocException(String msg) {
		super(msg);
	}

	public SimpleDocException(Throwable cause) {
		super(cause);
	}

	public SimpleDocException(String msg, Throwable cause) {
		super(msg, cause);
	}

	public SimpleDocException(String msg, int code) {
		super(msg, code);
	}

	public SimpleDocException(int code) {
		super(code);
	}
}
