package org.oobium.app.routing.handlers;

import java.io.File;

import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.oobium.app.request.Request;
import org.oobium.app.response.Response;
import org.oobium.app.response.StaticResponse;
import org.oobium.app.routing.RouteHandler;
import org.oobium.app.routing.Router;

public class FileDirectoryHandler extends RouteHandler {

	private String basePath;
	private String filePath;
	
	/**
	 * @param basePath must be absolute to the current file system, and end with the correct separator char
	 */
	public FileDirectoryHandler(Router router, String basePath, String filePath) {
		super(router);
		this.basePath = basePath;
		this.filePath = filePath;
		if(File.separatorChar == '\\') {
			this.filePath.replace('/', '\\');
		}
	}

	@Override
	public Response routeRequest(Request request) throws Exception {
		File file = new File(basePath, filePath).getCanonicalFile();
		String path = file.getPath();
		if(path.startsWith(basePath)) {
			if(file.isFile()) {
				return new StaticResponse(file);
			} else {
				return null;
			}
		} else {
			return new Response(HttpResponseStatus.FORBIDDEN);
		}
	}

}
