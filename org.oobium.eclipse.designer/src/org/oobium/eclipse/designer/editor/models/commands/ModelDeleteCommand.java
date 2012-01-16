package org.oobium.eclipse.designer.editor.models.commands;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.gef.commands.Command;
import org.oobium.build.workspace.Module;
import org.oobium.eclipse.designer.editor.models.Connection;
import org.oobium.eclipse.designer.editor.models.ModelElement;
import org.oobium.eclipse.designer.editor.models.ModuleElement;
import org.oobium.eclipse.designer.editor.models.SiteSaveListener;

public class ModelDeleteCommand extends Command implements SiteSaveListener {

	private ModuleElement module;
	private ModelElement model;
	private List<Backup> backups;
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
		if(backups != null) {
			for(Backup backup : backups) {
				backup.dispose();
			}
		}
		super.finalize();
	}

	@Override
	public boolean preSave() {
		if(backups == null) {
			Module module = model.getModuleElement().getModule();
			backups = new ArrayList<Backup>();
			File file = model.getFile();
			backups.add(new Backup(file));
			backups.add(new Backup(module.getControllerFor(file)));
			backups.add(new Backup(module.getViewsFolder(file)));
			backups.add(new Backup(module.getNotifier(file)));
			model.destroy();
		} else {
			model.save();
			for(Backup backup : backups) {
				backup.restore();
			}
			backups.clear();
			backups = null;
		}
		return true;
	}

	@Override
	public void postSave() {
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
