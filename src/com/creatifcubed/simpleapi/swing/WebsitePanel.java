package com.creatifcubed.simpleapi.swing;

import java.awt.BorderLayout;
import java.net.URL;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;

public class WebsitePanel extends JPanel {
	
	public final JTextPane browser;
	
	public WebsitePanel() {
		super(new BorderLayout());
		
		this.browser = new JTextPane();
		this.browser.setEditable(false);
		this.browser.setMargin(null);
		this.browser.setContentType("text/html");
		
		JScrollPane scroll = new JScrollPane(this.browser);
		this.add(scroll, BorderLayout.CENTER);
	}
	
	public WebsitePanel(String url) {
		this();
		this.setPage(url);
	}
	
	public void setPage(final String url) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				WebsitePanel.this.browser.setText(String.format("<html><body><h1>Loading %s...</h1></body></html>", url));
				try {
					WebsitePanel.this.browser.setPage(url);
				} catch (Exception ex) {
					ex.printStackTrace();
					WebsitePanel.this.browser.setText(String.format("<html><body><h1>Failed to load page %s</h1><br>Error: " + ex.toString()
							+ "</body></html>", url));
				}
			}
		});
	}
}
