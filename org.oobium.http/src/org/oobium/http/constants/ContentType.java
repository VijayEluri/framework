/*******************************************************************************
 * Copyright (c) 2010 Oobium, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Jeremy Dowdall <jeremy@oobium.com> - initial API and implementation
 ******************************************************************************/
package org.oobium.http.constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum ContentType {

	ALL("*", "*"),
	AUDIO("audio", "x-wav", true),
	AUDIO_WAV("audio", "x-wav", true),
	CSS("text", "css"),
	IMG("image", "png", true),
	IMG_ICO("image", "ico", true),
	IMG_GIF("image", "gif", true),
	IMG_JPG("image", "jpg", true),
	IMG_JPEG("image", "jpeg", true),
	IMG_PNG("image", "png", true),
	JAVA_WS("application", "x-java-jnlp-file", "jnlp"),
	JS("text", "javascript", "js"),
	JSON("application", "json", "json"),
	HTML("text", "html"),
	MULTIPART("multipart", "form-data"),
	PLAIN("text", "plain", "txt"),
	RSS("application", "xhtml+xml"),
	XML("text", "xml", "xml"),
	UNKNOWN("unknown","unknown");

	private static Map<String, ContentType> contentTypes; 
	
	public static ContentType getFromExtension(String fileExt) {
		if(fileExt != null && fileExt.length() > 0) {
			for(ContentType type : ContentType.values()) {
				if(fileExt.equals(type.getFileExt())) {
					return type;
				}
			}
		}
		return null;
	}

	public static ContentType getFromExtension(String fileExt, ContentType defaultType) {
		ContentType type = ContentType.getFromExtension(fileExt);
		return (type == null) ? defaultType : type;
	}
	
	private static void initTypes() {
		if(contentTypes == null) {
			synchronized(ContentType.class) {
				if(contentTypes == null) {
					contentTypes = new HashMap<String, ContentType>();
					for(ContentType type : ContentType.values()) {
						contentTypes.put(type.getMediaType(), type);
					}
				}
			}
		}
	}
	
	public static ContentType get(String content) {
		if(content != null) {
			initTypes();
			for(String s : content.split(";")) {
				ContentType type = contentTypes.get(s.trim());
				if(type != null) {
					return type;
				}
			}
		}
		return ContentType.UNKNOWN;
	}

	public static ContentType[] getAll(String content) {
		if(content != null) {
			initTypes();
			List<ContentType> types = new ArrayList<ContentType>();
			for(String s : content.split(";")) {
				ContentType type = contentTypes.get(s.trim());
				if(type != null) {
					types.add(type);
				}
			}
			if(!types.isEmpty()) {
				return types.toArray(new ContentType[types.size()]);
			}
		}
		return new ContentType[] { ContentType.UNKNOWN };
	}

	public static ContentType parse(String contentType) {
		if(contentType != null && contentType.length() > 0) {
			String[] sa = contentType.split("/");
			if(sa.length == 2) {
				for(ContentType type : ContentType.values()) {
					if(sa[0].equals(type.getType()) && sa[1].equals(type.getSubType())) {
						return type;
					}
				}
			}
		}
		return null;
	}

	private String type;
	private String subType;
	private String fileExt;
	private String charSet;
	private boolean isBinary;

	private ContentType(String type, String subType) {
		this(type, subType, subType, "utf-8");
	}
	private ContentType(String type, String subType, boolean isBinary) {
		this(type, subType, subType, isBinary ? null : "utf-8");
		this.isBinary = isBinary;
	}
	private ContentType(String type, String subType, String fileExt) {
		this(type, subType, fileExt, "utf-8");
	}
	private ContentType(String type, String subType, String fileExt, String charSet) {
		this.type = type;
		this.subType = subType;
		this.fileExt = fileExt;
		this.charSet = charSet;
		isBinary = false;
	}
	
	public String getCharSet() {
		return charSet;
	}
	
	public String getFileExt() {
		return fileExt;
	}
	
	public String getRequestProperty() {
		if(isBinary) {
			return type + "/" + subType + ";";
		}
		return type + "/" + subType + "; charset=" + charSet;
	}

	public String getHeader() {
		if(isBinary) {
			return Header.CONTENT_TYPE.key() + ":" + type + "/" + subType + ";";
		}
		return Header.CONTENT_TYPE.key() + ":" + type + "/" + subType + "; charset=" + charSet;
	}

	public String getMediaType() {
		return type + "/" + subType;
	}
	
	public ContentType getPrimaryType() {
		if("image".equals(type)) {
			return IMG;
		} else {
			return this;
		}
	}
	
	public String getSubType() {
		return subType;
	}
	
	public String getType() {
		return type;
	}
	
	public boolean isBinary() {
		return isBinary;
	}
	
	public boolean isImage() {
		return isBinary && "image".equals(type);
	}
	
	public boolean isUnkown() {
		return this == UNKNOWN;
	}

	public boolean isWild() {
		return this == ALL;
	}
	
}
