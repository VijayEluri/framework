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
package org.oobium.build.workspace;

import org.oobium.build.workspace.Workspace.EventType;

public class WorkspaceEvent {

	public final EventType type;
	public final Object oldValue;
	public final Object newValue;
	
	public WorkspaceEvent(EventType type) {
		this(type, null, null);
	}
	
	public WorkspaceEvent(EventType type, Object oldValue, Object newValue) {
		this.type = type;
		this.oldValue = oldValue;
		this.newValue = newValue;
	}
	
}
