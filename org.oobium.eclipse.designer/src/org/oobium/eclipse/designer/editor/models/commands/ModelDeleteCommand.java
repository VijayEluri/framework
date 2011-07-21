package org.oobium.eclipse.designer.editor.models.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.gef.commands.Command;
import org.oobium.build.workspace.Module;
import org.oobium.eclipse.designer.editor.models.Connection;
import org.oobium.eclipse.designer.editor.models.ModelCommitListener;
import org.oobium.eclipse.designer.editor.models.ModelElement;
import org.oobium.eclipse.designer.editor.models.ModuleElement;

public class ModelDeleteCommand extends Command implements ModelCommitListener {

	private ModuleElement module;
	private ModelElement model;
	private Backup deletedModel;
	private Backup deletedController;
	private Backup deletedViews;
	private List<ConnectionDeleteCommand> cmds;
	
//	private boolean isUndo;
	
	public ModelDeleteCommand(ModelElement model) {
		this.model = model;
	}
	
	@Override
	public void execute() {
		module = model.getModuleElement();

		module.getSite().addCommitListener(this);
		
		cmds = new ArrayList<ConnectionDeleteCommand>();
		for(Connection connection : model.getSourceConnections()) {
			cmds.add(new ConnectionDeleteCommand(connection));
		}
		for(Connection connection : model.getTargetConnections()) {
			cmds.add(new ConnectionDeleteCommand(connection));
		}
		
		module.removeModel(model);
		
		for(ConnectionDeleteCommand cmd : cmds) {
			cmd.execute();
		}
	}
	
	@Override
	protected void finalize() throws Throwable {
		if(deletedModel != null) {
			deletedModel.dispose();
			deletedController.dispose();
			deletedViews.dispose();
		}
		super.finalize();
	}

	@Override
	public void preCommit(ModelElement m) {
		Module module = this.module.getModule();
		if(deletedModel == null) {
			deletedModel = new Backup(model.getFile());
			deletedController = new Backup(module.getControllerFor(model.getFile()));
			deletedViews = new Backup(module.getViewsFolder(model.getFile()));
		} else {
			deletedModel.restore();
			deletedController.restore();
			deletedViews.restore();
			deletedModel = null;
			deletedController = null;
			deletedViews = null;
		}
	}

	@Override
	public void postCommit(ModelElement model, Map<String, Object> mData) {
		// nothing to do
	}
	
	@Override
	public void redo() {
		module.removeModel(model);
		for(ConnectionDeleteCommand cmd : cmds) {
			cmd.redo();
		}
//		isUndo = false;
	}
	
	@Override
	public void undo() {
		module.addModel(model);
		for(ConnectionDeleteCommand cmd : cmds) {
			cmd.redo();
		}
//		isUndo = true;
	}
	
}
