package org.oobium.eclipse.designer.editor.parts;

import static org.oobium.utils.StringUtils.blank;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.ConnectionLocator;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PolygonDecoration;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editparts.AbstractConnectionEditPart;
import org.eclipse.gef.editpolicies.ConnectionEditPolicy;
import org.eclipse.gef.editpolicies.ConnectionEndpointEditPolicy;
import org.eclipse.gef.handles.ConnectionEndpointHandle;
import org.eclipse.gef.requests.GroupRequest;
import org.eclipse.swt.SWT;
import org.oobium.build.model.ModelDefinition;
import org.oobium.eclipse.designer.editor.models.Connection;
import org.oobium.eclipse.designer.editor.models.commands.ConnectionDeleteCommand;
import org.oobium.eclipse.designer.editor.tools.ConnectionTargetHandle;

public class ConnectionPart extends AbstractConnectionEditPart {

	private static final PointList DOUBLE_TRIANGLE_TIP = new PointList(new int[] { 0,0, -1,1, -1,0, -2,1, -2,-1, -1,0, -1,-1 });
	

	@Override
	protected void createEditPolicies() {
		installEditPolicy(EditPolicy.CONNECTION_ENDPOINTS_ROLE, new ConnectionEndpointEditPolicy() {
			protected List<Object> createSelectionHandles() {
				List<Object> list = new ArrayList<Object>();
				list.add(new ConnectionEndpointHandle((ConnectionEditPart) getHost(), ConnectionLocator.SOURCE));
				list.add(new ConnectionTargetHandle((ConnectionEditPart) getHost()));
				return list;
			}
		});
		installEditPolicy(EditPolicy.CONNECTION_ROLE, new ConnectionEditPolicy() {
			@Override
			protected Command getDeleteCommand(GroupRequest request) {
				Connection connection = (Connection) getHost().getModel();
				return new ConnectionDeleteCommand(connection);
			}
		});
	}

	@Override
	protected IFigure createFigure() {
		PolylineConnection connection = new PolylineConnection();
		
		connection.setLineStyle(getModel().isThrough() ? SWT.LINE_DASH : SWT.LINE_SOLID);
		connection.setLineWidth(2);
		connection.setAntialias(SWT.ON);

		if(!blank(getModel().getTargetField())) {
			PolygonDecoration source = new PolygonDecoration();
			ModelDefinition def = getModel().getTargetModel().getDefinition();
			String field = getModel().getTargetField();
			if(def.hasField(field)) {
				if(def.hasMany(field)) {
					source.setTemplate(DOUBLE_TRIANGLE_TIP);
				} else {
					source.setTemplate(PolygonDecoration.TRIANGLE_TIP);
				}
			} else {
				source.setTemplate(new PointList(new int[] { 0,1, -1,1, -1,-1, 0,-1 }));
				source.setBackgroundColor(ColorConstants.red);
				source.setForegroundColor(ColorConstants.red);
			}
			source.setAntialias(SWT.ON);
			connection.setSourceDecoration(source);
		}
		
		PolygonDecoration target = new PolygonDecoration();
		if(getModel().getSourceModel().getDefinition().hasMany(getModel().getSourceField())) {
			target.setTemplate(DOUBLE_TRIANGLE_TIP);
		} else {
			target.setTemplate(PolygonDecoration.TRIANGLE_TIP);
		}
		target.setAntialias(SWT.ON);
		connection.setTargetDecoration(target);
		
//		ConnectionLayer layer = (ConnectionLayer) getLayer(LayerConstants.CONNECTION_LAYER);
//		connection.setConnectionRouter(layer.getConnectionRouter());
//		
//		List<Bendpoint> bendpoints = new ArrayList<Bendpoint>();
//		RelativeBendpoint bp = new RelativeBendpoint(connection);
//		bp.setRelativeDimensions(new Dimension(100, 10), new Dimension(10, 1));
//		bp.setWeight(0.1f);
//		bendpoints.add(bp);
//		connection.setRoutingConstraint(bendpoints);
		
		return connection;
	}

	@Override
	public Connection getModel() {
		return (Connection) super.getModel();
	}
	
}
