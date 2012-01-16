package org.oobium.eclipse.designer.editor.tools;

import static org.oobium.utils.StringUtils.varName;

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.tools.ConnectionCreationTool;
import org.eclipse.swt.SWT;
import org.oobium.build.model.ModelDefinition;
import org.oobium.eclipse.designer.editor.dialogs.ConnectionCreateDialog;
import org.oobium.eclipse.designer.editor.models.ApplicationElement;
import org.oobium.eclipse.designer.editor.models.ModelElement;
import org.oobium.eclipse.designer.editor.models.SiteElement;
import org.oobium.eclipse.designer.editor.models.commands.ConnectionCreateCommand;
import org.oobium.eclipse.designer.editor.parts.SitePart;

public class ConnectionCreateTool extends ConnectionCreationTool {

	@Override
	protected void executeCommand(Command command) {
		if(command instanceof ConnectionCreateCommand) {
			ConnectionCreateCommand cmd = (ConnectionCreateCommand) command;
			String sourceModel = cmd.getSourceModel().getName();
			String targetModel = cmd.getTargetModel().getName();
			String sourceField = varName(targetModel);
			Map<String, ModelDefinition> models = new LinkedHashMap<String, ModelDefinition>();
			for(Object o : getCurrentViewer().getRootEditPart().getChildren()) {
				if(o instanceof SitePart) {
					SiteElement site = ((SitePart) o).getModel();
					for(ApplicationElement app : site.getApplications()) {
						for(ModelElement model : app.getModels()) {
							models.put(model.getName(), model.getDefinition());
						}
					}
				}
			}
			ConnectionCreateDialog dlg = new ConnectionCreateDialog(sourceModel, sourceField, targetModel, models);
			if(dlg.open() == SWT.OK) {
				cmd.setSourceField(dlg.getSourceField());
				cmd.setSourceHasMany(dlg.isSourceHasMany());
				cmd.setTargetField(dlg.getTargetField());
				cmd.setTargetHasMany(dlg.isTargetHasMany());
				cmd.setThrough(dlg.getThrough());
			} else {
				return;
			}
		}
		super.executeCommand(command);
	}
	
}
