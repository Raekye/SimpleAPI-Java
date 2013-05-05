/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.creatifcubed.simpleapi;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.WindowConstants;

/**
 * 
 * @author Adrian
 */
public class SimpleWaiter implements Runnable {
	private String title;
	private Runnable task;
	private JFrame parent;
	private JDialog dialog;

	public SimpleWaiter(String title, Runnable task, JFrame parent) {
		this.title = title;
		this.task = task;
		this.parent = parent;
		this.dialog = null;
	}

	@Override
	public void run() {
		this.dialog = new JDialog(this.parent, this.title, true);

		new Thread(new Runnable() {
			@Override
			public void run() {
				SimpleWaiter.this.task.run();
				SimpleWaiter.this.dialog.dispose();
			}
		}).start();

		JPanel panel = new JPanel();
		JProgressBar progress = new JProgressBar();
		progress.setIndeterminate(true);
		panel.add(progress);

		this.dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

		this.dialog.setContentPane(panel);
		this.dialog.pack();
		this.dialog.setLocationRelativeTo(null);
		this.dialog.setVisible(true);

		this.dialog.dispose();

	}

}
