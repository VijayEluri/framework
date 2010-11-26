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
package org.oobium.build.gen;

import static org.oobium.utils.FileUtils.readFile;
import static org.oobium.utils.FileUtils.writeFile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.oobium.build.esp.EspCompiler;
import org.oobium.build.esp.EspDom;
import org.oobium.build.esp.ESourceFile;
import org.oobium.build.workspace.Module;

public class EFileGenerator {

	public static List<File> generate(Module module, List<File> efiles) {
		List<File> genFiles = new ArrayList<File>();
		for(File efile : efiles) {
			File genFile = generate(module, efile);
			if(genFile != null) {
				genFiles.add(genFile);
			}
		}
		return genFiles;
	}
	
	/**
	 * return File object pointing to the generated java file,
	 * or null if the java file could not be generated.
	 * @param project
	 * @param efile a File object pointing to the EFile whose Java source is to be generated.
	 * @return a File object pointing to the Java source, if it could be generated; null otherwise.
	 */
	public static File generate(Module module, File efile) {
		if(!efile.exists()) {
			return null;
		}

		String name = efile.getName();
		String packageName = module.packageName(efile.getParentFile());
		
		EspDom dom = new EspDom(name, readFile(efile));
		EspCompiler compiler = new EspCompiler(packageName, dom);
		ESourceFile esf = compiler.compile();
		
		File genFile = module.getGenFile(efile);
		writeFile(genFile, esf.getSource());

		return genFile;
	}

	/**
	 * return File object pointing to the generated java file,
	 * or null if the java file could not be generated.
	 * @param project
	 * @param efile a File object pointing to the EFile whose Java source is to be generated.
	 * @return a EspSourceFile object if it could be generated; null otherwise.
	 */
	public static ESourceFile generate(Module module, File efile, CharSequence source) {
		if(!efile.exists()) {
			return null;
		}

		String name = efile.getName();
		String packageName = module.packageName(efile.getParentFile());
		
		EspDom dom = new EspDom(name, source);
		EspCompiler compiler = new EspCompiler(packageName, dom);
		ESourceFile esf = compiler.compile();
		
		File genFile = module.getGenFile(efile);
		writeFile(genFile, esf.getSource());

		return esf;
	}

}
