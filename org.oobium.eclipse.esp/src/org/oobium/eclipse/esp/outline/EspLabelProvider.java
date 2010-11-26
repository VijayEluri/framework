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

public class EspLabelProvider extends LabelProvider implements IColorProvider, IFontProvider, ILabelProvider {

	private static final int TAG		= 0;
	private static final int META_TAG	= 1;
	private static final int IMPORTS	= 2;
	private static final int IMPORT		= 3;
	private static final int JAVA		= 4;
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
				"java.gif",
				"java_line.gif",
				"javascript.gif",
				"stylesheet.gif",
				"title.png"
				};
		images = new Image[names.length];
		for(int i = 0; i < names.length; i++) {
			ImageDescriptor desc = ImageDescriptor.createFromFile(getClass(), "/icons/" + names[i]);
//			ImageDescriptor desc = EspPlugin.imageDescriptorFromPlugin(EspPlugin.ID, "icons/" + names[i]);
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
//		if(element instanceof EspLine) {
//			element = ((EspLine) element).get(0);
//		}
		if(element instanceof EspElement) {
			EspElement espElement = (EspElement) element;
			switch(espElement.getType()) {
			case ImportElement: return images[IMPORT];
			case JavaElement: return images[JAVA_LINE];
			case ScriptElement: return images[SCRIPT];
			case StyleElement: return images[STYLE];
			case HtmlElement: 
//				if("title".equals(espElement.getTag())) {
//					return images[TITLE];
//				}
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
		if(element instanceof EspElement) {
			EspElement espElement = (EspElement) element;
			return espElement.getText();
		}
//		if(element instanceof ScriptBlock) {
//			return "script section";
//		}
//		if(element instanceof StyleBlock) {
//			return "style section";
//		}
//		if(element instanceof MetaBlock) {
//			return "meta tags";
//		}
//		if(element instanceof TitleLineBlock) {
//			return "page title";
//		}
		return element.toString();
	}

}
