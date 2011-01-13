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
package org.oobium.client;

import static org.oobium.http.constants.RequestType.POST;
import static org.oobium.http.constants.RequestType.PUT;
import static org.oobium.utils.StringUtils.attrsEncode;

import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.oobium.http.constants.Header;
import org.oobium.http.constants.RequestType;
import org.oobium.utils.StringUtils;

public class ClientThread extends Thread {

	private Client client;
	private RequestType type;
	private String path;
	private Map<String, List<String>> headers;
	private Map<String, String> parameters;
	private ClientCallback callback;
	
	private ClientResponse response;
	
	ClientThread(Client client, RequestType type, String path, Map<String, List<String>> headers, Map<String, String> parameters) {
		if(!path.startsWith("/")) {
			path = "/" + path;
		}
		this.client = client;
		this.type = type;
		this.path = path;
		this.parameters = parameters;
		
		this.headers = (headers != null) ? new LinkedHashMap<String, List<String>>(headers) : new LinkedHashMap<String, List<String>>();
	}

	public void addHeader(String key, String value) {
		List<String> list = headers.get(key);
		if(list == null) {
			list = new ArrayList<String>();
			headers.put(key, list);
		}
		list.add(value);
	}
	
	public ClientResponse getResponse() {
		return response;
	}
	
	private URL getURL() throws MalformedURLException {
		String path = this.path;
		if(hasParameters()) {
			switch(type) {
			case GET:
			case HEAD:
			case DELETE:
				path = path + "?" + StringUtils.attrsEncode(parameters);
				break;
			}
		}
		if(client.url != null) {
			return new URL(client.url.getProtocol(), client.url.getHost(), client.url.getPort(), path);
		} else {
			return new URL(client.protocol, client.host, client.port, path);
		}
	}
	
	private boolean hasParameters() {
		return parameters != null && !parameters.isEmpty();
	}
	
	@Override
	public void run() {
		try {
			URL url = getURL();
	        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	        conn.setRequestMethod(type.name());
	        setHeaders(conn);
	        if(type == POST || type == PUT) {
		        conn.setDoOutput(true);
		        OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
		        wr.write(attrsEncode(parameters));
		        wr.flush();
		        wr.close();
	        }
	        response = new ClientResponse(conn);
		} catch(Exception e) {
	        response = new ClientResponse(e);
		} finally {
			if(callback != null) {
				callback.handleCallback(response);
			}
		}
	}

	public void setCallback(ClientCallback callback) {
		this.callback = callback;
	}
	
	private void setHeaders(URLConnection connection) {
		connection.addRequestProperty(Header.USER_AGENT.key(), "Oobium Client");
		for(Entry<String, List<String>> entry : headers.entrySet()) {
			for(String value : entry.getValue()) {
				connection.addRequestProperty(entry.getKey(), value);
			}
		}
	}
	
}
