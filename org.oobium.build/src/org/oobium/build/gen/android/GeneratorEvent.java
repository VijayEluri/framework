package org.oobium.build.gen.android;

public class GeneratorEvent {

	public enum Type { Logging }

	public final Type type;
	public Object data;
	
	public GeneratorEvent(Type type, Object data) {
		this.type = type;
		this.data = data;
	}
	
}
