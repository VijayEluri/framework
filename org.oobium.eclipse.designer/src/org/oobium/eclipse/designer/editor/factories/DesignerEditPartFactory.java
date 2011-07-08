package org.oobium.eclipse.designer.editor.factories;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;
import org.oobium.eclipse.designer.editor.models.ApplicationElement;
import org.oobium.eclipse.designer.editor.models.Connection;
import org.oobium.eclipse.designer.editor.models.ModelElement;
import org.oobium.eclipse.designer.editor.models.SiteDiagram;
import org.oobium.eclipse.designer.editor.parts.ApplicationPart;
import org.oobium.eclipse.designer.editor.parts.ConnectionPart;
import org.oobium.eclipse.designer.editor.parts.ModelPart;
import org.oobium.eclipse.designer.editor.parts.SiteDiagramPart;

public class DesignerEditPartFactory implements EditPartFactory {

	@Override
	public EditPart createEditPart(EditPart context, Object element) {
		EditPart part = createEditPart(element);
		part.setModel(element);
		return part;
	}

	private EditPart createEditPart(Object element) {
		if(element instanceof SiteDiagram) {
			return new SiteDiagramPart();
		}
		if(element instanceof ApplicationElement) {
			return new ApplicationPart();
		}
		if(element instanceof ModelElement) {
			return new ModelPart();
		}
		if(element instanceof Connection) {
			return new ConnectionPart();
		}
		throw new IllegalArgumentException("Can't create edit part for element: " + element);
	}
	
}
