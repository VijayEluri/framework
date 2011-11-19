package org.oobium.build.model;

import static org.oobium.utils.CharStreamUtils.closer;
import static org.oobium.utils.CharStreamUtils.findEOL;
import static org.oobium.utils.CharStreamUtils.isNext;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ModelUtils {

	public static int findStart(String simpleName, char[] ca) {
		char[] test1 = "class".toCharArray();
		char[] test2 = simpleName.toCharArray();
		
		int s = 0;
		int end = ca.length;
		while(s < end) {
			switch(ca[s]) {
			case '<':
			case '(':
			case '{':
			case '[':
			case '"':
				s = closer(ca, s, end, true) + 1;
				if(s == 0) {
					s = end;
				}
				break;
			case '/':
				if(s > 0 && ca[s-1] == '/') { // line comment
					s = findEOL(ca, s+1);
				} else {
					s++;
				}
				break;
			case '*':
				if(s > 0 && ca[s-1] == '/') { // multiline comment
					s++;
					while(s < end) {
						if(ca[s] == '/' && ca[s-1] == '*') {
							s++;
							break;
						}
						s++;
					}
				} else {
					s++;
				}
				break;
			default:
				if(isNext(ca, s, test1)) {
					if(s == 0 || Character.isWhitespace(ca[s-1])) {
						s = s + test1.length;
						if(s < ca.length && Character.isWhitespace(ca[s])) {
							do {
								s++;
							} while(s < ca.length && Character.isWhitespace(ca[s]));
							if(isNext(ca, s, test2)) {
								s = s + test2.length;
								if(s < ca.length && (ca[s] == '{' || Character.isWhitespace(ca[s]))) {
									// found the start of the class, rewind to beginning of line
									while(s >= 0 && ca[s] != '\n') {
										s--;
									}
									return s + 1;
								}
							}
						}
					}
				}
				s++;
			}
		}
		
		return -1;
	}
	
	public static List<String> getJavaArguments(char[] ca, int start, int end) {
		List<String> args = new ArrayList<String>();
		
		int s1 = start;
		int s = start;
		while(s < end) {
			switch(ca[s]) {
			case ',':
				String value = new String(ca, s1, s-s1).trim();
				args.add(value);
				s++;
				s1 = s;
				break;
			case '<':
			case '(':
			case '{':
			case '[':
			case '"':
				s = closer(ca, s, end, true) + 1;
				if(s == 0) {
					s = end;
				}
				break;
			case '/':
				if(ca[s-1] == '/') { // line comment
					s = findEOL(ca, s+1);
				} else {
					s++;
				}
				break;
			case '*':
				if(ca[s-1] == '/') { // multiline comment
					s++;
					while(s < end) {
						if(ca[s] == '/' && ca[s-1] == '*') {
							s++;
							break;
						}
						s++;
					}
				} else {
					s++;
				}
				break;
			default:
				s++;
			}
		}
	
		if(end >= s1) {
			String value = new String(ca, s1, end-s1).trim();
			if(value.length() > 0) {
				args.add(value);
			}
		}
		
		return args;
	}

	public static Map<String, String> getJavaEntries(char[] ca, int start, int end) {
		Map<String, String> entries = new LinkedHashMap<String, String>();
		
		String name = null;
		int s1 = start;
		int s = start;
		while(s < end) {
			switch(ca[s]) {
			case '=':
				name = new String(ca, s1, s-s1).trim();
				s++;
				s1 = s;
				break;
			case ',':
				String value = new String(ca, s1, s-s1).trim();
				entries.put(name, value);
				name = null;
				s++;
				s1 = s;
				break;
			case '<':
			case '(':
			case '{':
			case '[':
			case '"':
				s = closer(ca, s, end, true) + 1;
				if(s == 0) {
					s = end;
				}
				break;
			case '/':
				if(ca[s-1] == '/') { // line comment
					s = findEOL(ca, s+1);
				} else {
					s++;
				}
				break;
			case '*':
				if(ca[s-1] == '/') { // multiline comment
					s++;
					while(s < end) {
						if(ca[s] == '/' && ca[s-1] == '*') {
							s++;
							break;
						}
						s++;
					}
				} else {
					s++;
				}
				break;
			default:
				s++;
			}
		}
		
		if(name != null) {
			String value = new String(ca, s1, end-s1).trim();
			if(value.length() > 0) {
				entries.put(name, value);
			}
		}
		
		return entries;
	}

	public static Map<String, String> getJavaEntries(String s) {
		return getJavaEntries(s.toCharArray(), 0, s.length());
	}

	public static String getString(Object object) {
		if(object == null) {
			return null;
		}
		String in = object.toString();
		if(in.length() > 1 && in.charAt(0) == '"' && in.charAt(in.length()-1) == '"') {
			// it is a string literal - escape special characters
			StringBuilder sb = new StringBuilder(in.length());
			for(int j = 1; j < in.length()-1; j++) {
				char c = in.charAt(j);
				switch(c) {
				case '\\':
					if(j < in.length()-2) {
						char c2 = in.charAt(j+1);
						switch(c2) {
						case '\\':
						case '"':
							c = c2;
							j++; // skip the next character (don't add it twice)
						}
					}
				default:
					sb.append(c);
					break;
				}
			}
			return sb.toString();
		}
		return in;
	}

}
