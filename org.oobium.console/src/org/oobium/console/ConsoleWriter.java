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


public class ConsoleWriter {

	private final Console console;
	
	ConsoleWriter(Console console) {
		this.console = console;
	}
	
    public ConsoleWriter print(Object x) {
    	console.print(x, console.defNormal, false);
    	return this;
    }
    
    public ConsoleWriter print(Object x, int style) {
    	console.print(String.valueOf(x), console.getRegion(style), false);
    	return this;
    }
    
    public ConsoleWriter print(Object x, String href) {
    	printLink(String.valueOf(x), href, false);
    	return this;
    }
    
    public ConsoleWriter print(Object x, String...hrefs) {
    	printLinks(String.valueOf(x), hrefs, false);
    	return this;
    }
    
    public ConsoleWriter print(String x) {
    	console.print(x, null, false);
    	return this;
    }
    
    public ConsoleWriter print(String x, int style) {
    	console.print(x, console.getRegion(style), false);
    	return this;
    }
    
    public ConsoleWriter print(String x, String...hrefs) {
    	printLinks(x, hrefs, false);
    	return this;
    }
    
    public ConsoleWriter print(String x, String href) {
    	printLink(x, href, false);
    	return this;
    }
    
    private void printLink(String x, String href, boolean addNewLine) {
    	StringBuilder sb = new StringBuilder(x.length() + href.length() + 20);
    	int ix = x.indexOf("<a>");
    	if(ix == -1) {
	    	sb.append("<a href=\"").append(href).append("\" >").append(x).append("</a>");
    	} else {
    		sb.insert(ix+2, "href=\"" + href + "\"");
    	}
    	console.print(sb.toString(), console.defNormal, addNewLine);
    }
    
    private void printLinks(String x, String[] hrefs, boolean addNewLine) {
    	int len = x.length() + (8*hrefs.length);
    	for(String href : hrefs) {
    		len += href.length();
    	}
    	StringBuilder sb = new StringBuilder(len);
    	int i = 0;
    	int ix = x.indexOf("<a>");
    	while(ix != -1 && i < hrefs.length) {
    		sb.insert(ix+2, "href=\"" + hrefs[i] + "\"");
    		ix = x.indexOf("<a>", ix+9+hrefs[i].length());
    		i++;
    	}
    	console.print(sb.toString(), console.defNormal, addNewLine);
    }
    
    public ConsoleWriter println() {
    	console.print((String) null, null, true);
    	return this;
    }
    
    public ConsoleWriter println(Object x) {
    	console.print(x, console.defNormal, true);
    	return this;
    }
    
    public ConsoleWriter println(Object x, int style) {
    	console.print(String.valueOf(x), console.getRegion(style), true);
    	return this;
    }
    
    public ConsoleWriter println(Object x, String href) {
    	printLink(String.valueOf(x), href, true);
    	return this;
    }
    
    public ConsoleWriter println(Object x, String...hrefs) {
    	printLinks(String.valueOf(x), hrefs, true);
    	return this;
    }

    public ConsoleWriter println(String x) {
    	console.print(x, console.defNormal, true);
    	return this;
    }
    
    public ConsoleWriter println(String x, int style) {
    	console.print(x, console.getRegion(style), true);
    	return this;
    }

    public ConsoleWriter println(String x, String...hrefs) {
    	printLinks(x, hrefs, true);
    	return this;
    }

    public ConsoleWriter println(String x, String href) {
    	printLink(x, href, true);
    	return this;
    }

}
