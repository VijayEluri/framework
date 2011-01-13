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
package org.oobium.app.server.routing.routes;

import org.oobium.app.AssetProvider;
import org.oobium.app.server.routing.Route;
import org.oobium.http.constants.ContentType;
import org.oobium.http.constants.RequestType;

public class AssetRoute extends Route {

	public final String assetPath;
	public final ContentType contentType;
	public final String length;
	public final String lastModified;
	
	public AssetRoute(String path, String assetPath, ContentType contentType, String length, String lastModified) {
		super(Route.ASSET, RequestType.GET, path);
		setPattern(path);
		this.assetPath = assetPath;
		this.contentType = contentType;
		this.length = length;
		this.lastModified = lastModified;
		setString();
	}
	
	@Override
	protected String[][] params() {
		return null;
	}
	
	private void setString() {
		StringBuilder sb = new StringBuilder();
		sb.append('[').append(requestType).append(']').append(' ');
		sb.append(path).append(' ').append('-').append('>').append(" Asset");
		string = sb.toString();
	}
	
}
