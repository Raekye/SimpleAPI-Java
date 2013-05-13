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

import com.creatifcubed.simpleapi.SimpleException;
import com.creatifcubed.simpleapi.SimpleStringOutputStreamAdapter;

/**
 * 
 * @author Adrian
 * @version 1.0.0
 * A wrapper class to simplify redirecting stdout and stdin to/from JTextComponents
 * 
 */
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
	
	public final String encoding;
	private final PrintStream out;
	private final PrintStream err;
	private final InputStream in;
	private final ReentrantReadWriteLock outputLock;
	private final ReentrantReadWriteLock inputLock;
	private OutputBridge output;
	private InputBridge input;
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
	/**
	 * Creates a new SimpleGUIConsole with the default encoding from Charset.defaultCharset()
	 * @see java.nio.charset.Charset#defaultCharset()
	 */
	public SimpleGUIConsole() {
		this(DEFAULT_ENCODING);
	}
	/**
	 * Creates a new SimpleGUIConsole with the specified encoding
	 * @param encoding One of {@link java.nio.charset.Charset#name()). If null, this will be {@link java.nio.charset.Charset#defaultCharset()}
	 * @throws IllegalStateException If the output or input bridge have already been attached to another SimpleGUIConsole
	 * @throws IllegalCharsetNameException If the given charset name is illegal
	 * @throws UnsupportedCharsetException If no support for the named charset is available in this instance of the Java virtual machine
	 * @see java.nio.charset.Charset#forName(String)
	 */
	public SimpleGUIConsole(String encoding) {
		// Checks
		if (encoding == null) {
			encoding = Charset.defaultCharset().name();
		}
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
		this.input = null;
		this.output = null;

		// Not simple
		PrintStream tmpOut = null;
		try {
			tmpOut = new PrintStream(new SystemOut(), true, this.encoding);
		} catch (UnsupportedEncodingException ex) {
			throw new SimpleException(ex);
		}
		this.out = tmpOut;
		PrintStream tmpErr = null;
		try {
			tmpErr = new PrintStream(new SystemErr(), true, this.encoding);
		} catch (UnsupportedEncodingException ex) {
			throw new SimpleException(ex);
		}
		this.err = tmpErr;
	}
	/**
	 * Initializes with the bridges SimpleGUIConsole.OutputAdapter and SimpleGUIConsole.InputAdapter
	 * @return true if the SimpleGUIConsole actually initializes (has not already been initialized)
	 */
	public boolean init() {
		return this.init(null, null);
	}
	/**
	 * Initializes console
	 * @param output The output bridge. If null, it will default to SimpleGUIConsole.OutputAdapter
	 * @param input The input bridge. If null, it will default to SimpleGUIConsole.InputAdapter
	 * @return true if the SimpleGUIConsole actually initializes (has not already been initialized)
	 */
	public synchronized boolean init(OutputBridge output, InputBridge input) {
		// Bridges
		if (output == null) {
			output = new OutputAdapter(this);
		}
		if (input == null) {
			input = new InputAdapter(this);
		}
		if (this.input != null || this.output != null) {
			return false;
		}
		this.output = output;
		this.input = input;
		
		// Other init
		this.writeToOutputField(this.getNewlinePrefix(), DEFAULT_NEWLINE_PREFIX);
		return true;
	}

	/* PUBLIC USEABILITY METHODS */
	/**
	 * @return This object's redirected out
	 */
	public PrintStream getOut() {
		return this.out;
	}
	/**
	 * @return This object's redirected err
	 */
	public PrintStream getErr() {
		return this.err;
	}
	/**
	 * 
	 * @return This object's redirected in
	 */
	public InputStream getIn() {
		return this.in;
	}
	
	/**
	 * @return The JEditorPane which this object's out is being redirected to
	 */
	public JEditorPane getOutputField() {
		return this.output.getComponent();
	}
	/**
	 * @return The JTextField from which this object is receiving data for in
	 */
	public JTextField getInputField() {
		return this.input.getComponent();
	}
	/**
	 * Creates a default setup for a GUI console:
	 * - JPanel with a BorderLayout manager
	 * - The JEditorPane out in BorderLayout.CENTER
	 * - The JTextField in in BorderLayout.SOUTH
	 * @return console JPanel
	 */
	public JPanel getCompleteConsole() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(new JScrollPane(this.getOutputField(), ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED), BorderLayout.CENTER);
		panel.add(this.getInputField(), BorderLayout.SOUTH);
		return panel;
	}

	/* PUBLIC OPTION METHODS */
	/**
	 * @param classname Gets the style class associated with this object's out's document
	 * @return The Style from {@link javax.swing.text.StyledDocument#getStyle(String)} or null if the document is not a StyledDocument
	 */
	public Style getStyle(String classname) {
		Document doc = this.getOutputField().getDocument();
		if (doc instanceof StyledDocument) {
			StyledDocument styledDoc = (StyledDocument) doc;
			return styledDoc.getStyle(classname);
		}
		return null;
	}
	/**
	 * @param classname Style name
	 * @param parent Style parent (null for new style)
	 * @return The newly created style
	 * @see javax.swing.text.StyledDocument#addStyle(String, Style)
	 */
	public Style addStyle(String classname, Style parent) {
		Document doc = this.getOutputField().getDocument();
		if (doc instanceof StyledDocument) {
			StyledDocument styledDoc = (StyledDocument) doc;
			return styledDoc.addStyle(classname, parent);
		}
		return null;
	}
	
	/**
	 * Sets the word wrap indent if this OutputBridge's JEditorPane supports it
	 * @param pixels Pixels offset
	 */
	public void setWordWrapIndent(int pixels) {
		this.wordwrapIndent = pixels;
	}
	/**
	 * @param prefix The prefix to append every '\n' in this OutputBridge's JEditorPane
	 */
	public void setNewlinePrefix(String prefix) {
		this.newLinePrefix = prefix;
	}
	/**
	 * @return This object's word wrap indent
	 */
	public int getWordWrapIndent() {
		return this.wordwrapIndent;
	}
	/**
	 * @return This object's newline prefix
	 */
	public String getNewlinePrefix() {
		return this.newLinePrefix;
	}
	/**
	 * @param f The font for this object's OutputBridge's JEditorPane
	 * @see javax.swing.JComponent#setFont(Font)
	 */
	public void setFont(Font f) {
		this.getOutputField().setFont(f);
	}
	/**
	 * @return This object's OutputBridge's JEditorPane's font
	 */
	public Font getFont() {
		return this.getOutputField().getFont();
	}
	
	/**
	 * @param colour The new colour for this object's OutputBridge's JEditorPane STYLE_OUT_CLASS style
	 */
	public void setOutColour(Color colour) {
		Style style = this.getStyle(STYLE_OUT_CLASS);
		if (style != null) {
			StyleConstants.setForeground(style, colour);
		}
	}
	/**
	 * @return The colour for this object's OutputBridge's JEditorPane STYLE_OUT_CLASS style
	 */
	public Color getOutColour() {
		Style style = this.getStyle(STYLE_OUT_CLASS);
		if (style != null) {
			return StyleConstants.getForeground(style);
		}
		return null;
	}
	/**
	 * @param colour The new colour for this object's OutputBridge's JEditorPane STYLE_ERR_CLASS style
	 */
	public void setErrColour(Color colour) {
		Style style = this.getStyle(STYLE_ERR_CLASS);
		if (style != null) {
			StyleConstants.setForeground(style, colour);
		}
	}
	/**
	 * @return The colour for this object's OutputBridge's JEditorPane STYLE_ERR_CLASS style
	 */
	public Color getErrColour() {
		Style style = this.getStyle(STYLE_ERR_CLASS);
		if (style != null) {
			return StyleConstants.getForeground(style);
		}
		return null;
	}
	/**
	 * @param colour The new colour for this object's OutputBridge's JEditorPane STYLE_IN_CLASS style
	 */
	public void setInColour(Color colour) {
		Style style = this.getStyle(STYLE_IN_CLASS);
		if (style != null) {
			StyleConstants.setForeground(style, colour);
		}
	}
	/**
	 * @return The colour for this object's OutputBridge's JEditorPane STYLE_IN_CLASS style
	 */
	public Color getInColour() {
		Style style = this.getStyle(STYLE_IN_CLASS);
		if (style != null) {
			return StyleConstants.getForeground(style);
		}
		return null;
	}
	/**
	 * @param colour The new colour for this object's OutputBridge's JEditorPane STYLE_NEWLINE_CLASS style
	 */
	public void setNewlineColour(Color colour) {
		Style style = this.getStyle(STYLE_NEWLINE_CLASS);
		if (style != null) {
			StyleConstants.setForeground(style, colour);
		}
	}
	/**
	 * @return The colour for this object's OutputBridge's JEditorPane STYLE_NEWLINE_CLASS style
	 */
	public Color getNewlineColour() {
		Style style = this.getStyle(STYLE_NEWLINE_CLASS);
		if (style != null) {
			return StyleConstants.getForeground(style);
		}
		return null;
	}

	/* PRIVATE BRIDGE METHODS */
	private void writeToOutputField(String flushed, String styleClass) {
		this.outputLock.writeLock().lock();
		try {
			this.output.onWrite(flushed, styleClass);
		} finally {
			this.outputLock.writeLock().unlock();
		}
	}

	private boolean pullToInputBuffer(String contents) {
		this.inputLock.writeLock().lock();
		try {
			if (this.shouldRead && this.reachedEnd) {
				this.inputBuffer += contents + "\n";
				this.writeToOutputField(contents + "\n", STYLE_IN_CLASS);
				if (!contents.trim().isEmpty()) {
					this.previousCommands.addFirst(contents);
					this.previousCommandsIterator = this.previousCommands.listIterator();
				}
				this.inputTmpBuffer = "";
				synchronized (this) {
					this.notifyAll();
				}
				return true;
			}
			return false;
		} finally {
			this.inputLock.writeLock().unlock();
		}
	}

	private String getInputHistoryUp() {
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

	private String getInputHistoryDown() {
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

	private void setInputTmpBuffer(String current) {
		this.inputLock.writeLock().lock();
		try {
			this.inputTmpBuffer = current;
		} finally {
			this.inputLock.writeLock().unlock();
		}
	}

	private String getInputTmpBuffer() {
		this.inputLock.readLock().lock();
		try {
			return this.inputTmpBuffer;
		} finally {
			this.inputLock.readLock().unlock();
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
				throw new SimpleException(ex);
			}
		}
	}

	private void ensureOpen() throws IOException {
		if (this.isClosed) {
			throw new IOException("Stream closed");
		}
	}

	/* PRIVATE HELPER METHODS */

	/* PUBLIC CLASSES */
	/**
	 * The bridge used to redirect the SimpleGUIConsole's OutputStream to a JEditorPane
	 */
	public static abstract class OutputBridge extends Bridge {
		public OutputBridge(SimpleGUIConsole console) {
			super(console);
		}
		/**
		 * @return The attached JEditorPane
		 */
		public abstract JEditorPane getComponent();
		/**
		 * @param text The String being flushed to out
		 * @param style The style class (i.e. STYLE_OUT_CLASS, STYLE_ERR_CLASS, and STYLE_NEWLINE_CLASS by the default implementation)
		 */
		public abstract void onWrite(String text, String style);
	}
	
	/**
	 * Default implementation of {@link OutputBridge}
	 */
	public static class OutputAdapter extends OutputBridge {
		private final JEditorPane outputField;
		public OutputAdapter(SimpleGUIConsole console) {
			this(console, new JTextPane());
		}
		public OutputAdapter(SimpleGUIConsole console, JEditorPane outputField) {
			super(console);
			this.outputField = outputField;
			configureOutputField(this.outputField);
		}
		@Override
		public void onWrite(String text, String styleClass) {
			if (this.console == null) {
				return;
			}
			try {
				Document doc = this.outputField.getDocument();
				String[] parts = text.split("\n", -1);
				Style style = null;
				Style newlineStyle = null;
				if (doc instanceof StyledDocument) {
					style = ((StyledDocument) doc).getStyle(styleClass);
					newlineStyle = ((StyledDocument) doc).getStyle(STYLE_NEWLINE_CLASS);
				}
				doc.insertString(doc.getLength(), parts[0], style);
				for (int i = 1; i < parts.length; i++) {
					doc.insertString(doc.getLength(), "\n" + this.console.getNewlinePrefix(), newlineStyle);
					doc.insertString(doc.getLength(), parts[i], style);
				}
			} catch (BadLocationException ex) {
				throw new SimpleException(ex);
			}
		}

		@Override
		public JEditorPane getComponent() {
			return this.outputField;
		}

		public static void configureOutputField(JEditorPane outputField) {
			outputField.setEditable(false);
			outputField.setFont(DEFAULT_FONT);
			loadDefaultStyles(outputField);
		}

		public static void loadDefaultStyles(JEditorPane outputField) {
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
				StyleConstants.setLeftIndent(indentStyle, DEFAULT_WORDWRAP_INDENT);
				StyleConstants.setFirstLineIndent(indentStyle, -DEFAULT_WORDWRAP_INDENT);
				styledDoc.setParagraphAttributes(0, 0, indentStyle, false);
			}
		}
	}
	
	/**
	 * The bridge used to send data to the SimpleGUIConsole's InputStream
	 */
	public static abstract class InputBridge extends Bridge {
		public InputBridge(SimpleGUIConsole console) {
			super(console);
		}
		/**
		 * @return The attached JTextField
		 */
		public abstract JTextField getComponent();
		
		/**
		 * Fetches the next entry in the attached SimpleGUIConsole's history, and shifts the internal pointer over one
		 * @return The next entry (earlier) in the attached SimpleGUIConsole's history, null if already at the top
		 * @throws IllegalStateException If called when not attached to a SimpleGUIConsole (not passed to the constructor)
		 * @see com.creatifcubed.simpleapi.swing.SimpleGUIConsole#SimpleGUIConsole(String, OutputBridge, InputBridge)
		 */
		public String getInputHistoryUp() {
			if (this.console == null) {
				throw new IllegalStateException("InputBridge not attached to a SimpleGUIConsole");
			}
			return this.console.getInputHistoryUp();
		}
		/**
		 * Fetches the previous entry in the attached SimpleGUIConsole's history, and shifts the internal pointer over one
		 * @return The previous entry (more recent) in the attached SimpleGUIConsole's history, the temporary input buffer if at the bottom
		 * @throws IllegalStateException If called when not attached to a SimpleGUIConsole (not passed to the constructor)
		 * @see com.creatifcubed.simpleapi.swing.SimpleGUIConsole#SimpleGUIConsole(String, OutputBridge, InputBridge)
		 */
		public String getInputHistoryDown() {
			if (this.console == null) {
				throw new IllegalStateException("InputBridge not attached to a SimpleGUIConsole");
			}
			return this.console.getInputHistoryDown();
		}
		/**
		 * Sends text to flush to the attached SimpleGUIConsole's InputStream
		 * @param Text to flush
		 * @return True if the content was actually flushed, false otherwise
		 * @throws IllegalStateException If called when not attached to a SimpleGUIConsole (not passed to the constructor)
		 * @see com.creatifcubed.simpleapi.swing.SimpleGUIConsole#SimpleGUIConsole(String, OutputBridge, InputBridge)
		 */
		public boolean send(String contents) {
			if (this.console == null) {
				throw new IllegalStateException("InputBridge not attached to a SimpleGUIConsole");
			}
			return this.console.pullToInputBuffer(contents);
		}
		/**
		 * @param current The current text in the input buffer (To be used as the most recent entry in the SimpleGUIConsole's input history)
		 * @throws IllegalStateException If called when not attached to a SimpleGUIConsole (not passed to the constructor)
		 * @see com.creatifcubed.simpleapi.swing.SimpleGUIConsole#SimpleGUIConsole(String, OutputBridge, InputBridge)
		 */
		public void setInputTmpBuffer(String current) {
			if (this.console == null) {
				throw new IllegalStateException("InputBridge not attached to a SimpleGUIConsole");
			}
			this.console.setInputTmpBuffer(current);
		}
		/**
		 * @return The current text in the input buffer (To be used as the most recent entry in the SimpleGUIConsole's input history)
		 * @throws IllegalStateException If called when not attached to a SimpleGUIConsole (not passed to the constructor)
		 * @see com.creatifcubed.simpleapi.swing.SimpleGUIConsole#SimpleGUIConsole(String, OutputBridge, InputBridge)
		 */
		public String getInputTmpBuffer() {
			if (this.console == null) {
				throw new IllegalStateException("InputBridge not attached to a SimpleGUIConsole");
			}
			return this.console.getInputTmpBuffer();
		}
	}
	
	/**
	 * Default implementation of {@link InputBridge}
	 */
	public static class InputAdapter extends InputBridge {
		private final JTextField inputField;
		public InputAdapter(SimpleGUIConsole console) {
			this(console, new JTextField());
		}
		public InputAdapter(SimpleGUIConsole console, JTextField inputField) {
			super(console);
			this.inputField = inputField;
			configureInputField(this, this.inputField);
		}

		@Override
		public JTextField getComponent() {
			return this.inputField;
		}

		public static void configureInputField(final InputBridge self, final JTextField inputField) {
			inputField.setFont(DEFAULT_FONT);
			inputField.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent event) {
					if (self.send(inputField.getText())) {
						inputField.setText("");
					}
				}
			});
			inputField.addKeyListener(new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent event) {
					if (event.getKeyCode() == KeyEvent.VK_UP) {
						String next = self.getInputHistoryUp();
						if (next != null) {
							inputField.setText(next);
						}
					} else if (event.getKeyCode() == KeyEvent.VK_DOWN) {
						inputField.setText(self.getInputHistoryDown());
					} else {
						self.setInputTmpBuffer(inputField.getText());
					}
				}
			});
		}

	}

	/* PRIVATE CLASSES */
	private static abstract class Bridge {
		public final SimpleGUIConsole console;
		public Bridge(SimpleGUIConsole console) {
			if (console == null) {
				throw new NullPointerException("Param 0 (SimpleGUIConsole) is null");
			}
			this.console = console;
		}
	}

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
}