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
package org.oobium.build.util;

import java.io.File;
import java.util.Comparator;

public class JarComparator implements Comparator<File> {

	private String name;
	
	public JarComparator(String name) {
		this.name = name;
	}
	
	@Override
	public int compare(File file1, File file2) {
		JarVersion version1 = JarVersion.parse(file1.getName().substring(name.length()));
		JarVersion version2 = JarVersion.parse(file2.getName().substring(name.length()));
		return version1.compareTo(version2);
	}

}
