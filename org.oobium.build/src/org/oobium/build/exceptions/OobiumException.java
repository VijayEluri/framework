package org.oobium.build.exceptions;

public class OobiumException extends Exception {

	private static final long serialVersionUID = 7928231009893299125L;

	
	public OobiumException() {
		super();
	}
	
	public OobiumException(String message) {
		super(message);
	}
	
	public OobiumException(Throwable cause) {
		super(cause);
	}
	
	public OobiumException(String message, Throwable cause) {
		super(message, cause);
	}

}
