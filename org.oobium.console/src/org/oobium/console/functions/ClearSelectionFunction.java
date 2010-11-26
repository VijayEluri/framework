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

import org.eclipse.swt.SWT;
import org.oobium.console.Function;

public class ClearSelectionFunction extends Function {

	public ClearSelectionFunction() {
		super(SWT.CONTROL | SWT.SHIFT, 'a');
	}

	public void execute() {
		console.clearSelection();
	}
	
}
