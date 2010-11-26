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
package org.oobium.build.console.commands;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.oobium.build.console.BuilderCommand;
import org.oobium.build.workspace.Workspace;
import org.oobium.console.Suggestion;
import org.oobium.utils.FileUtils;

public class RmCommand extends BuilderCommand {

	@Override
	public void configure() {
		maxParams = 2;
		minParams = 1;
	}

	@Override
	protected void run() {
		String flags;
		if(paramCount() == 2) {
			if(param(0).charAt(0) != '-') {
				console.err.println("unknown flags: " + param(0) + ". Useage: rm [-r] filename");
				return;
			}
			flags = param(0);
		} else {
			flags = "";
		}
		
		File file = new File(param(paramCount()-1));
		if(!file.isAbsolute()) {
			file = new File(getPwd(), param(paramCount()-1));
		}

		List<File> deletedFiles = new ArrayList<File>();
		if(file.exists()) {
			if(file.isDirectory()) {
				if(flags.indexOf('r') == -1) {
					String[] contents = file.list();
					if(contents != null && contents.length > 0) {
						if(!"Y".equalsIgnoreCase(ask("Directory is not empty.  Remove it and all its children? [Y/N] "))) {
							console.out.println("operation cancelled");
							return;
						}
					}
				}
				deletedFiles.addAll(FileUtils.deleteContents(file));
			}
			
			if(file.delete()) {
				deletedFiles.add(file);
			}

			if(!deletedFiles.isEmpty()) {
				if(flags.indexOf('v') != -1) {
					for(File deletedFile : deletedFiles) {
						console.out.println("removed " + deletedFile);
					}
				}
				Workspace workspace = getWorkspace();
				for(File deletedFile : deletedFiles) {
					try {
						if(workspace.getBundle(deletedFile.getCanonicalFile()) != null) {
							workspace.refresh();
							break;
						}
					} catch(IOException e) {
						console.err.println(e.getLocalizedMessage());
					}
				}
			}
		} else {
			console.err.println("file does not exist");
		}
	}
	
	@Override
	protected Suggestion[] suggest(String args, Suggestion[] suggestions) {
		String path;
		int ix = args.lastIndexOf(File.separatorChar);
		if(ix == -1) {
			path = "";
		} else {
			path = args.substring(0, ix);
		}
		int start = 0;
		File dir = new File(path);
		if(!dir.isAbsolute()) {
			String pwd = getPwd();
			start = pwd.length() + 1;
			dir = new File(pwd, path);
		}
		File[] files = dir.listFiles();
		if(files != null && files.length > 0) {
			Suggestion[] tmp = Arrays.copyOf(suggestions, suggestions.length + files.length);
			for(int i = suggestions.length; i < tmp.length; i++) {
				File file = files[i-suggestions.length];
				if(file.isDirectory()) {
					tmp[i] = new Suggestion(file.getAbsolutePath().substring(start) + File.separator, "Show the contents of this directory");
				} else {
					tmp[i] = new Suggestion(file.getAbsolutePath().substring(start), "Show the contents of this directory");
				}
			}
			return tmp;
		}
		return suggestions;
	}

}
