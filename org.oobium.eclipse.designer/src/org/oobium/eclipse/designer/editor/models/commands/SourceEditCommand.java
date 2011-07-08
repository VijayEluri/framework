package org.oobium.eclipse.designer.editor.models.commands;

import static org.oobium.utils.StringUtils.*;

import org.eclipse.gef.commands.Command;
import org.oobium.build.model.ModelRelation;
import org.oobium.eclipse.designer.editor.models.Connection;
import org.oobium.eclipse.designer.editor.models.ModelElement;

public class SourceEditCommand extends Command {

	private Connection connection;
	private Connection targetConnection;
	
	private ModelElement model;
	private ModelRelation oldRelation;
	private ModelRelation newRelation;
	
	private String field;
	private boolean hasMany;
	
	public SourceEditCommand(Connection connection) {
		this.connection = connection;
	}

	public void setField(String field) {
		this.field = field;
	}
	
	public void setHasMany(boolean hasMany) {
		this.hasMany = hasMany;
	}
	
	@Override
	public boolean canExecute() {
		return connection != null && !blank(field);
	}
	
	@Override
	public void execute() {
		model = connection.getSourceModel();
		oldRelation = model.getDefinition().getRelation(connection.getSourceField());

		newRelation = oldRelation.getCopy(hasMany);
		newRelation.name = field;
		
		if(oldRelation.hasOpposite()) {
//			orelation = ...
		}
		
		redo();
	}
	
	@Override
	public void redo() {
		model.replace(oldRelation, newRelation);
		connection.reconnect(model, newRelation.name, connection.getTargetModel(), connection.getTargetField());
	}
	
	@Override
	public void undo() {
		model.replace(newRelation, oldRelation);
		connection.reconnect(model, oldRelation.name, connection.getTargetModel(), connection.getTargetField());
	}
	
}
