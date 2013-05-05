/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.creatifcubed.simpleapi.swing;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

/**
 * 
 * @author Adrian
 */
public class SimpleLinkedLabel extends JEditorPane {

	public SimpleLinkedLabel(String text, String link) {
		this.setContentType("text/html");
		this.setEditable(false);
		this.setText(String.format("<html><a href=\"%s\">%s</a></html>", link, text));
		this.setBackground(new Color(0, 0, 0, 0));
		this.setToolTipText(link);

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

		this.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				SimpleLinkedLabel.this.setCursor(new Cursor(Cursor.HAND_CURSOR));
			}

			@Override
			public void mouseExited(MouseEvent e) {
				SimpleLinkedLabel.this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}
		});
	}
}
