package org.oobium.eclipse.designer.editor.parts;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.ConnectionLocator;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PolygonDecoration;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editparts.AbstractConnectionEditPart;
import org.eclipse.gef.editpolicies.ConnectionEditPolicy;
import org.eclipse.gef.editpolicies.ConnectionEndpointEditPolicy;
import org.eclipse.gef.handles.ConnectionEndpointHandle;
import org.eclipse.gef.requests.GroupRequest;
import org.eclipse.swt.SWT;
import org.oobium.eclipse.designer.editor.models.Connection;
import org.oobium.eclipse.designer.editor.models.commands.ConnectionDeleteCommand;
import org.oobium.eclipse.designer.editor.tools.ConnectionTargetHandle;

public class ConnectionPart extends AbstractConnectionEditPart {

	@Override
	protected void createEditPolicies() {
//		installEditPolicy(EditPolicy.CONNECTION_ENDPOINTS_ROLE, new ConnectionEndpointEditPolicy());
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
		
		connection.setLineWidth(2);
		connection.setAntialias(SWT.ON);

		PolygonDecoration decoration = new PolygonDecoration();
		decoration.setTemplate(PolygonDecoration.TRIANGLE_TIP);
		decoration.setAntialias(SWT.ON);
		
		connection.setTargetDecoration(decoration);
		
//		Label label = new Label("hello");
//		label.setBackgroundColor(ColorConstants.green);
//		label.setOpaque(true);
//		connection.add(label, new MidpointLocator(connection, 0));
		
		return connection;
	}

	@Override
	public Connection getModel() {
		return (Connection) super.getModel();
	}
	
}
