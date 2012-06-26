package org.oobium.build.esp.dom.elements;

import org.oobium.build.esp.dom.EspElement;
import org.oobium.build.esp.dom.EspPart;

public class ImportElement extends EspElement {

	private EspPart importPart;
	private boolean isStatic;
	
	public ImportElement() {
		super(Type.ImportElement);
	}
	
	public EspPart getImportPart() {
		return importPart;
	}
	
	public boolean hasImportPart() {
		return importPart != null;
	}
	
	public boolean isStatic() {
		return isStatic;
	}

	public ImportElement setImportPart(EspPart importPart) {
		this.importPart = importPart;
		return this;
	}
	
	public ImportElement setStatic(boolean isStatic) {
		this.isStatic = isStatic;
		return this;
	}
	
}
