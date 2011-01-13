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
package org.oobium.app.server.routing.handlers;

import static org.oobium.utils.DateUtils.httpDate;

import java.net.URL;
import java.text.ParseException;
import java.util.Date;

import org.oobium.app.server.response.AssetResponse;
import org.oobium.app.server.response.Response;
import org.oobium.app.server.routing.RouteHandler;
import org.oobium.app.server.routing.Router;
import org.oobium.http.HttpRequest;
import org.oobium.http.constants.ContentType;
import org.oobium.http.constants.Header;
import org.oobium.http.constants.RequestType;
import org.oobium.utils.Config.Mode;

public class AssetHandler extends RouteHandler {

	private final String assetPath;
	private final ContentType type;
	private final String length;
	private final String lastModified;
	
	public AssetHandler(Router router, String assetPath, ContentType type, String length, String lastModified) {
		super(router);
		this.assetPath = assetPath;
		this.type = type;
		this.length = length;
		this.lastModified = lastModified;
	}

//	private URL getUrl() {
//		String asset = request.getPath();
//		int ix = asset.lastIndexOf('.');
//		if(ix != -1) {
//			ContentType type = ContentType.getFromExtension(asset.substring(ix + 1), ContentType.HTML);
//			URL url = null;
//			if(request.getType() != RequestType.HEAD) {
//				ClassLoader loader = router.getService().getClass().getClassLoader();
//				url = loader.getResource(asset);
//				if(url == null) {
//					switch(type) {
//					case CSS:
//						asset = "/styles" + asset;
//						break;
//					case HTML:
//						asset = "/html" + asset;
//						break;
//					case IMG:
//					case IMG_GIF:
//					case IMG_ICO:
//					case IMG_JPEG:
//					case IMG_JPG:
//					case IMG_PNG:
//						asset = "/images" + asset;
//						break;
//					case JS:
//						asset = "/scripts" + asset;
//						break;
//					}
//					url = loader.getResource(asset);
//				}
//			}
//			return new AssetResponse(request.getType(), type, url, length, lastModified);
//		}
//		return null;
//	}

	private boolean isNotModified(HttpRequest request) {
		if(Mode.isNotDEV() && lastModified != null && lastModified.length() > 0) {
			String ifMod = request.getHeader(Header.IF_MODIFIED_SINCE);
			if(ifMod != null && ifMod.length() > 0) {
				try {
					Date since = httpDate(ifMod);
					Date last = httpDate(lastModified);
					if(!since.after(last)) {
						return true;
					}
				} catch(ParseException e) {
					if(logger.isLoggingTrace()) {
						logger.trace(e.getMessage());
					}
					// fall through
				}
			}
		}
		return false;
	}
	
	@Override
	public Response routeRequest(HttpRequest request) throws Exception {
		if(isNotModified(request)) {
			return Response.notModified(request.getType());
		}
		
		RequestType requestType = request.getType();

		if(requestType == RequestType.HEAD) {
			return new AssetResponse(requestType, type, null, length, lastModified);
		}

		ClassLoader loader = router.getService().getClass().getClassLoader();
		URL url = loader.getResource(assetPath);
		if(url != null) {
			return new AssetResponse(requestType, type, url, length, lastModified);
		}
		return null;
	}
	
	@Override
	public String toString() {
		return assetPath;
	}
	
}
