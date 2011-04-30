package org.oobium.client;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

public class MessagePart {
	
	private final InputStream stream;
	private final String contentType;
	private Map<String, String> params;

	public MessagePart(InputStream stream) {
		this(stream, null);
	}

	public MessagePart(InputStream stream, String typeOrExt) {
		this.stream = stream;
		if(typeOrExt == null) {
			this.contentType = null;
		} else {
			if(typeOrExt.indexOf('/') == -1) {
				throw new UnsupportedOperationException("multipart mimetypes: not yet implemented");
//				ContentType type = ContentType.getFromExtension(typeOrExt, ContentType.MULTIPART);
//				this.contentType = type.getMediaType();
			} else {
				this.contentType = typeOrExt;
			}
		}
	}

	public Map<String, String> getParameters() {
		return (params != null) ? params : new LinkedHashMap<String, String>(0);
	}
	
	public String getContentType() {
		return contentType;
	}
	
	public InputStream getStream() {
		return stream;
	}

	public boolean hasContentType() {
		return contentType != null && contentType.length() > 0;
	}
	
	public MessagePart put(String name, String value) {
		if(params == null) {
			params = new LinkedHashMap<String, String>();
		}
		params.put(name, value);
		return this;
	}
	
}