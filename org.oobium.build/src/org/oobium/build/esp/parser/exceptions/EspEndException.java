package org.oobium.build.esp.parser.exceptions;

public class EspEndException extends EspException {

	private static final EspEndException instance = new EspEndException();
	
	private static final long serialVersionUID = 1L;

	public static EspEndException instance(int offset) {
		return instance.setOffset(offset);
	}

	
	@Override
	public EspEndException setOffset(int offset) {
		return (EspEndException) super.setOffset(offset);
	}
	
}
