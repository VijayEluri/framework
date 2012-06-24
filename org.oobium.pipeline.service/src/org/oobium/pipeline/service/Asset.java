package org.oobium.pipeline.service;

import java.io.File;

import org.oobium.app.http.MimeType;
import org.oobium.pipeline.PipelinedAsset;

public class Asset implements PipelinedAsset {

	private final MimeType type;
	
	private String location;
	private File file;
	private boolean ready;
	
	public Asset(MimeType type) {
		this.type = type;
	}
	
	@Override
	public String getLocation() {
		return location;
	}

	@Override
	public Object getPayload() {
		return file;
	}
	
	@Override
	public boolean isFile() {
		return true;
	}
	
	@Override
	public boolean ready() {
		return ready;
	}

	public void setFile(File file) {
		this.file = file;
		String name = file.getName();
		boolean gz = name.endsWith(".gz");
		if(gz) {
			name = name.substring(0, name.length()-3);
		}
		StringBuilder sb = new StringBuilder();
		sb.append('/').append(name);
		String version = "-" + file.length(); // TODO use MD5 hash
		int ix = sb.lastIndexOf(".");
		if(ix == -1) {
			sb.append(version);
		} else {
			sb.insert(ix, version);
		}
		location = sb.toString();
		ready = true;
	}

	@Override
	public long getLastModified() {
		return file.lastModified();
	}

	@Override
	public long getLength() {
		return file.length();
	}

	@Override
	public MimeType getMimeType() {
		return type;
	}
	
}
