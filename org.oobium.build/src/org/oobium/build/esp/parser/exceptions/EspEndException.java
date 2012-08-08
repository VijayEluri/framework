package org.oobium.build.esp.parser.exceptions;

public class EspEndException extends EspException {

	private static final EspEndException instance = new EspEndException();
	
	private static final long serialVersionUID = 1L;

	public static EspEndException instance(int offset) {
		return instance.setOffset(offset);
	}

	public static EspEndException instance(Object source, int offset) {
		return instance.setSource(source).setOffset(offset);
	}

	
	private Object source = null;
	
	public <T> T getSourceAs(Class<T> type) {
		return type.cast(source);
	}
	
	public EspEndException setSource(Object source) {
		this.source = source;
		return this;
	}
	
	@Override
	public EspEndException setOffset(int offset) {
		return (EspEndException) super.setOffset(offset);
	}
	
}
