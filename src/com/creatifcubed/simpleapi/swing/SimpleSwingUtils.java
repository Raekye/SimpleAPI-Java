package com.creatifcubed.simpleapi.swing;

import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;

public class SimpleSwingUtils {
	private SimpleSwingUtils() {
		return;
	}

	public static Border createLineBorder(String heading) {
		return BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), heading);
	}
	
	public static boolean setSystemLookAndFeel() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			return true;
		} catch (ClassNotFoundException ex) {
			ex.printStackTrace();
		} catch (InstantiationException ex) {
			ex.printStackTrace();
		} catch (IllegalAccessException ex) {
			ex.printStackTrace();
		} catch (UnsupportedLookAndFeelException ex) {
			ex.printStackTrace();
		}
		return false;
	}
	
	public static <T extends JTextComponent> T setAutoscroll(T textComponent, boolean autoscroll) {
		if (autoscroll) {
			textComponent.getDocument().addDocumentListener(new AutoscrollListener(textComponent));
		} else {
			textComponent.getDocument().removeDocumentListener(new AutoscrollListener(textComponent));
		}
		return textComponent;
	}
	
	private static class AutoscrollListener implements DocumentListener {
		private final JTextComponent component;
		public AutoscrollListener(JTextComponent component) {
			this.component = component;
		}
		@Override
		public void changedUpdate(DocumentEvent event) {
			this.component.setCaretPosition(event.getDocument().getLength());
		}

		@Override
		public void insertUpdate(DocumentEvent event) {
			return;
		}

		@Override
		public void removeUpdate(DocumentEvent event) {
			return;
		}
		
		@Override
		public int hashCode() {
			return 0;
		}
		
		@Override
		public boolean equals(Object o) {
			if (o instanceof AutoscrollListener) {
				return this.component.equals(((AutoscrollListener) o).component);
			}
			return false;
		}
	}
}
