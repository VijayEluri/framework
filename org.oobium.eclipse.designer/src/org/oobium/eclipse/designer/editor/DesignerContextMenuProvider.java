package org.oobium.eclipse.designer.editor;

import org.eclipse.gef.ContextMenuProvider;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.gef.ui.actions.GEFActionConstants;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.ui.actions.ActionFactory;
import org.oobium.eclipse.designer.editor.actions.CreateConnectionsAction;
import org.oobium.eclipse.designer.editor.actions.CreateModelsAction;

public class DesignerContextMenuProvider extends ContextMenuProvider {
	
	private ActionRegistry actionRegistry;

	public DesignerContextMenuProvider(EditPartViewer viewer, ActionRegistry registry) {
		super(viewer);
		if(registry == null) {
			throw new IllegalArgumentException();
		}
		actionRegistry = registry;
	}

	@Override
	public void buildContextMenu(IMenuManager menu) {
		GEFActionConstants.addStandardActionGroups(menu);
		
		appendAction(menu, GEFActionConstants.GROUP_UNDO, ActionFactory.UNDO.getId());
		appendAction(menu, GEFActionConstants.GROUP_UNDO, ActionFactory.REDO.getId());
		appendAction(menu, GEFActionConstants.GROUP_UNDO, ActionFactory.SAVE.getId());
		appendAction(menu, GEFActionConstants.GROUP_EDIT, ActionFactory.DELETE.getId());
		appendAction(menu, GEFActionConstants.GROUP_ADD, CreateModelsAction.ID);
		appendAction(menu, GEFActionConstants.GROUP_ADD, CreateConnectionsAction.ID);
	}

	private void appendAction(IMenuManager menu, String group, String action) {
		menu.appendToGroup(group, actionRegistry.getAction(action));
	}
	
}
