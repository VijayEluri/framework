package org.oobium.cache.file;

import static org.oobium.utils.DateUtils.httpDate;

import java.io.File;

import org.oobium.cache.CacheObject;

public class FileCacheObject implements CacheObject {

	private final File file;
	
	public FileCacheObject(File file) {
		this.file = file;
	}
	
	@Override
	public String lastModified() {
		return httpDate(file.lastModified());
	}

	@Override
	public String contentLength() {
		return Long.toString(file.length());
	}

	@Override
	public Object payload() {
		return file;
	}

}
