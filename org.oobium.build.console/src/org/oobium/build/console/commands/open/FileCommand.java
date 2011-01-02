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
package org.oobium.build.console.commands.open;

import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.oobium.build.console.BuilderCommand;
import org.oobium.build.console.BuilderConsoleActivator;

public class FileCommand extends BuilderCommand {

	private String path;

	@Override
	protected void configure() {
		maxParams = 1;
	}
	
	@Override
	public void run() {
		path = options;
		if(path == null) {
			console.getDisplay().syncExec(new Runnable() {
				@Override
				public void run() {
					FileDialog dlg = new FileDialog(console.getShell(), SWT.OPEN);
					dlg.setFilterPath(getPwd());
					path = dlg.open();
				}
			});
			if(path == null) {
				console.out.println("operation canceled");
				return;
			}
		}
		
		File file = new File(path);
		if(!file.isAbsolute()) {
			file = new File(getPwd(), path);
		}

		BuilderConsoleActivator.sendOpen(file);
	}

}
