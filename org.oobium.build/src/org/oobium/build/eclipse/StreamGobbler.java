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
package org.oobium.build.eclipse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class StreamGobbler extends Thread {

	private final InputStream inputStream;
	
	StreamGobbler(InputStream in) {
		this.setDaemon(true);
		this.inputStream = in;
	}
	
	public StreamGobbler activate() {
		start();
		return this;
	}
	
	@Override
	public void run() {
		BufferedReader in = null;
		try {
			in = new BufferedReader(new InputStreamReader(inputStream));
			String line;
			while((line = in.readLine()) != null) {
				System.out.println(line);
			}
		} catch(IOException e) {
			// stream may have closed... exit
		} finally {
			if(in != null) {
				try {
					in.close();
				} catch(IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
