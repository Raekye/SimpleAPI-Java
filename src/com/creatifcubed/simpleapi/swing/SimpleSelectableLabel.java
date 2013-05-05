/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.creatifcubed.simpleapi.swing;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JEditorPane;

/**
 * 
 * @author Adrian
 */
public class SimpleSelectableLabel extends JEditorPane {

	public SimpleSelectableLabel(String text) {
		this.setContentType("text/html");
		this.setEditable(false);
		this.setText(text);
		this.setBackground(new Color(0, 0, 0, 0));

		this.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				SimpleSelectableLabel.this.setCursor(new Cursor(Cursor.TEXT_CURSOR));
			}

			@Override
			public void mouseExited(MouseEvent e) {
				SimpleSelectableLabel.this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}
		});
	}
}
