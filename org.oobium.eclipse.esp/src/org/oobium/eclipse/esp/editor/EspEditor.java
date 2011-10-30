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
package org.oobium.eclipse.esp.editor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.ITextInputListener;
import org.eclipse.jface.text.ITextViewerExtension5;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.projection.ProjectionSupport;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
import org.eclipse.ui.texteditor.TextOperationAction;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.oobium.build.esp.ESourceFile;
import org.oobium.build.esp.EspDom;
import org.oobium.build.esp.EspElement;
import org.oobium.build.esp.EspPart;
import org.oobium.build.esp.EspPart.Type;
import org.oobium.build.workspace.Module;
import org.oobium.eclipse.esp.EspCore;
import org.oobium.eclipse.esp.editor.actions.DefineFoldingRegionAction;
import org.oobium.eclipse.esp.outline.EspOutlinePage;
import org.oobium.eclipse.workspace.OobiumCore;

public class EspEditor extends TextEditor {
	
	public static final String ID = EspEditor.class.getCanonicalName();
	
	/**
	 * The EFile (.esp, .emt, .ess, .ejs) being edited
	 */
	private IFile eResource;
	
	/**
	 * The Java source file that is generated from {@link #eResource}
	 */
	private IFile jResource;
	
	/**
	 * The ESourceFile that is created from {@link #eResource}
	 */
	private ESourceFile eSourceFile;
	
	private StyledText textWidget;
	private IDocument document;
	private IResourceChangeListener buildListener;
	
	private EspOutlinePage outlinePage;
	private ProjectionSupport projectionSupport;
	private IDocumentListener docListener;
	private ISelectionChangedListener selListener;
	private UpdateDaemon updateDaemonFast;
	private UpdateDaemon updateDaemonSlow;
	
	public EspEditor() {
		super();
		
		docListener = new IDocumentListener() {
			@Override
			public void documentAboutToBeChanged(DocumentEvent event) {
				// nothing to do
			}
			@Override
			public void documentChanged(DocumentEvent event) {
				updateDaemonFast.interrupt();
				updateDaemonSlow.interrupt();
			}
		};
		
		selListener = new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				ISelection selection = event.getSelection();
				if(selection.isEmpty()) {
					resetHighlightRange();
				} else if(document != null) {
					EspOutlinePage page = outlinePage;
					outlinePage = null;
					Object sel = ((IStructuredSelection) selection).getFirstElement();
					if(sel instanceof EspElement) {
						EspElement element = (EspElement) sel;
						try {
//							setHighlightRange(offset, length, true);
							int start = element.getStart();
							int length = element.getElementText().length();
							selectAndReveal(start, length);
						} catch(IllegalArgumentException x) {
							resetHighlightRange();
						}
					} else if(sel instanceof EspPart) {
						EspPart part = (EspPart) sel;
						int start = part.getStart();
						int end = part.getEnd();
						selectAndReveal(start, end-start);
					}
					outlinePage = page;
				}
			}
		};

		updateDaemonFast = new UpdateDaemon(50, new Runnable() {
			@Override
			public void run() {
				updateStyleRanges();
			}
		});

		updateDaemonSlow = new UpdateDaemon(750, new Runnable() {
			@Override
			public void run() {
				updateSourceFiles();
				updateOutline();
			}
		});
	}

	/*
	 * @see org.eclipse.ui.texteditor.AbstractTextEditor#adjustHighlightRange(int, int)
	 */
	protected void adjustHighlightRange(int offset, int length) {
		ISourceViewer viewer= getSourceViewer();
		if (viewer instanceof ITextViewerExtension5) {
			ITextViewerExtension5 extension= (ITextViewerExtension5) viewer;
			extension.exposeModelRange(new Region(offset, length));
		}
	}
	
	protected void createActions() {
		super.createActions();
		
		IAction a= new TextOperationAction(EspEditorMessages.getResourceBundle(), "ContentAssistProposal.", this, ISourceViewer.CONTENTASSIST_PROPOSALS); //$NON-NLS-1$
		a.setActionDefinitionId(ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS);
		setAction("ContentAssistProposal", a); //$NON-NLS-1$
		
		a= new TextOperationAction(EspEditorMessages.getResourceBundle(), "ContentAssistTip.", this, ISourceViewer.CONTENTASSIST_CONTEXT_INFORMATION);  //$NON-NLS-1$
		a.setActionDefinitionId(ITextEditorActionDefinitionIds.CONTENT_ASSIST_CONTEXT_INFORMATION);
		setAction("ContentAssistTip", a); //$NON-NLS-1$
		
		a= new DefineFoldingRegionAction(EspEditorMessages.getResourceBundle(), "DefineFoldingRegion.", this); //$NON-NLS-1$
		setAction("DefineFoldingRegion", a); //$NON-NLS-1$
	}

	public void createPartControl(Composite parent) {
		super.createPartControl(parent);

		ProjectionViewer viewer= (ProjectionViewer) getSourceViewer();
		textWidget = viewer.getTextWidget();
		
		updateDaemonFast.start();
		updateDaemonSlow.start();
		textWidget.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				updateDaemonFast.cancel();
				updateDaemonSlow.cancel();
				updateDaemonFast = null;
				updateDaemonSlow = null;
			}
		});
		
		projectionSupport= new ProjectionSupport(viewer, getAnnotationAccess(), getSharedColors());
		projectionSupport.addSummarizableAnnotationType("org.eclipse.ui.workbench.texteditor.error"); //$NON-NLS-1$
		projectionSupport.addSummarizableAnnotationType("org.eclipse.ui.workbench.texteditor.warning"); //$NON-NLS-1$
		projectionSupport.install();
		
		viewer.doOperation(ProjectionViewer.TOGGLE);
		
		viewer.addTextInputListener(new ITextInputListener() {
			@Override
			public void inputDocumentAboutToBeChanged(IDocument oldInput, IDocument newInput) {
			}
			@Override
			public void inputDocumentChanged(IDocument oldInput, IDocument newInput) {
				if(oldInput != null) {
					EspCore.remove(oldInput);
				}
			}
		});
		
		IFile file = getEResource();
		OobiumCore.editing(file, true);
		if(OobiumCore.isEFile(file)) {
			buildListener = new IResourceChangeListener() {
				@Override
				public void resourceChanged(IResourceChangeEvent event) {
					IResourceDelta delta = event.getDelta();
					if(delta != null) {
						delta = delta.findMember(getJavaResource().getFullPath());
						if(delta != null) {
							updateStyleRanges();
							updateMarkers();
						}
					}
				}
			};
			ResourcesPlugin.getWorkspace().addResourceChangeListener(buildListener, IResourceChangeEvent.POST_BUILD);
		}

		if(document != null) {
			updateStyleRanges();
			updateMarkers();
		}
	}
	
	protected ISourceViewer createSourceViewer(Composite parent, IVerticalRuler ruler, int styles) {
		fAnnotationAccess= createAnnotationAccess();
		fOverviewRuler= createOverviewRuler(getSharedColors());
		
		ISourceViewer viewer= new ProjectionViewer(parent, ruler, getOverviewRuler(), isOverviewRulerVisible(), styles);
		getSourceViewerDecorationSupport(viewer);

		return viewer;
	}

	public void dispose() {
		OobiumCore.editing(getEResource(), false);
		if(buildListener != null) {
			ResourcesPlugin.getWorkspace().removeResourceChangeListener(buildListener);
		}
		if(outlinePage != null) {
			outlinePage.setInput(null);
		}
		super.dispose();
	}
	
	public void doRevertToSaved() {
		super.doRevertToSaved();
		if(outlinePage != null) {
			outlinePage.setInput(document);
		}
	}
	
	public void doSave(IProgressMonitor monitor) {
		super.doSave(monitor);
		eSourceFile = null;
		if(outlinePage != null) {
			outlinePage.setInput(document);
		}
	}
	
	public void doSaveAs() {
		super.doSaveAs();
		eResource = null;
		eSourceFile = null;
		jResource = null;
		if(outlinePage != null) {
			outlinePage.setInput(document);
		}
	}
	
	public void doSetInput(IEditorInput input) throws CoreException {
		if(document != null) {
			document.removePrenotifiedDocumentListener(docListener);
		}
		
		super.doSetInput(input);
		
		if(input != null) {
			document = getDocumentProvider().getDocument(input);
			String name = getEditorInput().getName();
			int ix = name.indexOf(' '); // covers Subclipse names
			if(ix != -1) {
				name = name.substring(0, ix);
			}
			EspCore.get(document).setName(name);
		} else {
			document = null;
		}
		if(outlinePage != null) {
			outlinePage.setInput(document);
		}
		if(document != null) {
			document.addPrenotifiedDocumentListener(docListener);
		}
		
		eSourceFile = null;
	}
	
	protected void editorContextMenuAboutToShow(IMenuManager menu) {
		super.editorContextMenuAboutToShow(menu);
		addAction(menu, "ContentAssistProposal"); //$NON-NLS-1$
		addAction(menu, "ContentAssistTip"); //$NON-NLS-1$
		addAction(menu, "DefineFoldingRegion");  //$NON-NLS-1$
	}
	
	@SuppressWarnings("rawtypes")
	public Object getAdapter(Class required) {
		if(IContentOutlinePage.class.equals(required)) {
			if(outlinePage == null) {
				outlinePage = new EspOutlinePage(getDocumentProvider(), this);
				if(document != null) {
					outlinePage.setInput(document);
					outlinePage.addSelectionChangedListener(selListener);
				}
			}
			return outlinePage;
		}
		
		if (projectionSupport != null) {
			Object adapter= projectionSupport.getAdapter(getSourceViewer(), required);
			if(adapter != null) {
				return adapter;
			}
		}
		
		return super.getAdapter(required);
	}
	
	private IFile getEResource() {
		if(eResource == null) {
			eResource = (IFile) getEditorInput().getAdapter(IFile.class);
		}
		return eResource;
	}
	
	public ESourceFile getEspJavaFile() {
		if(eSourceFile == null) {
			updateSourceFiles();
		}
		return eSourceFile;
	}
	
	public IFile getJavaResource() {
		if(jResource == null) {
			IFile res = getEResource();
			IProject project = res.getProject();
			Module module = OobiumCore.getModule(res);
			if(module != null) {
				File genFile = module.getGenFile(res.getLocation().toFile());
				if(genFile != null) {
					String genFileName = genFile.getAbsolutePath().substring(project.getLocation().toOSString().length() + 1);
					jResource = project.getFile(genFileName);
				}
			}
		}
		return jResource;
	}
	
	public IMarker[] getMarkers(int offset) {
		IFile file = getEResource();
		if(file != null && file.exists() && OobiumCore.isEFile(file)) {
			try {
				return file.findMarkers(IMarker.MARKER, true, IResource.DEPTH_ZERO);
			} catch(CoreException e) {
				e.printStackTrace();
			}
		}		
		return null;
	}
	
	@Override
	protected void handleCursorPositionChanged() {
		super.handleCursorPositionChanged();
		if(outlinePage != null && document != null) {
			EspDom dom = EspCore.get(document);
			int offset = textWidget.getCaretOffset();
			EspPart part = dom.getPart(offset);
			if(part != null && !part.isA(Type.StyleSelectorPart)) {
				part = part.getElement();
			}

			outlinePage.removeSelectionChangedListener(selListener);
			outlinePage.setSelection((part == null) ? new StructuredSelection() : new StructuredSelection(part));
			outlinePage.addSelectionChangedListener(selListener);
		}
	}

	protected void initializeEditor() {
		super.initializeEditor();
		setSourceViewerConfiguration(new EspSourceViewerConfiguration(this));
	}
	
	/**
	 * this method is only valid for ESP files (which have a corresponding Java file);
	 * for all other types of files this method simply returns
	 */
	private void updateMarkers() {
		IFile file = getEResource();
		if(file == null || !file.exists() || !OobiumCore.isEFile(file)) {
			return;
		}
		
		System.out.println("update markers");
		
		try {
			IMarker[] markers = file.findMarkers(IMarker.MARKER, true, IResource.DEPTH_ZERO);
			for(IMarker marker : markers) {
				marker.delete();
			}
			
			IFile jfile = getJavaResource();
			if(jfile == null || !jfile.exists()) {
				return;
			}
			
			markers = jfile.findMarkers(IMarker.PROBLEM, true, IResource.DEPTH_ZERO);
			if(markers.length > 0) {
				final List<Integer> ranges = new ArrayList<Integer>();
				ESourceFile jf = getEspJavaFile();
				for(IMarker jmarker : markers) {
					if(jmarker.isSubtypeOf(IMarker.PROBLEM)) {
						try {
							int start = jf.getEspOffset((Integer) jmarker.getAttribute(IMarker.CHAR_START));
							int end = jf.getEspOffset((Integer) jmarker.getAttribute(IMarker.CHAR_END) - 1) + 1;

							IMarker marker = file.createMarker(jmarker.getType());
							if(start != -1 && end != -1) {
								marker.setAttribute(IMarker.CHAR_START, start);
								marker.setAttribute(IMarker.CHAR_END, end);
								ranges.add(start);
								ranges.add(end - start);
							} else {
								marker.setAttribute(IMarker.LINE_NUMBER, 1);
							}
							marker.setAttribute(IMarker.SEVERITY, jmarker.getAttribute(IMarker.SEVERITY));
							marker.setAttribute(IMarker.PRIORITY, IMarker.PRIORITY_HIGH);//jmarker.getAttribute(IMarker.PRIORITY));
							marker.setAttribute(IMarker.MESSAGE, jmarker.getAttribute(IMarker.MESSAGE));
						} catch(ClassCastException e) {
							e.printStackTrace();
						}
					}
				}
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						TextStyle style = new TextStyle();
						style.underline = true;
						style.underlineColor = Display.getDefault().getSystemColor(SWT.COLOR_RED);
						style.underlineStyle = SWT.UNDERLINE_SQUIGGLE;
						for(int i = 0; i < ranges.size(); i+=2) {
							StyleRange range = new StyleRange(style);
							range.start = ranges.get(i);
							range.length = ranges.get(i+1);
							textWidget.setStyleRange(range);
						}
					}
				});

			}
		} catch(CoreException e) {
			e.printStackTrace();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private void updateOutline() {
		if(outlinePage != null) {
			Display.getDefault().syncExec(new Runnable() {
				@Override
				public void run() {
					outlinePage.setInput(document);
				}
			});
		}
	}

	/**
	 * Generate {@link #eResource}, update {@link #jResource} and {@link ESourceFile}
	 */
	private void updateSourceFiles() {
		IFile esp = getEResource();
		if(OobiumCore.isEFile(esp)) {
			eSourceFile = OobiumCore.generate(esp, document.get());
			IFile file = getJavaResource();
			if(file != null) {
				try {
					file.refreshLocal(IResource.DEPTH_ONE, null);
					System.out.println("jResource refreshed");
				} catch (CoreException e1) {
					e1.printStackTrace();
				}
			}
		}
	}
	
	private void updateStyleRanges() {
		textWidget.getDisplay().syncExec(new Runnable() {
			@Override
			public void run() {
				if(!textWidget.isDisposed()) {
					EspStyleRanges ranges = new EspStyleRanges(EspCore.get(document));
					ranges.applyRanges(textWidget);
				}
			}
		});
	}
}
