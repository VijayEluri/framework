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
import org.oobium.http.HttpRequest.Type;

public class AssetRoute extends Route {

	public final AssetProvider provider;
	public final String length;
	public final String lastModified;
	
	public AssetRoute(String path, AssetProvider provider, String length, String lastModified) {
		super(Route.ASSET, Type.GET, path);
		setPattern(path);
		this.provider = provider;
		this.length = length;
		this.lastModified = lastModified;
		setString();
	}
	
	public ClassLoader loader() {
		return provider.getClass().getClassLoader();
	}
	
	private void setString() {
		StringBuilder sb = new StringBuilder();
		sb.append('[').append(requestType).append(']').append(' ');
		sb.append(path).append(' ').append('-').append('>').append(" Asset:").append(provider.getName());
		string = sb.toString();
	}
	
}
