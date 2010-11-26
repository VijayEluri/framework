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

import static org.oobium.utils.DateUtils.*;

import java.net.URL;
import java.text.ParseException;
import java.util.Date;

import org.oobium.app.server.response.AssetResponse;
import org.oobium.app.server.response.Response;
import org.oobium.app.server.routing.RouteHandler;
import org.oobium.http.HttpRequest;
import org.oobium.http.HttpRequest.Type;
import org.oobium.http.constants.ContentType;
import org.oobium.http.constants.Header;

public class AssetHandler extends RouteHandler {

	private final ClassLoader loader;
	private final String length;
	private final String lastModified;
	
	public AssetHandler(ClassLoader loader, String length, String lastModified) {
		this.loader = loader;
		this.length = length;
		this.lastModified = lastModified;
	}

	@Override
	public Response routeRequest(HttpRequest request) throws Exception {
		if(lastModified != null && lastModified.length() > 0) {
			String ifMod = request.getHeader(Header.IF_MODIFIED_SINCE);
			if(ifMod != null && ifMod.length() > 0) {
				try {
					Date since = httpDate(ifMod);
					Date last = httpDate(lastModified);
					if(last.before(since)) {
						return Response.notModified(request.getType());
					}
				} catch(ParseException e) {
					// ignore and fall through
				}
			}
		}
		String asset = request.getPath();
		int ix = asset.lastIndexOf('.');
		if(ix != -1) {
			ContentType type = ContentType.getFromExtension(asset.substring(ix + 1), ContentType.HTML);
			URL url = null;
			if(request.getType() != Type.HEAD) {
				url = loader.getResource(asset);
				if(url == null) {
					switch(type) {
					case CSS:
						asset = "/styles" + asset;
						break;
					case HTML:
						asset = "/html" + asset;
						break;
					case IMG:
					case IMG_GIF:
					case IMG_ICO:
					case IMG_JPEG:
					case IMG_JPG:
					case IMG_PNG:
						asset = "/images" + asset;
						break;
					case JS:
						asset = "/scripts" + asset;
						break;
					}
					url = loader.getResource(asset);
				}
			}
			return new AssetResponse(request.getType(), type, url, length, lastModified);
		}
		return null;
	}
	
}
