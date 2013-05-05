/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.creatifcubed.simpleapi;

/**
 *
 * @author Adrian
 */
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class SimpleConsole {

	private SimpleConsole() {
		return;
	}

	public static void print(String msg) {
		System.out.print(msg);
	}

	public static void print(int msg) {
		System.out.print(msg);
	}

	public static void print(double msg) {
		System.out.print(msg);
	}

	public static void print(boolean msg) {
		System.out.print(msg);
	}

	public static void print(char msg) {
		System.out.print(msg);
	}

	public static void print(char[] msg) {
		System.out.print(msg);
	}

	public static void print(Object[] msg) {
		System.out.print("{");
		if (msg != null) {
			for (Object o : msg) {
				System.out.print("{" + (o == null ? "{null}" : o.toString()) + "}, ");
			}
		}
		System.out.print("}");
	}

	public static void print(Object o) {
		System.out.print(o);
	}

	public static void println(String msg) {
		System.out.println(msg);
	}

	public static void println() {
		System.out.println();
	}

	public static void println(int msg) {
		System.out.println(msg);
	}

	public static void println(double msg) {
		System.out.println(msg);
	}

	public static void println(boolean msg) {
		System.out.println(msg);
	}

	public static void println(char msg) {
		System.out.println(msg);
	}

	public static void println(char[] msg) {
		System.out.println(msg);
	}

	public static void println(Object[] msg) {
		System.out.println("{");
		if (msg != null) {
			for (Object o : msg) {
				System.out.println("\t{" + (o == null ? "{null}" : o.toString() + "},\n"));
			}
		}
		System.out.println("}");
	}

	public static void println(Object o) {
		System.out.println(o);
	}

	public static String readLine() {
		return readLine("");
	}

	public static String readLine(String msg) {
		String input = "";
		InputStreamReader converter = new InputStreamReader(System.in);
		BufferedReader in = new BufferedReader(converter);

		while (input.length() == 0) {
			try {
				print(msg);
				input = in.readLine().trim();
			} catch (Exception ignore) {
				//
			}
		}
		return input;
	}

	public static int readInt(String msg) {
		int x = 0;
		while (true) {
			try {
				x = Integer.parseInt(readLine(msg));
				break;
			} catch (Exception ex) {
				println("Invalid int.");
			}
		}
		return x;
	}

	public static double readDouble(String msg) {
		double d = 0;
		while (true) {
			try {
				d = Double.parseDouble(readLine(msg));
				break;
			} catch (Exception ex) {
				println("Invalid decimal.");
			}
		}
		return d;
	}

	public static void error(String msg) {
		System.err.println(msg);
	}

	public static void fatalError(String msg) {
		error(msg);
		System.exit(-1);
	}

	public static void debug(Object o) {
		throw new UnsupportedOperationException();
	}

	public static void debug(Object[] o) {
		throw new UnsupportedOperationException();

	}
}
