package com.creatifcubed.simpleapi.tests;

import static org.junit.Assert.*;

import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;

import org.junit.Test;

import com.creatifcubed.simpleapi.SimpleTask;
import com.creatifcubed.simpleapi.SimpleWaiter;
import com.creatifcubed.simpleapi.swing.SimpleSwingWaiter;

public class SimpleWaiterTests {

	//@Test
	public void testRandom() {
		final SimpleWaiter waiter = new SimpleWaiter("Hello", 50);
		waiter.task = new SimpleTask() {
			private int i = 0;
			@Override
			public void run() {
				while (i < 5) {
					waiter.stdout().println("I: " + i);
					System.out.println("I: " + i);
					try {
						Thread.sleep(500);
					} catch (InterruptedException ex) {
						ex.printStackTrace();
					}
					i++;
				}
				System.out.println("I done: " + i);
			}

			@Override
			public int getProgress() {
				//return -1;
				return (int) (i / 5.0  * 100);
			}
		};
		waiter.stdout().print("Hello");
		waiter.run();
	}
	
	@Test
	public void test2() {
		final SimpleSwingWaiter waiter = new SimpleSwingWaiter("Test");
		final Logger log = Logger.getGlobal();
		waiter.worker = new SimpleSwingWaiter.Worker(waiter) {
			@Override
			protected Void doInBackground() throws Exception {
				int i = 0;
				while (i < 20) {
					log.info("I: " + i);
					//this.setProgress((int) (i / 20.0 * 100));
					try {
						Thread.sleep(50);
					} catch (InterruptedException ex) {
						ex.printStackTrace();
					}
					i++;
				}
				System.out.println("I done: " + i);
				return null;
			}
			@Override
			public boolean isIndeterminate() {
				return true;
			}
			@Override
			public boolean isCancellable() {
				return false;
			}
		};
		log.addHandler(new StreamHandler(System.out, new SimpleFormatter()) {
			@Override
			public void flush() {
				System.err.println("Flush called");
				super.flush();
			}
			@Override
			public void publish(LogRecord log) {
				System.err.println("Publishing");
				super.publish(log);
				super.flush();
			}
		});
		waiter.run();
	}

}
