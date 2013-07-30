package com.creatifcubed.simpleapi.swing;

import java.awt.BorderLayout;
import java.net.URL;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.Document;

public class SimpleWebsitePanel extends JPanel {

	public final JTextPane browser;

	public SimpleWebsitePanel() {
		super(new BorderLayout());

		this.browser = new JTextPane();
		this.browser.setEditable(false);
		this.browser.setMargin(null);
		this.browser.setContentType("text/html");

		this.browser.addHyperlinkListener(new SimpleHyperlinkListener("Unable to open %s"));

		JScrollPane scroll = new JScrollPane(this.browser);
		this.add(scroll, BorderLayout.CENTER);
	}

	public SimpleWebsitePanel(String url) {
		this();
		this.setPage(url);
	}

	public void setPage(final String url) {
		// Refresh
		Document doc = this.browser.getDocument();
		doc.putProperty(Document.StreamDescriptionProperty, null);
		
		SimpleWebsitePanel.this.browser.setText(String.format("<html><body><h1>Loading %s...</h1></body></html>", url));
		try {
			SimpleWebsitePanel.this.browser.setPage(url);
		} catch (Exception ex) {
			ex.printStackTrace();
			SimpleWebsitePanel.this.browser.setText(String.format("<html><body><h1>Failed to load page %s</h1><br>Error: " + ex.toString()
					+ "</body></html>", url));
		}
	}
}
