package org.oobium.eclipse.designer.editor;

import java.util.EventObject;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.gef.ContextMenuProvider;
import org.eclipse.gef.DefaultEditDomain;
import org.eclipse.gef.EditDomain;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.Tool;
import org.eclipse.gef.dnd.TemplateTransferDropTargetListener;
import org.eclipse.gef.editparts.ScalableFreeformRootEditPart;
import org.eclipse.gef.requests.CreationFactory;
import org.eclipse.gef.requests.SimpleFactory;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.gef.ui.parts.GraphicalEditor;
import org.eclipse.gef.ui.parts.GraphicalViewerKeyHandler;
import org.eclipse.jface.util.TransferDropTargetListener;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.oobium.eclipse.designer.editor.actions.CreateConnectionsAction;
import org.oobium.eclipse.designer.editor.actions.CreateModelsAction;
import org.oobium.eclipse.designer.editor.factories.DesignerEditPartFactory;
import org.oobium.eclipse.designer.editor.models.SiteDiagram;
import org.oobium.eclipse.designer.editor.tools.DesignerSelectionTool;

public class Designer extends GraphicalEditor {

	public static String ID = Designer.class.getCanonicalName();

	
	private SiteDiagram diagram;

	public Designer() {
		DefaultEditDomain domain = new DefaultEditDomain(this);
		domain.setDefaultTool(new DesignerSelectionTool());
		domain.setActiveTool(domain.getDefaultTool());
		setEditDomain(domain);
	}
	
	@Override
	protected void configureGraphicalViewer() {
		super.configureGraphicalViewer();
		
		GraphicalViewer viewer = getGraphicalViewer();
		viewer.setEditPartFactory(new DesignerEditPartFactory());
		viewer.setRootEditPart(new ScalableFreeformRootEditPart());
		viewer.setKeyHandler(new GraphicalViewerKeyHandler(viewer));
		
		ActionRegistry registry = getActionRegistry();
		registry.registerAction(new CreateModelsAction(getEditDomain()));
		registry.registerAction(new CreateConnectionsAction(getEditDomain()));
		ContextMenuProvider provider = new DesignerContextMenuProvider(viewer, getActionRegistry());
		viewer.setContextMenu(provider);
		getSite().registerContextMenu(provider, viewer);
	}
	
	@Override
	public void commandStackChanged(EventObject event) {
		firePropertyChange(PROP_DIRTY);
		super.commandStackChanged(event);
	}
	
	@Override
	protected void initializeGraphicalViewer() {
		GraphicalViewer viewer = getGraphicalViewer();
		viewer.setContents(getModel());

		// TODO delete this
		viewer.addDropTargetListener(createTransferDropTargetListener());
	}

	// TODO delete this
	private TransferDropTargetListener createTransferDropTargetListener() {
		return new TemplateTransferDropTargetListener(getGraphicalViewer()) {
			protected CreationFactory getFactory(Object template) {
				return new SimpleFactory((Class<?>) template);
			}
		};
	}

	SiteDiagram getModel() {
		return diagram;
	}
	
	@Override
	protected void setInput(IEditorInput input) {
		super.setInput(input);
		IFile file = ((IFileEditorInput) input).getFile();
		try {
			diagram = SiteDiagram.load(file);
		} catch(CoreException e) {
			e.printStackTrace();
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
			SiteDiagram.save(diagram, file, monitor);
			getCommandStack().markSaveLocation();
		} catch(CoreException e) {
			e.printStackTrace();
		}
	}
	
	
}
