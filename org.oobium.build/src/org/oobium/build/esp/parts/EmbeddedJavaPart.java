package org.oobium.build.esp.parts;

import static org.oobium.utils.CharStreamUtils.*;

import org.oobium.build.esp.EspPart;

public class EmbeddedJavaPart extends EspPart {

	public static int embeddedJavaCheck(EspPart parent, char[] ca, int start, int end) {
		if(ca[start] == '$' && (ca[start-1] != '\\' || ca[start-2] == '\\')) {
			if(ca[start+1] == '{') {
				EspPart part = new EmbeddedJavaPart(parent, start, end);
				return part.getEnd() - 1;
			}
			else if(Character.isJavaIdentifierStart(ca[start+1])) {
				if(!Character.isJavaIdentifierPart(ca[start-1]) || ca[start-2] == '\\') {
					EspPart part = new EmbeddedJavaPart(parent, start, end);
					return part.getEnd() - 1;
				}
			}
		}
		return start;
	}

	private static int findJavaEnd(char[] ca, int start, int end) {
		int s1 = start;
		if(s1 != end - 1) {
			s1++;
			if(ca[s1] == '{') { // expression
				int s2 = closer(ca, s1, end) + 1;
				if(s2 == 0) {
					s2 = end;
				}
				return s2;
			} else if(Character.isJavaIdentifierStart(ca[s1])) { // variable
				int s2 = s1+1;
				while(s2 < end) {
					if(!Character.isJavaIdentifierPart(ca[s2])) {
						break;
					}
					s2++;
				}
				return s2;
			}
		}
		return s1;
	}

	public static int skipEmbeddedJava(char[] ca, int start, int end) {
		if(start < ca.length) {
			if(ca[start] == '$' && (ca[start-1] != '\\' || ca[start-2] == '\\')) {
				if(ca[start+1] == '{') {
					return findJavaEnd(ca, start, end);
				}
				else if(Character.isJavaIdentifierStart(ca[start+1])) {
					if(!Character.isJavaIdentifierPart(ca[start-1]) || ca[start-2] == '\\') {
						return findJavaEnd(ca, start, end);
					}
				}
			}
		}
		return start;
	}

	
	private EspPart source;
	
	public EmbeddedJavaPart(EspPart parent, int start, int end) {
		super(parent, start);
		type = Type.JavaPart;
		this.end = end;
		parse();
	}
	
	public String getSource() {
		return (source != null) ? source.getText() : "";
	}
	
	public EspPart getSourcePart() {
		return source;
	}

	private void parse() {
		int s1 = start;
		if(s1 != end - 1) {
			s1++;
			if(ca[s1] == '{') { // expression
				int s2 = closer(ca, s1, end) + 1;
				if(s2 == 0) {
					s2 = end;
				}
				end = s2;
				s1 = forward(ca, s1+1);
				s2 = reverse(ca, s2-1);
				source = new EspPart(this, Type.JavaPart, s1, s2);
			} else if(Character.isJavaIdentifierStart(ca[s1])) { // variable
				int s2 = s1+1;
				while(s2 < end) {
					if(!Character.isJavaIdentifierPart(ca[s2])) {
						break;
					}
					s2++;
				}
				source = new EspPart(this, Type.JavaPart, s1, s2);
				end = s2;
			} else {
				end = s1;
			}
		}
	}

}
