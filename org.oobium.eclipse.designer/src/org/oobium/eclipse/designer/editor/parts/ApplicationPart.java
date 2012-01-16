package org.oobium.eclipse.designer.editor.parts;

import static org.oobium.eclipse.designer.editor.models.ModuleElement.PROP_MODELS;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.ConnectionLayer;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FreeformLayer;
import org.eclipse.draw2d.FreeformLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.ShortestPathConnectionRouter;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.LayerConstants;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.gef.editpolicies.RootComponentEditPolicy;
import org.oobium.eclipse.designer.editor.models.ApplicationElement;

public class ApplicationPart extends AbstractGraphicalEditPart implements PropertyChangeListener {

	@Override
	public void activate() {
		if(!isActive()) {
			super.activate();
			getModel().addListener(this);
		}
	}

	@Override
	public void deactivate() {
		if (isActive()) {
			super.deactivate();
			getModel().removeListener(this);
		}
	}
	
	@Override
	public ApplicationElement getModel() {
		return (ApplicationElement) super.getModel();
	}
	
	@Override
	protected List<Object> getModelChildren() {
		ApplicationElement model = getModel();
		List<Object> list = new ArrayList<Object>();
		list.addAll(model.getModels());
		list.addAll(model.getModules());
		return list;
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		String prop = evt.getPropertyName();
		if(PROP_MODELS.equals(prop)) {
			refreshChildren();
		}
	}

	@Override
	protected IFigure createFigure() {
		Figure figure = new FreeformLayer();
		figure.setBorder(new MarginBorder(3));
		figure.setLayoutManager(new FreeformLayout());
		
		ConnectionLayer layer = (ConnectionLayer) getLayer(LayerConstants.CONNECTION_LAYER);
//		layer.setConnectionRouter(new BendpointConnectionRouter());
		layer.setConnectionRouter(new ShortestPathConnectionRouter(figure));
//		layer.setConnectionRouter(new ManhattanConnectionRouter());
		
		return figure;
	}

	@Override
	protected void createEditPolicies() {
		installEditPolicy(EditPolicy.COMPONENT_ROLE, new RootComponentEditPolicy());
		installEditPolicy(EditPolicy.LAYOUT_ROLE, new SiteXYLayoutPolicy());
	}

}
