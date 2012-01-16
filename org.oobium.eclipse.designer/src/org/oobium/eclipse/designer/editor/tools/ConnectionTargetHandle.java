package org.oobium.eclipse.designer.editor.tools;

import org.eclipse.draw2d.ConnectionLocator;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.DragTracker;
import org.eclipse.gef.handles.ConnectionEndpointHandle;

public class ConnectionTargetHandle extends ConnectionEndpointHandle {

	public ConnectionTargetHandle(ConnectionEditPart owner) {
		super(owner, ConnectionLocator.TARGET);
	}

	protected DragTracker createDragTracker() {
		if (isFixed()) {
			return null;
		}
		return new ConnectionTargetTracker((ConnectionEditPart) getOwner(), getCursor());
	}
	
}
