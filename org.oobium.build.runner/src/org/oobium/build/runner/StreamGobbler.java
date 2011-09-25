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

	private final Runner runner;
	private final InputStream inputStream;
	private PrintStream outputStream;
	private boolean error;
	
	
	StreamGobbler(Runner runner, InputStream in) {
		this.runner = runner;
		this.inputStream = in;
		this.setDaemon(true);
	}
	
	public StreamGobbler activate() {
		start();
		return this;
	}

	public void print(Object o) {
		if(outputStream != null) {
			outputStream.print(o);
		}
	}
	
	public void println() {
		if(outputStream != null) {
			outputStream.println();
		}
	}
	
	public void println(Object o) {
		if(outputStream != null) {
			outputStream.println(o);
		}
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
				if(error) {
					if(line.contains("(ERROR)")) {
						RunnerService.notifyListeners(Type.Error, runner.getApplication(), line);
					}
					else if(line.contains("(WARN)")) {
						RunnerService.notifyListeners(Type.Warning, runner.getApplication(), line);
					}
				} else {
					if(!appStarted || !serverStarted) {
						if(!serverStarted && line.endsWith("(INFO)  org.oobium.app: Server started")) {
							serverStarted = true;
						}
						if(!appStarted && line.endsWith(runner.startString)) {
							appStarted = true;
						}
						if(serverStarted && appStarted) {
							runner.handleStarted();
						}
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
	
	public void setError(PrintStream err) {
		this.error = true;
		this.outputStream = err;
	}

	public void setOut(PrintStream out) {
		this.error = false;
		this.outputStream = out;
	}

}
