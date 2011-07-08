package org.oobium.eclipse.designer.editor.parts;

import java.util.List;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.XYLayoutEditPolicy;
import org.eclipse.gef.requests.ChangeBoundsRequest;
import org.eclipse.gef.requests.CreateRequest;
import org.oobium.eclipse.designer.editor.models.ApplicationElement;
import org.oobium.eclipse.designer.editor.models.ModelElement;
import org.oobium.eclipse.designer.editor.models.ModuleElement;
import org.oobium.eclipse.designer.editor.models.SiteDiagram;
import org.oobium.eclipse.designer.editor.models.commands.ModelCreateCommand;
import org.oobium.eclipse.designer.editor.models.commands.ModelSetConstraintCommand;

public class SiteXYLayoutPolicy extends XYLayoutEditPolicy {

	@Override
	protected Command createChangeConstraintCommand(ChangeBoundsRequest request, EditPart child, Object constraint) {
		if(child instanceof ApplicationPart) {
			// TODO
			return null;
		}
		if(child instanceof ModelPart && constraint instanceof Rectangle) {
			return new ModelSetConstraintCommand((ModelElement) child.getModel(), (Rectangle) constraint);
		}
		return super.createChangeConstraintCommand(request, child, constraint);
	}
	
	@Override
	protected Command createChangeConstraintCommand(EditPart child, Object constraint) {
		return null;
	}
	
	@Override
	protected Command createAddCommand(EditPart child, Object constraint) {
		if(child instanceof ModelPart && constraint instanceof Rectangle) {
			return new ModelSetConstraintCommand((ModelElement) child.getModel(), (Rectangle) constraint);
		}
		return super.createAddCommand(child, constraint);
	}
	
	@Override
	protected Command getCreateCommand(CreateRequest request) {
		Object childClass = request.getNewObjectType();
		if(childClass == ModelElement.class) {
			Object parent = getHost().getModel();
			if(parent instanceof SiteDiagram) {
				List<ApplicationElement> apps = ((SiteDiagram) parent).getApplications();
				if(!apps.isEmpty()) {
					parent = apps.get(0);
				}
			}
			if(parent instanceof ModuleElement) {
				ModuleElement module = (ModuleElement) parent;
				Rectangle bounds = (Rectangle) getConstraintFor(request);
				return new ModelCreateCommand(module, bounds);
			}
		}
		return null;
	}

}
