package org.oobium.eclipse.designer.editor.models.commands;

import static org.oobium.utils.StringUtils.blank;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.gef.commands.Command;
import org.oobium.build.model.ModelRelation;
import org.oobium.eclipse.designer.editor.models.Connection;
import org.oobium.eclipse.designer.editor.models.ModelElement;

public class ConnectionReconnectCommand extends Command {

	private Connection connection;
	private List<Command> cmds;
	
	private ModelElement oldSourceModel;
	private String oldSourceField;
	private ModelElement oldTargetModel;
	private String oldTargetField;
	private ModelRelation origSourceRelation;
	private ModelRelation newSourceRelation;
	
	private ModelElement newSourceModel;
	private String newSourceField;
	private ModelElement newTargetModel;
	private String newTargetField;
	private ModelRelation origTargetRelation;
	private ModelRelation newTargetRelation;
	
	public ConnectionReconnectCommand(Connection connection) {
		this.connection = connection;
	}

	private void addCommand(Command cmd) {
		if(cmds == null) {
			cmds = new ArrayList<Command>(3);
		}
		cmds.add(cmd);
	}
	
	@Override
	public boolean canExecute() {
		if(newSourceModel != null && newSourceField != null) {
			return true;
		}
		else if(newTargetModel != null) {
			return true;
		}
		return false;
	}

	@Override
	public void execute() {
		oldSourceModel = connection.getSourceModel();
		oldSourceField = connection.getSourceField();
		oldTargetModel = connection.getTargetModel();
		oldTargetField = connection.getTargetField();
		
		if(newSourceModel != null) {
			origSourceRelation = newSourceModel.getDefinition().getRelation(newSourceField);
			newSourceRelation = newSourceModel.setRelation(newSourceField, oldTargetModel.getType(), oldTargetField, false);
			connection.reconnect(newSourceModel, newSourceField, oldTargetModel, oldTargetField);
		}
		else if(newTargetModel != null) {
			origSourceRelation = oldSourceModel.getDefinition().getRelation(oldSourceField);
			newSourceRelation = oldSourceModel.setRelation(oldSourceField, newTargetModel.getType(), newTargetField, false);
			if(oldTargetField != null) {
				origTargetRelation = oldTargetModel.getDefinition().getRelation(oldTargetField);
				if(origTargetRelation != null) {
					Connection conn = Connection.getConnection(oldTargetModel, oldTargetField);
					if(conn != null) {
						ConnectionDeleteCommand cmd = new ConnectionDeleteCommand(conn);
						cmd.setRemoveTarget(false);
						addCommand(cmd);
					}
				}
			}
			if(newTargetField != null) {
				origTargetRelation = newTargetModel.getDefinition().getRelation(newTargetField);
				if(origTargetRelation != null) {
					Connection conn = Connection.getConnection(newTargetModel, newTargetField);
					// conn will be null if the endpoint was _just_ added by the TargetCreateCommand (as part of a CompoundCommand)
					if(conn != null) {
						if(conn.getTargetModel() != oldSourceModel || conn.getTargetField() == null || !conn.getTargetField().equals(oldSourceField)) {
							ConnectionReconnectCommand cmd = new ConnectionReconnectCommand(conn);
							cmd.setTarget(oldSourceModel, oldSourceField);
							addCommand(cmd);
						}
					}
				}
				newTargetRelation = newTargetModel.setRelation(newTargetField, oldSourceModel.getType(), oldSourceField, false);
			}
			connection.reconnect(oldSourceModel, oldSourceField, newTargetModel, newTargetField);
		}
		if(cmds != null) {
			for(Command cmd : cmds) { cmd.execute(); }
		}
	}

	@Override
	public void redo() {
		if(newSourceModel != null) {
			newSourceModel.setRelation(newSourceRelation);
			connection.reconnect(newSourceModel, newSourceField, oldTargetModel, oldTargetField);
		}
		else if(newTargetModel != null) {
			if(newTargetField != null) {
				newTargetModel.setRelation(newTargetRelation);
			}
			connection.reconnect(oldSourceModel, oldSourceField, newTargetModel, newTargetField);
		}
		if(cmds != null) {
			for(Command cmd : cmds) { cmd.redo(); }
		}
	}

	public Connection getConnection() {
		return connection;
	}
	
	public ModelElement getSourceModel() {
		return newSourceModel;
	}
	
	public String getSourceField() {
		return newSourceField;
	}

	public ModelElement getTargetModel() {
		return newTargetModel;
	}
	
	public String getTargetField() {
		return newTargetField;
	}
	
	public boolean hasTargetField() {
		return !blank(newTargetField);
	}
	
	public void setSource(ModelElement model, String field) {
		newTargetModel = null;
		newTargetField = null;
		setLabel("move connection startpoint");
		newSourceModel = model;
		newSourceField = field;
	}
	
	public void setTarget(ModelElement model, String field) {
		newSourceModel = null;
		newSourceField = null;
		setLabel("move connection endpoint");
		newTargetModel = model;
		newTargetField = field;
	}
	
	@Override
	public void undo() {
		if(cmds != null) {
			for(Command cmd : cmds) { cmd.undo(); }
		}
		if(newSourceModel != null) {
			if(origSourceRelation == null) {
				newSourceModel.remove(newSourceField);
			} else {
				newSourceModel.setRelation(origSourceRelation);
			}
			if(newTargetField != null) {
				if(origTargetRelation == null) {
					newTargetModel.remove(newTargetField);
				} else {
					newTargetModel.setRelation(origTargetRelation);
				}
			}
		}
		else if(newTargetModel != null) {
			oldSourceModel.setRelation(origSourceRelation);
			if(newTargetField != null) {
				if(origTargetRelation == null) {
					newTargetModel.remove(newTargetField);
				} else {
					newTargetModel.setRelation(origTargetRelation);
				}
			}
		}
		connection.reconnect(oldSourceModel, oldSourceField, oldTargetModel, oldTargetField);
	}
	
}
