package org.oobium.build.esp.dom.elements;

import org.oobium.build.esp.dom.EspElement;
import org.oobium.build.esp.dom.EspPart;

public class JavaElement extends EspElement {

	private EspPart source;
	
	public JavaElement() {
		super(Type.JavaElement);
	}

	public EspPart getSource() {
		return source;
	}
	
	public JavaElement setSource(EspPart source) {
		this.source = source;
		return this;
	}
	
}
