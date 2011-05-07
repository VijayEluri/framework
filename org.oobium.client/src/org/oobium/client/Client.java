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

import static org.jboss.netty.handler.codec.http.HttpMethod.*;
import static org.oobium.utils.StringUtils.attrEncode;
import static org.oobium.utils.StringUtils.attrsEncode;
import static org.oobium.utils.coercion.TypeCoercer.coerce;

import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
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

import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.oobium.utils.StringUtils;

public class Client {

	public static Client client(int port) {
		return new Client("localhost", port);
	}

	public static Client client(String url) throws MalformedURLException {
		return new Client(url);
	}
	
	public static Client client(String host, int port) {
		return new Client(host, port);
	}
	
	
	private HttpMethod method;
	private final URL url;
	private final String protocol;
	private final String host;
	private final int port;
	private String path;
	private Map<String, List<String>> headers;
	private Map<String, ?> parameters;

	private String boundary;
	private boolean sendBinaryAsJson;
	
	private ClientResponse response;

	
	public Client(String url) throws MalformedURLException {
		if(url != null && url.length() > 0 && url.indexOf("://") == -1) {
			url = "http://" + url;
		}
		this.protocol = null;
		this.host = null;
		this.port = -1;
		this.url = new URL(url);

		this.headers = new LinkedHashMap<String, List<String>>();
	}

	public Client(String host, int port) {
		this.protocol = "http";
		this.host = host;
		this.port = port;
		this.url = null;
	}
	
	public void addHeader(String name, String value) {
		List<String> list = headers.get(name);
		if(list == null) {
			list = new ArrayList<String>();
			headers.put(name, list);
		}
		list.add(value);
	}

	public void addParameter(String key, Object value) {
		doAddParameter(key, value);
	}
	
	public ClientResponse delete() {
		return request(DELETE, getPath());
	}
	
	public ClientResponse delete(Map<String, ?> parameters) {
		return request(DELETE, parameters);
	}
	
	public ClientResponse delete(String path) {
		return request(DELETE, path);
	}
	
	public ClientResponse delete(String path, Map<String, ?> parameters) {
		return request(DELETE, path, parameters);
	}

	@SuppressWarnings("unchecked")
	private void doAddParameter(String key, Object value) {
		if(parameters == null) {
			parameters = new LinkedHashMap<String, Object>();
		}
		((Map<String, Object>) parameters).put(key, value);
	}
	
	public ClientResponse get() {
		return request(GET);
	}
	
	public ClientResponse get(Map<String, ?> parameters) {
		return request(GET, getPath(), parameters);
	}

	public ClientResponse get(String path) {
		return request(GET, path, null);
	}
	
	public ClientResponse get(String path, Map<String, ?> parameters) {
		return request(GET, path, parameters);
	}
	
	public String getBoundary() {
		return boundary;
	}

	public String getHost() {
		return (url != null) ? url.getHost() : host;
	}

	public String getPath() {
		return (url != null) ? url.getPath() : null;
	}
	
	public int getPort() {
		return (url != null) ? url.getPort() : port;
	}
	
	public String getProtocol() {
		return (url != null) ? url.getProtocol() : protocol;
	}
	
	public ClientResponse getResponse() {
		return response;
	}

	public URL getUrl() {
		try {
			return (url != null) ? url : new URL(protocol, host, port, null);
		} catch(MalformedURLException e) {
			return null;
		}
	}
	
	private URL getURL(Map<String, String> parameters) throws MalformedURLException {
		String path = this.path;
		if(parameters != null && !parameters.isEmpty()) {
			switch(method.getName().charAt(0)) {
			case 'G': // GET
			case 'H': // HEAD
			case 'D': // DELETE
				path = path + ((path.indexOf('?') == -1) ? "?" : "&") + StringUtils.attrsEncode(parameters);
				break;
			}
		}
		if(url != null) {
			return new URL(url.getProtocol(), url.getHost(), url.getPort(), path);
		} else {
			return new URL(protocol, host, port, path);
		}
	}
	
	public ClientResponse head() {
		return request(HEAD);
	}
	
	public ClientResponse head(Map<String, ?> parameters) {
		return request(HEAD, parameters);
	}
	
	public ClientResponse head(String path) {
		return request(HEAD, path);
	}
	
	public ClientResponse head(String path, Map<String, ?> parameters) {
		return request(HEAD, path, parameters);
	}

	public ClientResponse post() {
		return request(POST);
	}

	public ClientResponse post(Map<String, ?> parameters) {
		return request(POST, parameters);
	}

	public ClientResponse post(String path) {
		return request(POST, path);
	}
	
	public ClientResponse post(String path, Map<String, ?> parameters) {
		return request(POST, path, parameters);
	}

	public ClientResponse put() {
		return request(PUT);
	}

	public ClientResponse put(Map<String, ?> parameters) {
		return request(PUT, parameters);
	}

	public ClientResponse put(String path) {
		return request(PUT, path);
	}

	public ClientResponse put(String path, Map<String, ?> parameters) {
		return request(PUT, path, parameters);
	}
	
	public ClientResponse request(HttpMethod type) {
		return request(type, getPath(), null);
	}
	
	public ClientResponse request(HttpMethod type, Map<String, ?> parameters) {
		return request(type, getPath(), parameters);
	}

	public ClientResponse request(HttpMethod type, String path) {
		return request(type, path, parameters);
	}

	public ClientResponse request(HttpMethod type, String path, Map<String, ?> parameters) {
		if(path == null) {
			path = "/";
		} else if(!path.startsWith("/")) {
			path = "/" + path;
		}
		this.method = type;
		this.path = path;
		this.parameters = parameters;
		
		run();
		
		return response;
	}
	
	public void sendBinaryAsJson(boolean sendAsJson) {
		this.sendBinaryAsJson = sendAsJson;
	}
	
	private void run() {
		try {
			Map<String, String> params = null;
			Map<String, MessagePart> parts = null;
			if(parameters != null) {
				params = new LinkedHashMap<String, String>();
				for(Entry<String, ?> entry : parameters.entrySet()) {
					String key = entry.getKey();
					Object val = entry.getValue();
					if(val instanceof File) {
						File file = (File) val;
						String name = file.getName();
						int ix = name.lastIndexOf('.');
						String ext = (ix == -1) ? null : name.substring(ix+1);
						MessagePart part = new MessagePart(new FileInputStream(file), ext);
						part.put("filename", name);
						val = part;
					} else if(val instanceof InputStream) {
						val = new MessagePart((InputStream) val);
					} else if(!sendBinaryAsJson && val != null && val.getClass().getComponentType() == byte.class) {
						val = new MessagePart(new ByteArrayInputStream((byte[]) val));
					}
					if(val instanceof MessagePart) {
						if(parts == null) {
							parts = new LinkedHashMap<String, MessagePart>();
						}
						parts.put(key, (MessagePart) val);
					} else {
						params.put(key, coerce(val, String.class));
					}
				}
			}
			
			URL url = getURL(params);
	        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	        conn.setRequestMethod(method.getName());
	        setHeaders(conn);
	        if(method == POST || method == PUT) {
	        	conn.setDoOutput(true);
	        	if(parts == null) {
			        OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream());
			        out.write(attrsEncode(params));
			        out.flush();
			        out.close();
	        	} else {
	        		String boundary = this.boundary;
	        		if(boundary == null) {
		        		List<String> ctypes = headers.get(HttpHeaders.Names.CONTENT_TYPE);
		        		if(ctypes != null && !ctypes.isEmpty()) {
		        			for(String ctype : ctypes) {
		        				String[] sa = ctype.split("\\s*;\\s*");
		        				for(int i = 0; i < sa.length; i++) {
		        					if(sa[i].startsWith("boundary=")) {
		        						if(sa[i].length() > 9) {
		        							boundary = (sa[i].substring(9));
		        						}
		        						break;
		        					}
		        				}
		        				if(boundary != null) {
		        					break;
		        				}
		        			}
		        		}
		        		if(boundary == null) {
		        			boundary = "---------------------------OobiumBoundary";
		        		}
	        		}
	        		conn.setRequestProperty(HttpHeaders.Names.CONTENT_TYPE, "multipart/form-data; boundary=" + boundary);
	                DataOutputStream out = new DataOutputStream(conn.getOutputStream());
	                out.writeBytes("--" + boundary + "\r\n");
	                for(Entry<String, String> entry : params.entrySet()) {
	                	writeParam(out, entry.getKey(), entry.getValue(), boundary);
	                }
	                for(Entry<String, MessagePart> entry : parts.entrySet()) {
	                	writePart(out, entry.getKey(), entry.getValue(), boundary);
	                }
	                out.writeBytes("--");
	                out.flush();
	                out.close();
	        	}
	        }
	        response = new ClientResponse(conn);
		} catch(Exception e) {
	        response = new ClientResponse(e);
		}
	}

	public void setAccepts(String...types) {
		for(String type : types) {
			addHeader(HttpHeaders.Names.ACCEPT, type);
		}
	}

	public void setBoundary(String boundary) {
		this.boundary = boundary;
	}

	public void setHeader(String name, String value) {
		List<String> list = new ArrayList<String>();
		list.add(value);
		headers.put(name, list);
	}
	
	private void setHeaders(URLConnection connection) {
		if(headers == null) {
    		connection.addRequestProperty(HttpHeaders.Names.USER_AGENT, "Oobium Client");
    		connection.addRequestProperty(HttpHeaders.Names.HOST, connection.getURL().getHost());
		} else {
	    	if(!headers.containsKey(HttpHeaders.Names.USER_AGENT)) {
	    		connection.addRequestProperty(HttpHeaders.Names.USER_AGENT, "Oobium Client");
	    	}
	    	if(!headers.containsKey(HttpHeaders.Names.HOST)) {
	    		connection.addRequestProperty(HttpHeaders.Names.HOST, connection.getURL().getHost());
	    	}
			for(Entry<String, List<String>> entry : headers.entrySet()) {
				for(String value : entry.getValue()) {
					connection.addRequestProperty(entry.getKey(), value);
				}
			}
		}
	}
	
    @Override
	public String toString() {
		if(url != null) return url.toString();
		return protocol + "://" + host + ":" + port;
	}
 	    
	private void writeParam(DataOutputStream out, String name, String value, String boundary) throws Exception {
        out.writeBytes("content-disposition: form-data; name=\"" + name + "\"\r\n\r\n");
        out.writeBytes(value);
        out.writeBytes("\r\n--" + boundary + "\r\n");
    }
	
	private void writePart(DataOutputStream out, String name, MessagePart part, String boundary) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("Content-Disposition").append(": form-data; name=\"").append(name).append('"');
		for(Entry<String, String> entry : part.getParameters().entrySet()) {
			sb.append(';').append(attrEncode(entry.getKey(), entry.getValue())).append('"');
		}
		sb.append("\r\n");
		if(part.hasContentType()) {
			sb.append(HttpHeaders.Names.CONTENT_TYPE).append(": ").append(part.getContentType()).append("\r\n");
		}
		sb.append("\r\n");
		out.writeBytes(sb.toString());
		InputStream in = part.getStream();
		byte[] bytes = new byte[4*1024];
		int read;
		while((read = in.read(bytes)) > 0) {
			out.write(bytes, 0, read);
		}
		in.close();
		out.writeBytes("\r\n--" + boundary + "\r\n");
    }

}
