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
package org.oobium.build.console.commands.destroy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.oobium.build.console.BuilderCommand;
import org.oobium.build.console.BuilderConsoleActivator;
import org.oobium.build.workspace.Bundle;
import org.oobium.build.workspace.Module;
import org.oobium.console.Suggestion;

public class BundleCommand extends BuilderCommand {

	@Override
	public void configure() {
		maxParams = 1;
		minParams = 0;
	}

	@Override
	public void run() {
		Bundle bundle;
		if(paramCount() == 1) {
			bundle = getWorkspace().getBundle(param(0));
			if(bundle == null) {
				console.err.println(param(0) + " does not exist in the workspace");
				return;
			}
		} else {
			bundle = getBundle();
			if(bundle == null) {
				console.err.println("Project is not set");
				return;
			}
		}

		remove(bundle);
		
		BuilderConsoleActivator.sendRefresh(bundle, 100);
	}
	
	protected void remove(Bundle bundle) {
		String confirm = flag('f') ? "Y" : ask("Permanently remove \"" + bundle + "\"  from the file system? [Y/N] ");
		if("Y".equalsIgnoreCase(confirm)) {
			bundle.delete();
			BuilderConsoleActivator.sendRemove(bundle.file);
			console.out.println(bundle + " successfully removed from the file system");
			if(getClass() != BundleCommand.class && bundle.isModule()) {
				Module module = (Module) bundle;
				bundle = getWorkspace().getMigratorFor(module);
				if(bundle != null) {
					confirm = flag('f') ? "Y" : ask("Also remove the associated migration (" + bundle + ")\n  from the file system? [Y/N] ");
					if("Y".equalsIgnoreCase(confirm)) {
						bundle.delete();
						BuilderConsoleActivator.sendRemove(bundle.file);
						console.out.println(bundle + " successfully removed from the file system");
					} else {
						console.out.println(bundle + " skipped");
					}
				}
				bundle = getWorkspace().getTestSuiteFor(module);
				if(bundle != null) {
					confirm = flag('f') ? "Y" : ask("Also remove the associated test suite (" + bundle + ")\n  from the file system? [Y/N] ");
					if("Y".equalsIgnoreCase(confirm)) {
						bundle.delete();
						BuilderConsoleActivator.sendRemove(bundle.file);
						console.out.println(bundle + " successfully removed from the file system");
					} else {
						console.out.println(bundle + " skipped");
					}
				}
			}
		} else {
			console.out.println("operation cancelled by user");
		}
	}
	
	@Override
	protected Suggestion[] suggest(String cmd, Suggestion[] suggestions) {
		List<Bundle> projects = new ArrayList<Bundle>(Arrays.asList(getWorkspace().getBundles()));
		for(Iterator<Bundle> iter = projects.iterator(); iter.hasNext(); ) {
			if(!iter.next().name.startsWith(cmd)) {
				iter.remove();
			}
		}
		if(projects.isEmpty()) {
			return suggestions;
		} else {
			Suggestion[] tmp = Arrays.copyOf(suggestions, suggestions.length + projects.size());
			for(int i = suggestions.length; i < tmp.length; i++) {
				Bundle project = projects.get(i-suggestions.length);
				tmp[i] = new Suggestion(project.name, "set active project to " + project.name);
			}
			return tmp;
		}
	}

}
