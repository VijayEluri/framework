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
package org.oobium.build.console.commands.open;

import static org.oobium.utils.coercion.TypeCoercer.coerce;

import org.oobium.build.console.BuilderCommand;
import org.oobium.build.console.Eclipse;

public class ResourceCommand extends BuilderCommand {

	@Override
	public void configure() {
		maxParams = 2;
		minParams = 1;
	}
	
	@Override
	public void run() {
		int line = coerce(param(1)).to(int.class);
		Eclipse.openResource(param(0), line);
	}

}
