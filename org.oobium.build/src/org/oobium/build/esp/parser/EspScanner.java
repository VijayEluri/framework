//package org.oobium.build.esp.parser;
//
//import org.oobium.build.esp.dom.EspElement;
//import org.oobium.build.esp.dom.EspPart;
//import org.oobium.build.esp.dom.EspPart.Type;
//import org.oobium.build.esp.dom.parts.Comment;
//import org.oobium.build.esp.dom.parts.JavaPart;
//import org.oobium.build.esp.parser.exceptions.EspEndException;
//import org.oobium.build.esp.parser.exceptions.EspException;
//import org.oobium.build.esp.parser.exceptions.IncompleteException;
//import org.oobium.build.esp.parser.exceptions.UnexpectedException;
//
//public class EspScanner {
//
//	/** End of Line */
//	public static final char EOL = '\n';
//	
//	/** End of Element */
//	public static final char EOE = (char) 25; // "End of Element"
//
//	private static boolean all(char c, char...test) {
//		for(char t : test) {
//			if(c != t) {
//				return false;
//			}
//		}
//		return true;
//	}
//
//	private static boolean any(char c, char...test) {
//		for(char t : test) {
//			if(c == t) {
//				return true;
//			}
//			if(t == ' ' && Character.isWhitespace(c)) {
//				return true;
//			}
//		}
//		return false;
//	}
//	
//	private final char[] ca;
//	private EspPart part;
//	
//	private int level;
//	
//	private int mark;
//	private int offset;
//
//	public EspScanner(EspPart part, int offset) {
//		this.part = part;
//		this.ca = part.ca;
//		this.mark = -1;
//		this.offset = offset;
//		this.level = getLevelAt(part.getStart());
//	}
//
//	/**
//	 * @return the position of the end of the comment
//	 * @throws EspEndException if comment consumes the rest of the document (in which case, this method
//	 * would have return ca.length)
//	 */
//	public EspScanner checkComment() throws EspEndException {
//		int next = offset + 1;
//		if(next < ca.length) {
//			if(ca[offset] == '/') {
//				if(ca[next] == '/') {
//					offset = next;
//					while(++offset < ca.length && ca[offset] != '\n');
//					new Comment(part, next-1, offset);
//					if(offset == ca.length) throw EspEndException.instance(offset);
//				}
//				if(ca[next] == '*') {
//					offset = next;
//					while(++offset < ca.length && !(ca[offset] == '/' && ca[offset-1] == '*'));
//					new Comment(part, next-1, offset);
//					if(offset == ca.length) throw EspEndException.instance(offset);
//				}
//			}
//		}
//		return this;
//	}
//	
//	public EspScanner checkJava() {
//		if(isChar('$') && !isCharEscaped()) {
//			new JavaPart(part, this);
//		}
//		return this;
//	}
//	
//	public void checkJavaIdentifierPart() {
//		if(!Character.isJavaIdentifierPart(getChar())) {
//			throw EspException.instance(offset);
//		}
//	}
//
//	public void checkJavaIdentifierStart() {
//		if(!Character.isJavaIdentifierStart(getChar())) {
//			throw EspException.instance(offset);
//		}
//	}
//	
//	public EspScanner checkLevel() throws EspEndException {
//		if(isChar('\n')) {
//			for(int j = ++offset; offset < ca.length; offset++) {
//				int l2 = offset-j;
//				if(l2 <= level) {
//					EspPart p = part;
//					while(p != null) {
//						if(p.end < p.start) {
//							p.end = j;
//						}
//						p = p.parent;
//					}
//					throw EspEndException.instance(j);
//				}
//				if(ca[offset] != '\t') break;
//			}
//			if(offset < ca.length) {
//				throw EspEndException.instance(offset);
//			}
//		}
//		return this;
//	}
//	
//	public void checkNext(char...c) {
//		if(!isNext(c)) {
//			throw UnexpectedException.instance(offset, new String(c));
//		}
//	}
//	
//	public EspScanner findCloser() throws IncompleteException {
//		if(offset >= 0 && offset+1 < ca.length) {
//			char opener = ca[offset];
//			char closer = closerChar(opener);
//			if(closer != 0) {
//				return closer(opener, closer);
//			}
//		}
//		return this;
//	}
//	
//	public EspScanner findCloserIf(char...test) throws IncompleteException {
//		if(isChar(test)) {
//			findCloser();
//		}
//		return this;
//	}
//
//	private EspScanner closer(char opener, char closer) {
//		boolean escapeComments = (opener != '"' && opener != '\''); // quotes beat comments!
//		
//		int count = 1;
//		try {
//			while(true) {
//				next();
//				if(ca[offset] == opener && ca[offset] != closer) {
//					count++;
//				}
//				else if(ca[offset] == closer) {
//					if((closer != '"' && closer != '\'') || ca[offset-1] != '\\') { // check for escape char
//						count--;
//						if(count == 0) {
//							return this;
//						}
//					}
//				}
//				else if(ca[offset] == '"') {
//					closer('"', '"'); // just entered a string - get out of it
//				}
//				else if(escapeComments) {
//					checkComment();
//				}
//				else if(opener == '"') {
//					checkJava();
//				}
//			}
//		} catch(EspEndException e) {
//			// fall through
//		}
//		throw IncompleteException.instance(offset);
//	}
//	
//	public char closerChar(char c) {
//		switch(c) {
//			case '<':  return '>';
//			case '(':  return ')';
//			case '{':  return '}';
//			case '[':  return ']';
//			case '"':  return '"';
//			case '\'': return '\'';
//			default: return 0;
//		}
//	}
//	
//	public EspScanner find(char c) {
//		if(getChar() != c) {
//			while(next(c != EOL).getChar() != c);
//		}
//		return this;
//	}
//	
//	public EspScanner findAll(char...test) {
//		boolean checkLevel = !any(EOL, test);
//		if(!all(getChar(), test)) {
//			while(!all(next(checkLevel).getChar(), test));
//		}
//		return this;
//	}
//
//	public EspScanner findAny(char...test) throws EspEndException {
//		boolean checkLevel = !any(EOL, test);
//		if(!any(getChar(), test)) {
//			while(!any(next(checkLevel).getChar(), test));
//		}
//		return this;
//	}
//	
//	public int findChild() {
//		while(offset < ca.length) {
//			checkComment();
//			if(ca[offset] == '\n') {
//				int l2 = getLevelAt(offset+1);
//				if(l2 > level) {
//					return offset + l2;
//				} else {
//					break; // fall through and throw EoeExceptoion
//				}
//			}
//			offset++;
//		}
//		throw EspEndException.instance(offset);
//	}
//	
//	public EspScanner findEndOfCssSelector() {
//		if(isWordChar() || ca[offset] == '.' || ca[offset] == '#') {
//			while(next(false).isWordChar() || ca[offset] == '-' || ca[offset] == '[' || ca[offset] == ']');
//		}
//		return this;
//	}
//
//	public EspScanner findEndOfElement() {
//		try {
//			while(next().hasNext());
//		} catch(EspEndException e) {
//			offset = e.getOffset();
//		}
//		return this;
//	}
//	
//	public EspScanner findEndOfJavaIdentifier() {
//		if(Character.isJavaIdentifierStart(getChar())) {
//			while(Character.isJavaIdentifierPart(next(false).getChar()));
//		}
//		return this;
//	}
//
//	public EspScanner findEndOfLine() {
//		return find(EOL);
//	}
//
//	public EspScanner findEndOfWord() {
//		if(isWordChar()) {
//			while(next(false).isWordChar());
//		}
//		return this;
//	}
//
//	/**
//	 * Advance from offset (inclusive) to the first non-whitespace character.
//	 * The search goes until either the character string ends or the part's element ends
//	 * (a {@link EspEndException} will be thrown in this case).
//	 * @param part the EspPart to search in (uses it's char[] and element level)
//	 * @param start the position to offset the search
//	 * @return the position of the first non-whitespace character in the given part's element, or -1 if one is not found
//	 * @throws EspEndException if the part's element ends before a non-whitespace character is found
//	 */
//	public EspScanner forward() throws EspEndException {
//		checkComment();
//		if(Character.isWhitespace(getChar())) {
//			while(Character.isWhitespace(next().getChar()));
//		}
//		return this;
//	}
//
//	public char getChar() {
//		if(offset < ca.length) {
//			if(ca[offset] == '<') {
//				int next = offset + 1;
//				if(next < ca.length && ca[next] == '-') {
//					// TODO ???
//					EspPart sep = new EspPart(part.dom, Type.ElementSeparator, offset, offset+2);
//					offset = sep.end;
//					return EOL;
//				}
//			}
//			return ca[offset];
//		}
//		return EOL;
//	}
//	
//	public int getLevel() {
//		return level;
//	}
//	
//	public int getLevelAt(int offset) {
//		int i = offset;
//		while(i > 0) {
//			if(ca[i] == '\n') {
//				break;
//			}
//			i--;
//		}
//		int j = i;
//		for( ; i < ca.length; i++) {
//			if(ca[i] != '\t') break;
//		}
//		return i-j;
//	}
//	
//	public int getMark() {
//		return mark;
//	}
//	
//	public int getOffset() {
//		return offset;
//	}
//
//	public boolean hasChild() {
//		try {
//			findChild();
//			return true;
//		} catch(EspEndException e) {
//			return false;
//		}
//	}
//	
//	public boolean hasNext() {
//		return offset < ca.length;
//	}
//	
//	public boolean isChar(char c) {
//		if(offset < ca.length) {
//			return ca[offset] == c;
//		}
//		return c == EOL;
//	}
//	
//	public boolean isChar(char...c) {
//		if(offset < ca.length) {
//			return any(ca[offset], c);
//		}
//		return any(EOL, c);
//	}
//	
//	public boolean isCharEscaped() {
//		if(offset >= 1) {
//			return ca[offset-1] == '\\' && ca[offset-2] == '\\';
//		}
//		return false;
//	}
//	
//	public boolean isEmpty() {
//		return length() == 0;
//	}
//	
//	public boolean isEndOfLine() {
//		return isChar(EOL);
//	}
//	
//	public boolean isLowerCase() {
//		if(offset < ca.length) {
//			return Character.isLowerCase(ca[offset]);
//		}
//		return false;
//	}
//	
//	public boolean isNext(char c) {
//		if(ca[offset+1] == c) {
//			offset++;
//			return true;
//		}
//		return false;
//	}
//
//	public boolean isNext(char...c) {
//		for(int i = 0; i < c.length; i++) {
//			int j = offset+i+1;
//			if(j >= ca.length || ca[j] != c[i]) {
//				return false;
//			}
//		}
//		offset += c.length;
//		return true;
//	}
//	
//	public boolean isNotChar(char c) {
//		return ! isChar(c);
//	}
//	
//	public boolean isNotChar(char...c) {
//		return ! isChar(c);
//	}
//	
//	public boolean isNotEndOfLine() {
//		return ! isChar(EOL);
//	}
//
//	public boolean isNotWordChar() {
//		return ! isWordChar();
//	}
//	
//	public boolean isWhitespace() {
//		return Character.isWhitespace(getChar());
//	}
//
//	public boolean isWordChar() {
//		char c = getChar();
//		return Character.isLetterOrDigit(c) || c == '_';
//	}
//	
//	public int length() {
//		return (mark == -1) ? 0 : (offset - mark);
//	}
//	
//	public EspScanner mark() {
//		mark = offset;
//		return this;
//	}
//	
//	public EspScanner next() {
//		return next(true);
//	}
//	
//	private EspScanner next(boolean checkLevel) {
//		offset++;
//		checkComment();
//		if(checkLevel) {
//			checkLevel();
//		}
//		return this;
//	}
//	
//	public EspPart pop() {
//		EspPart prev = part;
//		if(prev != null && prev.end < prev.start) {
//			prev.end = offset;
//		}
//		part = (part.parent != null) ? part.parent : part.dom;
//		return prev;
//	}
//
//	public EspPart pop(EspPart to) {
//		EspPart popped;
//		while((popped = pop()) != to);
//		return popped;
//	}
//
//	public EspPart push() {
//		return push(Type.Unknown);
//	}
//	
//	public EspPart push(Type type) {
//		this.part = new EspPart(part, type, offset);
//		return this.part;
//	}
//	
//	public EspScanner push(EspPart part) {
//		this.part = part;
//		if(part instanceof EspElement) {
//			level = ((EspElement) part).getLevel();
//		}
//		return this;
//	}
//	
//	public <T extends EspPart> T replaceWith(T replacement) {
//		part.parent.parts.remove(part);
//		if(part.parts != null) {
//			replacement.parts = part.parts;
//			for(EspPart p : part.parts) {
//				p.parent = replacement;
//			}
//		}
//		part = replacement;
//		return replacement;
//	}
//	
//	public EspScanner reverse() {
//		int orig = offset;
//		while(offset >= part.start && offset < ca.length) {
//			if(!Character.isWhitespace(ca[offset])) {
//				return this;
//			}
//			offset--;
//		}
//		throw UnexpectedException.instance(-1, "any non-whitespace character between " + part.start + " and " + orig, "none");
//	}
//	
//	public EspScanner setOffset(int offset) {
//		this.offset = offset;
//		return this;
//	}
//	
//}
