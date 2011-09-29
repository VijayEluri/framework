package org.oobium.build.esp.parts;

import static org.oobium.utils.CharStreamUtils.closer;
import static org.oobium.build.esp.parts.EmbeddedJavaPart.embeddedJavaCheck;

import org.oobium.build.esp.EspPart;

public class ScriptPart extends EspPart {

	public ScriptPart(EspPart parent, int start, int end) {
		super(parent, Type.ScriptPart, start, end);
		parse();
	}

	protected int commentCheck(EspPart parent, char[] ca, int ix) {
		if(ix >= 0) {
			if(ix < end) {
				if(ca[ix] == '"' || ca[ix] == '\'') {
					ix = closer(ca, ix, end, true, true);
					if(ix == -1) {
						ix = end;
					}
				} else if(ca[ix] == '/' && (ca[ix+1] == '*' || ca[ix+1] == '/')) {
					CommentPart comment = new CommentPart(parent, ix);
					end = ix-1;
					ix = comment.getEnd();
				}
			}
		}
		return ix;
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
