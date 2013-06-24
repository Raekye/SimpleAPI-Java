/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.creatifcubed.simpleapi;

import java.awt.BorderLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintStream;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.WindowConstants;

import com.creatifcubed.simpleapi.swing.SimpleGUIConsole;

/**
 * 
 * @author Adrian
 */
public class SimpleWaiter implements Runnable {
	public String title;
	public SimpleTask task;
	public Window parent;
	public int updatePeriod;
	private final SimpleGUIConsole console;
	
	public static final int DEFAULT_UPDATE_PERIOD = 1000 * 2;
	
	public SimpleWaiter(String title) {
		this(title, DEFAULT_UPDATE_PERIOD);
	}
	public SimpleWaiter(String title, int updatePeriod) {
		this(title, updatePeriod, null);
	}
	public SimpleWaiter(String title, int updatePeriod, Window parent) {
		this.title = title;
		this.parent = parent;
		this.console = new SimpleGUIConsole();
		this.console.init();
		this.updatePeriod = updatePeriod;
	}
	
	public PrintStream stdout() {
		return this.console.getOut();
	}
	public PrintStream stderr() {
		return this.console.getErr();
	}
	
	@Override
	public void run() {
		JPanel panel = new JPanel(new BorderLayout());
		final JProgressBar progress = new JProgressBar(0, SimpleTask.PROGRESS_MAX);
		progress.setIndeterminate(this.task.getProgress() == -1);
		
		panel.add(progress, BorderLayout.NORTH);
		panel.add(new JScrollPane(this.console.getOutputField()));
		
		final JDialog dialog = new JDialog(this.parent, this.title, JDialog.DEFAULT_MODALITY_TYPE);
		
		dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		//dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		
		dialog.setContentPane(panel);
		dialog.pack();
		dialog.setLocationRelativeTo(null);
		
		final Timer updates = new Timer(this.updatePeriod, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						progress.setValue(SimpleWaiter.this.task.getProgress());
					}
				});
			}
		});
		updates.start();
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				SimpleWaiter.this.task.run();
				dialog.dispose();
				updates.stop();
			}
		}).start();
		
		dialog.setVisible(true);
	}
}
