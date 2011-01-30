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

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.oobium.build.esp.EspElement;
import org.oobium.build.esp.EspPart;
import org.oobium.build.esp.elements.JavaElement;

public class EspLabelProvider extends LabelProvider implements IColorProvider, IFontProvider, ILabelProvider {

	private static final int TAG		= 0;
	private static final int META_TAG	= 1;
	private static final int IMPORTS	= 2;
	private static final int IMPORT		= 3;
	private static final int CTOR		= 4;
	private static final int JAVA_LINE	= 5;
	private static final int SCRIPT		= 6;
	private static final int STYLE		= 7;
	private static final int TITLE		= 8;
	
	private Image[] images;
	
	public EspLabelProvider() {
		String[] names = {
				"html_tag.gif", 
				"meta_tag.gif", 
				"imports.gif", 
				"import.gif", 
				"constructor.gif", 
				"java_line.gif",
				"javascript.gif",
				"stylesheet.gif",
				"title.png"
				};
		images = new Image[names.length];
		for(int i = 0; i < names.length; i++) {
			ImageDescriptor desc = ImageDescriptor.createFromFile(getClass(), "/icons/" + names[i]);
			if(desc != null) {
				images[i] = desc.createImage();
			}
		}
	}
	
	@Override
	public void dispose() {
		if(images != null) {
			for(Image image : images) {
				if(image != null && !image.isDisposed()) {
					image.dispose();
				}
			}
		}
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
			case ImportElement: return images[IMPORT];
			case ConstructorElement: return images[CTOR];
			case JavaElement: return images[JAVA_LINE];
			case ScriptElement: return images[SCRIPT];
			case StyleElement: return images[STYLE];
			case HtmlElement:
				if(espElement.getElementText().startsWith("title ")) {
					return images[TITLE];
				}
				return images[TAG];
			}
		}
		if(element instanceof Imports) {
			return images[IMPORTS];
		}
		return null;
	}

	@Override
	public String getText(Object element) {
		if(element instanceof Imports) {
			return "imports declarations";
		}
		if(element instanceof JavaElement) {
			return ((JavaElement) element).getSource();
		}
		if(element instanceof EspElement) {
			String text = ((EspElement) element).getElementText();
			if(text.startsWith("title ")) {
				return text.substring(6);
			}
			return text;
		}
		if(element instanceof EspPart) {
			return ((EspPart) element).getText();
		}
		return element.toString();
	}

}
