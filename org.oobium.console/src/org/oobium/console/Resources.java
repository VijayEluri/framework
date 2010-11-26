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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

public class Resources {

	private static Listener disposeListener = new Listener() {
		public void handleEvent(Event event) {
			List<String> invalids = new ArrayList<String>();
			for(Entry<String, Image> entry : images.entrySet()) {
				Image img = entry.getValue();
				if(event.display == img.getDevice()) {
					invalids.add(entry.getKey());
					if(!img.isDisposed()) {
						img.dispose();
					}
				}
			}
			for(String key : invalids) {
				images.remove(key);
			}
			displays.remove(event.display);
		}
	};
	
	private static final Map<String, Image> images = new HashMap<String, Image>();
	private static final Set<Display> displays = new HashSet<Display>();

	public static Image getImage(String name) {
		Image img = images.get(name);
		if(img == null || img.isDisposed()) {
			Display display = Display.getCurrent();
			if(!displays.contains(display)) {
				display.addListener(SWT.Dispose, disposeListener);
				displays.add(display);
			}
			InputStream inputStream = Resources.class.getResourceAsStream(name);
			img = new Image(display, inputStream);
			images.put(name, img);
		}
		return img;
	}

	
	private Map<String, String> strings;

	Resources() {
		// default protected constructor
	}
	
	void putStrings(ResourceBundle resourceBundle) {
		if(strings == null) {
			strings = new HashMap<String, String>();
		}
		for(Enumeration<String> e = resourceBundle.getKeys(); e.hasMoreElements(); ) {
			String key = e.nextElement();
			strings.put(key, resourceBundle.getString(key));
		}
	}

	void putStrings(String bundleName, ClassLoader classLoader) {
		ResourceBundle resourceBundle = ResourceBundle.getBundle(bundleName, Locale.getDefault(), classLoader);
		putStrings(resourceBundle);
	}

	void clear() {
		if(strings != null) {
			strings.clear();
		}
	}
	
	Locale getLocale() {
		return Locale.getDefault();
	}
	
	String getString(String key) {
		return getString(key, true);
	}

	String getString(String key, boolean returnMissing) {
		String string = strings.get(key);
		if(string != null) {
			return string;
		}
		if(returnMissing) {
			return returnMissing ? ('!' + key + '!') : null;
		}
		return null;
	}
	
}
