package org.oobium.build.esp.dom;

import static org.oobium.build.esp.dom.EspPart.Type.ImportElement;

import java.util.ArrayList;
import java.util.Iterator;

public class EspDom extends EspPart implements Iterable<EspPart> {

	public enum DocType {
		ESP, // Dynamic HTML (Elemental Server Page)
		EMT, // Dynamic HTML (Elemental Mailer Template)
		ESS, // Dynamic CSS (Elemental Style Sheet)
		EJS, // Dynamic JS (Elemental JavasSript)
		CSS, // Static CSS
		JS 	 // Static JS
	}
	
	
	private final String name;
	private final DocType doctype;
	final char[] ca;
	
	public EspDom(DocType doctype, String name, char[] ca) {
		super(Type.DOM);
		this.dom = this;
		this.name = name;
		this.doctype = doctype;
		this.ca = ca;
		this.start = 0;
		this.end = ca.length;
	}
	
	public DocType getDocType() {
		return doctype;
	}
	
	public String getName() {
		if(name == null) {
			throw new IllegalArgumentException("name has not been set");
		}
		if(doctype == null) {
			throw new IllegalArgumentException("DocType has not been set for " + new String(name));
		}
		
		switch(doctype) {
		case ESP:	return name + ".esp";
		case EMT:	return name + ".emt";
		case ESS:	return name + ".ess";
		case EJS:	return name + ".ejs";
		case CSS:	return name + ".css";
		case JS:	return name + ".js";
		default:
			throw new IllegalArgumentException("Invalid DocType: " + doctype);
		}
	}

	public int getNextImportOffset() {
		EspPart part = null;
		for(int i = 0; i < parts.size(); i++) {
			part = parts.get(i);
			if(!part.isA(ImportElement)) {
				if(i == 0) part = null;
				else part = parts.get(i-1);
				break;
			}
		}
		return (part != null) ? (part.getEnd() + 1) : 0;
	}
	
	public String getSimpleName() {
		return name;
	}
	
	public boolean is(DocType doctype) {
		return this.doctype == doctype;
	}
	
	public boolean isScript() {
		return doctype == DocType.EJS || doctype == DocType.JS;
	}
	
	public boolean isStyle() {
		return doctype == DocType.ESS || doctype == DocType.CSS;
	}
	
	@Override
	public Iterator<EspPart> iterator() {
		return (parts != null) ? parts.iterator() : new ArrayList<EspPart>(0).iterator();
	}
	
	@Override
	public EspPart setEnd(int end) {
		// ignore setting - the dom always ends at ca.length
		return this;
	}
	
	@Override
	public EspPart setParent(EspPart newParent) {
		// ignore setting - the dom's parent is always null (for it has no parent)
		return this;
	}
	
	@Override
	public EspPart setStart(int start) {
		// ignore setting - the dom always starts at 0
		return this;
	}
	
	public int size() {
		return (parts != null) ? parts.size() : 0;
	}
	
}
