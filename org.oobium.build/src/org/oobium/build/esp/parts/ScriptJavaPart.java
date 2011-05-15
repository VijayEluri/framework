package org.oobium.build.esp.parts;

import static org.oobium.utils.CharStreamUtils.*;

import org.oobium.build.esp.EspPart;

public class ScriptJavaPart extends EspPart {

	private EspPart source;
	
	public ScriptJavaPart(EspPart parent, int start) {
		super(parent, start);
		type = Type.JavaPart;
		end = closer(ca, start+1);
		if(end == -1) {
			end = ca.length;
		}
		int srcStart = forward(ca, start+2, end);
		if(srcStart != -1) {
			int srcEnd = reverse(ca, end-1) + 1;
			if(srcEnd > srcStart) {
				source = new EspPart(this, Type.JavaSourcePart, srcStart, srcEnd);
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
