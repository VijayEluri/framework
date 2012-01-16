package org.oobium.eclipse.designer.editor.models.commands;

import static org.oobium.utils.StringUtils.*;

import org.eclipse.gef.commands.Command;
import org.oobium.build.model.ModelRelation;
import org.oobium.eclipse.designer.editor.models.Connection;
import org.oobium.eclipse.designer.editor.models.ModelElement;

public class ConnectionCreateCommand extends Command {

	private Connection connection;
	
	private ModelElement sourceModel;
	private ModelRelation oldSourceRelation;
	private ModelRelation newSourceRelation;
	
	private ModelElement targetModel;
	private ModelRelation oldTargetRelation;
	private ModelRelation newTargetRelation;

	private String sourceField;
	private String targetField;
	private boolean sourceHasMany;
	private boolean targetHasMany;

	private String throughField;
	private String throughSubField;
	

	public ConnectionCreateCommand(ModelElement sourceModel) {
		this.sourceModel = sourceModel;
	}
	
	public ConnectionCreateCommand(ModelElement sourceModel, ModelElement targetModel) {
		this.sourceModel = sourceModel;
		this.targetModel = targetModel;
	}
	
	@Override
	public boolean canExecute() {
		return sourceModel != null && targetModel != null;
	}

	@Override
	public boolean canUndo() {
		return connection != null;
	}
	
	@Override
	public void execute() {
		oldSourceRelation = sourceModel.getDefinition().getRelation(sourceField);
		oldTargetRelation = targetModel.getDefinition().getRelation(targetField);

		if(!blank(throughField)) {
			String through = (throughSubField == null || throughSubField.equals(varName(targetModel.getName()))) ? throughField : (throughField + ":" + throughSubField);
			newSourceRelation = sourceModel.setThroughRelation(sourceField, targetModel.getType(), through, sourceHasMany);
		}
		else {
			newSourceRelation = sourceModel.setRelation(sourceField, targetModel.getType(), targetField, sourceHasMany);
			newTargetRelation = blank(targetField) ? null : targetModel.setRelation(targetField, sourceModel.getType(), sourceField, targetHasMany);
		}
		
		connection = new Connection(sourceModel, sourceField, targetModel, targetField);
	}
	
	public ModelElement getSourceModel() {
		return sourceModel;
	}
	
	public ModelElement getTargetModel() {
		return targetModel;
	}
	
	@Override
	public void redo() {
		sourceModel.setRelation(newSourceRelation);
		if(newTargetRelation != null) {
			targetModel.setRelation(newTargetRelation);
		}
		connection.reconnect();
	}
	
	public void setSourceField(String field) {
		this.sourceField = field;
	}
	
	public void setSourceHasMany(boolean hasMany) {
		this.sourceHasMany = hasMany;
	}
	
	public void setTargetField(String field) {
		this.targetField = field;
	}
	
	public void setTargetHasMany(boolean hasMany) {
		this.targetHasMany = hasMany;
	}
	
	public void setThrough(String[] through) {
		throughField = (through == null || through.length < 1) ? null : through[0];
		throughSubField = (through == null || through.length < 2) ? null : through[1];
	}
	
	@Override
	public void undo() {
		connection.disconnect();

		if(oldSourceRelation == null) {
			sourceModel.remove(connection.getSourceField());
		} else {
			sourceModel.setRelation(oldSourceRelation);
		}

		if(oldTargetRelation == null) {
			targetModel.remove(connection.getTargetField());
		} else {
			targetModel.setRelation(oldTargetRelation);
		}
	}
	
}
