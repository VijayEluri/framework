package org.oobium.app.routing.routes;

import java.io.File;
import java.util.regex.Pattern;

import org.jboss.netty.handler.codec.http.HttpMethod;
import org.oobium.app.routing.Route;

public class FileDirectoryRoute extends Route {

	/**
	 * paths should be passed with '/' separators, converted here to actual file system separators
	 */
	private static String basePath(String path) {
		if(File.separatorChar == '\\') {
			path = path.replace('/', '\\');
		}
		File tmp = new File(path);
		if(!tmp.isAbsolute()) {
			tmp = new File(System.getProperty("user.dir"), path);
		}
		return tmp.getAbsolutePath() + File.separator;
	}
	
	
	public final String basePath;
	
	public FileDirectoryRoute(String path) {
		super(Route.FILE_DIRECTORY, HttpMethod.GET, path);

		this.basePath = basePath(path);
		
		if(path.charAt(0) != '/') {
			path = "/" + path;
		}
		this.pattern = Pattern.compile("{path}|{path}/(.+)".replace("{path}", path));

		StringBuilder sb = new StringBuilder();
		sb.append("[GET] ").append(pattern).append(" -> File");
		string = sb.toString();
	}

	@Override
	protected String[][] params() {
		return null;
	}

}
