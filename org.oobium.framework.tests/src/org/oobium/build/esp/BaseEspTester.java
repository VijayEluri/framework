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
package org.oobium.build.esp;

import org.oobium.build.esp.compiler.ESourceFile;
import org.oobium.build.esp.compiler.EspCompiler;
import org.oobium.build.esp.dom.EspDom;
import org.oobium.build.esp.dom.EspResolver;
import org.oobium.build.esp.parser.EspBuilder;

public abstract class BaseEspTester {

	private boolean trimComments;
	
	protected abstract String getFileName();
	
	protected void trimComments(boolean trim) {
		this.trimComments = trim;
	}
	
	private String trimComments(String in) {
		if(trimComments) {
			boolean inline = false;
			boolean multiline = false;
			char[] ca = in.toCharArray();
			StringBuilder sb = new StringBuilder(ca.length);
			for(int i = 0; i < ca.length; i++) {
				if(inline) {
					if(ca[i] == '\n') inline = false;
				}
				else if(multiline) {
					if(ca[i] == '/' && ca[i-1] == '*') {
						multiline = false;
						if(i+1 < ca.length && ca[i+1] == '\n') {
							i++;
						}
					}
				}
				else {
					if(ca[i] == '/' && i+1 < ca.length && ca[i+1] == '/') {
						inline = true;
					} else if(ca[i] == '/' && i+1 < ca.length && ca[i+1] == '*') {
						multiline = true;
					} else {
						sb.append(ca[i]);
					}
				}
			}
			return sb.toString();
		}
		return in;
	}
	
	protected String body(String method) {
		int s1 = 0;
		while(s1 < method.length() && method.charAt(s1) != '{') {
			s1++;
		}
		s1++;
		while(s1 < method.length() && Character.isWhitespace(method.charAt(s1))) {
			s1++;
		}
		int s2 = method.length() - 3;
		if(s2 > s1) {
			return trimComments(method.substring(s1, method.length() - 3).replace("\n\t\t", "\n"));
		}
		return "";
	}
	
	protected String render(String src) {
		ESourceFile esf = esf(src);
		return body(esf.getMethod("doRender"));
	}
	
	protected String asset(String src) {
		ESourceFile esf = esf(src);
		return trimComments(esf.getAsset(esf.getAssets()[0]));
	}
	
	protected ESourceFile esf(String src) {
		EspDom dom = EspBuilder.newEspBuilder(getFileName()).parse(src);
		EspCompiler e2j = EspCompiler.newEspCompiler("com.mydomain");
		ESourceFile esf = e2j.compile(dom);
		
		System.out.println(esf.getSource());
		for(String e : esf.getAssets()) {
			System.out.println();
			System.out.println(e + ":");
			System.out.println("\t" + esf.getAsset(e).replace("\n", "\n\t"));
		}
		
		return esf;
	}

}
