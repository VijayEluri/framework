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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;

import org.oobium.build.console.commands.AddCommand;
import org.oobium.build.console.commands.BrowseCommand;
import org.oobium.build.console.commands.CatCommand;
import org.oobium.build.console.commands.CdCommand;
import org.oobium.build.console.commands.CleanCommand;
import org.oobium.build.console.commands.CreateCommand;
import org.oobium.build.console.commands.DebugCommand;
import org.oobium.build.console.commands.DestroyCommand;
import org.oobium.build.console.commands.ExportCommand;
import org.oobium.build.console.commands.GenerateCommand;
import org.oobium.build.console.commands.GetCommand;
import org.oobium.build.console.commands.HttpCommand;
import org.oobium.build.console.commands.ImportCommand;
import org.oobium.build.console.commands.LsCommand;
import org.oobium.build.console.commands.MigrateCommand;
import org.oobium.build.console.commands.MkdirCommand;
import org.oobium.build.console.commands.OpenCommand;
import org.oobium.build.console.commands.PwdCommand;
import org.oobium.build.console.commands.RefreshCommand;
import org.oobium.build.console.commands.RemoteCommand;
import org.oobium.build.console.commands.RemoveCommand;
import org.oobium.build.console.commands.RmCommand;
import org.oobium.build.console.commands.SetCommand;
import org.oobium.build.console.commands.ShowCommand;
import org.oobium.build.console.commands.StartCommand;
import org.oobium.build.console.commands.StopCommand;
import org.oobium.build.console.commands.TouchCommand;
import org.oobium.build.workspace.Application;
import org.oobium.build.workspace.Bundle;
import org.oobium.build.workspace.Migrator;
import org.oobium.build.workspace.Module;
import org.oobium.build.workspace.Project;
import org.oobium.build.workspace.TestSuite;
import org.oobium.build.workspace.Workspace;


public class BuilderRootCommand extends BuilderCommand {

	private Workspace workspace;
	private Application application;
	private Project project;
	private String pwd;
	
	private PropertyChangeSupport listeners;
	
	public BuilderRootCommand(Workspace workspace) {
		this.workspace = workspace;
		this.listeners = new PropertyChangeSupport(this);
	}
	
	public void addListener(String propertyName, PropertyChangeListener listener) {
		this.listeners.addPropertyChangeListener(propertyName, listener);
	}
	
	@Override
	public void configure() {
		add(new AddCommand());
		add(new BrowseCommand());
		add(new CatCommand());
		add(new CdCommand());
		add(new CleanCommand());
		add(new CreateCommand());
		add(new DebugCommand());
		add(new DestroyCommand());
		add(new ExportCommand());
		add(new GenerateCommand());
		add(new GetCommand());
		add(new HttpCommand());
		add(new ImportCommand());
		add(new LsCommand());
		add(new MigrateCommand());
		add(new MkdirCommand());
		add(new OpenCommand());
		add(new PwdCommand());
		add(new RefreshCommand());
		add(new RemoteCommand());
		add(new RemoveCommand());
		add(new RmCommand());
		add(new SetCommand());
		add(new ShowCommand());
		add(new StartCommand());
		add(new StopCommand());
		add(new TouchCommand());
	}
	
	@Override
	public Workspace getWorkspace() {
		return workspace;
	}
	
	public Application getApplication() {
		return application;
	}

	public Bundle getBundle() {
		if(project instanceof Bundle) {
			return (Bundle) project;
		}
		return null;
	}
	
	public Project getProject() {
		return project;
	}
	
	public String getProjectName() {
		return (project != null) ? project.name : "";
	}
	
	public Module getModule() {
		if(project instanceof Module) {
			return (Module) project;
		}
		if(project instanceof Migrator) {
			return workspace.getModule(((Migrator) project).moduleName);
		}
		if(project instanceof TestSuite) {
			return workspace.getModule(((TestSuite) project).moduleName);
		}
		return null;
	}
	
	public String getPwd() {
		return (pwd != null) ? pwd : System.getProperty("user.dir");
	}

	public boolean hasApplication() {
		return application != null;
	}
	
	public boolean hasBundle() {
		return project instanceof Bundle;
	}

	public boolean hasModule() {
		return getModule() != null;
	}
	
	public void removeListener(String propertyName, PropertyChangeListener listener) {
		listeners.removePropertyChangeListener(propertyName, listener);
	}
	
	public void setApplication(Application application) {
		Application oldValue = this.application;
		this.application = application;
		listeners.firePropertyChange("application", oldValue, application);
	}

	public void setProject(Project project) {
		Project oldValue = this.project;
		this.project = project;
		listeners.firePropertyChange("project", oldValue, project);
	}
	
	public boolean setPwd(String pwd) {
		File dir = new File(pwd);
		if(!dir.isAbsolute()) {
			dir = new File(getPwd(), pwd);
		}
		if(dir.isDirectory()) {
			try {
				String oldValue = this.pwd;
				this.pwd = dir.getCanonicalPath();
				listeners.firePropertyChange("pwd", oldValue, this.pwd);
				return true;
			} catch(IOException e) {
				// fall through
			}
		}
		return false;
	}
	
}
