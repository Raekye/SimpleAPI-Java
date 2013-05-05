/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.creatifcubed.simpleapi.swing;

import java.awt.Color;
import java.awt.Desktop;

import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

/**
 * 
 * @author Adrian
 */
public class SimpleLinkableLabel extends JEditorPane {

	public SimpleLinkableLabel() {
		this.addHyperlinkListener(new HyperlinkListener() {
			@Override
			public void hyperlinkUpdate(HyperlinkEvent e) {
				if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
					try {
						Desktop.getDesktop().browse(e.getURL().toURI());
					} catch (Exception ignore) {
						JOptionPane.showMessageDialog(null, "Open link: " + e.getURL().toString());
					}
				}
			}
		});
		this.setContentType("text/html");
		this.setEditable(false);
		this.setBackground(new Color(0, 0, 0, 0));
	}

	public SimpleLinkableLabel(String html) {
		this();
		this.setText("<html>" + html + "</html>");
	}

	public SimpleLinkableLabel(String html, int width) {
		this();
		this.setText(String.format("<html><div style=\"width: %d;\">%s</div></html>", width, html));
	}
}
