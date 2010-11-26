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
package org.oobium.eclipse.connector.controllers;

import static org.oobium.utils.StringUtils.blank;

import java.io.File;
import java.sql.SQLException;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditor;
import org.oobium.app.server.controller.Controller;
import org.oobium.http.HttpRequest;

public class CommandController extends Controller {

	public CommandController(HttpRequest request, Map<String, Object> routeParams) {
		super(request, routeParams);
	}

	private void handleRemove() {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject project = root.getProject(param("project"));
		try {
			IProgressMonitor monitor = new NullProgressMonitor();
			project.delete(true, monitor);
		} catch(CoreException e) {
			e.printStackTrace();
		}
	}
	
	private void handleImport() {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject project = root.getProject(param("project"));
		IProgressMonitor monitor = new NullProgressMonitor();
		try {
			project.create(monitor);
			project.open(monitor);
		} catch(CoreException e1) {
			try {
				IProjectDescription description = root.getWorkspace().newProjectDescription(param("project"));
				description.setLocationURI(new File(param("file")).toURI());
				project.create(description, monitor);
				project.open(monitor);
			} catch(CoreException e2) {
				logger.warn(e2);
			}
		}
	}
	
	private void handleOpen() {
		try {
			IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(param("project"));
			if(!project.isOpen()) {
				project.open(new NullProgressMonitor());
			}
			final IFile file = project.getFile(param("file"));
			if(file.exists()) {
				file.refreshLocal(IResource.DEPTH_ONE, new NullProgressMonitor());
				Display.getDefault().syncExec(new Runnable() {
					@Override
					public void run() {
						try {
							IWorkbenchWindow window = PlatformUI.getWorkbench().getWorkbenchWindows()[0];
						    IEditorPart editor = IDE.openEditor(window.getActivePage(), file, true);
						    if(editor != null) {
								editor.setFocus();
								// TODO Shell#forceActive() does not currently work... check SWT bugs
								window.getShell().forceActive();
						    }
						} catch(PartInitException e) {
							e.printStackTrace();
						}
					}
				});
			}
			renderOK();
		} catch(CoreException e) {
			renderErrors(e.getMessage());
		}
	}

	private void handleOpenResource() {
		String resource = param("resource");
		final int line = param("line", int.class);
		throw new UnsupportedOperationException("not yet implemented: open resource {" + resource + ":" + line + "}");
	}

	private void handleOpenType() {
		String type = param("type");
		final int line = param("line", int.class);
		
		SearchPattern pattern = SearchPattern.createPattern(type, IJavaSearchConstants.TYPE, IJavaSearchConstants.DECLARATIONS, SearchPattern.R_EXACT_MATCH);
		IJavaSearchScope scope = SearchEngine.createWorkspaceScope();
		
		SearchRequestor sr = new SearchRequestor() {
			@Override
			public void acceptSearchMatch(SearchMatch match) throws CoreException {
				IResource resource = match.getResource();
				if(resource instanceof IFile) {
					final IFile file = (IFile) match.getResource();
					System.out.println(match);
					Display.getDefault().syncExec(new Runnable() {
						@Override
						public void run() {
							try {
								IWorkbenchWindow window = PlatformUI.getWorkbench().getWorkbenchWindows()[0];
							    IEditorPart editor = IDE.openEditor(window.getActivePage(), file, true);
							    if(editor instanceof AbstractDecoratedTextEditor) {
								    AbstractDecoratedTextEditor ed = (AbstractDecoratedTextEditor) editor;
								    IDocument doc = ed.getDocumentProvider().getDocument(ed.getEditorInput());
								    try {
										int offset = doc.getLineOffset(line - 1);
										ed.selectAndReveal(offset, 0);
									} catch(BadLocationException e) {
										e.printStackTrace();
									}
							    }
								editor.setFocus();
								// TODO Shell#forceActive() does not currently work... check SWT bugs
								window.getShell().forceActive();
							} catch(PartInitException e) {
								e.printStackTrace();
							}
						}
					});
				}
			}
		};
			
		SearchEngine engine = new SearchEngine();
		try {
			engine.search(pattern, new SearchParticipant[] { SearchEngine.getDefaultSearchParticipant() }, scope, sr, null);
		} catch(CoreException e) {
			e.printStackTrace();
		}
	}

	private void handleRefresh() {
		if(hasParam("project")) {
			try {
				IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(param("project"));
				if(!project.isOpen()) {
					project.open(new NullProgressMonitor());
				}

				if(hasParam("file")) {
					final IFile file = project.getFile(param("file"));
					file.refreshLocal(IResource.DEPTH_ONE, new NullProgressMonitor());
				} else {
					project.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
				}
				
				renderOK();
			} catch(CoreException e) {
				renderErrors(e.getMessage());
			}
		}
	}
	
	@Override
	public void handleRequest() throws SQLException {
		String cmd = param("command");
		if(!blank(cmd)) {
			if("open".equals(cmd)) {
				handleOpen();
			} else if("open_resource".equals(cmd)) {
				handleOpenResource();
			} else if("open_type".equals(cmd)) {
				handleOpenType();
			} else if("refresh".equals(cmd)) {
				handleRefresh();
			} else if("import".equals(cmd)) {
				handleImport();
			} else if("remove".equals(cmd)) {
				handleRemove();
			} else {
				renderErrors("unknown command: " + cmd);
			}
		} else {
			renderErrors("command not given");
		}
	}
	
}
