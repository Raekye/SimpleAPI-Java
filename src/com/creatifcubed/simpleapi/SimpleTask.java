package com.creatifcubed.simpleapi;

public interface SimpleTask extends Runnable {
	public static final int PROGRESS_MAX = 100;
	/**
	 * @return Progress as percent. -1 for indeterminate
	 */
	public int getProgress();
}
