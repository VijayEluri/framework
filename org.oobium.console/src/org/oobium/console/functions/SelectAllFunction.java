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
package org.oobium.console.functions;

import org.oobium.console.Function;

public class SelectAllFunction extends Function {

	public SelectAllFunction() {
		super(COMMAND, 'a');
	}

	public void execute() {
		console.selectAll();
	}
	
}
