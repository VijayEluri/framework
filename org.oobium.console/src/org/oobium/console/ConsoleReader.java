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

public class ConsoleReader {

	private char[] ca;
	
	ConsoleReader() {
		// private constructor
	}
	
	synchronized void notify(char c) {
		ca = new char[1];
		ca[0] = c;
		notify();
	}
	
	synchronized void notify(String s) {
		ca = s.toCharArray();
		notify();
	}
	
	public synchronized String readLine() {
		StringBuilder sb = new StringBuilder();
		while(true) {
			try {
				wait();
			} catch(InterruptedException e) {
				// discard
			}
			for(char c : ca) {
				if(c == '\n') {
					return sb.toString();
				} else {
					sb.append(c);
				}
			}
		}
	}
	
}
