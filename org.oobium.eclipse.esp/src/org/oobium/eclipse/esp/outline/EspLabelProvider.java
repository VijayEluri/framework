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
package org.oobium.eclipse.esp.outline;

import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.oobium.build.esp.dom.EspElement;
import org.oobium.build.esp.dom.EspPart;
import org.oobium.build.esp.dom.elements.JavaElement;
import org.oobium.build.esp.dom.elements.MarkupElement;
import org.oobium.eclipse.esp.EspPlugin;

public class EspLabelProvider extends LabelProvider implements IColorProvider, IFontProvider, ILabelProvider {

	@Override
	public void dispose() {
		// nothing to do
	}
	
	@Override
	public Color getBackground(Object element) {
		return null;
	}
	
	@Override
	public Font getFont(Object element) {
		return null;
	}
	
	@Override
	public Color getForeground(Object element) {
		return null;
	}

	@Override
	public Image getImage(Object element) {
		if(element instanceof EspElement) {
			EspElement espElement = (EspElement) element;
			switch(espElement.getType()) {
			case ImportElement: return EspPlugin.getImage(EspPlugin.IMG_IMPORT);
			case Constructor: return EspPlugin.getImage(EspPlugin.IMG_CTOR);
			case JavaElement: return EspPlugin.getImage(EspPlugin.IMG_JAVA_LINE);
			case ScriptElement: return EspPlugin.getImage(EspPlugin.IMG_SCRIPT);
			case StyleElement: return EspPlugin.getImage(EspPlugin.IMG_STYLE);
			case MarkupElement:
				MarkupElement markup = (MarkupElement) element;
				if("title".equals(markup.getTag().getText())) {
					return EspPlugin.getImage(EspPlugin.IMG_TITLE);
				}
				return EspPlugin.getImage(EspPlugin.IMG_HTML_TAG);
			}
		}
		if(element instanceof Imports) {
			return EspPlugin.getImage(EspPlugin.IMG_IMPORTS);
		}
		return null;
	}

	@Override
	public String getText(Object element) {
		if(element instanceof EspPart) {
			EspPart part = (EspPart) element;
			switch(part.getType()) {
			case JavaElement:   return ((JavaElement) part).getSource().getText();
			case ScriptElement: return "script";
			case StyleElement:  return "style";
			case MarkupElement:
				MarkupElement markup = (MarkupElement) element;
				if("title".equals(markup.getTag().getText())) {
					return "title: " + markup.getInnerText().getText();
				}
			default:
				String s = part.getText();
				int i1 = s.indexOf("<-");
				int i2 = s.indexOf('\n');
				int ix = (i1 == -1) ? i2 : Math.min(i1, i2);
				return ((ix == -1) ? s : s.substring(0, ix)).trim();
			}
		}
		return String.valueOf(element);
	}

}
