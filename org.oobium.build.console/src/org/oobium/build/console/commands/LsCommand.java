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
import java.io.FileFilter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.oobium.build.console.BuilderCommand;
import org.oobium.console.Suggestion;

public class LsCommand extends BuilderCommand {

	@Override
	public void configure() {
		maxParams = 2;
	}
	
	@Override
	public void run() {
		String path;
		if(paramCount() == 2) {
			if(param(0).charAt(0) != '-') {
				console.err.println("unknown flags: " + param(0) + ". Useage: ls [-flags] filename");
				return;
			}
			path = param(1);
		} else if(paramCount() == 1 && param(0).charAt(0) == '-') {
			path = getPwd();
		} else {
			if(paramCount() == 1) {
				path = param(0);
			} else {
				path = getPwd();
			}
		}

		File dir = new File(path);
		if(!dir.isAbsolute()) {
			dir = new File(getPwd(), path);
		}
		File[] files = dir.listFiles();
		if(files != null && files.length > 0) {
			List<File> list = new ArrayList<File>(Arrays.asList(files));
			Collections.sort(list, new Comparator<File>() {
				@Override
				public int compare(File o1, File o2) {
					if(o1.isDirectory() && !o2.isDirectory()) {
						return -1;
					}
					if(!o1.isDirectory() && o2.isDirectory()) {
						return 1;
					}
					return o1.compareTo(o2);
				}
			});
			if(flag('l')) {
				String[] sizes = new String[list.size()];
				int maxlen = 0;
				long totalSize = 0;
				for(int i = 0; i < list.size(); i++) {
					long size = list.get(i).length();
					totalSize += size;
					sizes[i] = String.valueOf(size);
					maxlen = Math.max(maxlen, sizes[i].length());
				}
				for(int i = 0; i < list.size(); i++) {
					while(sizes[i].length() < maxlen) {
						sizes[i] = " " + sizes[i];
					}
				}
				
				console.out.println("total: " + totalSize);

				SimpleDateFormat sdf = new SimpleDateFormat(" yyyy-MM-dd kk:mm:ss ");
				for(int i = 0; i < list.size(); i++) {
					File file = list.get(i);
					console.out.print("  ");
					console.out.print(file.isDirectory() ? 'd' : '-');
					console.out.print(file.canRead() ? 'r' : '-');
					console.out.print(file.canWrite() ? 'w' : '-');
					console.out.print(file.canExecute() ? 'x' : '-');
					console.out.print(' ').print(sizes[i]).print(sdf.format(new Date(file.lastModified())));
					if(file.isDirectory()) {
						console.out.println(file.getName(), "cd " + file.getAbsolutePath());
					} else {
						console.out.println(file.getName());
					}
				}
			} else {
				for(File file : list) {
					if(file.isDirectory()) {
						console.out.print("  ").println(file.getName(), "cd " + file.getAbsolutePath());
					} else {
						console.out.print("  ").println(file.getName());
					}
				}
			}
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
		File[] files = dir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File file) {
				return file.isDirectory();
			}
		});
		if(files != null && files.length > 0) {
			Suggestion[] tmp = Arrays.copyOf(suggestions, suggestions.length + files.length);
			for(int i = suggestions.length; i < tmp.length; i++) {
				tmp[i] = new Suggestion(files[i-suggestions.length].getAbsolutePath().substring(start) + File.separator, "Show the contents of this directory");
			}
			return tmp;
		}
		return suggestions;
	}

}
