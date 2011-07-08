package org.oobium.eclipse.designer.editor.tools;

import org.eclipse.gef.DragTracker;
import org.eclipse.gef.tools.SelectionTool;

public class DesignerSelectionTool extends SelectionTool {

	@Override
	public Input getCurrentInput() {
		return super.getCurrentInput();
	}

	@Override
	protected boolean handleFocusLost() {
//		DragTracker tracker = getDragTracker();
//		if(tracker instanceof ConnectionTargetTracker) {
//			ConnectionTargetTracker targetTracker = (ConnectionTargetTracker) tracker;
//			return false;
//		} else {
			return super.handleFocusLost();
//		}
	}
	
	@Override
	public void setDragTracker(DragTracker newDragTracker) {
		System.out.println("set drag tracker: " + newDragTracker);
		super.setDragTracker(newDragTracker);
	}
	
}
