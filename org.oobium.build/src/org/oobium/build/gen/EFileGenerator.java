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

import org.oobium.build.esp.compiler.ESourceFile;
import org.oobium.build.esp.compiler.EspCompiler;
import org.oobium.build.esp.dom.EspDom;
import org.oobium.build.esp.parser.EspBuilder;
import org.oobium.build.workspace.Module;

public class EFileGenerator {

	public static List<File> generate(Module module, List<File> efiles) {
		List<File> genFiles = new ArrayList<File>();
		for(File efile : efiles) {
			genFiles.addAll(generate(module, efile));
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
	public static List<File> generate(Module module, File efile) {
		if(!efile.exists()) {
			return new ArrayList<File>(0);
		}
		
		List<File> generated = new ArrayList<File>();

		String name = efile.getName();
		String packageName = module.packageName(efile.getParentFile());
		
		EspDom dom = EspBuilder.newEspBuilder(name).parse(readFile(efile));
		EspCompiler compiler = EspCompiler.newEspCompiler(packageName);
		ESourceFile esf = compiler.compile(dom);
		
		File genFile = module.getGenFile(efile);
		generated.add(writeFile(genFile, esf.getSource()));

		for(String asset : esf.getAssets()) {
			String source = esf.getAsset(asset);
			generated.add(writeFile(module.assets, asset, source));
		}
		
		return generated;
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

		EspDom dom = EspBuilder.newEspBuilder(name).parse(source);
		EspCompiler compiler = EspCompiler.newEspCompiler(packageName);
		ESourceFile esf = compiler.compile(dom);

		File genFile = module.getGenFile(efile);
		writeFile(genFile, esf.getSource());

		return esf;
	}

}
