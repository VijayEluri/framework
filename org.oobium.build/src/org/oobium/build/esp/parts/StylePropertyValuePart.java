package org.oobium.build.esp.parts;

import static org.oobium.build.esp.parts.EmbeddedJavaPart.embeddedJavaCheck;

import org.oobium.build.esp.EspPart;

public class StylePropertyValuePart extends EspPart {

	public StylePropertyValuePart(EspPart parent, int start, int end) {
		super(parent, Type.StylePropertyValuePart, start, end);
		parse();
	}
	
	public boolean isSimple() {
		return !hasParts();
	}
	
	private void parse() {
		for(int s = start; s < end; s++) {
			s = embeddedJavaCheck(this, ca, s, end);
			s = commentCheck(parent, ca, s);
		}
	}

}
