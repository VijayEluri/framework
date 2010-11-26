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

import static java.lang.Math.max;
import static java.lang.Math.min;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

import org.eclipse.swt.graphics.Point;

class Buffer {

	final Line[] lines;
	private int count;

	private String prompt;
	Deque<Region> regions;
	
	Buffer(String prompt, int maxLines) {
		this.prompt = prompt;
		this.lines = new Line[maxLines];
		regions = new ArrayDeque<Region>();
		addPrompt();
	}

	Segment startRegion(Region type) {
		regions.push(type);
		return handleRegionChange();
	}
	
	Line addLine() {
		return addLine(new Line(this, 20));
	}
	
	Line addLine(int length) {
		return addLine(new Line(this, length));
	}
	
	Line addLine(String string) {
		return addLine(new Line(this, string, string.length() + 20));
	}

	Line addLine(String string, int length) {
		return addLine(new Line(this, string, length));
	}

	Line addLine(StringBuilder sb) {
		return addLine(new Line(this, sb));
	}
	
	private Line addLine(Line line) {
		if(count > 0) {
			lines[count-1].freeze();
		}
		if(count < lines.length) {
			count++;
		} else {
			count = lines.length;
			for(int i = 1; i < count; i++) {
				lines[i-1] = lines[i];
			}
		}
		return lines[count-1] = line;
	}
	
	Line addPrompt() {
		return addLine(prompt, prompt.length() + 20);
	}

	void clear() {
		clear(true);
	}
	
	void clear(boolean addPrompt) {
		for(int i = 0; i < lines.length; i++) {
			lines[i] = null;
		}
		count = 0;
		if(addPrompt) {
			addPrompt();
		}
	}
	
	private Segment handleRegionChange() {
		Line line = getLast();
		List<Segment> segs = line.segments;
		if(segs.isEmpty()) {
			return null;
		} else {
			Segment seg = segs.get(segs.size() - 1);
			if(seg.length() == 0) {
				seg.setDefinition(regions.peek());
			} else {
				seg.end = line.length();
				seg = new Segment(this, line, seg.end);
				segs.add(seg);
			}
			return seg;
		}
	}
	
	void endRegion() {
		if(!regions.isEmpty()) {
			regions.pop();
		}
		handleRegionChange();
	}
	
	void endRegions() {
		regions.clear();
		handleRegionChange();
	}

	Line get(int line) {
		return lines[line];
	}

	Line getLast() {
		return (count > 0) ? lines[count-1] : null;
	}

	int getLastLength() {
		return (count > 0) ? lines[count-1].length() : 0;
	}
	
	int getLength(int line) {
		return lines[line].length();
	}
	
	int getMaxLength() {
		int w = 0;
		for(int i = 0; i < count; i++) {
			w = max(lines[i].length(), w);
		}
		return w;
	}
	
	String getPrompt() {
		return prompt;
	}
	
	Segment getSegment(Point point) {
		if(point.y >= 0 && point.y < count) {
			for(Segment seg : lines[point.y]) {
				int start = seg.start;
				int end = seg.line.frozen() ? seg.end : (start + seg.length());
				if(point.x >= start && point.x < end) {
					return seg;
				}
			}
		}
		return null;
	}
	
	boolean isEmpty() {
		return count > 0;
	}
	
	int position() {
		return position(count);
	}
	
	int position(int line) {
		int pos = line - 1; // new line characters
		for(int i = 0; i < line; i++) {
			pos += lines[i].length();
		}
		return pos;
	}
	
	void setLastToPrompt() {
		Line line = getLast();
		if(line.length() == 0) {
			line.sb.append(prompt);
		} else {
			addPrompt();
		}
	}
	
	int size() {
		return count;
	}
	
	@Override
	public String toString() {
		int chars = 0;
		for(int i = 0; i < count; i++) {
			chars += lines[i].length();
			chars++;
		}
		StringBuilder sb = new StringBuilder(chars);
		for(int i = 0; i < count; i++) {
			if(i != 0) {
				sb.append('\n');
			}
			sb.append(lines[i].sequence());
		}
		return sb.toString();
	}
	
	public String toString(int firstLine) {
		int chars = 0;
		for(int i = firstLine; i < count; i++) {
			chars += lines[i].length();
			chars++;
		}
		StringBuilder sb = new StringBuilder(chars);
		for(int i = firstLine; i < count; i++) {
			if(i != firstLine) {
				sb.append('\n');
			}
			sb.append(lines[i].sequence());
		}
		return sb.toString();
	}

	public String toString(Selection sel) {
		if(!sel.isValid()) {
			return "";
		}
		if(sel.y1 == sel.y2) {
			int x1 = max(sel.x1, 0);
			int x2 = min(sel.x2, lines[sel.y1].length());
			if(x2 > x1) {
				return lines[sel.y1].substring(x1, x2);
			} else {
				return "";
			}
		} else {
//			TODO pre-calculate builder capacity
//			int chars = lines[sel.y1].length() - sel.x1 + (sel.y2 - sel.y1);
//			for(int i = sel.y1+1; i < sel.y2-1 && i < count; i++) {
//				chars += lines[i].length();
//				chars++;
//			}
//			if(sel.y2 < count) {
//				chars += sel.x2 - lines[sel.y2].length();
//			}
			StringBuilder sb = new StringBuilder();
			if(sel.x1 < lines[sel.y1].length()) {
				sb.append(lines[sel.y1].substring(sel.x1));
			}
			for(int i = sel.y1+1; i < sel.y2 && i < count; i++) {
				sb.append('\n').append(lines[i].sequence());
			}
			if(sel.y2 < count) {
				sb.append('\n').append(lines[sel.y2].substring(0, min(sel.x2, lines[sel.y2].length())));
			}
			return sb.toString();
		}
	}
	
}
