package org.oobium.build.util;

public class SSHEvent {

	public enum Type { In, InExt, Err, ScpStart, ScpEnd }

	public final Type type;
	public Object data;
	
	public SSHEvent(Type type, Object data) {
		this.type = type;
		this.data = data;
	}
	
}
