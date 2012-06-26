package org.oobium.build.esp.parser.exceptions;

public class EspException extends Exception {

	private static final EspException instance = new EspException();
	
	private static final long serialVersionUID = 1L;

	public static EspException instance(int offset) {
		return instance.setOffset(offset);
	}

	
	protected int offset;
	protected String message;

	public EspException() {
		super();
	}
	
	public EspException(Throwable t) {
		super(t);
	}
	
	@Override
	public String getLocalizedMessage() {
		return (message != null) ? message : super.getLocalizedMessage();
	}
	
	@Override
	public String getMessage() {
		return (message != null) ? message : super.getMessage();
	}
	
	public int getOffset() {
		return offset;
	}
	
	public EspException setMessage(String message) {
		this.message = message;
		return this;
	}
	
	public EspException setOffset(int offset) {
		this.offset = offset;
		return this;
	}
	
}
