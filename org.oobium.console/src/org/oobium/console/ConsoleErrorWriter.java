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


public class ConsoleErrorWriter {
	
	private final Console console;
	
	ConsoleErrorWriter(Console console) {
		this.console = console;
	}
	
    public void print(Object x) {
    	console.print(x, console.defError, false);
    }
    
    public void print(String x) {
    	console.print(x, console.defError, false);
    }
    
    public void println() {
    	console.print((String) null, console.defError, true);
    }
    
    public void println(Object x) {
    	console.print(x, console.defError, true);
    }

    public void println(String x) {
    	console.print(x, console.defError, true);
    }

}
