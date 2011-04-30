package org.oobium.cache;

public interface CacheObject {

	/**
	 * The last modified date of this cache object, in the same format
	 * as the Last-Modified HTTP Header.
	 */
	public abstract String lastModified();

	/**
	 * The size of this cache object's payload, in bytes and matching
	 * the Content-Length HTTP Header.
	 */
	public abstract String contentLength();

	/**
	 * The actual data payload of this cache object. An Object is used
	 * so that it may be what ever is best for the cache service - a
	 * String, a byte[], a File, an InputStream, etc.
	 */
	public abstract Object payload();
	
}
