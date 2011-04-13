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

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.oobium.build.console.BuilderCommand;
import org.oobium.build.workspace.Bundle;
import org.oobium.utils.Config.Mode;

public class DependenciesCommand extends BuilderCommand {

	@Override
	public void configure() {
		bundleRequired = true;
	}
	
	@Override
	public void run() {
		Map<Bundle, List<Bundle>> deps;
		try {
			Mode mode = Mode.valueOf(param("mode").toUpperCase());
			deps = getBundle().getDependencies(getWorkspace(), mode);
		} catch(Exception e) {
			deps = getBundle().getDependencies(getWorkspace());
		}
		if(deps != null) {
			String regex = param("regex");
			for(Entry<Bundle, List<Bundle>> entry : deps.entrySet()) {
				Bundle dep = entry.getKey();
				if(regex == null || dep.name.matches(regex)) {
					console.out.println(entry.getKey().name);
					for(Bundle bundle : entry.getValue()) {
						console.out.println("  " + bundle.name);
					}
				}
			}
		}
	}

}
