package org.oobium.eclipse.designer.editor.figures;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.RoundedRectangle;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Pattern;
import org.eclipse.swt.graphics.TextLayout;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.widgets.Display;

public class ModelFigure extends RoundedRectangle {

	private String text;
	
	@Override
	public void paint(Graphics g) {
		g.setLineWidth(2);
		
		Rectangle r = getBounds().getCopy();
		r.x++;
		r.y++;
		r.width -= 2;
		r.height -= 2;
		
		Pattern pattern = new Pattern(Display.getCurrent(), r.x, r.y-20, r.x, r.y+r.height, ColorConstants.white, ColorConstants.lightBlue);
		g.setBackgroundPattern(pattern);
		g.fillRoundRectangle(r, 20, 20);

		if(text != null) {
			TextLayout tl = new TextLayout(Display.getCurrent());
			tl.setText(text);
			org.eclipse.swt.graphics.Rectangle tr = tl.getBounds();
			g.drawTextLayout(tl, r.x+10, r.y+10);
		}
		
		g.setForegroundColor(ColorConstants.lightBlue);
		g.drawRoundRectangle(r, 20, 20);
	}

	public void setText(String text) {
		this.text = text;
		repaint();
	}
}
