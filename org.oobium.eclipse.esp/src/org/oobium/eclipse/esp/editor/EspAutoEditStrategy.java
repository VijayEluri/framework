package org.oobium.eclipse.esp.editor;

import static org.oobium.build.esp.EspPart.Type.*;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.TextUtilities;
import org.oobium.build.esp.EspDom;
import org.oobium.build.esp.EspElement;
import org.oobium.build.esp.EspPart;
import org.oobium.eclipse.esp.EspCore;

public class EspAutoEditStrategy implements IAutoEditStrategy {

	private void autoComplete(IDocument doc, DocumentCommand cmd, char c) throws BadLocationException {
		boolean complete = false;
		boolean skip = false;
		switch(c) {
		case ' ':
			if(cmd.offset == 0) {
				cmd.text = "\t";
			} else {
				char pc = doc.getChar(cmd.offset-1);
				if(pc == '\t' || pc == '\n') {
					cmd.text = "\t";
				}
			}
			break;
		case '(':
			cmd.text = "()";
			complete = true;
			break;
		case '{':
			cmd.text = "{}";
			complete = true;
			break;
		case '"':
			if(doc.getChar(cmd.offset) == '"') {
				skip = true;
			} else {
				cmd.text = "\"\"";
				complete = true;
			}
			break;
		case '\'':
			if(doc.getChar(cmd.offset) == '\'') {
				skip = true;
			} else {
				cmd.text = "''";
				complete = true;
			}
			break;
		case ')':
			if(doc.getChar(cmd.offset) == ')') {
				skip = true;
			}
			break;
		case '}':
			if(doc.getChar(cmd.offset) == '}') {
				skip = true;
			}
			break;
		case '>':
			if(doc.getChar(cmd.offset) == '>') {
				skip = true;
			}
			break;
		}
		if(complete) {
			cmd.caretOffset = cmd.offset+1;
			cmd.shiftsCaret = false;
		}
		if(skip) {
			cmd.text = null;
			cmd.caretOffset = cmd.offset+1;
			cmd.shiftsCaret = false;
			cmd.doit = false;
		}
	}

	private void handleNewLine(IDocument doc, DocumentCommand cmd) throws BadLocationException {
		if(cmd.offset == -1 || doc.getLength() == 0) {
			return;
		}

		int p = (cmd.offset == doc.getLength() ? cmd.offset - 1 : cmd.offset);
		IRegion info = doc.getLineInformationOfOffset(p);
		int lineStart = info.getOffset();
		int lineEnd = lineStart + info.getLength();
		int indentEnd = findEndOfWhiteSpace(doc, lineStart, cmd.offset);
		boolean javaElement = false;

		EspDom dom = EspCore.get(doc);
		if(dom != null) {
			EspPart part = dom.getPart(cmd.offset);
			if(part != null && part != dom) {
				EspElement element = part.getElement();
				if(element != null) {
					if(part.isA(ScriptPart) && !element.isA(ScriptElement)) {
						cmd.offset = part.getEnd();
						cmd.text = null;
						cmd.length = 0;
						return;
					}
					if(element.isA(JavaElement)) {
						javaElement = true;
					}
				}
			}
		}

		String indentText = (indentEnd > lineStart) ? doc.get(lineStart, indentEnd-lineStart) : null;
		
		int prevCharOffset = findPreviousCharOffset(doc, cmd.offset);
		if(doc.getChar(prevCharOffset) == '{') {
			int offset = prevCharOffset+1;
			int eoflb = getEndOfLineOrBlock(doc, cmd.offset, lineEnd);
			StringBuilder sb = new StringBuilder(cmd.text);
			if(indentText != null) sb.append(indentText);
			sb.append('\t');
			cmd.caretOffset = offset + sb.length();
			if(eoflb != offset) {
				int s = findEndOfWhiteSpace(doc, cmd.offset, lineEnd);
				sb.append(doc.get(s, eoflb - s));
			}
			sb.append('\n');
			if(indentText != null) sb.append(indentText);
			if(javaElement) {
				int endJavaStart = findEndOfWhiteSpace(doc, indentEnd+1, cmd.offset);
				sb.append(doc.get(indentEnd, endJavaStart-indentEnd));
			}
			if(doc.getChar(eoflb) != '}') sb.append('}');
			cmd.offset = offset;
			cmd.text = sb.toString();
			cmd.length = eoflb - offset;
			cmd.shiftsCaret = false;
		} else {
			if(indentText != null) {
				StringBuilder sb = new StringBuilder(cmd.text);
				sb.append(indentText);
				cmd.text = sb.toString();
			}
		}
	}
	
	@Override
	public void customizeDocumentCommand(IDocument doc, DocumentCommand cmd) {
		try {
			if(cmd.length == 0 && cmd.text != null) {
				if(TextUtilities.endsWith(doc.getLegalLineDelimiters(), cmd.text) != -1) {
					handleNewLine(doc, cmd);
				} else if(cmd.text.length() == 1) {
					autoComplete(doc, cmd, cmd.text.charAt(0));
				}
			}
		} catch(BadLocationException e) {
			// exit
		}
	}
	
	private int findEndOfWhiteSpace(IDocument doc, int offset, int end) throws BadLocationException {
		while(offset < end) {
			char c = doc.getChar(offset);
			if(c != ' ' && c != '\t') {
				return offset;
			}
			offset++;
		}
		return end;
	}

	private int findPreviousCharOffset(IDocument doc, int offset) throws BadLocationException {
		offset--;
		while(offset >= 0) {
			char c = doc.getChar(offset);
			if(c != ' ' && c != '\t') {
				return offset;
			}
			offset--;
		}
		return 0;
	}
	
	private int getEndOfLineOrBlock(IDocument doc, int offset, int end) throws BadLocationException {
		while(offset < end) {
			char c = doc.getChar(offset);
			if(c == '\n' || c == '}') {
				return offset;
			}
			offset++;
		}
		return end;
	}

}
