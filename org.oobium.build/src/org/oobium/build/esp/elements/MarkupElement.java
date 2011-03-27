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
package org.oobium.build.esp.elements;

import static org.oobium.utils.CharStreamUtils.closer;
import static org.oobium.utils.CharStreamUtils.findEOL;
import static org.oobium.utils.CharStreamUtils.forward;
import static org.oobium.utils.CharStreamUtils.isEqual;
import static org.oobium.utils.CharStreamUtils.isWhitespace;
import static org.oobium.utils.CharStreamUtils.reverse;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.oobium.build.esp.EspElement;
import org.oobium.build.esp.EspPart;
import org.oobium.build.esp.parts.EntryPart;
import org.oobium.build.esp.parts.JavaContainerPart;

public class MarkupElement extends EspElement {

	public static final int NONE				= 0;
	public static final int JAVA_TYPE			= 1 << 0;
	public static final int JAVA_TYPE_REQUIRED	= 1 << 1;
	public static final int ID					= 1 << 2;
	public static final int CLASSES				= 1 << 3;
	public static final int ARGS				= 1 << 4;
	public static final int ENTRIES				= 1 << 5;
	public static final int CTOR_ARGS			= 1 << 6;
	public static final int STYLES				= 1 << 7;
	public static final int INNER_TEXT			= 1 << 8;
	public static final int INNER_TEXT_JAVA		= 1 << 9;
	public static final int CHILDREN			= 1 << 10;
	public static final int CLOSING_TAG			= 1 << 11;

	
	protected int style;
	protected EspPart tag;
	protected EspPart javaType;
	private EspPart id;
	private List<EspPart> classNames;
	private boolean hide;
	protected List<EspPart> args;
	protected Map<String, EntryPart> entries;
	protected EspPart innerText;
	protected List<EspElement> children;
	
	public MarkupElement(EspPart parent, int start) {
		super(parent, start);
		parse();
	}

	private void addArg(int start, int end) {
		if(args == null) {
			args = new ArrayList<EspPart>();
		}
		args.add(new JavaContainerPart(this, Type.ArgPart, start, end));
	}
	
	private void addArg(int start, int end, boolean isEntry) {
		if(isEntry) {
			if((style & ENTRIES) != 0) {
				addEntry(start, end);
			}
		} else {
			if((style & ARGS) != 0) {
				addArg(start, end);
			}
		}
	}
	
	protected void addChild(EspElement child) {
		if(children == null) {
			children = new ArrayList<EspElement>();
		}
		children.add(child);
	}
	
	private void addClassName(int start, int end) {
		if(classNames == null) {
			classNames = new ArrayList<EspPart>();
		}
		classNames.add(new JavaContainerPart(this, Type.ClassPart, start, end));
	}
	
	private void addEntry(int start, int end) {
		if(entries == null) {
			entries = new TreeMap<String, EntryPart>();
		}
		EntryPart entry = new EntryPart(this, start, end);
		EspPart key = entry.getKey();
		entries.put((key == null) ? "" : key.getText(), entry);
	}
	
	protected int createChild(int offset) {
		int start = forward(ca, offset);
		if(start == -1) {
			return offset;
		} else {
			EspElement element;
			if(ca[start] == '-') {
				element = new JavaElement(this, offset);
			} else if(InnerTextElement.isInnerText(ca, start)) {
				element = new InnerTextElement(this, offset);
			} else if(isNext(start, '/', '*')) {
				element = new CommentElement(this, offset);
			} else if(isNext(start, '/', '/')) {
				element = new CommentElement(this, offset);
			} else if(isNext(start, 's', 'c', 'r', 'i', 'p', 't')) {
				element = new ScriptElement(this, offset);
			} else if(isNext(start, 's', 't', 'y', 'l', 'e')) {
				element = new StyleElement(this, offset);
			} else if(Character.isLowerCase(ca[start])) {
				element = new MarkupElement(this, offset);
			} else {
				return findEOL(ca, offset);
			}
			addChild(element);
			return element.getEnd();
		}
	}

	protected int findEnd(int offset) {
		int eol = findEOL(ca, offset);
		while(offset < eol) {
			switch(ca[offset]) {
			case '"':
			case '{':
				offset = closer(ca, offset, ca.length, true);
				if(offset == -1) {
					offset = eol - 1;
				}
				break;
			case '\n':
				return offset;
			case '\r':
				if(offset < ca.length-1 && ca[offset+1] == '\n') {
					return offset;
				}
				break;
			case '<':
				if(offset < ca.length-1 && ca[offset+1] == '-') {
					return offset;
				}
				break;
			case '/':
				if(offset < ca.length-1) {
					if(ca[offset+1] == '/') {
						return offset;
					} else if(ca[offset+1] == '*') {
						offset = commentCheck(this, offset);
						if(offset >= ca.length) {
							return ca.length;
						}
					}
				}
				break;
			}
			offset++;
		}
		return offset;
	}
	
	public EspPart getArg(int index) {
		return args.get(index);
	}
	
	public List<EspPart> getArgs() {
		return args;
	}
	
	public EspElement getChild(int index) {
		return children.get(index);
	}
	
	public List<EspElement> getChildren() {
		return children;
	}

	public List<EspPart> getClassNames() {
		return classNames;
	}
	
	@Override
	public String getElementText() {
		if(parts != null) {
			int end = start;
			for(EspPart part : parts) {
				if(part instanceof EspElement) {
					break;
				} else {
					end = part.getEnd();
				}
			}
			end = reverse(ca, end-1) + 1;
			if(end < ca.length && ca[end] == ')') end++; // TODO crap hack
			return new String(ca, start, end - start);
		}
		return getText();
	}
	
	public Map<String, EntryPart> getEntries() {
		return entries;
	}
	
	public EntryPart getEntry(String name) {
		return entries.get(name);
	}
	
	public EspPart getEntryKey(String name) {
		EntryPart entry = entries.get(name);
		return (entry != null) ? entry.getKey() : null;
	}
	
	public String getEntryText(String name) {
		EspPart part = getEntryValue(name);
		return (part != null) ? part.getText() : null;
	}
	
	public EspPart getEntryValue(String name) {
		EntryPart entry = (entries != null) ? entries.get(name) : null;
		return (entry != null) ? entry.getValue() : null;
	}
	
	public EspPart getId() {
		return id;
	}
	
	public EspPart getInnerText() {
		return innerText;
	}

	public String getJavaType() {
		return (javaType != null) ? javaType.getText() : null;
	}
	
	public String getTag() {
		return (tag != null) ? tag.getText() : "";
	}
	
	public boolean handlesChildren() {
		return (style & CHILDREN) != 0;
	}

	public boolean hasArgs() {
		return args != null;
	}
	
	public boolean hasChildren() {
		return children != null;
	}

	public boolean hasClassNames() {
		return classNames != null;
	}
	
	public boolean hasClosingTag() {
		return (style & CLOSING_TAG) != 0;
	}
	
	public boolean hasEntries() {
		return entries != null;
	}
	
	public boolean hasEntry(String name) {
		return (entries != null) && entries.containsKey(name);
	}

	public boolean hasEntryValue(String name) {
		return (entries != null) && entries.containsKey(name) && (entries.get(name).getValue() != null);
	}
	
	public boolean hasId() {
		return id != null;
	}
	
	public boolean hasInnerText() {
		return innerText != null;
	}

	public boolean hasJavaType() {
		return javaType != null && javaType.getLength() > 0;
	}
	
	public boolean isHidden() {
		return hide;
	}

	protected void parse() {
		int s1 = start;
		int eoe = findEnd(s1);

		s1 = setTagTypeAndStyle(start, eoe);

		s1 = parseJavaType(s1, eoe);

		s1 = parseId(s1, eoe);

		s1 = parseClasses(s1, eoe);
		
		s1 = parseStyles(s1, eoe); // Styles (before arguments)
		
		s1 = parseArgsAndEntries(s1, eoe);
		
		s1 = parseStyles(s1, eoe); // Styles (after arguments)
		
		s1 = parseInnerText(s1, eoe);

		s1 = parseChildren(s1, eoe);
		
		end = (s1 < ca.length) ? s1 : ca.length;
	}
	
	protected void parseArgs(int start, int end) {
		boolean isEntry = false;
		int s1 = forward(ca, start, end);
		for(int s = s1; s1 != -1 && s < end; s++) {
			if(ca[s] == '"' || ca[s] == '{') {
				int s2 = closer(ca, s, end, true);
				if(s2 == -1) {
					s2 = end;
				}
				s = s2;
				if(s >= end-1) {
					addArg(s1, reverse(ca, end-1) + 1, isEntry);
				}
			} else if(ca[s] == ',') {
				if(s != 0) {
					addArg(s1, reverse(ca, s-1) + 1, isEntry);
					isEntry = false;
					s1 = forward(ca, s + 1, end);
				}
			} else if(ca[s] == ':') {
				isEntry = true;
				if(s == end-1) {
					addArg(s1, s + 1, isEntry);
				}
			} else if(s == end-1) {
				addArg(s1, reverse(ca, s) + 1, isEntry);
			}
		}
	}
	
	protected int parseArgsAndEntries(int start, int eoe) {
		int s1 = commentCheck(this, start);
		if((style & (ARGS | ENTRIES | CTOR_ARGS)) != 0) {
			if(s1 < eoe && ca[s1] == '(') { // Args
				int s2 = closer(ca, s1, eoe, true);
				if(s2 == -1) {
					s2 = eoe;
				}
				s1++;
				if(s2 > s1 && !isWhitespace(ca, s1, s2)) {
					parseArgs(s1, s2);
				}
				s1 = s2 + 1;
			}
		}
		return s1;
	}

	protected int parseChildren(int start, int eoe) {
		int s1 = commentCheck(this, start);
		if(eoe < ca.length && ca[eoe] == '/') { // a comment finishes the line
			s1 = createChild(eoe);
		} else if(eoe < ca.length && ca[eoe] == '<') { // in-line child
			s1 = createChild(eoe + 2);
		} else {
			s1 = eoe;
			while(s1 < ca.length) {
				int level = 0;
				while((s1+1+level) < ca.length && ca[s1+1+level] == '\t') {
					level++;
				}
				if(this.level < level) {
					int pos = s1+1+level;
					if(pos >= ca.length) {
						break;
					} else if(ca[pos] == '\n') { // handle lines that are only tabs chars (*nix)
						s1 = pos;
					} else if(ca[pos] == '\r') { // handle lines that are only tabs chars (windows)
						if((pos+1) < ca.length && ca[pos+1] == '\n') {
							s1 = pos + 1;
						} else {
							s1 = pos;
						}
					} else {
						s1 = createChild(s1+1);
					}
				} else {
					break;
				}
			}
		}
		return s1;
	}
	
	protected int parseClasses(int start, int eoe) {
		int s1 = commentCheck(this, start);
		if((style & CLASSES) != 0) {
			while(s1 < eoe && ca[s1] == '.') { // Class(es)
				int s2 = s1 = s1 + 1;
				while(s2 < eoe) {
					if('_' != ca[s2] && '-' != ca[s2] && !Character.isLetterOrDigit(ca[s2])) {
						if('{' == ca[s2]) {
							s2 = closer(ca, s2, eoe, true);
							if(s2 == -1) {
								s2 = eoe;
								break;
							} else if((ca[s2] == '*' && ca[s2-1] == '/') || (ca[s2] == '*' && ca[s2-1] == '/')) {
								
							}
						} else {
							break;
						}
					}
					s2++;
				}
				if(s2 > s1) {
					addClassName(s1, s2);
					s1 = s2;
				}
			}
		}
		return s1;
	}
	
	protected int parseId(int start, int eoe) {
		int s1 = commentCheck(this, start);
		if((style & ID) != 0) {
			if(s1 < eoe && ca[s1] == '#') { // ID
				int s2 = s1 = s1 + 1;
				while(s2 < eoe) {
					if('[' != ca[s2] && ']' != ca[s2] && '_' != ca[s2] && '-' != ca[s2] && !Character.isLetterOrDigit(ca[s2])) {
						if('{' == ca[s2]) {
							s2 = closer(ca, s2, eoe, true);
							if(s2 == -1) {
								s2 = eoe;
								break;
							}
						} else {
							break;
						}
					}
					s2++;
				}
				if(s2 > s1) {
					id = new JavaContainerPart(this, Type.IdPart, s1, s2);
					s1 = s2;
				}
			}
		}
		return s1;
	}
	
	protected int parseInnerText(int start, int eoe) {
		int s1 = commentCheck(this, start);
		if((style & INNER_TEXT) != 0) {
			if(s1 < eoe-1 && ca[s1] == ' ') { // inner html
				if((style & INNER_TEXT_JAVA) != 0) {
					innerText = new JavaContainerPart(this, Type.InnerTextPart, s1+1, eoe);
				} else {
					innerText = new EspPart(this, Type.InnerTextPart, s1+1, eoe);
				}
			}
		}
		return s1;
	}

	protected int parseJavaType(int start, int eoe) {
		int s1 = commentCheck(this, start);
		if((style & JAVA_TYPE) != 0) {
			if(s1 < eoe && ca[s1] == '<') { // Type
				int s2 = closer(ca, s1, eoe, true);
				if(s2 == -1) {
					s2 = eoe;
				}
				javaType = new EspPart(this, Type.JavaTypePart, ++s1, s2);
				s1 = s2+1;
			}
		}
		return s1;
	}

	private int parseStyles(int start, int eoe) {
		int s1 = commentCheck(this, start);
		if((style & STYLES) != 0) {
			if(s1 < eoe && ca[s1] == '|') { // Styles (after arguments)
				int s2 = s1 + 1;
				while(s2 < eoe && Character.isLetter(ca[s2])) {
					s2++;
				}
				if(isEqual(ca, s1+1, s2, "hide")) {
					hide = true;
				}
				new EspPart(this, Type.StylePart, s1+1, s2);
				s1 = s2;
			}
		}
		return s1;
	}

	protected int setTagTypeAndStyle(int start, int end) {
		int s1 = start;
		while(s1 < end && (ca[s1] == '_' || Character.isLetterOrDigit(ca[s1]))) {
			s1++;
		}

		tag = new EspPart(this, Type.TagPart, start, s1);
		
		String tag = new String(ca, start, s1-start);
		if("form".equals(tag)) {
			type = Type.MarkupElement;
			style = JAVA_TYPE | ID | CLASSES | ARGS | ENTRIES | STYLES | INNER_TEXT | INNER_TEXT_JAVA | CHILDREN | CLOSING_TAG;
		} else if("a".equals(tag) || "link".equals(tag) || "button".equals(tag)) {
			type = Type.MarkupElement;
			style = ID | CLASSES | ARGS | ENTRIES | STYLES | INNER_TEXT | INNER_TEXT_JAVA | CHILDREN | CLOSING_TAG;
		} else if("fields".equals(tag)) {
			type = Type.MarkupElement;
			style = ARGS | ENTRIES;
		} else if("textArea".equals(tag)) {
			type = Type.MarkupElement;
			style = ID | CLASSES | ARGS | ENTRIES | STYLES | CLOSING_TAG;
		} else if("select".equals(tag)) {
			type = Type.MarkupElement;
			style = ID | CLASSES | ARGS | ENTRIES | STYLES | CHILDREN | CLOSING_TAG;
		} else if("option".equals(tag)) {
			type = Type.MarkupElement;
			style = ID | CLASSES | ARGS | STYLES | INNER_TEXT | CLOSING_TAG;
		} else if("options".equals(tag)) {
			type = Type.MarkupElement;
			style = JAVA_TYPE | ARGS | ENTRIES;
		} else if("label".equals(tag)) {
			type = Type.MarkupElement;
			style = JAVA_TYPE | ID | CLASSES | ARGS | ENTRIES | STYLES | INNER_TEXT | INNER_TEXT_JAVA | CHILDREN | CLOSING_TAG;
		} else if("radio".equals(tag) || "reset".equals(tag) || "date".equals(tag)
				|| "submit".equals(tag) || "hidden".equals(tag) || "check".equals(tag)
				|| "file".equals(tag) || "input".equals(tag) || "number".equals(tag)
				|| "password".equals(tag) || "text".equals(tag)) {
			type = Type.MarkupElement;
			style = ID | CLASSES | ARGS | ENTRIES | STYLES;
		} else if("img".equals(tag)) {
			type = Type.MarkupElement;
			style = ID | CLASSES | ARGS | ENTRIES | STYLES | INNER_TEXT | INNER_TEXT_JAVA | CHILDREN | CLOSING_TAG;
		} else if("capture".equals(tag) || "contentFor".equals(tag)) {
			type = Type.MarkupElement;
			style = ARGS | CHILDREN;
		} else if("view".equals(tag)) {
			type = Type.MarkupElement;
			style = JAVA_TYPE | JAVA_TYPE_REQUIRED | ARGS | ENTRIES;
		} else if("head".equals(tag)) {
			type = Type.MarkupElement;
			style = CHILDREN;
		} else if("errors".equals(tag)) {
			type = Type.MarkupElement;
			style = ARGS | ENTRIES;
		} else if("messages".equals(tag)) {
			type = Type.MarkupElement;
			style = NONE;
		} else if("title".equals(tag)) {
			type = Type.MarkupElement;
			style = INNER_TEXT | INNER_TEXT_JAVA;
		} else if("meta".equals(tag)) {
			type = Type.MarkupElement;
			style = ENTRIES;
		} else if("script".equals(tag)) {
			throw new IllegalStateException("parsing a script element as a markup element");
		} else if("style".equals(tag)) {
			throw new IllegalStateException("parsing a style element as a markup element");
		} else if("yield".equals(tag)) {
			type = Type.YieldElement;
			style = ARGS;
		} else {
			type = Type.MarkupElement;
			style = ID | CLASSES | ENTRIES | STYLES | INNER_TEXT | INNER_TEXT_JAVA | CHILDREN | CLOSING_TAG;
		}
		
		return s1;
	}

}
