package org.oobium.eclipse.designer.editor.models.commands;

import static org.oobium.utils.StringUtils.*;

import org.eclipse.gef.commands.Command;
import org.oobium.build.model.ModelRelation;
import org.oobium.eclipse.designer.editor.models.Connection;
import org.oobium.eclipse.designer.editor.models.ModelElement;

public class TargetCreateCommand extends Command {

	private Connection connection;
	
	private ModelElement model;
	private ModelRelation relation;
	
	private String field;
	private boolean hasMany;
	
	public TargetCreateCommand(Connection connection) {
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
		model = connection.getTargetModel();

		String type = connection.getSourceModel().getType();
		String opposite = connection.getSourceField();
		
		relation = model.setRelation(field, type, opposite, hasMany);
		relation.name(field);
		
		redo();
	}
	
	@Override
	public void redo() {
		model.setRelation(relation);
	}
	
	@Override
	public void undo() {
		model.remove(relation.name());
	}
	
}
