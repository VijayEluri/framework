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
package org.oobium.build.workspace;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VersionRange {

	public final Version start;
	public final boolean startInclusive;
	public final Version end;
	public final boolean endInclusive;
	
	public VersionRange() {
		start = end = null;
		startInclusive = endInclusive = true;
	}
	
	public VersionRange(String str) {
		str = str.trim();
		int ix = str.indexOf(',');
		if(ix == -1) {
			start = new Version(str);
			startInclusive = true;
			end = null;
			endInclusive = true;
		} else if(ix == str.length()-1) { // a single version without quotes
			start = new Version(str.substring(0, str.length()-1).trim());
			startInclusive = true;
			end = null;
			endInclusive = true;
		} else {
			Pattern p = Pattern.compile("([\\[\\(])\\s*([\\w\\.]+)\\s*,\\s*([\\w\\.]+)\\s*([\\]\\)])");
			Matcher m = p.matcher(str);
			if(m.matches()) {
				start = new Version(m.group(2));
				startInclusive = "[".equals(m.group(1));
				end = new Version(m.group(3));
				endInclusive = "]".equals(m.group(4));
			} else {
				throw new IllegalArgumentException("cannot parse " + str + " into a valid OSGi version range");
			}
		}
	}
	
	@Override
	public String toString() {
		if(start == null && end == null) {
			return "[*, *]";
		}
		if(end == null) {
			return "[" + start + ", *]";
		}
		return (startInclusive ? "[" : "(") + start + ", " + end + (endInclusive ? "]" : ")");
	}
	
}
