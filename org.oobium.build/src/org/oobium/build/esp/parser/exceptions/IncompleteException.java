package org.oobium.build.esp.parser.exceptions;

public class IncompleteException extends EspException {

	private static final IncompleteException instance = new IncompleteException();
	
	private static final long serialVersionUID = 1L;
	
	public static IncompleteException instance(int offset) {
		return instance.setOffset(offset);
	}

	
	@Override
	public IncompleteException setOffset(int offset) {
		return (IncompleteException) super.setOffset(offset);
	}
	
}
