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
package org.oobium.eclipse.esp;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.oobium.eclipse.esp.editor.EspColorProvider;

/**
 * The example java editor plug-in class.
 * 
 * @since 3.0
 */
public class EspPlugin extends AbstractUIPlugin {

	public static final String ID = EspPlugin.class.getCanonicalName();
	public static final String ESP_PARTITIONING = "__esp_partitioning"; //$NON-NLS-1$

	public static final String IMG_HTML_TAG  = "/icons/html_tag.gif";
	public static final String IMG_META_TAG  = "/icons/meta_tag.gif";
	public static final String IMG_IMPORTS   = "/icons/imports.gif";
	public static final String IMG_IMPORT    = "/icons/import.gif";
	public static final String IMG_CTOR      = "/icons/constructor.gif";
	public static final String IMG_JAVA_LINE = "/icons/java_line.gif";
	public static final String IMG_SCRIPT    = "/icons/javascript.gif";
	public static final String IMG_STYLE     = "/icons/stylesheet.gif";
	public static final String IMG_TITLE     = "/icons/title.png";
	
	
	private static EspPlugin instance;

	public static EspPlugin getDefault() {
		return instance;
	}

	private EspColorProvider colorProvider;
	
	/**
	 * Creates a new plug-in instance.
	 */
	public EspPlugin() {
		instance = this;
	}

	public EspColorProvider getEspColorProvider() {
		if(colorProvider == null) {
			colorProvider = new EspColorProvider();
		}
		return colorProvider;
	}

	public static Image getImage(String key) {
		Image image = instance.getImageRegistry().get(key);
		if(image == null) {
			image = ImageDescriptor.createFromFile(EspPlugin.class, key).createImage(true);
			instance.getImageRegistry().put(key, image);
		}
		return image;
	}
	
	public static ImageDescriptor getImageDescriptor(String key) {
		ImageDescriptor descriptor = instance.getImageRegistry().getDescriptor(key);
		if(descriptor == null) {
			descriptor = ImageDescriptor.createFromFile(EspPlugin.class, key);
			instance.getImageRegistry().put(key, descriptor);
		}
		return descriptor;
	}
	
}
