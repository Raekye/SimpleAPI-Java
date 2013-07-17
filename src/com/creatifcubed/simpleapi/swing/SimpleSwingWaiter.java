package com.creatifcubed.simpleapi.swing;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.PrintStream;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.WindowConstants;


public class SimpleSwingWaiter implements Runnable {
	public String title;
	public Window parent;
	public Worker worker;
	private final SimpleGUIConsole console;
	private JDialog dialog;
	public String doneMessage;

	public SimpleSwingWaiter(String title) {
		this(title, null);
	}
	public SimpleSwingWaiter(String title, Window parent) {
		this.title = title;
		this.parent = parent;
		this.dialog = null;
		this.worker = null;
		this.doneMessage = null;
		this.console = new SimpleGUIConsole();
		this.console.init();
	}

	public PrintStream stdout() {
		return this.console.getOut();
	}
	public PrintStream stderr() {
		return this.console.getErr();
	}

	private void done() {
		if (this.doneMessage != null) {
			JOptionPane.showMessageDialog(this.dialog, this.doneMessage);
		}
		this.dialog.dispose();
		this.dialog = null;
	}

	private void cancel() {
		if (this.worker.isCancellable()) {
			if (JOptionPane.showConfirmDialog(this.dialog, "Are you sure you want to cancel this task?") == JOptionPane.YES_OPTION) {
				this.worker.cancel();
			}
		} else {
			JOptionPane.showMessageDialog(this.dialog, "This task cannot be cancelled.");
		}
	}

	@Override
	public void run() {
		this.dialog = new JDialog(this.parent, this.title, JDialog.DEFAULT_MODALITY_TYPE);
		this.dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		this.dialog.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent event) {
				SimpleSwingWaiter.this.cancel();
			}
		});

		JPanel panel = new JPanel(new BorderLayout());
		final JEditorPane consoleField = this.console.getOutputField();
		SimpleSwingUtils.setAutoscroll(consoleField, true);
		JPanel topPanel = new JPanel(new BorderLayout());
		final JProgressBar progress = new JProgressBar();
		progress.setIndeterminate(this.worker.isIndeterminate());
		final JCheckBox autoscroll = new JCheckBox("Autoscroll", true);
		autoscroll.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				SimpleSwingUtils.setAutoscroll(consoleField, autoscroll.isSelected());
			}
		});

		topPanel.add(progress, BorderLayout.CENTER);
		panel.add(topPanel, BorderLayout.NORTH);
		panel.add(new JScrollPane(consoleField), BorderLayout.CENTER);
		panel.add(autoscroll, BorderLayout.SOUTH);
		
		if (this.worker.isCancellable()) {
			JButton cancel = new JButton("Cancel");
			cancel.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent event) {
					SimpleSwingWaiter.this.cancel();
				}
			});
			topPanel.add(cancel, BorderLayout.EAST);
			
			this.worker.addPropertyChangeListener(new PropertyChangeListener() {
				@Override
				public void propertyChange(PropertyChangeEvent event) {
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							progress.setValue(SimpleSwingWaiter.this.worker.getProgress());
						}
					});
				}
			});
		}

		this.dialog.setContentPane(panel);
		this.dialog.setPreferredSize(new Dimension(300, 200));
		this.dialog.pack();
		this.dialog.setLocationRelativeTo(null);

		this.worker.execute();
		dialog.setVisible(true);
	}

	public static abstract class Worker extends SwingWorker<Void, Void> {
		private final SimpleSwingWaiter controller;
		public Worker(SimpleSwingWaiter controller) {
			this.controller = controller;
		}
		public void cancel() {
			super.cancel(true);
		}
		public boolean isIndeterminate() {
			return true;
		}
		public boolean isCancellable() {
			return true;
		}
		@Override
		public void done() {
			this.controller.done();
		}
	}
}
