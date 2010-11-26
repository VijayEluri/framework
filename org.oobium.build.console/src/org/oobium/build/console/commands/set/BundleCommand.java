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
package org.oobium.build.console.commands.set;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.oobium.build.console.BuilderCommand;
import org.oobium.build.workspace.Bundle;
import org.oobium.console.Suggestion;

public class BundleCommand extends BuilderCommand {

	@Override
	public void configure() {
		maxParams = 1;
		minParams = 1;
	}
	
	@Override
	public void run() {
		Bundle project = getWorkspace().getBundle((paramCount() == 0) ? null : param(0));
		if(project != null) {
			setBundle(project);
			console.out.println("project successfully set");
		} else {
			console.err.println("failed to set project: project does not exist");
		}
	}

	@Override
	protected Suggestion[] suggest(String cmd, Suggestion[] suggestions) {
		List<Bundle> bundles = new ArrayList<Bundle>(Arrays.asList(getWorkspace().getBundles()));
		for(Iterator<Bundle> iter = bundles.iterator(); iter.hasNext(); ) {
			if(!iter.next().name.startsWith(cmd)) {
				iter.remove();
			}
		}
		if(bundles.isEmpty()) {
			return suggestions;
		} else {
			Suggestion[] tmp = Arrays.copyOf(suggestions, suggestions.length + bundles.size());
			for(int i = suggestions.length; i < tmp.length; i++) {
				Bundle project = bundles.get(i-suggestions.length);
				tmp[i] = new Suggestion(project.name, "set active project to " + project.name);
			}
			return tmp;
		}
	}

}
