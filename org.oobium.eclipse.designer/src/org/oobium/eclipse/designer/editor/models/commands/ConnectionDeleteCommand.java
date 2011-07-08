package org.oobium.eclipse.designer.editor.models.commands;

import static org.oobium.eclipse.designer.editor.models.Connection.getConnection;

import org.eclipse.gef.commands.Command;
import org.oobium.build.model.ModelRelation;
import org.oobium.eclipse.designer.editor.models.Connection;

public class ConnectionDeleteCommand extends Command {

	private final Connection connection;
	private ModelRelation sourceRelation;
	private ModelRelation targetRelation;
	private boolean removeSource = true;
	private boolean removeTarget = true;
	
	private Connection opposite;

	public ConnectionDeleteCommand(Connection connection) {
		this.connection = connection;
	}

	@Override
	public boolean canExecute() {
		return connection != null;
	}
	
	@Override
	public void execute() {
		sourceRelation = connection.getSourceModel().getDefinition().getRelation(connection.getSourceField());
		targetRelation = connection.getTargetModel().getDefinition().getRelation(connection.getTargetField());
		if(targetRelation != null && targetRelation.opposite != null) {
			opposite = getConnection(connection.getTargetModel(), connection.getTargetField());
		}
		redo();
	}

	@Override
	public void redo() {
		if(removeSource) {
			connection.getSourceModel().remove(connection.getSourceField());
		}
		if(removeTarget) {
			connection.getTargetModel().remove(connection.getTargetField());
		}
		connection.disconnect();
		if(opposite != null) {
			opposite.disconnect();
		}
	}
	
	public void setRemoveSource(boolean remove) {
		this.removeSource = remove;
	}
	
	public void setRemoveTarget(boolean remove) {
		this.removeTarget = remove;
	}
	
	@Override
	public void undo() {
		if(removeSource) {
			connection.getSourceModel().setRelation(sourceRelation);
		}
		if(removeTarget) {
			connection.getTargetModel().setRelation(targetRelation);
		}
		connection.reconnect();
		if(opposite != null) {
			opposite.reconnect();
		}
	}
	
}
