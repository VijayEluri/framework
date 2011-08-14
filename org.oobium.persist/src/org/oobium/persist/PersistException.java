package org.oobium.persist;

public class PersistException extends Exception {

	private static final long serialVersionUID = -8774070882491562815L;

	
	public PersistException(String message) {
		super(message);
	}
	
	public PersistException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public PersistException(Throwable cause) {
		super(cause);
	}
	
}
