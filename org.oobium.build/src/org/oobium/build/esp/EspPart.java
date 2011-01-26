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

import java.util.ArrayList;
import java.util.List;

import org.oobium.build.esp.parts.CommentPart;
import org.oobium.utils.CharStreamUtils;

public class EspPart implements CharSequence {

	public enum Type {
		DOM,
		ImportElement, ImportPart,
		ConstructorElement, CtorArgPart, VarTypePart, VarNamePart, DefaultValuePart,
		HtmlElement, InnerTextElement, YieldElement,
		JavaElement, JavaPart, JavaSourcePart,
		JavaTypePart, TagPart, IdPart, ClassPart, ArgPart, EntryPart, StyleEntryPart, EntryKeyPart, EntryValuePart, InnerTextPart, StylePart,
		CommentElement, Unknown,
		ScriptElement, ScriptPart,
		StyleElement, StyleChildElement, StyleSelectorPart, StylePropertyPart, StylePropertyNamePart, StylePropertyValuePart,
		CommentPart
		}

	
	public static int commentCheck(EspPart parent, int ix) {
		return commentCheck(parent, parent.ca, ix);
	}
	
	private static int commentCheck(EspPart parent, char[] ca, int ix) {
		if(ix >= 0) {
			if(ix < ca.length-1 && ca[ix] == '/' && ca[ix+1] == '*') {
				CommentPart comment = new CommentPart(parent, ix);
				ix = comment.getEnd();
			}
		}
		return ix;
	}
	
	public static int commentCloser(char[] ca, int ix) {
		if(ix < ca.length-1 && ca[ix] == '/' && ca[ix+1] == '*') {
			ix += 2;
			while(ix < ca.length) {
				if(ca[ix] == '/' && ca[ix-1] == '*') {
					break;
				}
				ix++;
			}
		}
		return ix;
	}
	
	
	protected EspDom dom;
	protected EspPart parent;
	protected char[] ca;
	protected Type type;
	protected int start;
	protected int end;
	protected List<EspPart> parts;
	
	public EspPart(EspPart parent, int start) {
		if(parent != null) this.dom = parent.dom;
		this.parent = parent;
		this.start = start;
		if(parent != null) {
			this.ca = parent.ca;
			for(int i = start; i < end; i++) {
				i = commentCheck(this, ca, i);
			}
			parent.addPart(this);
		}
	}
	
	public EspPart(EspPart parent, Type type, int start, int end) {
		this(parent, type, start, end, false);
	}
	
	public EspPart(EspPart parent, Type type, int start, int end, boolean literal) {
		if(parent != null) this.dom = parent.dom;
		this.parent = parent;
		this.type = type;
		this.start = start;
		this.end = end;
		if(parent != null) {
			this.ca = parent.ca;
			if(!literal) {
				for(int i = start; i < end; i++) {
					i = commentCheck(this, ca, i);
				}
			}
			parent.addPart(this);
		}
	}
	
	protected void addPart(EspPart part) {
		if(parts == null) {
			parts = new ArrayList<EspPart>();
		}
		parts.add(part);
	}
	
	@Override
	public char charAt(int index) {
		return ca[index];
	}
	
	public EspDom getDom() {
		return dom;
	}
	
	public EspElement getElement() {
		EspPart part = parent;
		while(part != null) {
			if(part instanceof EspElement) {
				return part.getElement();
			}
			part = part.parent;
		}
		return null;
	}
	
	public int getEnd() {
		return end;
	}

	public int getLength() {
		return end - start;
	}
	
	public EspPart getNextSubPart(int offset) {
		if(parts != null) {
			for(EspPart part : parts) {
				if(part.start > offset) {
					return part.getPart(part.start);
				}
			}
		}
		return null;
	}
	
	public EspPart getParent() {
		return parent;
	}
	
	public EspPart getPart(int offset) {
		if(parts != null) {
			for(EspPart part : parts) {
				if(part.start <= offset && offset < part.end) {
					return part.getPart(offset);
				}
			}
		}
		if(start <= offset && offset < end) {
			return this;
		}
		return null;
	}

	public List<EspPart> getParts() {
		return new ArrayList<EspPart>(parts);
	}
	
	public int getStart() {
		return start;
	}
	
	public String getText() {
		return new String(ca, start, end-start);
	}
	
	public Type getType() {
		return type;
	}
	
	public boolean hasParts() {
		return parts != null;
	}
	
	public boolean isA(Type type) {
		return this.type == type;
	}
	
	public boolean isElementA(Type type) {
		EspElement element = getElement();
		return (element != null) && element.isA(type);
	}

	@Override
	public int length() {
		return getLength();
	}

	public boolean isNext(int start, char...test) {
		return CharStreamUtils.isNext(ca, start, test);
	}
	
	@Override
	public CharSequence subSequence(int start, int end) {
		return new String(ca, start, end-start);
	}

	public String substring(int beginIndex) {
		return new String(ca, beginIndex, end-beginIndex);
	}

	public String substring(int beginIndex, int endIndex) {
		return new String(ca, beginIndex, endIndex-beginIndex);
	}

	@Override
	public String toString() {
		return type.name() + " {" + getText() + "}";
	}
	
}
