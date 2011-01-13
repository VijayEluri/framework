package org.oobium.build.esp.parts;

import static org.oobium.utils.CharStreamUtils.closer;

import org.oobium.build.esp.EspPart;

public class ScriptPart extends EspPart {

	public ScriptPart(EspPart parent, int start, int end) {
		super(parent, Type.ScriptPart, start, end);
		int s1 = findJava(start, end);
		while(s1 != -1) {
			int s2 = findJavaEnd(s1, end);
			if(s2 <= s1) {
				break;
			} else {
				new ScriptJavaPart(this, s1, s2);
				s1 = findJava(s2, end);
			}
		}
	}

	private int findJavaEnd(int start, int end) {
		for(int i = start; i < end; i++) {
			switch(ca[i]) {
			case ',':
			case '}':
			case ';':
				return i;
			default:
				i = skip(ca, i, end);
			}
		}
		return end;
	}

	private int skip(char[] ca, int start, int end) {
		int i = start;
		switch(ca[i]) {
		case '"':
			i = closer(ca, i, end, true, true);
			break;
		case '/':
			if(ca[i-1] == '/') {
				for(i = i + 1; i < end && ca[i] != '\n'; i++);
			} else if(i < end-1 && ca[i+1] == '*') {
				for(i = i + 1; i < end && !(ca[i] == '/' && ca[i-1] == '*'); i++);
			}
			break;
		}
		return i;
	}
	
	private int findJava(int start, int end) {
		for(int i = start; i < end; i++) {
			switch(ca[i]) {
			case '=':
				if(ca[i-1] == ':') {
					return i-1;
				}
				break;
			default:
				i = skip(ca, i, end);
			}
		}
		return -1;
	}
	
}
