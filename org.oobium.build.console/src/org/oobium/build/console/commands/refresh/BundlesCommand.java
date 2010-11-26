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
package org.oobium.build.console.commands.refresh;

import org.oobium.build.console.BuilderCommand;
import org.oobium.build.workspace.Bundle;
import org.oobium.build.workspace.WorkspaceEvent;
import org.oobium.build.workspace.WorkspaceListener;

public class BundlesCommand extends BuilderCommand {

	private WorkspaceListener listener;
	
	public BundlesCommand() {
		listener = new WorkspaceListener() {
			@Override
			public void handleEvent(WorkspaceEvent event) {
				for(Object o : (Object[]) event.oldValue) {
					console.out.println("  removed obsolete bundle: " + (Bundle) o);
				}
				for(Object o : (Object[]) event.newValue) {
					console.out.println("  added new bundle: " + (Bundle) o);
				}
			}
		};
	}

	@Override
	public void run() {
		console.out.println("refreshing workspace and bundles repos...");
		getWorkspace().addListener(listener);
		getWorkspace().refresh();
		getWorkspace().removeListener(listener);
		console.out.println("workspace and bundles repos up to date.");
	}

}
