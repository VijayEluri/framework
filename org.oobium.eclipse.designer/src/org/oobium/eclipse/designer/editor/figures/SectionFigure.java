package org.oobium.eclipse.designer.editor.figures;

import org.eclipse.draw2d.AbstractBorder;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Rectangle;

public class SectionFigure extends Figure {

	public SectionFigure() {
		ToolbarLayout layout = new ToolbarLayout();
		layout.setMinorAlignment(ToolbarLayout.ALIGN_TOPLEFT);
		layout.setStretchMinorAxis(false);
		layout.setSpacing(2);
		setLayoutManager(layout);
		
		setBorder(new AbstractBorder() {
			public void paint(IFigure figure, Graphics graphics, Insets insets) {
				Rectangle r = getPaintRectangle(figure, insets);
				graphics.drawLine(r.getTopLeft(), r.getTopRight());
			}
			public Insets getInsets(IFigure figure) {
				return new Insets(1, 0, 0, 0);
			}
		});
	}
	
}
