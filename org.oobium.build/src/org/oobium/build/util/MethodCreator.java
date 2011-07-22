package org.oobium.build.util;

import java.util.ArrayList;
import java.util.List;

public class MethodCreator {
	public String name;
	private List<String> lines = new ArrayList<String>();

	public MethodCreator(String name) {
		this.name = name;
	}

	public void addLine(String line) {
		lines.add(line + "\n");
	}

	public String toString() {
		int openBrackets = 0;
		StringBuilder builder = new StringBuilder();
		for(String line : lines) {
			builder.append(formatLine(openBrackets, line));
			openBrackets += getBracketCount(line);
		}
		return builder.toString();
	}

	private String formatLine(int bracketCount, String line) {
		String returnLine = "";

		if(line.trim().charAt(0) == '}') {
			bracketCount--;
		}
		returnLine = line.trim() + "\n";
		for(int i = 0; i < bracketCount; i++) {
			returnLine = "\t" + returnLine;
		}
		return returnLine;
	}

	private int getBracketCount(String line) {
		int bracketCount = 0;
		for(char c : line.toCharArray()) {
			if(c == '{') {
				bracketCount++;
			}
			else if(c == '}') {
				bracketCount--;
			}
		}
		return bracketCount;
	}

}
