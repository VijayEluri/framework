package org.oobium.build.esp.elements;

import org.oobium.build.esp.EspPart;

public class MarkupCommentElement extends MarkupElement {

	public MarkupCommentElement(EspPart parent, int start) {
		super(parent, start);
	}
	
	protected void parse() {
		int s1 = start;
		int eoe = findEnd(s1);

		s1 += 3;
		tag = new EspPart(this, Type.TagPart, start, s1);
		type = Type.MarkupCommentElement;
		style = ENTRIES | INNER_TEXT | INNER_TEXT_JAVA | CHILDREN;

		s1 = parseArgsAndEntries(s1, eoe);
		
		s1 = parseInnerText(s1, eoe);

		s1 = parseChildren(s1, eoe);
		
		end = (s1 < ca.length) ? s1 : ca.length;
	}

}
