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
package org.oobium.build.console.commands.show;

import java.util.Arrays;

import org.oobium.build.console.BuilderCommand;
import org.oobium.build.workspace.Bundle;
import org.oobium.build.workspace.ExportedPackage;
import org.oobium.build.workspace.ImportedPackage;
import org.oobium.build.workspace.RequiredBundle;
import org.oobium.build.workspace.Workspace;
import org.oobium.utils.Config.Mode;

public class BundlesCommand extends BuilderCommand {

	@Override
	public void run() {
		Workspace ws = getWorkspace();
		Bundle bundle = hasParam("bundle") ? ws.getBundle(param("bundle")) : getBundle();
		if(bundle == null) {
			console.err.println(hasParam("bundle") ? ("no bundle \"" + param("bundle") + "\" found in workspace") : "no bundle currently selected");
			return;
		}
		
		Bundle[] bundles = null;

		if(hasParam("exporting")) {
			bundles = ws.getBundles(new ExportedPackage(param("exporting")));
		}
		
		if(bundles == null && hasParam("importing")) {
			bundles = ws.getBundles(new ImportedPackage(param("importing")));
		}
		
		if(bundles == null && hasParam("requiring")) {
			bundles = ws.getBundles(new RequiredBundle(param("requiring")));
		}

		if(bundles == null) {
			try {
				Mode mode = Mode.valueOf(param("mode").toUpperCase());
				bundles = bundle.getDependencies(ws, mode).keySet().toArray(new Bundle[0]);
			} catch(Exception e) {
				bundles = bundle.getDependencies(ws).keySet().toArray(new Bundle[0]);
			}
		}
		
		if(bundles != null) {
			Arrays.sort(bundles);
			for(Bundle b : bundles) {
				console.out.println(b.getName());
			}
		}
	}

}
