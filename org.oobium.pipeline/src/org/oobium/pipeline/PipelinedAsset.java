package org.oobium.pipeline;

import org.oobium.app.http.MimeType;

public interface PipelinedAsset {

	public abstract long getLastModified();
	
	public abstract long getLength();
	
	public abstract String getLocation();
	
	public abstract MimeType getMimeType();
	
	public abstract Object getPayload();
	
	public abstract boolean isFile();
	
	public abstract boolean ready();

}
