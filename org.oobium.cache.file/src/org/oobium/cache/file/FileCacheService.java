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

import static org.oobium.utils.FileUtils.delete;
import static org.oobium.utils.FileUtils.deleteContents;
import static org.oobium.utils.FileUtils.writeFile;
import static org.oobium.utils.literal.Dictionary;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

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
	private final String basePath;
	
	public FileCacheService() {
		logger = LogProvider.getLogger(FileCacheService.class);
		lock = new ReentrantReadWriteLock();
		String path = System.getProperty("cache.file.path", "cache");
		File file = new File(path);
		if(!file.isAbsolute()) {
			file = new File(System.getProperty("user.dir"), path);
		}
		basePath = file.getAbsolutePath();
	}
	
	@Override
	public void start(BundleContext context) throws Exception {
		logger.setTag(context.getBundle().getSymbolicName());
		logger.info("starting FileCacheService...");
		
		if("true".equalsIgnoreCase(System.getProperty("org.oobium.cache.file.expireOnStart"))) {
			logger.info("expiring cache...");
			expire();
			logger.info("cache expired");
		}

		context.registerService(CacheService.class.getName(), this, Dictionary(CacheService.TYPE, CacheService.TYPE_FILE));

		logger.info("FileCacheService started");
	}
	
	@Override
	public void stop(BundleContext context) throws Exception {
		logger.info("FileCacheService stopped");
		logger.setTag(null);
	}

	private File getFile(String key) {
		if(adjustKey) {
			return new File(basePath, key.replace('/', File.separatorChar));
		} else {
			return new File(basePath, key);
		}
	}
	
	@Override
	public void expire() {
		lock.writeLock().lock();
		try {
			deleteContents(new File(basePath));
		} finally {
			lock.writeLock().unlock();
		}
	}
	
	@Override
	public void expire(String key) {
		if(key == null || key.length() == 0) {
			throw new IllegalArgumentException("key cannot be blank");
		}
		lock.writeLock().lock();
		try {
			delete(getFile(key));
		} finally {
			lock.writeLock().unlock();
		}
	}

	@Override
	public CacheObject get(String key) {
		if(key == null || key.length() == 0) {
			throw new IllegalArgumentException("key cannot be blank");
		}
		lock.readLock().lock();
		try {
			File file = getFile(key);
			if(file.isFile()) {
				return new FileCacheObject(file);
			}
			return null;
		} finally {
			lock.readLock().unlock();
		}
	}
	
	@Override
	public void set(String key, byte[] value) {
		if(key == null || key.length() == 0) {
			throw new IllegalArgumentException("key cannot be blank");
		}
		lock.writeLock().lock();
		try {
			File file = getFile(key);
			if(value == null || value.length == 0) {
				file.getParentFile().mkdirs();
				file.createNewFile();
			} else {
				writeFile(file, new ByteArrayInputStream(value));
			}
		} catch(IOException e) {
			logger.warn("could not cache: {}", key);
		} finally {
			lock.writeLock().unlock();
		}
	}

}
