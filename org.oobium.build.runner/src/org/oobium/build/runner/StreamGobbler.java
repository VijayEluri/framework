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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;

import org.oobium.build.runner.RunEvent.Type;

public class StreamGobbler extends Thread {

	private Runner runner;
	private final InputStream inputStream;
	private PrintStream outputStream;
	
	StreamGobbler(InputStream in) {
		this.setDaemon(true);
		this.inputStream = in;
	}
	
	StreamGobbler(Runner runner, InputStream in) {
		this(in);
		this.runner = runner;
	}
	
	public StreamGobbler activate() {
		start();
		return this;
	}
	
	@Override
	public void run() {
		boolean serverStarted = false;
		boolean appStarted = false;
		
		BufferedReader in = null;
		try {
			in = new BufferedReader(new InputStreamReader(inputStream));
			String line;
			while((line = in.readLine()) != null) {
				if(runner != null) {
					if(!serverStarted && line.endsWith("(INFO)  org.oobium.server: Server started")) {
						serverStarted = true;
					}
					if(!appStarted && line.endsWith(runner.startString)) {
						appStarted = true;
					}
					if(serverStarted && appStarted) {
						Runner tmp = runner;
						runner = null;
						tmp.handleStarted();
					}
					if(runner != null && line.contains("(ERROR) org.oobium.server: could not listen on port ")) {
						RunnerService.notifyListeners(Type.Error, runner.getApplication(), line);
					}
				}
				if(outputStream != null) {
					outputStream.println(line);
				}
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
	
	public void setOut(PrintStream out) {
		this.outputStream = out;
	}
}
