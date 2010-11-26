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

public class ExportedPackage implements Comparable<ExportedPackage> {

	private static final Pattern p1 = Pattern.compile(";\\s*(version\\s*=\\s*\"?([\\w\\.]+)\"?)");

	public final String name;
	public final Version version;
	
	public ExportedPackage(String str) {
		int ix = str.indexOf(';');
		if(ix == -1) {
			name = str;
			version = new Version();
		} else {
			name = new String(str.substring(0, ix)).trim();
		
			Matcher m = p1.matcher(str);
			if(m.find(ix)) {
				version = new Version(m.group(2));
			} else {
				version = new Version();
			}
		}
	}

	@Override
	public int compareTo(ExportedPackage o) {
		return name.compareTo(o.name);
	}
	
	public boolean isFramework() {
		return name.equals("org.osgi.framework");
	}
	
	public boolean resolves(ImportedPackage importedPackage) {
		return name.equals(importedPackage.name) && version.resolves(importedPackage.versionRange);
	}
	
	public String toDeclaration() {
		return name + ";version=\"" + version + "\"";
	}
	
	@Override
	public String toString() {
		return "Export-Package: " + name + "_" + version;
	}

}
