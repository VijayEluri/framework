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

public class ImportedPackage implements Comparable<ImportedPackage> {

	private static final Pattern p1 = Pattern.compile(";\\s*(version\\s*=\\s*\"?([\\w\\s\\.,\\[\\]\\(\\)]+)\"?)");
	private static final Pattern p2 = Pattern.compile(";\\s*(resolution\\s*:\\s*\\=\\s*\"?(optional)\"?)");

	public final String name;
	public final VersionRange versionRange;
	public final boolean optional;
	
	public ImportedPackage(String str) {
		int ix = str.indexOf(';');
		if(ix == -1) {
			name = str;
			versionRange = new VersionRange();
			optional = false;
		} else {
			name = new String(str.substring(0, ix)).trim();
		
			Matcher m = p1.matcher(str);
			if(m.find(ix)) {
				versionRange = new VersionRange(m.group(2));
			} else {
				versionRange = new VersionRange();
			}

			optional = p2.matcher(str).find(ix);
		}
	}

	@Override
	public int compareTo(ImportedPackage o) {
		return name.compareTo(o.name);
	}
	
	@Override
	public String toString() {
		return "Import-Package: " + name + "_" + versionRange + (optional ? " (optional)" : "");
	}

}
