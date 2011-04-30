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
package org.oobium.cache.file;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.oobium.cache.CacheObject;
import org.oobium.cache.CacheService;
import org.oobium.logging.LogProvider;
import org.oobium.logging.Logger;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class FileCacheService implements BundleActivator, CacheService {

	private static final boolean adjustKey = File.separatorChar != '/';

	private final Logger logger;
	private final ReadWriteLock lock;
	
	public FileCacheService() {
		logger = LogProvider.getLogger(FileCacheService.class);
		lock = new ReentrantReadWriteLock();
	}
	
	@Override
	public void start(BundleContext context) throws Exception {
		logger.setTag(context.getBundle().getSymbolicName());
		
		Properties properties = new Properties();
		properties.put(CacheService.TYPE, CacheService.TYPE_FILE);
		context.registerService(CacheService.class.getName(), this, properties);

		logger.info("CacheService started");
	}
	
	@Override
	public void stop(BundleContext context) throws Exception {
		logger.info("CacheService stopped");
		logger.setTag(null);
	}

	private String OSKey(String key) {
		if(adjustKey) {
			return key.replace('/', File.separatorChar);
		} else {
			return key;
		}
	}
	
	@Override
	public void expire(String key) {
		lock.writeLock().lock();
		try {
			File file = new File("cache", OSKey(key));
			if(file.exists()) {
				file.delete();
			}
		} finally {
			lock.writeLock().unlock();
		}
	}

	@Override
	public CacheObject get(String key) {
		lock.readLock().lock();
		try {
			File file = new File("cache", OSKey(key));
			if(file.exists()) {
				return new FileCacheObject(file);
			}
			return null;
		} finally {
			lock.readLock().unlock();
		}
	}
	
	@Override
	public String[] getKeys() {
		lock.readLock().lock();
		try {
			File dir = new File("cache");
			if(dir.exists() && dir.isDirectory()) {
				File[] files = dir.listFiles(new FileFilter() {
					@Override
					public boolean accept(File file) {
						return file.isFile();
					}
				});
				String[] keys = new String[files.length];
				for(int i = 0; i < keys.length; i++) {
					keys[i] = files[i].getName();
				}
			}
			return new String[0];
		} finally {
			lock.readLock().unlock();
		}
	}
	
	@Override
	public String[] getKeys(String regex) {
		lock.readLock().lock();
		try {
			File dir = new File("cache");
			if(dir.exists() && dir.isDirectory()) {
				final Matcher matcher = Pattern.compile(regex).matcher("");
				File[] files = dir.listFiles(new FileFilter() {
					@Override
					public boolean accept(File file) {
						return file.isFile() && matcher.reset(file.getName()).matches();
					}
				});
				String[] keys = new String[files.length];
				for(int i = 0; i < keys.length; i++) {
					keys[i] = files[i].getName();
				}
			}
			return new String[0];
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public void set(String key, byte[] value) {
		lock.writeLock().lock();
		try {
			File file = new File("cache", OSKey(key));
			if(!file.exists()) {
				file.getParentFile().mkdirs();
				try {
					file.createNewFile();
				} catch(IOException e) {
					logger.warn(e);
					return;
				}
			}
			
			FileOutputStream out = null;
			try {
				out = new FileOutputStream(file);
				out.write(value);
				out.flush();
			} catch(Exception e) {
				logger.warn(e);
			} finally {
				if(out != null) {
					try {
						out.close();
					} catch(IOException e) {
						// discard
					}
				}
			}
		} finally {
			lock.writeLock().unlock();
		}
	}

}
