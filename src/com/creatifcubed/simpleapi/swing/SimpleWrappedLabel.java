/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.creatifcubed.simpleapi.swing;

import javax.swing.JLabel;

/**
 * 
 * @author Adrian
 */
public class SimpleWrappedLabel extends JLabel {

	public SimpleWrappedLabel(String text, int width) {
		super(String.format("<html><div style=\"width: %d;\">%s</div></html>", width, text));
	}

	public void wrapText(String text, int width) {
		this.setText(String.format("<html><div style=\"width: %d;\">%s</div></html>", width, text));
	}
}
