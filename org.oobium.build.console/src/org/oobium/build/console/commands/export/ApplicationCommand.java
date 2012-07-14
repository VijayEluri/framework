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
package org.oobium.build.console.commands.export;

import static org.oobium.utils.coercion.TypeCoercer.coerce;

import java.io.File;

import org.oobium.build.console.BuilderCommand;
import org.oobium.build.workspace.Application;
import org.oobium.build.workspace.Exporter;
import org.oobium.build.workspace.Workspace;
import org.oobium.utils.Config.Mode;

public class ApplicationCommand extends BuilderCommand {

	@Override
	public void configure() {
		applicationRequired = true;
	}
	
	@Override
	public void run() {
		Workspace ws = getWorkspace();
		Application app = getApplication();
		
		Mode mode = Mode.parse(param("mode"));
		boolean migrators = coerce(param("migrators")).from(false);

		if(flag('v')) {
			console.out.println("exporting in " + mode + " mode");
			if(migrators) {
				console.out.println("including migrators");
			} else {
				console.out.println("not including migrators");
			}
			console.capture();
		}
		
		try {
			long start = System.currentTimeMillis();

			Exporter exporter = new Exporter(ws, app);
			exporter.setMode(mode);
			exporter.setClean(true);
			exporter.setIncludeMigrator(true);
			File exportDir = exporter.export();
			
			String msg = "exported <a href=\"open file \"" + exportDir + "\"\">" + app.name() + "</a>";
			if(flag('v')) {
				console.out.println(msg + " in " + (System.currentTimeMillis() - start) + "ms");
			} else {
				console.out.println(msg);
			}
			
		} catch(Exception e) {
			console.err.print(e);
		} finally {
			if(flag('v')) {
				console.release();
			}
		}
	}
	
}
