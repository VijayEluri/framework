package org.oobium.eclipse.esp.editor;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.TextUtilities;

public class EspAutoEditStrategy implements IAutoEditStrategy {

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
	
	private int getEndOfLineBlock(IDocument doc, int offset, int end) throws BadLocationException {
		while(offset < end) {
			char c = doc.getChar(offset);
			if(c == '\n' || c == '}') {
				return offset;
			}
			offset++;
		}
		return end;
	}
	
	private void autoIndentAfterNewLine(IDocument doc, DocumentCommand cmd) {
		if(cmd.offset == -1 || doc.getLength() == 0) {
			return;
		}

		try {
			int p = (cmd.offset == doc.getLength() ? cmd.offset - 1 : cmd.offset);
			IRegion info = doc.getLineInformationOfOffset(p);
			int lineStart = info.getOffset();
			int lineEnd = lineStart + info.getLength();
			int end = findEndOfWhiteSpace(doc, lineStart, cmd.offset);
			
			String indents = (end > lineStart) ? doc.get(lineStart, end-lineStart) : null;
			
			int prevCharOffset = findPreviousCharOffset(doc, cmd.offset);
			if(doc.getChar(prevCharOffset) == '{') {
				int offset = prevCharOffset+1;
				int eoflb = getEndOfLineBlock(doc, cmd.offset, lineEnd);
				StringBuilder sb = new StringBuilder(cmd.text);
				if(indents != null) sb.append(indents);
				sb.append('\t');
				cmd.caretOffset = offset + sb.length();
				if(eoflb != offset) {
					int s = findEndOfWhiteSpace(doc, cmd.offset, lineEnd);
					sb.append(doc.get(s, eoflb - s));
				}
				sb.append('\n');
				if(indents != null) sb.append(indents);
				if(doc.getChar(eoflb) != '}') sb.append('}');
				cmd.offset = offset;
				cmd.text = sb.toString();
				cmd.length = eoflb - offset;
				cmd.shiftsCaret = false;
			} else {
				if(indents != null) {
					StringBuilder sb = new StringBuilder(cmd.text);
					sb.append(indents);
					cmd.text = sb.toString();
				}
			}
		} catch(BadLocationException excp) {
			// stop work
		}
	}

	@Override
	public void customizeDocumentCommand(IDocument doc, DocumentCommand cmd) {
		if(cmd.length == 0 && cmd.text != null && TextUtilities.endsWith(doc.getLegalLineDelimiters(), cmd.text) != -1) {
			autoIndentAfterNewLine(doc, cmd);
		}
	}

}
