package org.oobium.app.http;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MimeType {
	
	public enum Name {
		ALL,
		APPLICATION_SDP,
		AUDIO,
		AUDIO_MPEG,
		AUDIO_WAV,
		AUDIO_XWAV,
		CSS,
		GZIP,
		IMG,
		IMG_ICO,
		IMG_GIF,
		IMG_JPG,
		IMG_JPEG,
		IMG_PNG,
		JAR,
		JAVA_WS,
		JS,
		JSON,
		HTML,
		MULTIPART,
		OCTET_STREAM,
		PDF,
		PLAIN,
		RSS,
		TAR,
		TAR_GZ,
		XML,
		VIDEO,
		VIDEO_3GP,
		VIDEO_AVI,
		VIDEO_MP4,
		VIDEO_MPEG,
		VIDEO_OGG,
		VIDEO_QUICKTIME,
		ZIP,
		UNKNOWN,
		CUSTOM
	}
	
	private static final Map<String, MimeType> types;
	
	static {
		types = new HashMap<String, MimeType>();
	}

	public static final MimeType ALL = new MimeType("*", "*");
	public static final MimeType APPLICATION_SDP = new MimeType("application", "sdp");
	public static final MimeType AUDIO = new MimeType("audio", "*", true);
	public static final MimeType AUDIO_MPEG = new MimeType("audio", "mpeg", "mp3", true);
	public static final MimeType AUDIO_WAV = new MimeType("audio", "wav", true);
	public static final MimeType AUDIO_XWAV = new MimeType("audio", "x-wav", "wav", true);
	public static final MimeType CSS = new MimeType("text", "css");
	public static final MimeType GZIP = new MimeType("application", "x-zip", "gzip", true);
	public static final MimeType IMG = new MimeType("image", "*", true);
	public static final MimeType IMG_ICO = new MimeType("image", "ico", true);
	public static final MimeType IMG_GIF = new MimeType("image", "gif", true);
	public static final MimeType IMG_JPG = new MimeType("image", "jpg", true);
	public static final MimeType IMG_JPEG = new MimeType("image", "jpeg", true);
	public static final MimeType IMG_PNG = new MimeType("image", "png", true);
	public static final MimeType JAR = new MimeType("application", "java-archive", "jar", true);
	public static final MimeType JAVA_WS = new MimeType("application", "x-java-jnlp-file", "jnlp");
	public static final MimeType JS = new MimeType("text", "javascript", "js");
	public static final MimeType JSON = new MimeType("application", "json");
	public static final MimeType HTML = new MimeType("text", "html");
	public static final MimeType MULTIPART = new MimeType("multipart", "form-data");
	public static final MimeType OCTET_STREAM = new MimeType("application", "octet-stream", "apk", true);
	public static final MimeType PDF = new MimeType("application", "pdf", true);
	public static final MimeType PLAIN = new MimeType("text", "plain", "txt");
	public static final MimeType RSS = new MimeType("application", "xhtml+xml", "rss");
	public static final MimeType TAR = new MimeType("application", "x-tar", "tar", true);
	public static final MimeType TAR_GZ = new MimeType("application", "x-gzip", "tar.gz,tgz,gz", true);
	public static final MimeType XML = new MimeType("text", "xml");
	public static final MimeType VIDEO = new MimeType("video", "*", true);
	public static final MimeType VIDEO_3GP = new MimeType("video", "3gpp", "3gp", true);
	public static final MimeType VIDEO_AVI = new MimeType("video", "avi", true);
	public static final MimeType VIDEO_MP4 = new MimeType("video", "mp4", true);
	public static final MimeType VIDEO_MPEG = new MimeType("video", "mpeg", "mp2,mpa,mpe,mpeg,mpg,mpv2", true);
	public static final MimeType VIDEO_OGG = new MimeType("video", "ogg", "ogv", true);
	public static final MimeType VIDEO_QUICKTIME = new MimeType("video", "quicktime", "mov,qt", true);
	public static final MimeType ZIP = new MimeType("application", "zip", true);
	public static final MimeType UNKNOWN = new MimeType("unknown", "unknown");

	public static List<MimeType> getAll(String header) {
		List<MimeType> list = new ArrayList<MimeType>();
		if(header != null) {
			for(String s : header.split(",")) {
				int ix = s.indexOf(';');
				if(ix != -1) {
					s = s.substring(0, ix);
				}
				MimeType type = types.get(s.trim());
				if(type != null) {
					list.add(type);
				}
			}
		}
		if(!list.isEmpty()) {
			list.add(HTML);
		}
		return list;
	}

	public static MimeType getFromExtension(String extension) {
		if(extension != null && extension.length() > 0) {
			int ix = extension.lastIndexOf('.');
			if(ix != -1) {
				extension = extension.substring(ix+1);
			}
			for(MimeType type : values()) {
				for(String ext : type.extensions) {
					if(extension.equalsIgnoreCase(ext)) {
						return type;
					}
				}
			}
		}
		return null;
	}
	
	public static MimeType getFromExtension(String extension, MimeType defaultType) {
		MimeType type = getFromExtension(extension);
		return (type != null) ? type : defaultType;
	}

	public static MimeType valueOf(String acceptsType) {
		return types.get(acceptsType);
	}
	
	public static MimeType[] values() {
		return types.values().toArray(new MimeType[types.size()]);
	}

	public final Name name;
	
	public final String type;
	public final String subtype;
	public final String charset;
	public final String[] extensions;
	public final boolean binary;
	
	/**
	 * The type plus subtype: type + "/" + subtype. This is what the request header "Accept" contains.
	 */
	public final String acceptsType;
	
	/**
	 * The acceptsType plus the charset, if one exists: acceptsType + ";" + charset.
	 * This is what the response header "Content-Type" contains.
	 */
	public final String contentType;

	public MimeType(String type, String subtype) {
		this(type, subtype, subtype, null, false);
	}
	
	public MimeType(String type, String subtype, boolean binary) {
		this(type, subtype, subtype, null, binary);
	}
	
	public MimeType(String type, String subtype, String extension) {
		this(type, subtype, extension, null, false);
	}
	
	public MimeType(String type, String subtype, String extension, String charset) {
		this(type, subtype, extension, charset, false);
	}
	
	public MimeType(String type, String subtype, String extension, boolean binary) {
		this(type, subtype, extension, null, binary);
	}
	
	private MimeType(String type, String subtype, String extension, String charset, boolean binary) {
		this.name = (types.size() < Name.values().length) ? Name.values()[types.size()] : Name.CUSTOM;
		this.type = type;
		this.subtype = subtype;
		this.extensions = extension.split("\\s*,\\s*");
		this.charset = charset;
		this.binary = binary;
		this.acceptsType = type + "/" + subtype;
		this.contentType = (charset != null) ? (acceptsType + ";" + charset) : acceptsType;
		types.put(acceptsType, this);
	}

	public String extension() {
		return extensions[0];
	}
	
	public boolean resolves(MimeType type) {
		return this == ALL || this == type || resolves(type.type, type.subtype);
	}
	
	public boolean resolves(String type, String subtype) {
		if("*".equals(this.type)) return true;
		return type.equalsIgnoreCase(this.type) && ("*".equals(this.subtype) || subtype.equalsIgnoreCase(this.subtype));
	}
	
	public boolean resolvesImage() {
		return this == ALL || "image".equalsIgnoreCase(type);
	}

	@Override
	public String toString() {
		return name + ": " + acceptsType;
	}
	
}
