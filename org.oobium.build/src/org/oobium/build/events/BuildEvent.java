package org.oobium.build.events;

public class BuildEvent {

	public enum Type { FileExists }
	
	public Type type;
	public Object data;
	public boolean doIt = true;
	
	public BuildEvent() {
		// default constructor
	}
	
	public BuildEvent(Type type, Object data) {
		this.type = type;
		this.data = data;
	}
	
}
