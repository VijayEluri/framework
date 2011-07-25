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
import org.oobium.build.workspace.Application;
import org.oobium.console.Suggestion;

public class ApplicationCommand extends BuilderCommand {

	@Override
	public void configure() {
		maxParams = 1;
		minParams = 1;
	}
	
	@Override
	public void run() {
		Application application = getWorkspace().getApplication((paramCount() == 0) ? null : param(0));
		if(application != null) {
			setApplication(application);
			setProject(application);
			console.out.println("application successfully set");
		} else {
			console.err.println("failed to set application: application does not exist");
		}
	}

	@Override
	protected Suggestion[] suggest(String cmd, Suggestion[] suggestions) {
		List<Application> applications = new ArrayList<Application>(Arrays.asList(getWorkspace().getApplications()));
		for(Iterator<Application> iter = applications.iterator(); iter.hasNext(); ) {
			if(!iter.next().name().startsWith(cmd)) {
				iter.remove();
			}
		}
		if(applications.isEmpty()) {
			return suggestions;
		} else {
			Suggestion[] tmp = Arrays.copyOf(suggestions, suggestions.length + applications.size());
			for(int i = suggestions.length; i < tmp.length; i++) {
				Application application = applications.get(i-suggestions.length);
				tmp[i] = new Suggestion(application.name(), "set active project to " + application.name());
			}
			return tmp;
		}
	}

}
