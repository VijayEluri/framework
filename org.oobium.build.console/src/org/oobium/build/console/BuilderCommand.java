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
package org.oobium.build.console;

import org.oobium.build.workspace.Application;
import org.oobium.build.workspace.Bundle;
import org.oobium.build.workspace.Module;
import org.oobium.build.workspace.Project;
import org.oobium.build.workspace.Workspace;
import org.oobium.console.Command;

public abstract class BuilderCommand extends Command {

	protected boolean applicationRequired;
	protected boolean moduleRequired;
	protected boolean bundleRequired;
	
	public BuilderCommand() {
		super();
	}
	
	public BuilderCommand(String name) {
		super(name);
	}
	
	@Override
	protected boolean canExecute() {
		if(applicationRequired && !hasApplication()) {
			console.err.println("application is not set");
			return false;
		} else if(moduleRequired && !hasModule()) {
			console.err.println("bundle is not set to a module");
			return false;
		} else if(bundleRequired && !hasBundle()) {
			console.err.println("bundle is not set");
			return false;
		}
		return super.canExecute();
	}
	
	@Override
	public BuilderCommand getRoot() {
		return (BuilderCommand) super.getRoot();
	}
	
	public boolean hasApplication() {
		return getRoot().hasApplication();
	}
	
	public boolean hasModule() {
		return getRoot().hasModule();
	}
	
	public boolean hasBundle() {
		return getRoot().hasBundle();
	}
	
	public boolean hasProject() {
		return getRoot().hasProject();
	}
	
	public Application getApplication() {
		return getRoot().getApplication();
	}

	public Bundle getBundle() {
		return getRoot().getBundle();
	}
	
	public Module getModule() {
		return getRoot().getModule();
	}
	
	public Project getProject() {
		return getRoot().getProject();
	}

	public String getProjectName() {
		return getRoot().getProjectName();
	}
	
	public Workspace getWorkspace() {
		return getRoot().getWorkspace();
	}
	
	public void setApplication(Application application) {
		getRoot().setApplication(application);
	}
	
	public void setProject(Bundle bundle) {
		getRoot().setProject(bundle);
	}

	public String getPwd() {
		return getRoot().getPwd();
	}
	
	public boolean setPwd(String pwd) {
		return getRoot().setPwd(pwd);
	}
	
}
