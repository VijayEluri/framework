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
package org.oobium.server;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Ignore;
import org.junit.Test;

public class LoadTests {
	
	private volatile AtomicInteger running;

	@Ignore
	@Test
	public void testSerial() throws Exception {
		long start = System.currentTimeMillis();
		int requests = 4000;
		for(int i = 0; i < requests; i++) {
			System.out.println("iteration " + i);
			URL url = new URL("http", "localhost", 5000, "/");
	        URLConnection conn = url.openConnection();
	        assertEquals("iteration " + i, "HTTP/1.0 200 - OK", conn.getHeaderField(0));
		}
		long duration = System.currentTimeMillis() - start;
		System.out.println("elapsed time: " + (duration) + " millis");
		System.out.println("average time: " + ((double) duration / requests) + " millis");
		System.out.println("req / second: " + (int) ((double) requests / ((double) duration / 1000)));
	}
	
	@Ignore
	@Test
	public void testWithCurl() throws Exception {
		long start = System.currentTimeMillis();
		int requests = 100;
		Thread[] threads = new Thread[requests];
		for(int i = 0; i < requests; i++) {
			threads[i] = new Thread() {
				public void run() {
					try {
						Runtime.getRuntime().exec(new String[] { "curl", "http://guides.oobium.org/getting_started"});
					} catch(IOException e) {
						e.printStackTrace();
					}
					System.out.println("ran");
				};
			};
		}
		for(int i = 0; i < requests; i++) {
			threads[i].start();
		}
		long duration = System.currentTimeMillis() - start;
		System.out.println("elapsed time: " + (duration) + " millis");
		System.out.println("average time: " + ((double) duration / requests) + " millis");
		System.out.println("req / second: " + (int) ((double) requests / ((double) duration / 1000)));
		
		Thread.sleep(5000);
	}
	
	@Ignore
	@Test
	public void testParallel() throws Exception {
		final int numThreads = 2;
		final int iterationsPerThread = 2;
		Runnable runnable = new Runnable() {
			public void run() {
				int i = 0;
				try {
					for( ; i < iterationsPerThread; i++) {
						URL url = new URL("http", "localhost", 5000, "/");
				        URLConnection conn = url.openConnection();
				        if(!"HTTP/1.0 200 - OK".equals(conn.getHeaderField(0))) {
							System.err.println("iteration " + i + " failed: " + conn.getHeaderField(0));
							break;
				        }
					}
				} catch(Exception e) {
					System.err.println("iteration " + i + " failed");
					e.printStackTrace();
				}
				running.decrementAndGet();
			}
		};
		
		Thread[] threads = new Thread[numThreads];
		for(int i = 0; i < threads.length; i++) {
			threads[i] = new Thread(runnable, "test" + i);
		}
		
		running = new AtomicInteger(threads.length);
		long start = System.currentTimeMillis();

		for(int i = 0; i < threads.length; i++) {
			threads[i].start();
		}
		while(running.get() > 0) {
			Thread.yield();
		}
		
		long duration = System.currentTimeMillis() - start;
		int requests = (numThreads * iterationsPerThread);
		System.out.println("ran " + requests + " requests in " + numThreads + " threads");
		System.out.println("elapsed time: " + (duration) + " millis");
		System.out.println("average time: " + ((double) duration / requests) + " millis");
		System.out.println("req / second: " + (int) ((double) requests / ((double) duration / 1000)));
	}
	
}
