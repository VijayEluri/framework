package org.oobium.eclipse.designer.editor.figures;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.RoundedRectangle;
import org.eclipse.draw2d.ScaledGraphics;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Pattern;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.TextLayout;
import org.eclipse.swt.widgets.Display;

public class ModelFigure extends RoundedRectangle {

	private static final Map<RGB, Color> colors = new HashMap<RGB, Color>();
	
	private String text;
	private Color color; // from the colors map
	
	public ModelFigure() {
		setColor(null);
	}
	
	@Override
	public void paint(Graphics g) {
		g.setLineWidth(2);
		
		Rectangle r = getBounds().getCopy();

		float y1 = r.y - 20;
		float y2 = r.y + r.height + 10;
		if(g instanceof ScaledGraphics) {
			double zoom = ((ScaledGraphics) g).getAbsoluteScale();
			y1 *= zoom;
			y2 *= zoom;
		}
		
		Pattern pattern = new Pattern(Display.getCurrent(), 0, y1, 0, y2, ColorConstants.white, color);
		g.setBackgroundPattern(pattern);
		g.fillRoundRectangle(r, 20, 20);

		if(text != null) {
			TextLayout tl = new TextLayout(Display.getCurrent());
			tl.setText(text);
			g.drawTextLayout(tl, r.x+10, r.y+10);
		}
		
		r.x++;
		r.y++;
		r.width -= 2;
		r.height -= 2;

		g.setForegroundColor(color);
		g.drawRoundRectangle(r, 20, 20);
	}

	public void setColor(RGB rgb) {
		if(rgb == null) {
			color = ColorConstants.lightBlue;
		} else {
			color = colors.get(rgb);
			if(color == null) {
				colors.put(new RGB(rgb.red, rgb.green, rgb.blue), color = new Color(Display.getDefault(), rgb));
			}
		}
	}
	
	public void setText(String text) {
		this.text = text;
		repaint();
	}
}
