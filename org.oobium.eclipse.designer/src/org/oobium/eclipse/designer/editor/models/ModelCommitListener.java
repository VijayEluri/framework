package org.oobium.eclipse.designer.editor.models;

import java.util.Map;

public interface ModelCommitListener {

	public abstract void preCommit(ModelElement model);
	
	public abstract void postCommit(ModelElement model, Map<String, Object> mData);

}
