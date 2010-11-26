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
import java.util.Iterator;
import java.util.List;

class Line implements Iterable<Segment> {

	private final Buffer buffer;
	String string;
	List<Segment> segments;
	StringBuilder sb;

	Line(Buffer buffer, int capacity) {
		this.buffer = buffer;
		sb = new StringBuilder(capacity);
		segments = new ArrayList<Segment>();
		segments.add(new Segment(this.buffer, this));
	}

	Line(Buffer buffer, String string, int capacity) {
		this.buffer = buffer;
		sb = new StringBuilder(capacity);
		sb.append(string);
		segments = new ArrayList<Segment>();
		segments.add(new Segment(this.buffer, this));
	}

	Line(Buffer buffer, StringBuilder sb) {
		this.buffer = buffer;
		this.sb = sb;
		segments = new ArrayList<Segment>();
		segments.add(new Segment(this.buffer, this));
	}

	void freeze() {
		try {
			for(Iterator<Segment> iter = segments.iterator(); iter.hasNext();) {
				if(iter.next().length() < 1) {
					iter.remove();
				}
			}
			if(!segments.isEmpty()) {
				segments.get(segments.size() - 1).end = sb.length();
			}
			string = sb.toString();
			sb = null;
		} catch(Exception e) {
			// TODO: wtf?
		}
	}

	boolean frozen() {
		return string != null;
	}

	@Override
	public Iterator<Segment> iterator() {
		return new ArrayList<Segment>(segments).iterator();
	}

	int length() {
		return frozen() ? string.length() : sb.length();
	}

	CharSequence sequence() {
		return frozen() ? string : sb;
	}

	String substring(int start) {
		return frozen() ? string.substring(start) : sb.substring(start);
	}

	String substring(int start, int end) {
		return frozen() ? string.substring(start, end) : sb.substring(start, end);
	}

	@Override
	public String toString() {
		return frozen() ? string : sb.toString();
	}
	
}
