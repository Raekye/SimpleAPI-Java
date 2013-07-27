package com.creatifcubed.simpleapi.swing;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.Image;
import java.awt.Toolkit;
import java.net.URISyntaxException;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.JTextComponent;

import com.creatifcubed.simpleapi.SimpleResources;
import com.creatifcubed.simpleapi.SimpleUtils;

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
	
	public static <T extends JTextComponent> T scrollToEnd(T textComponent) {
		textComponent.setCaretPosition(textComponent.getDocument().getLength());
		return textComponent;
	}
	
	public static void setIcon(JFrame frame, String path) {
		URL iconURL = SimpleResources.loadAsURL(path);
		Toolkit kit = Toolkit.getDefaultToolkit();
		Image img = kit.createImage(iconURL);
		frame.setIconImage(img);
	}
	
	public static class AutoscrollListener implements DocumentListener {
		private final JTextComponent component;
		public AutoscrollListener(JTextComponent component) {
			this.component = component;
		}
		@Override
		public void changedUpdate(DocumentEvent event) {
			return;
		}

		@Override
		public void insertUpdate(DocumentEvent event) {
			scrollToEnd(this.component);
		}

		@Override
		public void removeUpdate(DocumentEvent event) {
			scrollToEnd(this.component);
		}
		
		@Override
		public int hashCode() {
			return this.component.hashCode();
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
