package org.oobium.build.esp.parts;

import static org.oobium.build.esp.parts.EmbeddedJavaPart.embeddedJavaCheck;

import org.oobium.build.esp.EspPart;

public class JavaSourcePart extends EspPart {

	private boolean simple;
	
	public JavaSourcePart(EspPart parent, Type type, int start, int end) {
		super(parent, type, start, end, true);
		parse();
	}

	public boolean isSimple() {
		return simple;
	}

	private int parseString(int start) {
		int s = start;
		for( ; s < end; s++) {
			if(ca[s] == '"') {
				if(ca[s-1] != '\\' || ca[s-2] == '\\') {
					return s + 1;
				}
			} else {
				s = embeddedJavaCheck(this, ca, s, end);
			}
		}
		return s;
	}
	
	private void parse() {
		int count = 0;
		int s1 = start;
		while(s1 < end) {
			if(ca[s1] == '"') {
				s1 = parseString(s1+1);
				count++;
			} else {
				s1++;
			}
		}
		
		simple = (count == 1 && ca[start] == '"' && ca[end-1] == '"' && !hasParts());
	}
	
}
