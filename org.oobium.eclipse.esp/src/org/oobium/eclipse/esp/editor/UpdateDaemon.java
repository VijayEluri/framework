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
package org.oobium.eclipse.esp.editor;



public class UpdateDaemon extends Thread {

	private final int interval;
	private final Runnable runnable;
	private volatile boolean run;
	
	public UpdateDaemon(int interval, Runnable runnable) {
		super("UpdateDaemon:" + interval);
		this.interval = interval;
		this.runnable = runnable;
	}
	
	public void run() {
		run = true;
		while(run) {
			try {
				sleep(interval);
				if(run) {
					runnable.run();
					synchronized(this) {
						wait();
					}
				}
			} catch(InterruptedException e) {
				// start over
			}
		}
	};

	public synchronized void cancel() {
		run = false;
		interrupt();
	}
	
}
