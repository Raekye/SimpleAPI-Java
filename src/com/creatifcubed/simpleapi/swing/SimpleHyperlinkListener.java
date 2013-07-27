package com.creatifcubed.simpleapi.swing;

import javax.swing.JOptionPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import com.creatifcubed.simpleapi.SimpleUtils;

public class SimpleHyperlinkListener implements HyperlinkListener {
	private final String errorMsg;
	public SimpleHyperlinkListener(String errorMsg) {
		this.errorMsg = errorMsg;
	}
	
	@Override
	public void hyperlinkUpdate(HyperlinkEvent e) {
		if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
			if (SimpleUtils.openLink(e.getURL())) {
				return;
			}
			JOptionPane.showMessageDialog(null, String.format(this.errorMsg, e.getURL().toString()));
		}
	}
}
