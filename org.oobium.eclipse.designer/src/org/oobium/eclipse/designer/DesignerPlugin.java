/*******************************************************************************
 * Copyright (c) 2011 Oobium, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Jeremy Dowdall <jeremy@oobium.com> - initial API and implementation
 ******************************************************************************/
package org.oobium.eclipse.designer;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * The example java editor plug-in class.
 * 
 * @since 3.0
 */
public class DesignerPlugin extends AbstractUIPlugin {

	public static final String IMG_CURSOR = "/icons/cursor.png";
	public static final String IMG_ADD = "/icons/add.png";
	public static final String IMG_ADD_MODEL = "/icons/add_model.png";
	public static final String IMG_ADD_CONNECTION = "/icons/add_connection.png";
	public static final String IMG_ARROW_DOWN = "/icons/arrow_down.gif";
	public static final String IMG_ARROW_UP = "/icons/arrow_up.gif";
	public static final String IMG_DELETE = "/icons/delete.png";
	public static final String IMG_DIALOG_NEW_MODEL = "/icons/wizards/NewModel.gif";
	public static final String IMG_DIALOG_EDIT_MODEL = "/icons/wizards/NewModel.gif";
	public static final String IMG_GRID = "/icons/grid-dot.png";
	public static final String IMG_SNAP_TO_GRID = "/icons/grid-snap-dot.png";
	public static final String IMG_ZOOM_ACTUAL = "/icons/magnifier-zoom-actual-equal.png";
	public static final String IMG_ZOOM_FIT = "/icons/magnifier-zoom-fit.png";
	public static final String IMG_ZOOM_IN = "/icons/magnifier-zoom-in.png";
	public static final String IMG_ZOOM_OUT = "/icons/magnifier-zoom-out.png";

	
	public static final String ID = DesignerPlugin.class.getCanonicalName();

	private static DesignerPlugin instance;

	public static DesignerPlugin getDefault() {
		return instance;
	}


	/**
	 * Creates a new plug-in instance.
	 */
	public DesignerPlugin() {
		instance = this;
	}

	public static Image getImage(String key) {
		Image image = instance.getImageRegistry().get(key);
		if(image == null) {
			image = ImageDescriptor.createFromFile(DesignerPlugin.class, key).createImage(true);
			instance.getImageRegistry().put(key, image);
		}
		return image;
	}
	
	public static ImageDescriptor getImageDescriptor(String key) {
		ImageDescriptor descriptor = instance.getImageRegistry().getDescriptor(key);
		if(descriptor == null) {
			descriptor = ImageDescriptor.createFromFile(DesignerPlugin.class, key);
			instance.getImageRegistry().put(key, descriptor);
		}
		return descriptor;
	}
	
}
