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
package org.oobium.build.runner;

import org.oobium.build.workspace.Application;
import org.oobium.build.workspace.Bundle;

public class RunEvent {

	public enum Type { Start, Started, Stop, Update, Updated, Error }
	
	public final Type type;
	public final Application application;
	public final Bundle[] bundles;
	private String message;
	
	public RunEvent(Type type, Application app, Bundle...bundles) {
		this.type = type;
		this.application = app;
		this.bundles = bundles;
	}

	public String getMessage() {
		return message;
	}
	
	void setMessage(String message) {
		this.message = message;
	}
	
}
