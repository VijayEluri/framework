/*******************************************************************************
 * Copyright (c) 2010 Oobium, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Jeremy Dowdall <jeremy@oobium.com> - initial API and implementation
 ******************************************************************************/
package org.oobium.eclipse.esp.editor;


import java.util.*;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

/**
 * Manager for colors used in the editor
 */
public class EspColorProvider {

	protected Map<RGB, Color> colors= new HashMap<RGB, Color>(10);

	/**
	 * Release all of the color resources held onto by the receiver.
	 */	
	public void dispose() {
		for(Color color : colors.values()) {
			color.dispose();
		}
	}
	
	/**
	 * Return the color that is stored in the color table under the given RGB
	 * value.
	 * 
	 * @param rgb the RGB value
	 * @return the color stored in the color table for the given RGB value
	 */
	public Color getColor(int r, int g, int b) {
		RGB rgb = new RGB(r, g, b);
		Color color = (Color) colors.get(rgb);
		if (color == null) {
			color = new Color(Display.getCurrent(), rgb);
			colors.put(rgb, color);
		}
		return color;
	}
	
}
