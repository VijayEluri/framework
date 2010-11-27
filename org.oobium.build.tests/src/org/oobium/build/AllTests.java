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
package org.oobium.build;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.oobium.build.esp.EspCompilerPositionTests;
import org.oobium.build.esp.EspCompilerTests;
import org.oobium.build.esp.EspDomTests;
import org.oobium.build.workspace.BundleTests;

@RunWith(Suite.class)
@SuiteClasses({
	EspDomTests.class,
	EspCompilerTests.class,
	EspCompilerPositionTests.class,
	BundleTests.class
})
public class AllTests {
	// empty class for now
}
