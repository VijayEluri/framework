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
package org.oobium.eclipse.workspace;

import java.io.File;

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ResourcesPlugin;
import org.oobium.eclipse.OobiumPlugin;

public class ResourceChangeListener implements IResourceChangeListener {

	@Override
	public void resourceChanged(IResourceChangeEvent event) {
		if(event.getType() == IResourceChangeEvent.POST_CHANGE) {
			for(IResourceDelta delta : event.getDelta().getAffectedChildren()) {
				switch(delta.getKind()) {
				case IResourceDelta.ADDED:
					OobiumPlugin.getWorkspace().loadBundle(delta.getResource().getLocation().toFile());
					break;
				case IResourceDelta.REMOVED:
					File file = ResourcesPlugin.getWorkspace().getRoot().getLocation().toFile();
					file = new File(file, delta.getResource().getFullPath().toString());
					OobiumPlugin.getWorkspace().remove(file);
					break;
				}
			}
		}
	}

}
