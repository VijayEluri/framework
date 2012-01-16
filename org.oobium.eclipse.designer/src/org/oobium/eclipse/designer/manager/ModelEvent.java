package org.oobium.eclipse.designer.manager;

public class ModelEvent {

	public enum Type {
		Created,
		Updated,
		Destroyed
	}

	
	public final Type type;
	private String message;
	
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public ModelEvent(Type type) {
		this.type = type;
	}
	
}
