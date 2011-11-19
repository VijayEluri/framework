package org.oobium.build.model;

import java.util.ArrayList;
import java.util.List;

class Edits {
	
	private class Edit {

		private int start;
		private int end;
		private String text;
		
		public Edit(int start, int end, String text) {
			this.start = start;
			this.end = end;
			this.text = text;
		}
		
		public int apply(StringBuilder sb) {
			int offset = 0;
			if(start != -1) {
				if(text != null) {
					if(end == -1) {
						sb.insert(start, '\n');
						sb.insert(start, text);
						offset = text.length() + 1;
					} else {
						sb.replace(start, end, text);
						offset = text.length() - (end - start);
					}
				} else {
					if(end != -1) {
						sb.delete(start, end);
						offset = -(end - start);
						if(sb.charAt(start) == '\n') {
							sb.deleteCharAt(start);
							offset--;
						}
					}
				}
			}
			if(start > 0 && sb.charAt(start-1) != '\n') {
				sb.insert(start, '\n');
				offset++;
			}
			return offset;
		}
		
		public void move(int offset) {
			start += offset;
			if(end != -1) end += offset;
		}

	}

	
	private final List<Edit> edits;

	public Edits() {
		this.edits = new ArrayList<Edit>();
	}
	
	public void add(int start, int end, String text) {
		this.edits.add(new Edit(start, end, text));
	}
	
	public void apply(StringBuilder sb) {
		for(int i = 0; i < edits.size(); i++) {
			Edit edit = edits.get(i);
			int offset = edit.apply(sb);
			if(offset != 0) {
				for(int j = i+1; j < edits.size(); j++) {
					Edit next = edits.get(j);
					if(next.start >= edit.start) {
						next.move(offset);
					}
				}
			}
		}
	}
	
}