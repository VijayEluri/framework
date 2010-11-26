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

class Segment {

	Line line;
	int end;
	int start;
	private Region type;
	Object data; // implementation specific

	Segment(Buffer buffer, Line line) {
		this.line = line;
		setDefinition(buffer.regions.peek());
	}

	Segment(Buffer buffer, Line line, int start) {
		this(buffer, line);
		this.start = start;
	}

	Region getDefintion(Region defaultType) {
		return (type != null) ? type : defaultType;
	}

	int length() {
		if(line.frozen()) {
			return end - start;
		} else {
			return (end > 0) ? (end - start) : (line.length() - start);
		}
	}

	void setData(Object data) {
		this.data = data;
	}

	void setDefinition(Region type) {
		if(type != null) {
			this.type = type;
		}
	}

	@Override
	public String toString() {
		if(line.frozen()) {
			try {
				return line.string.substring(start, end);
			} catch(Exception e) {
//				e.printStackTrace();
				return " oops!";
			}
		} else {
			return line.sb.substring(start, (end > 0) ? end : line.length());
		}
	}

}
