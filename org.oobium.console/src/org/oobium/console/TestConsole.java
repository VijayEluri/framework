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
package org.oobium.console;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class TestConsole {

	static class RootCommand extends Command {
		@Override
		public void configure() {
			add(new TestInCommand());
			add(new TestLongRunningCommand());
		}
	}
	
	static class TestInCommand extends Command {
		@Override
		public void run() {
			console.out.print("enter something: ");
			String input = console.in.readLine();
			console.out.println();
			console.out.print("  you entered: " + input);
		}
	}
	
	static class TestLongRunningCommand extends Command {
		@Override
		public void run() {
			for(int i = 0; i < 5; i++) {
				console.out.println("cycle\n " + (i+1));
				try {
					Thread.sleep(1000);
				} catch(InterruptedException e) {
					console.err.println(e);
				}
			}
		}
	}
	
	public static void main(String[] args) {
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setText("Oobium Developer Console");
		shell.setLayout(new GridLayout());

		Console console = new Console(shell, SWT.BORDER);
		console.setRootCommand(new RootCommand());
		console.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		console.out.println("test out stream1");
		console.err.println("test err stream1");
		console.out.println("test <a>out</a> stream2");
		console.err.println("test <a>err</a> stream2");
		
		shell.setSize(600, 400);
		Point size = new Point(600, 400);
		Rectangle bounds = shell.getMonitor().getBounds();
		shell.setBounds(bounds.x + (bounds.width - size.x) / 2, bounds.y + (bounds.height - size.y) / 2, size.x, size.y);
		shell.open();
		
		while(!shell.isDisposed()) {
			if(!display.readAndDispatch()) {
				display.sleep();
			}
		}
		display.dispose();
	}
	
}
