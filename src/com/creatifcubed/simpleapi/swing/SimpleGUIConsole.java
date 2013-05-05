package com.creatifcubed.simpleapi.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Logger;

import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import com.creatifcubed.simpleapi.SimpleStringOutputStreamAdapter;

public class SimpleGUIConsole {

	public static final String DEFAULT_ENCODING = Charset.defaultCharset().name();
	public static final String DEFAULT_FONT_NAME = "Courier New";
	public static final Font DEFAULT_FONT = new Font(DEFAULT_FONT_NAME, Font.PLAIN, 12);
	public static final int DEFAULT_WORDWRAP_INDENT = 20;
	public static final String DEFAULT_NEWLINE_PREFIX = "> ";
	public static final Color DEFAULT_OUT_COLOUR = Color.BLACK;
	public static final Color DEFAULT_ERR_COLOUR = Color.RED;
	public static final Color DEFAULT_IN_COLOUR = Color.BLUE;
	public static final Color DEFAULT_NEWLINE_COLOUR = Color.BLACK;
	public static final String STYLE_OUT_CLASS = "out";
	public static final String STYLE_ERR_CLASS = "err";
	public static final String STYLE_IN_CLASS = "in";
	public static final String STYLE_NEWLINE_CLASS = "newline";
	public static final String STYLE_INDENT = "indent";

	private final PrintStream out;
	private final PrintStream err;
	private final InputStream in;
	public final String encoding;
	private final ReentrantReadWriteLock outputLock;
	private final ReentrantReadWriteLock inputLock;
	private final JEditorPane output;
	private final JTextField input;
	private String inputBuffer;
	private volatile int inputBufferPos;
	private volatile int inputBufferMark;
	private volatile boolean shouldRead;
	private volatile boolean isClosed;
	private final LinkedList<String> previousCommands;
	private ListIterator<String> previousCommandsIterator;
	private volatile boolean reachedEnd;
	private String inputTmpBuffer;
	private volatile boolean islastInputScrollActionUp;
	private volatile int wordwrapIndent;
	private volatile String newLinePrefix;
	
	/* CONSTRUCTORS */
	public SimpleGUIConsole() {
		this(DEFAULT_ENCODING);
	}
	public SimpleGUIConsole(JEditorPane out) {
		this(DEFAULT_ENCODING, out, null);
	}
	public SimpleGUIConsole(String encoding) {
		this(encoding, null, null);
	}
	public SimpleGUIConsole(JTextField in) {
		this(DEFAULT_ENCODING, null, in);
	}
	public SimpleGUIConsole(String encoding, JEditorPane out) {
		this(encoding, out, null);
	}
	public SimpleGUIConsole(JEditorPane out, JTextField in) {
		this(DEFAULT_ENCODING, out, in);
	}
	public SimpleGUIConsole(String encoding, JTextField in) {
		this(DEFAULT_ENCODING, null, in);
	}
	public SimpleGUIConsole(String encoding, JEditorPane out, JTextField in) {
		// Checks
		Charset.forName(encoding);
		
		// Simple
		this.encoding = encoding;
		this.in = new SystemIn();
		this.outputLock = new ReentrantReadWriteLock();
		this.inputLock = new ReentrantReadWriteLock();
		this.inputBuffer = "";
		this.inputBufferPos = 0;
		this.inputBufferMark = -1;
		this.shouldRead = false;
		this.isClosed = false;
		this.previousCommands = new LinkedList<String>();
		this.previousCommandsIterator = this.previousCommands.listIterator();
		this.reachedEnd = true;
		this.inputTmpBuffer = "";
		this.islastInputScrollActionUp = true;
		this.wordwrapIndent = DEFAULT_WORDWRAP_INDENT;
		this.newLinePrefix = DEFAULT_NEWLINE_PREFIX;
		
		// Not simple
		PrintStream tmpOut = null;
		try {
			tmpOut = new PrintStream(new SystemOut(), true, this.encoding);
		} catch (UnsupportedEncodingException ex) {
			throw new RuntimeException(ex);
		}
		this.out = tmpOut;
		PrintStream tmpErr = null;
		try {
			tmpErr = new PrintStream(new SystemErr(), true, this.encoding);
		} catch (UnsupportedEncodingException ex) {
			throw new RuntimeException(ex);
		}
		this.err = tmpErr;
		
		if (out == null) {
			out = this.createDefaultOutputField();
		}
		this.output = out;
		if (in == null) {
			in = this.createDefaultInputField();
		}
		this.input = in;
	}
	
	/* PUBLIC USEABILITY METHODS */
	public PrintStream getOut() {
		return this.out;
	}
	public PrintStream getErr() {
		return this.err;
	}
	public InputStream getIn() {
		return this.in;
	}

	public JEditorPane getOutputField() {
		return this.output;
	}
	public JTextField getInputField() {
		return this.input;
	}
	public JPanel getCompleteConsole() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(new JScrollPane(this.getOutputField(), ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED), BorderLayout.CENTER);
		panel.add(this.getInputField(), BorderLayout.SOUTH);
		return panel;
	}
	
	/* PUBLIC OPTION METHODS */
	public Style getStyle(String classname) {
		Document doc = this.output.getDocument();
		if (doc instanceof StyledDocument) {
			StyledDocument styledDoc = (StyledDocument) doc;
			return styledDoc.getStyle(classname);
		}
		return null;
	}
	public Style addStyle(String classname, Style parent) {
		Document doc = this.output.getDocument();
		if (doc instanceof StyledDocument) {
			StyledDocument styledDoc = (StyledDocument) doc;
			return styledDoc.addStyle(classname, parent);
		}
		return null;
	}
	
	public void setWordWrapIndent(int pixels) {
		this.wordwrapIndent = pixels;
	}
	public void setNewlinePrefix(String prefix) {
		this.newLinePrefix = prefix;
	}
	public int getWordWrapIndent() {
		return this.wordwrapIndent;
	}
	public String getNewlinePrefix() {
		return this.newLinePrefix;
	}
	public void setFont(Font f) {
		this.output.setFont(f);
	}
	public Font getFont(Font f) {
		return this.output.getFont();
	}

	public void setOutColour(Color colour) {
		Style style = this.getStyle(STYLE_OUT_CLASS);
		if (style != null) {
			StyleConstants.setForeground(style, colour);
		}
	}
	public Color getOutColour() {
		Style style = this.getStyle(STYLE_OUT_CLASS);
		if (style != null) {
			return StyleConstants.getForeground(style);
		}
		return null;
	}
	public void setErrColour(Color colour) {
		Style style = this.getStyle(STYLE_ERR_CLASS);
		if (style != null) {
			StyleConstants.setForeground(style, colour);
		}
	}
	public Color getErrColour() {
		Style style = this.getStyle(STYLE_ERR_CLASS);
		if (style != null) {
			return StyleConstants.getForeground(style);
		}
		return null;
	}
	public void setInColour(Color colour) {
		Style style = this.getStyle(STYLE_IN_CLASS);
		if (style != null) {
			StyleConstants.setForeground(style, colour);
		}
	}
	public Color getInColour() {
		Style style = this.getStyle(STYLE_IN_CLASS);
		if (style != null) {
			return StyleConstants.getForeground(style);
		}
		return null;
	}
	public void setNewlineColour(Color colour) {
		Style style = this.getStyle(STYLE_NEWLINE_CLASS);
		if (style != null) {
			StyleConstants.setForeground(style, colour);
		}
	}
	public Color getNewlineColour() {
		Style style = this.getStyle(STYLE_NEWLINE_CLASS);
		if (style != null) {
			return StyleConstants.getForeground(style);
		}
		return null;
	}
	
	/* PUBLIC BRIDGE METHODS */
	public String getInputHistoryUp() {
		this.inputLock.writeLock().lock();
		try {
			if (!this.islastInputScrollActionUp) {
				if (this.previousCommandsIterator.hasNext()) {
					this.previousCommandsIterator.next();
				}
			}
			this.islastInputScrollActionUp = true;
			if (this.previousCommandsIterator.hasNext()) {
				return this.previousCommandsIterator.next();
			}
		} finally {
			this.inputLock.writeLock().unlock();
		}
		return null;
	}

	public String getInputHistoryDown() {
		this.inputLock.writeLock().lock();
		try {
			if (this.islastInputScrollActionUp) {
				if (this.previousCommandsIterator.hasPrevious()) {
					this.previousCommandsIterator.previous();
				}
			}
			if (this.previousCommandsIterator.hasPrevious()) {
				this.islastInputScrollActionUp = false;
				return this.previousCommandsIterator.previous();
			} else {
				this.islastInputScrollActionUp = true;
				return this.inputTmpBuffer;
			}
		} finally {
			this.inputLock.writeLock().unlock();
		}
	}

	public void setInputTmpBuffer(String current) {
		this.inputLock.writeLock().lock();
		try {
			this.inputTmpBuffer = current;
		} finally {
			this.inputLock.writeLock().unlock();
		}
	}

	public String getInputTmpBuffer(String current) {
		this.inputLock.readLock().lock();
		try {
			return this.inputTmpBuffer;
		} finally {
			this.inputLock.readLock().unlock();
		}
	}
	
	public void loadDefaultStyles() {
		this.loadDefaultStyles(this.output);
	}
	
	/* PRIVATE BRIDGE METHODS */
	private void writeToOutputField(String flushed, String styleClass) {
		this.outputLock.writeLock().lock();
		try {
			Document doc = this.output.getDocument();
			String[] parts = flushed.split("\n", -1);
			Style style = null;
			Style newlineStyle = null;
			if (doc instanceof StyledDocument) {
				style = ((StyledDocument) doc).getStyle(styleClass);
				newlineStyle = ((StyledDocument) doc).getStyle(STYLE_NEWLINE_CLASS);
			}
			doc.insertString(doc.getLength(), parts[0], style);
			for (int i = 1; i < parts.length; i++) {
				doc.insertString(doc.getLength(), "\n" + this.newLinePrefix, newlineStyle);
				doc.insertString(doc.getLength(), parts[i], style);
			}
		} catch (BadLocationException ex) {
			throw new RuntimeException(ex);
		} finally {
			this.outputLock.writeLock().unlock();
		}
	}

	private void pullToInputBuffer() {
		this.inputLock.writeLock().lock();
		try {
			if (!this.shouldRead) {
				return;
			}
			if (this.reachedEnd) {
				this.inputBuffer += this.input.getText() + "\n";
				this.writeToOutputField(this.input.getText() + "\n", STYLE_IN_CLASS);
				this.previousCommands.addFirst(this.input.getText());
				this.previousCommandsIterator = this.previousCommands.listIterator();
				this.input.setText("");
				this.inputTmpBuffer = "";
				synchronized (this) {
					this.notifyAll();
				}
			}
		} finally {
			this.inputLock.writeLock().unlock();
		}
	}
	
	/* PRIVATE INPUTSTREAM DELEGATE METHODS */
	private void resetInputBuffer() throws IOException {
		this.inputLock.writeLock().lock();
		try {
			this.ensureOpen();
			this.inputBuffer = this.inputBufferMark == -1 ? "" : this.inputBuffer.substring(this.inputBufferMark);
			this.inputBufferPos = 0;
			this.inputBufferMark = -1;
			this.reachedEnd = true;
		} finally {
			this.inputLock.writeLock().unlock();
		}
	}

	private void markInputBuffer(int limit) throws IOException {
		this.inputLock.writeLock().lock();
		try {
			this.ensureOpen();
			this.inputBufferMark = this.inputBufferPos;
		} finally {
			this.inputLock.writeLock().unlock();
		}
	}

	private void closeInputBuffer() throws IOException {
		this.inputLock.writeLock().lock();
		try {
			this.ensureOpen();
			this.resetInputBuffer();
			this.inputBuffer = "\n";
			synchronized (this) {
				this.notifyAll();
			}
			this.isClosed = true;
		} finally {
			this.inputLock.writeLock().unlock();
		}
	}

	private int availableInputBuffer() throws IOException {
		this.inputLock.readLock().lock();
		try {
			this.ensureOpen();
			return this.inputBuffer.length() - this.inputBufferPos;
		} finally {
			this.inputLock.readLock().unlock();
		}
	}

	private long skipInputBuffer(long n) throws IOException {
		this.inputLock.writeLock().lock();
		try {
			this.ensureOpen();
			long delta = Math.min(n, this.availableInputBuffer());
			this.inputBufferPos += delta;
			return delta;
		} finally {
			this.inputLock.writeLock().unlock();
		}
	}

	private int pullFromInputBuffer() throws IOException {
		while (true) {
			this.inputLock.writeLock().lock();
			try {
				this.ensureOpen();
				if (!this.reachedEnd && this.inputBuffer.length() == this.inputBufferPos) {
					this.reachedEnd = true;
					return -1;
				} else if (this.inputBufferPos < this.inputBuffer.length()) {
					this.shouldRead = false;
					this.reachedEnd = false;
					return this.inputBuffer.charAt(this.inputBufferPos++);
				}
			} finally {
				this.inputLock.writeLock().unlock();
			}
			try {
				synchronized (this) {
					this.shouldRead = true;
					this.wait();
				}
			} catch (InterruptedException ex) {
				throw new RuntimeException(ex);
			}
		}
	}
	
	private void ensureOpen() throws IOException {
		if (this.isClosed) {
			throw new IOException("Stream closed");
		}
	}
	
	/* PRIVATE HELPER METHODS */
	private JEditorPane createDefaultOutputField() {
		return this.configureOutputField(new JTextPane());
	}

	private JTextField createDefaultInputField() {
		return this.configureInputField(new JTextField());
	}

	private JEditorPane configureOutputField(final JEditorPane outputField) {
		outputField.setEditable(false);
		this.loadDefaultStyles(outputField);
		return outputField;
	}
	private void loadDefaultStyles(JEditorPane outputField) {
		outputField.setFont(DEFAULT_FONT);
		Document doc = outputField.getDocument();
		if (doc instanceof StyledDocument) {
			StyledDocument styledDoc = (StyledDocument) doc;

			Style outStyle = styledDoc.addStyle(STYLE_OUT_CLASS, null);
			StyleConstants.setForeground(outStyle, DEFAULT_OUT_COLOUR);

			Style errStyle = styledDoc.addStyle(STYLE_ERR_CLASS, null);
			StyleConstants.setForeground(errStyle, DEFAULT_ERR_COLOUR);

			Style inStyle = styledDoc.addStyle(STYLE_IN_CLASS, null);
			StyleConstants.setForeground(inStyle, DEFAULT_IN_COLOUR);
			
			Style newlineStyle = styledDoc.addStyle(STYLE_NEWLINE_CLASS, null);
			StyleConstants.setForeground(newlineStyle, DEFAULT_NEWLINE_COLOUR);
			
			Style indentStyle = styledDoc.addStyle(STYLE_INDENT, null);
			StyleConstants.setLeftIndent(indentStyle, this.wordwrapIndent);
			StyleConstants.setFirstLineIndent(indentStyle, -this.wordwrapIndent);
			styledDoc.setParagraphAttributes(0, 0, indentStyle, false);
		}
	}

	private JTextField configureInputField(final JTextField inputField) {
		inputField.setFont(DEFAULT_FONT);
		inputField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				SimpleGUIConsole.this.pullToInputBuffer();
			}
		});
		inputField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent event) {
				if (event.getKeyCode() == KeyEvent.VK_UP) {
					SimpleGUIConsole.this.inputLock.writeLock().lock();
					try {
						String next = SimpleGUIConsole.this.getInputHistoryUp();
						if (next != null) {
							inputField.setText(next);
						}
					} finally {
						SimpleGUIConsole.this.inputLock.writeLock().unlock();
					}
				} else if (event.getKeyCode() == KeyEvent.VK_DOWN) {
					SimpleGUIConsole.this.inputLock.writeLock().lock();
					try {
						inputField.setText(SimpleGUIConsole.this.getInputHistoryDown());
					} finally {
						SimpleGUIConsole.this.inputLock.writeLock().unlock();
					}
				} else {
					SimpleGUIConsole.this.inputLock.writeLock().lock();
					try {
						SimpleGUIConsole.this.inputTmpBuffer = inputField.getText();
					} finally {
						SimpleGUIConsole.this.inputLock.writeLock().unlock();
					}
				}
			}
		});
		return inputField;
	}

	/* PRIVATE CLASSES */
	private class SystemOut extends SimpleStringOutputStreamAdapter {
		public SystemOut() {
			super(SimpleGUIConsole.this.encoding, new Listener() {
				@Override
				public void onString(String flushed) {
					SimpleGUIConsole.this.writeToOutputField(flushed, STYLE_OUT_CLASS);
				}
			});
		}
	}

	private class SystemErr extends SimpleStringOutputStreamAdapter {
		public SystemErr() {
			super(SimpleGUIConsole.this.encoding, new Listener() {
				@Override
				public void onString(String flushed) {
					SimpleGUIConsole.this.writeToOutputField(flushed, STYLE_ERR_CLASS);
				}
			});
		}
	}

	private class SystemIn extends InputStream {
		@Override
		public int read() throws IOException {
			return SimpleGUIConsole.this.pullFromInputBuffer();
		}
		@Override
		public int available() throws IOException {
			return SimpleGUIConsole.this.availableInputBuffer();
		}
		@Override
		public void reset() throws IOException {
			SimpleGUIConsole.this.resetInputBuffer();
		}
		@Override
		public void mark(int limit) {
			try {
				SimpleGUIConsole.this.markInputBuffer(limit);
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		@Override
		public void close() throws IOException {
			SimpleGUIConsole.this.closeInputBuffer();
		}
		@Override
		public long skip(long n) throws IOException {
			return SimpleGUIConsole.this.skipInputBuffer(n);
		}
		
		@Override
		public boolean markSupported() {
			return true;
		}
	}
	
//	private class PrintStreamAdapter extends PrintStream {
//		public PrintStreamAdapter(OutputStream out) throws UnsupportedEncodingException {
//			super(out, true, SimpleGUIConsole.this.encoding);
//		}
//		
//		@Override
//		public void println() {
//			super.println();
//			this.onNewLine();
//		}
//		@Override
//		public void println(Object o) {
//			super.println(o);
//			this.onNewLine();
//		}
//		@Override
//		public void println(char o) {
//			super.println(o);
//			this.onNewLine();
//		}
//		@Override
//		public void println(char[] o) {
//			super.println(o);
//			this.onNewLine();
//		}
//		@Override
//		public void println(String o) {
//			super.println(o);
//			this.onNewLine();
//		}
//		@Override
//		public void println(boolean o) {
//			super.println(o);
//			this.onNewLine();
//		}
//		@Override
//		public void println(int o) {
//			super.println(o);
//			this.onNewLine();
//		}
//		@Override
//		public void println(float o) {
//			super.println(o);
//			this.onNewLine();
//		}
//		@Override
//		public void println(double o) {
//			super.println(o);
//			this.onNewLine();
//		}
//		@Override
//		public void println(long o) {
//			super.println(o);
//			this.onNewLine();
//		}
//		
//		private void onNewLine() {
//			this.print(SimpleGUIConsole.this.newLinePrefix);
//		}
//	}
}