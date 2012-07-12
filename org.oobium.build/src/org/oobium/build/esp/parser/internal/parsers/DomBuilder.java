package org.oobium.build.esp.parser.internal.parsers;

import org.oobium.build.esp.dom.EspDom;
import org.oobium.build.esp.dom.EspDom.DocType;
import org.oobium.build.esp.parser.exceptions.EspEndException;

public class DomBuilder extends Builder {

	public DomBuilder(Scanner scanner) {
		super(scanner);
	}
	
	EspDom parse(DocType doctype, String name, char[] ca) {
		if(doctype == null) {
			throw new IllegalArgumentException("DocType has not been set for " + name);
		}
		EspDom dom = scanner.push(new EspDom(doctype, name, ca));
		scanner.setContainmentToEOE();
		try {
			switch(doctype) {
			case ESP: parseEsp(dom); break;
			case EMT: parseEsp(dom); break; // no difference now, may change in the future...
			case ESS: parseEss(dom); break;
			case EJS: parseEjs(dom); break;
			case CSS: parseCss(dom); break;
			case JS:  parseJs(dom);  break;
			default:
				throw new IllegalArgumentException("don't know how to parse DocType: " + doctype);
			}
		} catch(EspEndException e) {
			// we're done - exit
		}
		return dom;
	}
	
	private void parseCss(EspDom dom) throws EspEndException {
		scanner.parseStyleElement();
	}
	
	private void parseEjs(EspDom dom) throws EspEndException {
		char[] name = dom.getSimpleName().toCharArray();
		while(scanner.hasNext()) {
			try {
				scanner.forward();
				if(scanner.isChar('-')) {
					scanner.parseJavaElement();
				}
				else if(scanner.isCharSequence('i', 'm', 'p', 'o', 'r', 't')) {
					scanner.parseImportElement();
				}
				else if(scanner.isCharSequence(name)) {
					scanner.parseConstructorElement();
				}
				else {
					scanner.parseScriptElement();
				}
			} catch(EspEndException e) {
				scanner.popTo(dom);
			}
		}
	}
	
	private void parseEsp(EspDom dom) {
		char[] name = dom.getSimpleName().toCharArray();
		while(scanner.hasNext()) {
			try {
				scanner.forward();
				if(scanner.isChar('-')) {
					scanner.parseJavaElement();
				}
				else if(scanner.isCharSequence('i', 'm', 'p', 'o', 'r', 't')) {
					scanner.parseImportElement();
				}
				else if(scanner.isCharSequence(name)) {
					scanner.parseConstructorElement();
				}
				else if(scanner.isCharSequence('s', 'c', 'r', 'i', 'p', 't')) {
					scanner.parseScriptElement();
				}
				else if(scanner.isCharSequence('s', 't', 'y', 'l', 'e')) {
					scanner.parseStyleElement();
				}
				else if(scanner.isCharSequence('!', '-', '-')) {
					scanner.parseMarkupComment();
				}
				else {
					scanner.parseMarkupElement();
				}
			} catch(EspEndException e) {
				scanner.popTo(dom);
			}
		}
	}
	
	private void parseEss(EspDom dom) throws EspEndException {
		char[] name = dom.getSimpleName().toCharArray();
		while(scanner.hasNext()) {
			try {
				scanner.forward();
				if(scanner.isChar('-')) {
					scanner.parseJavaElement();
				}
				else if(scanner.isCharSequence('i', 'm', 'p', 'o', 'r', 't')) {
					scanner.parseImportElement();
				}
				else if(scanner.isCharSequence(name)) {
					scanner.parseConstructorElement();
				}
				else {
					scanner.parseStyleElement();
				}
			} catch(EspEndException e) {
				scanner.popTo(dom);
			}
		}
	}
	
	private void parseJs(EspDom dom) throws EspEndException {
		scanner.parseScriptElement();
	}

	
}
