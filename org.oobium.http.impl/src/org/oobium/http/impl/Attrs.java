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
package org.oobium.http.impl;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Attrs {

	public static String[] attrDecode(String attribute) {
		String[] nvp = attribute.split("=");
		nvp[0] = decode(nvp[0]);
		nvp[1] = (nvp.length == 1) ? "" : decode(nvp[1]);
		return nvp;
	}
	
	public static Map<String, String> attrsDecode(String attributes) {
		Map<String, String> map = new HashMap<String, String>();
		String[] attrs = attributes.split("&");
		for(String attr : attrs) {
			String[] nvp = attr.split("=");
			String key = decode(nvp[0]);
			String val = (nvp.length == 1) ? "" : decode(nvp[1]);
			map.put(key, val);
		}
		return map;
	}

	public static String attrsEncode(Map<String, String> attributes) {
		if(attributes == null || attributes.isEmpty()) {
			return "";
		}

		StringBuffer sb = new StringBuffer();
		for(Iterator<String> iter = attributes.keySet().iterator(); iter.hasNext();) {
			String key = iter.next();
			if(key != null && key.length() > 0) {
				key = encode(key);
				String val = attributes.get(key);
				if(val == null) {
					val = "";
				} else {
					val = encode(val);
				}
				sb.append(key).append('=').append(val);
				if(iter.hasNext()) {
					sb.append('&');
				}
			}
		}
		return sb.toString();
	}

	private static String decode(String string) {
		try {
			return URLDecoder.decode(string, "UTF-8");
		} catch(UnsupportedEncodingException e) {
			return "";
		}
	}

	private static String encode(String string) {
		try {
			return URLEncoder.encode(string, "UTF-8");
		} catch(UnsupportedEncodingException e) {
			return "";
		}
	}

}
