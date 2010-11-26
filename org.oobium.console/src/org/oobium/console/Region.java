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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

public class Region {

	public static final int UNDERLINE	= 1 << (31-3);

	public static final int NORMAL		= 0;
	public static final int BOLD		= 1 << (31-2);
	public static final int ITALIC		= 1 << (31-1);

	public static final int RGB			= 1 << (31-0);
	
	public static final int WHITE		= SWT.COLOR_WHITE;
	public static final int BLACK		= SWT.COLOR_BLACK;
	public static final int RED			= SWT.COLOR_RED;
	public static final int DARK_RED	= SWT.COLOR_DARK_RED;
	public static final int GREEN		= SWT.COLOR_GREEN;
	public static final int DARK_GREEN	= SWT.COLOR_DARK_GREEN;
	public static final int YELLOW		= SWT.COLOR_YELLOW;
	public static final int DARK_YELLOW	= SWT.COLOR_DARK_YELLOW;
	public static final int BLUE		= SWT.COLOR_BLUE;
	public static final int DARK_BLUE	= SWT.COLOR_DARK_BLUE;
	public static final int MAGENTA		= SWT.COLOR_MAGENTA;
	public static final int DARK_MAGENTA= SWT.COLOR_DARK_MAGENTA;
	public static final int CYAN		= SWT.COLOR_CYAN;
	public static final int DARK_CYAN	= SWT.COLOR_DARK_CYAN;
	public static final int GRAY		= SWT.COLOR_GRAY;
	public static final int DARK_GRAY	= SWT.COLOR_DARK_GRAY;
	
	
	public final int style;
	
	private List<Listener> clickListeners;
	private List<Listener> enterListeners;
	private List<Listener> exitListeners;

	public Region(int style) {
		this.style = style;
	}

	public boolean underline() {
		return (style & UNDERLINE) != 0;
	}

	Font font(Console console) {
		if((style & BOLD) != 0 && (style & ITALIC) != 0) {
			return console.fontBoldItalic;
		} else if((style & BOLD) != 0) {
			return console.fontBold;
		} else if((style & ITALIC) != 0) {
			return console.fontItalic;
		} else {
			return console.fontNormal;
		}
	}

	Color color(Display display) {
		if((style & RGB) != 0) {
			throw new UnsupportedOperationException("TODO: implement Region style RGB");
		} else {
			return display.getSystemColor(0x0fff & style);
		}
	}
	
	private void createListeners() {
		clickListeners = new ArrayList<Listener>();
		enterListeners = new ArrayList<Listener>();
		exitListeners = new ArrayList<Listener>();
	}
	
	public void addListener(int type, Listener listener) {
		if(clickListeners == null) {
			createListeners();
		}
		switch(type) {
		case SWT.Selection:
			if(!clickListeners.contains(listener)) {
				clickListeners.add(listener);
			}
			break;
		case SWT.MouseEnter:
			if(!enterListeners.contains(listener)) {
				enterListeners.add(listener);
			}
			break;
		case SWT.MouseExit:
			if(!exitListeners.contains(listener)) {
				exitListeners.add(listener);
			}
			break;
		}
	}

	public boolean hasListeners(int type) {
		if(clickListeners == null) {
			createListeners();
		}
		switch(type) {
		case SWT.Selection:
			return !clickListeners.isEmpty();
		case SWT.MouseEnter:
			return !enterListeners.isEmpty();
		case SWT.MouseExit:
			return !exitListeners.isEmpty();
		default:
			return false;
		}
	}

	public void removeListener(int type, Listener listener) {
		if(clickListeners == null) {
			createListeners();
		}
		switch(type) {
		case SWT.Selection:
			clickListeners.remove(listener);
			break;
		case SWT.MouseEnter:
			enterListeners.remove(listener);
			break;
		case SWT.MouseExit:
			exitListeners.remove(listener);
			break;
		}
	}

	void notifyListeners(int type, Event event, Segment segment) {
		int typeBak = event.type;
		event.type = type;
		notifyListeners(event, segment);
		event.type = typeBak;
	}

	void notifyListeners(Event event, Segment segment) {
		if(hasListeners(event.type)) {
			Listener[] listeners;
			switch(event.type) {
			case SWT.Selection:
				listeners = clickListeners.toArray(new Listener[clickListeners.size()]);
				break;
			case SWT.MouseEnter:
				listeners = enterListeners.toArray(new Listener[enterListeners.size()]);
				break;
			case SWT.MouseExit:
				listeners = exitListeners.toArray(new Listener[exitListeners.size()]);
				break;
			default:
				listeners = null;
			}
			event.data = segment.data;
			event.text = segment.toString();
			for(Listener listener : listeners) {
				listener.handleEvent(event);
			}
		}
	}
}
