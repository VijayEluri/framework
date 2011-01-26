package org.oobium.build.esp.parts;

import static org.oobium.utils.CharStreamUtils.*;

import org.oobium.build.esp.EspPart;

public class ScriptJavaPart extends EspPart {

	public final char assignmentChar;
	private EspPart source;
	
	public ScriptJavaPart(EspPart parent, int start, int end) {
		super(parent, Type.JavaPart, start, end);
		int srcStart = forward(ca, start+2, end);
		if(srcStart == -1) {
			assignmentChar = '=';
		} else {
			int srcEnd = reverse(ca, end-1) + 1;
			if(srcEnd > srcStart) {
				source = new EspPart(this, Type.JavaSourcePart, srcStart, srcEnd);
			}
			if(end < ca.length && ca[end] == ';') {
				assignmentChar = '=';
			} else {
				assignmentChar = ':';
			}
		}
	}

	public String getSource() {
		return (source != null) ? source.getText() : null;
	}
	
	public EspPart getSourcePart() {
		return source;
	}
	
}
