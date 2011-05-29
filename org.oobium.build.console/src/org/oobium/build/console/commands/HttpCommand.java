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
package org.oobium.build.console.commands;

import static org.oobium.utils.StringUtils.blank;
import static org.oobium.utils.StringUtils.join;

import java.io.File;
import java.net.ConnectException;
import java.util.HashMap;
import java.util.Map;

import org.oobium.build.console.BuilderCommand;
import org.oobium.build.console.commands.http.DeleteCommand;
import org.oobium.build.console.commands.http.GetCommand;
import org.oobium.build.console.commands.http.HeadCommand;
import org.oobium.build.console.commands.http.PostCommand;
import org.oobium.build.console.commands.http.PutCommand;
import org.oobium.client.ClientResponse;
import org.oobium.utils.Base64;
import org.oobium.utils.FileUtils;

public class HttpCommand extends BuilderCommand {

	@Override
	public void configure() {
		add(new GetCommand());
		add(new HeadCommand());
		add(new PostCommand());
		add(new PutCommand());
		add(new DeleteCommand());
	}
	
	@Override
	public void run() {
		String host;
		int port;
		String path;
		Map<String, String> params;
		
		String url = options;
		
		int ix = url.indexOf("://");
		if(ix != -1) {
			url = url.substring(ix + 3);
		}
		
		ix = url.indexOf('?');
		if(ix != -1) {
			params = new HashMap<String, String>();
			String[] sa1 = url.substring(ix+1).split("\\&");
			for(String s : sa1) {
				String[] sa2 = s.split("\\=");
				if(sa2.length > 1 && sa2[1].length() > 2 && sa2[1].charAt(0) == '{' && sa2[1].charAt(sa2[1].length()-1) == '}') {
					if(sa2[1].substring(1, 8).equals("file://")) {
						String fileName = sa2[1].substring(8, sa2[1].length()-1);
						File file = new File(fileName);
						if(!file.isAbsolute()) {
							file = new File(getPwd(), fileName);
						}
						if(file.exists()) {
							byte[] data = FileUtils.readFile(file, new byte[(int) file.length()]);
							sa2[1] = new String(Base64.encode(data));
						} else {
							continue;
						}
					}
				}
				params.put(sa2[0], sa2[1]);
			}
			url = url.substring(0, ix);
		} else {
			params = new HashMap<String, String>(0);
		}
		
		ix = url.indexOf('/');
		if(ix != -1) {
			path = url.substring(ix);
			url = url.substring(0, ix);
		} else {
			path = "/";
		}
		
		ix = url.indexOf(':');
		if(ix != -1) {
			port = Integer.parseInt(url.substring(ix + 1));
			host = url.substring(0, ix);
		} else {
			port = 80;
			host = url;
		}

		ClientResponse response = executeRequest(host, port, path, params);
		console.out.println(response);
		for(String name : response.getHeaderNames()) {
			if(blank(name)) {
				console.out.println(join(response.getHeaders(name), ", "));
			} else {
				console.out.print(name);
				String headerValue = response.getHeader(name);
				if(!blank(headerValue)) {
					console.out.print(": ").println(join(response.getHeaders(name), "; "));
				}
			}
		}
		String body = response.getBody();
		if(!blank(body)) {
			console.out.println();
			console.out.println(response.getBody());
		}
		if(response.exceptionThrown()) {
			if(!blank(body)) {
				console.out.println();
			}
			Exception e = response.getException();
			if(e instanceof ConnectException) {
				console.err.println(e.getLocalizedMessage());
			} else {
				console.err.println(e);
			}
		}
	}

	protected ClientResponse executeRequest(String host, int port, String path, Map<String, String> parameters) {
		// should be overridden by subclasses and not called directly
		throw new IllegalStateException();
	}
	
}
