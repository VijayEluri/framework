package org.oobium.app.routing.handlers;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.oobium.app.http.MimeType;
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

	private void addListings(String requestPath, StringBuilder sb, SimpleDateFormat sdf, List<File> files) {
		for(File f : files) {
			String name = f.getName();
			String path = requestPath + ((filePath == null) ? ("/" + name) : (filePath + "/" + name));
			sb.append("<tr>");
			sb.append("<td class=\"name\"><a href=\"").append(path).append("\">").append(name).append("</a></td>");
			sb.append("<td class=\"size\">").append(f.length()).append("</td>");
			sb.append("<td class=\"date\">").append(sdf.format(new Date(f.lastModified()))).append("</td>");
			sb.append("</tr>");
		}
	}
	
	@Override
	public Response routeRequest(Request request) throws Exception {
		File file = ((filePath == null) ? new File(basePath) : new File(basePath, filePath)).getCanonicalFile();
		if(filePath == null || file.getPath().startsWith(basePath)) {
			if(file.isFile()) {
				return new StaticResponse(file);
			}
			else if(file.isDirectory()) {
				List<File> dirs = new ArrayList<File>();
				List<File> files = new ArrayList<File>();
				for(File f : file.listFiles()) {
					if(f.isDirectory()) dirs.add(f);
					else files.add(f);
				}

				SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss");
				StringBuilder sb = new StringBuilder();
				sb.append("<html><body><table>");
				addListings(request.getPath(), sb, sdf, dirs);
				addListings(request.getPath(), sb, sdf, files);
				sb.append("</table></body></html>");
				return new StaticResponse(MimeType.HTML, sb.toString());
			}
			else {
				return null;
			}
		} else {
			return new Response(HttpResponseStatus.FORBIDDEN);
		}
	}

}
