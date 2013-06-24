package com.creatifcubed.simpleapi.tests;

import static org.junit.Assert.*;

import org.junit.Test;

import com.creatifcubed.simpleapi.SimpleTask;
import com.creatifcubed.simpleapi.SimpleWaiter;

public class SimpleWaiterTests {

	@Test
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

}
