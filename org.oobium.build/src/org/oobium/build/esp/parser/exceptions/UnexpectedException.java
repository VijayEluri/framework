package org.oobium.build.esp.parser.exceptions;

public class UnexpectedException extends EspException {

	private static final UnexpectedException instance = new UnexpectedException();
	
	private static final long serialVersionUID = 1L;

	public static UnexpectedException instance(int offset, String expected) {
		return instance.setOffset(offset).setExpected(expected);
	}
	
	public static UnexpectedException instance(int offset, String expected, String found) {
		return instance.setOffset(offset).setExpected(expected).setFound(found);
	}

	
	private String expected;
	private String found;
	
	public String getExpected() {
		return expected;
	}

	public String getFound() {
		return found;
	}
	
	public UnexpectedException setExpected(String expected) {
		this.expected = expected;
		return this;
	}
	
	public UnexpectedException setFound(String found) {
		this.found = found;
		return this;
	}
	
	@Override
	public UnexpectedException setOffset(int offset) {
		return (UnexpectedException) super.setOffset(offset);
	}
	
}
