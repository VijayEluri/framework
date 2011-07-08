package org.oobium.eclipse.designer.editor.actions;

import org.eclipse.gef.DefaultEditDomain;
import org.eclipse.jface.action.Action;
import org.oobium.eclipse.designer.editor.tools.ModelCreateTool;

public class CreateModelsAction extends Action {

	public static final String ID = "Designer.Create.Models";
	
	private DefaultEditDomain editDomain;
	
	public CreateModelsAction(DefaultEditDomain editor) {
		super("Create Models");
		setId(ID);
		this.editDomain = editor;
	}
	
	@Override
	public void run() {
		editDomain.setActiveTool(new ModelCreateTool());
	}
	
}
