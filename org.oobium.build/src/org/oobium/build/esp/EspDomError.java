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

public class EspDomError {

	public enum ErrorType { Duplicate, IllegalArgument, IllegalCharacter, MissingCloser, MissingPart }

	private ErrorType type;
	private int startLine;
	private int startColumn;
	private int endLine;
	private int endColumn;
	
	public EspDomError(ErrorType type, int line, int column) {
		this(type, line, column, line, column);
	}
	
	public EspDomError(ErrorType type, int line, int startColumn, int endColumn) {
		this(type, line, startColumn, line, endColumn);
	}
	
	public EspDomError(ErrorType type, int startLine, int startColumn, int endLine, int endColumn) {
		this.type = type;
		this.startLine = startLine;
		this.startColumn = startColumn;
		this.endLine = endLine;
		this.endColumn = endColumn;
	}

	public int getEndColumn() {
		return endColumn;
	}
	
	public int getEndLine() {
		return endLine;
	}
	
	public int getStartColumn() {
		return startColumn;
	}
	
	public int getStartLine() {
		return startLine;
	}
	
	public ErrorType getType() {
		return type;
	}
	
	@Override
	public String toString() {
		return super.toString() + " {" + type + ": (" + startColumn + ", " + startLine + ") - (" + endColumn + ", " + endLine + ")}";
	}
	
}
