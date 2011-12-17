/*******************************************************************************
 * Copyright (c) 2010 Oobium, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Jeremy Dowdall <jeremy@oobium.com> - initial API and implementation
 ******************************************************************************/
package org.oobium.build.esp;

import static org.oobium.build.esp.EspPart.Type.ImportElement;
import static org.oobium.utils.CharStreamUtils.findEOL;
import static org.oobium.utils.CharStreamUtils.forward;
import static org.oobium.utils.StringUtils.join;
import static org.oobium.utils.StringUtils.when;

import org.oobium.build.BuildBundle;
import org.oobium.build.esp.elements.CommentElement;
import org.oobium.build.esp.elements.ConstructorElement;
import org.oobium.build.esp.elements.ImportElement;
import org.oobium.build.esp.elements.JavaElement;
import org.oobium.build.esp.elements.MarkupCommentElement;
import org.oobium.build.esp.elements.MarkupElement;
import org.oobium.build.esp.elements.ScriptElement;
import org.oobium.build.esp.elements.StyleElement;
import org.oobium.logging.LogProvider;


public class EspDom extends EspPart {

	public enum DocType {
		ESP, // Dynamic HTML (Elemental Server Page)
		EMT, // Dynamic HTML (Elemental Mailer Template)
		ESS, // Dynamic CSS (Elemental Style Sheet)
		EJS, // Dynamic JS (Elemental JavasSript)
		CSS, // Static CSS
		JS 	 // Static JS
	}
	
	/**
	 * the name of the document; used to determine constructors
	 */
	private char[] name;
	
	/**
	 * the type of the document; defaults to {@link DocType#ESP}
	 */
	private DocType type;

	
	public EspDom(String name, CharSequence src) {
		super(null, Type.DOM, 0, src.length());
		this.dom = this;
		setName(name);
		setSource(src);
	}

	/**
	 * Get the Element at the given index.
	 * @param index
	 */
	public EspElement get(int index) {
		return (EspElement) parts.get(index);
	}
	
	public DocType getDocType() {
		return type;
	}
	
	@Override
	public EspElement getElement() {
		return null;
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
		return (part != null) ? (part.end + 1) : 0;
	}
	
	public String getName() {
		return new String(name);
	}

	public boolean isEjs() {
		return type == DocType.EJS;
	}
	
	public boolean isEmpty() {
		return parts == null || parts.isEmpty();
	}
	
	public boolean isEmt() {
		return type == DocType.EMT;
	}
	
	public boolean isEsp() {
		return type == DocType.ESP;
	}
	
	public boolean isEss() {
		return type == DocType.ESS;
	}
	
	/**
	 * Is the DOM for a Script (.js or .ejs files)
	 */
	public boolean isScript() {
		return type == DocType.JS || type == DocType.EJS;
	}
	
	public boolean isStatic() {
		return type == DocType.CSS || type == DocType.JS;
	}

	/**
	 * Is the DOM for a Style Sheet (.css or .ess files)
	 */
	public boolean isStyle() {
		return type == DocType.CSS || type == DocType.ESS;
	}
	
	private void parse() {
		switch(type) {
		case ESP:	parseEsp(); break;
		case EMT:	parseEsp(); break; // no difference now, will change in the future...
		case ESS:	parseEss(); break;
		case EJS:	parseEjs(); break;
		case CSS:	parseCss(); break;
		case JS:	parseJs();	break;
		default:
			throw new IllegalArgumentException("don't know how to parse DocType: " + type);
		}
	}
	
	private void parseCss() {
		new StyleElement(this, 0);
	}
	
	private void parseEjs() {
		boolean inScript = false;
		int offset = 0;
		while(offset < ca.length) {
			EspElement element = null;
			
			int eol = findEOL(ca, offset);
			int start = forward(ca, offset, eol);
			if(start != -1) {
				if(!Character.isWhitespace(ca[start])) {
					if(isNext(start, '/', '*')) {
						element = new CommentElement(this, offset);
					} else if(isNext(start, '/', '/')) {
						element = new CommentElement(this, offset);
					} else {
						if(inScript) {
							element = new ScriptElement(this, offset);
						} else {
							if(ca[start] == '-') {
								element = new JavaElement(this, offset);
							} else if(isNext(start, 'i', 'm', 'p', 'o', 'r', 't')) {
								element = new ImportElement(this, offset);
							} else if(isNext(start, name)) {
								element = new ConstructorElement(this, offset);
							} else {
								element = new ScriptElement(this, offset);
								inScript = true;
							}
						}
					}
				}
			}
			
			if(element != null) {
				offset = element.getEnd() + 1;
			} else {
				offset = eol + 1;
			}
		}
	}
	
	private void parseEsp() {
		int offset = 0;
		while(offset < ca.length) {
			EspElement element = null;
			
			int eol = findEOL(ca, offset);
			int start = forward(ca, offset, eol);
			if(start != -1) {
				if(!Character.isWhitespace(ca[start])) {
					if(ca[start] == '-') {
						element = new JavaElement(this, offset);
					} else if(isNext(start, '/', '*')) {
						element = new CommentElement(this, offset);
					} else if(isNext(start, '/', '/')) {
						element = new CommentElement(this, offset);
					} else if(isNext(start, 'i', 'm', 'p', 'o', 'r', 't')) {
						element = new ImportElement(this, offset);
					} else if(isNext(start, name)) {
						element = new ConstructorElement(this, offset);
					} else if(isNext(start, 's', 'c', 'r', 'i', 'p', 't')) {
						element = new ScriptElement(this, offset);
					} else if(isNext(start, 's', 't', 'y', 'l', 'e')) {
						element = new StyleElement(this, offset);
					} else if(isNext(start, '!', '-', '-')) {
						element = new MarkupCommentElement(this, offset);
					} else if(Character.isLowerCase(ca[start])){
						element = new MarkupElement(this, offset);
					}
				}
			}
			
			if(element != null) {
				offset = element.getEnd() + 1;
			} else {
				offset = eol + 1;
			}
		}
	}
	
	private void parseEss() {
		boolean inCss = false;
		int offset = 0;
		while(offset < ca.length) {
			EspElement element = null;
			
			int eol = findEOL(ca, offset);
			int start = forward(ca, offset, eol);
			if(start != -1) {
				if(!Character.isWhitespace(ca[start])) {
					if(isNext(start, '/', '*')) {
						element = new CommentElement(this, offset);
					} else if(isNext(start, '/', '/')) {
						element = new CommentElement(this, offset);
					} else {
						if(inCss) {
							element = new StyleElement(this, offset);
						} else {
							if(ca[start] == '-') {
								element = new JavaElement(this, offset);
							} else if(isNext(start, 'i', 'm', 'p', 'o', 'r', 't')) {
								element = new ImportElement(this, offset);
							} else if(isNext(start, name)) {
								element = new ConstructorElement(this, offset);
							} else {
								element = new StyleElement(this, offset);
								inCss = true;
							}
						}
					}
				}
			}
			
			if(element != null) {
				offset = element.getEnd() + 1;
			} else {
				offset = eol + 1;
			}
		}
	}
	
	private void parseJs() {
		new ScriptElement(this, 0);
	}

	public void setName(String name) {
		if(name == null) {
			this.name = new char[0];
			this.type = DocType.ESP;
		} else {
			int ix = name.indexOf('.');
			if(ix == -1) {
				this.name = name.toCharArray();
				this.type = DocType.ESP;
			} else {
				this.name = new char[ix];
				name.getChars(0, ix, this.name, 0);
				String type = name.substring(ix+1);
				switch(when(type.toLowerCase(), "esp", "emt", "ess", "ejs", "css", "js", "json")) {
				case 0: this.type = DocType.ESP; break;
				case 1: this.type = DocType.EMT; break;
				case 2: this.type = DocType.ESS; break;
				case 3: this.type = DocType.EJS; break;
				case 4: this.type = DocType.CSS; break;
				case 5:
				case 6: this.type = DocType.JS; break;
				}
			}
		}
		if(ca != null) {
			setSource(ca);
		}
	}
	
	private void setSource(char[] ca) {
		if(parts != null) {
			parts.clear();
			parts = null;
		}
		this.ca = ca;
		this.end = ca.length;
		if(ca.length > 0) {
			try {
				parse();
			} catch(Throwable t) {
				t.printStackTrace();
				LogProvider.getLogger(BuildBundle.class).warn(t);
			}
		}
	}

	public void setSource(CharSequence  src) {
		setSource((src != null) ? src.toString().toCharArray() : new char[0]);
	}
	
	public int size() {
		return isEmpty() ? 0 : parts.size();
	}

	@Override
	public String toString() {
		return join(parts, '\n');
	}
	
}
