package org.oobium.eclipse.designer.editor.models.commands;

import static org.oobium.utils.FileUtils.readFile;
import static org.oobium.utils.FileUtils.writeFile;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.gef.commands.Command;
import org.oobium.eclipse.designer.editor.models.Connection;
import org.oobium.eclipse.designer.editor.models.ModelElement;

public class ModelDeleteCommand extends Command {

	private ModelElement model;
	private String source;
	private List<ConnectionDeleteCommand> cmds;
	
	public ModelDeleteCommand(ModelElement model) {
		this.model = model;
	}
	
	@Override
	public void execute() {
		source = model.getFile().isFile() ? readFile(model.getFile()).toString() : null;
		cmds = new ArrayList<ConnectionDeleteCommand>();
		for(Connection connection : model.getSourceConnections()) {
			cmds.add(new ConnectionDeleteCommand(connection));
		}
		for(Connection connection : model.getTargetConnections()) {
			cmds.add(new ConnectionDeleteCommand(connection));
		}
		model.setDeleted(true);
		for(ConnectionDeleteCommand cmd : cmds) {
			cmd.execute();
		}
	}
	
	@Override
	public void redo() {
		model.setDeleted(true);
		for(ConnectionDeleteCommand cmd : cmds) {
			cmd.redo();
		}
	}
	
	@Override
	public void undo() {
		model.setDeleted(false);
		if(source != null && !model.getFile().exists()) {
			writeFile(model.getFile(), source);
		}
		for(ConnectionDeleteCommand cmd : cmds) {
			cmd.redo();
		}
	}
	
}
