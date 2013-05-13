package com.creatifcubed.simpleapi;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class SimpleAggregatePrintStream extends PrintStream {
	private final List<PrintStream> children;
	private final boolean autoflush;
	
	public SimpleAggregatePrintStream(PrintStream... children) {
		this(false, children);
	}
	
	public SimpleAggregatePrintStream(boolean autoflush, PrintStream... children) {
		super(new OutputStream() {
			@Override
			public void write(int b) {
				throw new IllegalStateException("SimpleAggregatePrintStream's delegate OutputStream should not be used");
			}
		});
		this.autoflush = autoflush;
		this.children = new LinkedList<PrintStream>();
		for (int i = 0; i < children.length; i++) {
			this.children.add(children[i]);
		}
	}

	@Override
	public PrintStream append(char arg0) {
		for (PrintStream each : this.children) {
			each.append(arg0);
		}
		return this;
	}

	@Override
	public PrintStream append(CharSequence arg0, int arg1, int arg2) {
		for (PrintStream each : this.children) {
			each.append(arg0, arg1, arg2);
		}
		return this;
	}

	@Override
	public PrintStream append(CharSequence arg0) {
		for (PrintStream each : this.children) {
			each.append(arg0);
		}
		return this;
	}

	@Override
	public boolean checkError() {
		for (PrintStream each : this.children) {
			if (each.checkError()) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected void clearError() {
		for (PrintStream each : this.children) {
			// each.clearError();
		}
	}

	@Override
	public void close() {
		for (PrintStream each : this.children) {
			each.close();
		}
	}

	@Override
	public void flush() {
		for (PrintStream each : this.children) {
			each.flush();
		}
	}

	@Override
	public PrintStream format(Locale arg0, String arg1, Object... arg2) {
		for (PrintStream each : this.children) {
			each.format(arg0, arg1, arg2);
		}
		return this;
	}

	@Override
	public PrintStream format(String arg0, Object... arg1) {
		for (PrintStream each : this.children) {
			each.format(arg0, arg1);
		}
		return this;
	}

	@Override
	public void print(boolean arg0) {
		for (PrintStream each : this.children) {
			each.print(arg0);
		}
	}

	@Override
	public void print(char arg0) {
		for (PrintStream each : this.children) {
			each.print(arg0);
		}
	}

	@Override
	public void print(char[] arg0) {
		for (PrintStream each : this.children) {
			each.print(arg0);
		}
	}

	@Override
	public void print(double arg0) {
		for (PrintStream each : this.children) {
			each.print(arg0);
		}
	}

	@Override
	public void print(float arg0) {
		for (PrintStream each : this.children) {
			each.print(arg0);
		}
	}

	@Override
	public void print(int arg0) {
		for (PrintStream each : this.children) {
			each.print(arg0);
		}
	}

	@Override
	public void print(long arg0) {
		for (PrintStream each : this.children) {
			each.print(arg0);
		}
	}

	@Override
	public void print(Object arg0) {
		for (PrintStream each : this.children) {
			each.print(arg0);
		}
	}

	@Override
	public void print(String arg0) {
		for (PrintStream each : this.children) {
			each.print(arg0);
		}
	}

	@Override
	public PrintStream printf(Locale arg0, String arg1, Object... arg2) {
		for (PrintStream each : this.children) {
			each.printf(arg0, arg1, arg2);
		}
		return this;
	}

	@Override
	public PrintStream printf(String arg0, Object... arg1) {
		for (PrintStream each : this.children) {
			each.printf(arg0, arg1);
		}
		return this;
	}

	@Override
	public void println() {
		for (PrintStream each : this.children) {
			each.println();
			if (this.autoflush) {
				each.flush();
			}
		}
	}

	@Override
	public void println(boolean arg0) {
		for (PrintStream each : this.children) {
			each.println(arg0);
			if (this.autoflush) {
				each.flush();
			}
		}
	}

	@Override
	public void println(char arg0) {
		for (PrintStream each : this.children) {
			each.println(arg0);
			if (this.autoflush) {
				each.flush();
			}
		}
	}

	@Override
	public void println(char[] arg0) {
		for (PrintStream each : this.children) {
			each.println(arg0);
			if (this.autoflush) {
				each.flush();
			}
		}
	}

	@Override
	public void println(double arg0) {
		for (PrintStream each : this.children) {
			each.println(arg0);
			if (this.autoflush) {
				each.flush();
			}
		}
	}

	@Override
	public void println(float arg0) {
		for (PrintStream each : this.children) {
			each.println(arg0);
			if (this.autoflush) {
				each.flush();
			}
		}
	}

	@Override
	public void println(int arg0) {
		for (PrintStream each : this.children) {
			each.println(arg0);
			if (this.autoflush) {
				each.flush();
			}
		}
	}

	@Override
	public void println(long arg0) {
		for (PrintStream each : this.children) {
			each.println(arg0);
			if (this.autoflush) {
				each.flush();
			}
		}
	}

	@Override
	public void println(Object arg0) {
		for (PrintStream each : this.children) {
			each.println(arg0);
			if (this.autoflush) {
				each.flush();
			}
		}
	}

	@Override
	public void println(String arg0) {
		for (PrintStream each : this.children) {
			each.println(arg0);
			if (this.autoflush) {
				each.flush();
			}
		}
	}

	@Override
	protected void setError() {
		for (PrintStream each : this.children) {
			//each.setError();
		}
	}

	@Override
	public void write(byte[] arg0, int arg1, int arg2) {
		for (PrintStream each : this.children) {
			each.write(arg0, arg1, arg2);
			if (this.autoflush) {
				each.flush();
			}
		}
	}

	@Override
	public void write(int arg0) {
		for (PrintStream each : this.children) {
			each.write(arg0);
			if (this.autoflush && arg0 == '\n') {
				each.flush();
			}
		}
	}
}
