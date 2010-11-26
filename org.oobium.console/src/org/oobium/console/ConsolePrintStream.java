/*******************************************************************************
 * Copyright (c) 2010 Oobium, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Jeremy Dowdall <jeremy@oobium.com> - initial API and implementation
 ******************************************************************************/
package org.oobium.console;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Locale;

public class ConsolePrintStream extends PrintStream {

	private ConsoleErrorWriter err;
	private ConsoleWriter out;
	private PrintStream original;

	public ConsolePrintStream(ConsoleWriter writer) {
		super(new OutputStream() {
			@Override
			public void write(int b) throws IOException {
				// do nothing
				System.out.print(b);
			}
		});
		out = writer;
		original = System.out;
	}

	public ConsolePrintStream(ConsoleErrorWriter writer) {
		super(new OutputStream() {
			@Override
			public void write(int b) throws IOException {
				// do nothing
				System.err.print((char)b);
			}
		});
		err = writer;
		original = System.err;
	}

	public PrintStream getOriginal() {
		return original;
	}
	
	@Override
	public PrintStream append(char c) {
		return super.append(c);
	}

	@Override
	public PrintStream append(CharSequence csq) {
		return super.append(csq);
	}

	@Override
	public PrintStream append(CharSequence csq, int start, int end) {
		// TODO Auto-generated method stub
		return super.append(csq, start, end);
	}

	@Override
	public boolean checkError() {
		// TODO Auto-generated method stub
		return super.checkError();
	}

	@Override
	protected void clearError() {
		// TODO Auto-generated method stub
		super.clearError();
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		// TODO Auto-generated method stub
		return super.clone();
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		super.close();
	}

	@Override
	public boolean equals(Object obj) {
		// TODO Auto-generated method stub
		return super.equals(obj);
	}

	@Override
	protected void finalize() throws Throwable {
		// TODO Auto-generated method stub
		super.finalize();
	}

	@Override
	public void flush() {
		// TODO Auto-generated method stub
		super.flush();
	}

	@Override
	public PrintStream format(Locale l, String format, Object... args) {
		// TODO Auto-generated method stub
		return super.format(l, format, args);
	}

	@Override
	public PrintStream format(String format, Object... args) {
		// TODO Auto-generated method stub
		return super.format(format, args);
	}

	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return super.hashCode();
	}

	@Override
	public void print(boolean b) {
		// TODO Auto-generated method stub
		super.print(b);
	}

	@Override
	public void print(char c) {
		if(out != null) { out.print(String.valueOf(c)); } else { err.print(String.valueOf(c)); }
	}

	@Override
	public void print(char[] s) {
		// TODO Auto-generated method stub
		super.print(s);
	}

	@Override
	public void print(double d) {
		// TODO Auto-generated method stub
		super.print(d);
	}

	@Override
	public void print(float f) {
		// TODO Auto-generated method stub
		super.print(f);
	}

	@Override
	public void print(int i) {
		// TODO Auto-generated method stub
		super.print(i);
	}

	@Override
	public void print(long l) {
		// TODO Auto-generated method stub
		super.print(l);
	}

	@Override
	public void print(Object obj) {
		if(out != null) { out.print(obj); } else { err.print(obj); }
	}

	@Override
	public void print(String s) {
		if(out != null) { out.print(s); } else { err.print(s); }
	}

	@Override
	public PrintStream printf(Locale l, String format, Object... args) {
		// TODO Auto-generated method stub
		return super.printf(l, format, args);
	}

	@Override
	public PrintStream printf(String format, Object... args) {
		// TODO Auto-generated method stub
		return super.printf(format, args);
	}

	@Override
	public void println() {
		if(out != null) { out.println(); } else { err.println(); }
	}

	public void println(boolean x) {
		// TODO Auto-generated method stub
		super.println(x);
	};

	@Override
	public void println(char x) {
		// TODO Auto-generated method stub
		super.println(x);
	}

	@Override
	public void println(char[] x) {
		// TODO Auto-generated method stub
		super.println(x);
	}

	@Override
	public void println(double x) {
		// TODO Auto-generated method stub
		super.println(x);
	}

	@Override
	public void println(float x) {
		// TODO Auto-generated method stub
		super.println(x);
	}

	@Override
	public void println(int x) {
		// TODO Auto-generated method stub
		super.println(x);
	}

	@Override
	public void println(long x) {
		// TODO Auto-generated method stub
		super.println(x);
	}

	@Override
	public void println(Object x) {
		if(out != null) { out.println(x); } else { err.println(x); }
	}

	@Override
	public void println(String x) {
		if(out != null) { out.println(x); } else { err.println(x); }
	}

	@Override
	protected void setError() {
		super.setError();
	}

	@Override
	public String toString() {
		return super.toString();
	}

	@Override
	public void write(byte[] b) throws IOException {
		print(new String(b));
	}

	@Override
	public void write(byte[] buf, int off, int len) {
		print(new String(buf, off, len));
	}

	@Override
	public void write(int b) {
		print((char) b);
	}
}
