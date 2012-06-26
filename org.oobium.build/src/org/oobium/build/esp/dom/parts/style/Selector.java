package org.oobium.build.esp.dom.parts.style;

import org.oobium.build.esp.dom.EspPart;

public class Selector extends EspPart {

	public Selector() {
		super(Type.StyleSelector);
	}

	public Declaration getDeclaration() {
		return getRuleset().getDeclaration();
	}
	
	@Override
	public SelectorGroup getParent() {
		return (SelectorGroup) super.getParent();
	}
	
	public Ruleset getRuleset() {
		return getParent().getParent();
	}
	
}
