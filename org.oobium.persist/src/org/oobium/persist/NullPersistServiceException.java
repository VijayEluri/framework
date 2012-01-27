package org.oobium.persist;

public class NullPersistServiceException extends RuntimeException {

	private static final long serialVersionUID = 3485983824416711811L;

	public NullPersistServiceException() {
		super();
	}
	
	public NullPersistServiceException(String message) {
		super(message);
	}
	
}
