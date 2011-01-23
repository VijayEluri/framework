package org.oobium.client;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

public class MessagePart {
	
	private final InputStream stream;
	private Map<String, String> params;

	public MessagePart(InputStream stream) {
		this.stream = stream;
	}

	public Map<String, String> getParameters() {
		return (params != null) ? params : new LinkedHashMap<String, String>(0);
	}
	
	public InputStream getStream() {
		return stream;
	}

	public MessagePart put(String name, String value) {
		if(params == null) {
			params = new LinkedHashMap<String, String>();
		}
		params.put(name, value);
		return this;
	}
	
}