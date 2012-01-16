package org.oobium.eclipse.designer.editor;

import java.util.EventObject;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.gef.ContextMenuProvider;
import org.eclipse.gef.DefaultEditDomain;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.SnapToGrid;
import org.eclipse.gef.editparts.ScalableFreeformRootEditPart;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.gef.ui.parts.GraphicalEditor;
import org.eclipse.gef.ui.parts.GraphicalViewerKeyHandler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.oobium.eclipse.designer.editor.actions.CreateConnectionsAction;
import org.oobium.eclipse.designer.editor.actions.CreateModelsAction;
import org.oobium.eclipse.designer.editor.factories.DesignerEditPartFactory;
import org.oobium.eclipse.designer.editor.models.SiteElement;
import org.oobium.eclipse.designer.editor.models.commands.ModelSetConstraintCommand;
import org.oobium.eclipse.designer.editor.tools.DesignerSelectionTool;
import org.oobium.eclipse.designer.outline.DesignerOutlinePage;

public class Designer extends GraphicalEditor {

	public static String ID = Designer.class.getCanonicalName();

	private DesignerToolBar actionbar;
	private SiteElement diagram;
	
	private DesignerOutlinePage outlinePage;

	public Designer() {
		DefaultEditDomain domain = new DefaultEditDomain(this);
		domain.setDefaultTool(new DesignerSelectionTool());
		domain.setActiveTool(domain.getDefaultTool());
		setEditDomain(domain);
	}
	
	@Override
	protected void configureGraphicalViewer() {
		super.configureGraphicalViewer();
		
		ActionRegistry registry = getActionRegistry();
		registry.registerAction(new CreateModelsAction(getEditDomain()));
		registry.registerAction(new CreateConnectionsAction(getEditDomain()));

		GraphicalViewer viewer = getGraphicalViewer();
		viewer.setEditPartFactory(new DesignerEditPartFactory());
		viewer.setRootEditPart(new ScalableFreeformRootEditPart());
		viewer.setKeyHandler(new GraphicalViewerKeyHandler(viewer));
		viewer.setProperty(SnapToGrid.PROPERTY_GRID_ENABLED, true);
		
		ContextMenuProvider provider = new DesignerContextMenuProvider(viewer, getActionRegistry());
		viewer.setContextMenu(provider);

		getSite().registerContextMenu(provider, viewer);
	}
	
	@Override
	public void createPartControl(Composite parent) {
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.verticalSpacing = 0;
		parent.setLayout(layout);
		
		actionbar = new DesignerToolBar(parent);
		actionbar.setEditDomain(getEditDomain());
		
		super.createPartControl(parent);
		
		GraphicalViewer viewer = getGraphicalViewer();
		viewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		actionbar.setViewer(viewer);
	}
	
	@Override
	public void dispose() {
		if(outlinePage != null) {
			outlinePage.setInput(null);
		}
		super.dispose();
	}
	
	@Override
	public void commandStackChanged(EventObject event) {
		firePropertyChange(PROP_DIRTY);
		super.commandStackChanged(event);
		if(outlinePage != null) {
			if(!(getCommandStack().getUndoCommand() instanceof ModelSetConstraintCommand)) {
				outlinePage.refresh();
			}
		}
	}
	
	@Override
	protected void initializeGraphicalViewer() {
		GraphicalViewer viewer = getGraphicalViewer();
		viewer.setContents(getModel());
	}

	@SuppressWarnings("rawtypes")
	public Object getAdapter(Class type) {
		if(IContentOutlinePage.class.equals(type)) {
			if(outlinePage == null) {
				outlinePage = new DesignerOutlinePage();
				if(diagram != null) {
					outlinePage.setInput(diagram);
				}
			}
			return outlinePage;
		}
		return super.getAdapter(type);
	};
	
	public SiteElement getModel() {
		return diagram;
	}
	
	@Override
	protected void setInput(IEditorInput input) {
		super.setInput(input);
		IFile file = ((IFileEditorInput) input).getFile();
		try {
			diagram = SiteElement.load(file);
		} catch(CoreException e) {
			e.printStackTrace();
		}
		if(outlinePage != null) {
			outlinePage.setInput(diagram);
		}
	}
	
	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		IFile file = ((IFileEditorInput) getEditorInput()).getFile();
		try {
			SiteElement.save(diagram, file, monitor);
			getCommandStack().markSaveLocation();
			if(outlinePage != null) {
				outlinePage.setInput(diagram);
			}
		} catch(CoreException e) {
			e.printStackTrace();
		}
	}
	
}
