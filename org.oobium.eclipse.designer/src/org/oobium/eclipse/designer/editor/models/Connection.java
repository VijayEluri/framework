package org.oobium.eclipse.designer.editor.models;


public class Connection {

	public static Connection getConnection(ModelElement model, String field) {
		for(Connection c : model.getSourceConnections()) {
			if(field.equals(c.getSourceField())) {
				return c;
			}
		}
		return null;
	}

	
	private ModelElement sourceModel;
	private String sourceField;
	private ModelElement targetModel;
	private String targetField;
	private boolean connected;

	public Connection(ModelElement sourceModel, String sourceField, ModelElement targetModel) {
		reconnect(sourceModel, sourceField, targetModel, null);
	}
	
	public Connection(ModelElement sourceModel, String sourceField, ModelElement targetModel, String targetField) {
		reconnect(sourceModel, sourceField, targetModel, targetField);
	}
	
	public void disconnect() {
		if(connected) {
			sourceModel.removeConnection(this);
			targetModel.removeConnection(this);
			connected = false;
		}
	}
	
	public void reconnect() {
		if(!connected) {
			sourceModel.addConnection(this);
			targetModel.addConnection(this);
			connected = true;
		}
	}
	
	public void reconnect(ModelElement sourceModel, String sourceField, ModelElement targetModel, String targetField) {
		if(sourceModel == null || sourceField == null || targetModel == null) {
			throw new IllegalArgumentException();
		}
		if(sourceModel == targetModel && sourceField == targetField) {
			throw new IllegalArgumentException();
		}

		disconnect();
		this.sourceModel = sourceModel;
		this.sourceField = sourceField;
		this.targetModel = targetModel;
		this.targetField = targetField;
		reconnect();
	}
	
	public ModelElement getSourceModel() {
		return sourceModel;
	}
	
	public String getSourceField() {
		return (sourceField != null) ? sourceField : "";
	}
	
	public ModelElement getTargetModel() {
		return targetModel;
	}
	
	public String getTargetField() {
		return (targetField != null) ? targetField : "";
	}
	
}
