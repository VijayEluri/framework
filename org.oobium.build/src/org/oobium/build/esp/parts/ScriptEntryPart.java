package org.oobium.build.esp.parts;

import org.oobium.build.esp.EspPart;

public class ScriptEntryPart extends JavaSourcePart {

	public ScriptEntryPart(EspPart parent, int start, int end) {
		super(parent, Type.ScriptPart, start, end);
	}

}
