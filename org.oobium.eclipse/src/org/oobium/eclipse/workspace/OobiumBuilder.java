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
package org.oobium.eclipse.workspace;

import java.io.File;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.oobium.build.util.ProjectUtils;
import org.oobium.build.workspace.Bundle;
import org.oobium.build.workspace.Module;
import org.oobium.eclipse.OobiumPlugin;
import org.oobium.logging.Logger;
import org.oobium.utils.FileUtils;

public class OobiumBuilder extends IncrementalProjectBuilder {

	public static final String ID = OobiumBuilder.class.getCanonicalName();
	
	private Logger logger;

	public OobiumBuilder() {
		logger = Logger.getLogger(OobiumPlugin.class);
	}

	@Override
	@SuppressWarnings("rawtypes")
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor) throws CoreException {
		IProject project = getProject();
		if(kind == IncrementalProjectBuilder.FULL_BUILD) {
			logger.trace("OobiumBuilder: full build");
			File file = project.getLocation().toFile();
			Bundle bundle = OobiumPlugin.getWorkspace().getBundle(file);
			if(bundle instanceof Module) {
				FileUtils.deleteContents(((Module) bundle).generated);
				OobiumCore.generate(project, monitor);
			}
		} else {
			IResourceDelta delta = getDelta(project);
			if(delta != null) {
				incrementalBuild(delta, monitor);
			}
		}
		return null;
	}

	private void incrementalBuild(IResourceDelta delta, final IProgressMonitor monitor) {
		logger.trace("OobiumBuilder: incremental build");
		try {
			delta.accept(new IResourceDeltaVisitor() {
				public boolean visit(IResourceDelta delta) {
					IResource resource = delta.getResource();
					if(resource instanceof IFolder) {
						if("generated".equals(resource.getName())) {
							return false;
						}
						if("assets".equals(resource.getName())) {
							OobiumCore.generateAssetList(resource.getProject(), monitor);
							return false; // all changes to children are handled with generateAssetList
						}
						if(resource.getName().endsWith("bin")) {
							return false; // handled in UpdaterThread in org.oobium.build.runner
						}
					} else if(resource instanceof IFile) {
						String ext = resource.getFileExtension();
						if("java".equals(ext)) {
							File file = resource.getLocation().toFile();
							if(ProjectUtils.isModel(file)) {
								if(delta.getKind() == IResourceDelta.REMOVED) {
//									TODO OobiumCore.removeModel((IFile) resource, monitor);
								} else {
									OobiumCore.generateModel((IFile) resource, monitor);
								}
							} else if(ProjectUtils.isMailer(file)) {
								if(delta.getKind() == IResourceDelta.REMOVED) {
//									TODO OobiumCore.removeMailer((IFile) resource, monitor);
								} else {
									OobiumCore.generateMailer((IFile) resource, monitor);
								}
							}
						} else if(OobiumCore.isEFile(ext)) {
							if(delta.getKind() == IResourceDelta.REMOVED) {
								OobiumCore.remove((IFile) resource, monitor);
							} else {
								OobiumCore.generate((IFile) resource, monitor);
							}
						}
					}
					return true; // visit children too
				}
			});
		} catch(CoreException e) {
			logger.warn(e);
		}
	}
	
}
