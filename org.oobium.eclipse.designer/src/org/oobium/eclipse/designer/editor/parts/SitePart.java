package org.oobium.eclipse.designer.editor.parts;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FreeformLayer;
import org.eclipse.draw2d.FreeformLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.SnapToGrid;
import org.eclipse.gef.SnapToHelper;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.gef.editpolicies.RootComponentEditPolicy;
import org.oobium.eclipse.designer.editor.models.ApplicationElement;
import org.oobium.eclipse.designer.editor.models.SiteElement;

public class SitePart extends AbstractGraphicalEditPart implements PropertyChangeListener {

	private boolean snapToGrid = true;
	
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
	@SuppressWarnings("rawtypes")
	public Object getAdapter(Class key) {
		if(snapToGrid && key == SnapToHelper.class) {
			return new SnapToGrid(this);
		}
		return super.getAdapter(key);
	}
	
	@Override
	public SiteElement getModel() {
		return (SiteElement) super.getModel();
	}
	
	@Override
	protected List<ApplicationElement> getModelChildren() {
		return getModel().getApplications();
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected IFigure createFigure() {
		Figure figure = new FreeformLayer();
		figure.setBorder(new MarginBorder(3));
		figure.setLayoutManager(new FreeformLayout());
		
//		ConnectionLayer layer = (ConnectionLayer) getLayer(LayerConstants.CONNECTION_LAYER);
//		layer.setConnectionRouter(new ShortestPathConnectionRouter(figure));
		
		return figure;
	}

	@Override
	protected void createEditPolicies() {
		installEditPolicy(EditPolicy.COMPONENT_ROLE, new RootComponentEditPolicy());
		installEditPolicy(EditPolicy.LAYOUT_ROLE, new SiteXYLayoutPolicy());
	}

	@Override
	protected void refreshVisuals() {
		
	}

	public void setSnapToGrid(boolean snapToGrid) {
		this.snapToGrid = snapToGrid;
	}
	
}
