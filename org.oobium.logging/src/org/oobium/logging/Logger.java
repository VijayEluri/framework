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
package org.oobium.logging;

import static org.oobium.logging.Activator.getLogService;
import static org.oobium.logging.Activator.hasLogTracker;
import static org.oobium.logging.Activator.isBundle;
import static org.oobium.logging.LogFormatter.format;

import java.util.HashMap;
import java.util.Map;

import org.osgi.framework.Bundle;
import org.osgi.service.log.LogService;

public class Logger implements ILogger {

	protected static int systemConsoleLevel = getSystemConsoleLevel();
	protected static int systemFileLevel = getSystemFileLevel();
	protected static int systemEmailLevel = getSystemEmailLevel();

	public static final String SYS_PROP_CONSOLE = "org.oobium.logging.console";
	public static final String SYS_PROP_EMAIL = "org.oobium.logging.email";
	public static final String SYS_PROP_FILE = "org.oobium.logging.file";
	
	private static final Map<String, Logger> loggers = new HashMap<String, Logger>();
	
	/**
	 *  32bits split into 4 bytes: CONSOLE|FILE|EMAIL|LEVEL
	 */
	public static int decode(int level) {
		return (level & 0x000000ff);
	}
	
	/**
	 * Retrieve the Default Logger.
	 * If the Logger does not currently exist, it is created.<br>
	 * Is equivalent to calling <code>getLogger(null)</code>.
	 * @param key the key used to identify the Logger, or null
	 * @return the Logger; never null
	 * @see #getLogger(Class)
	 * @see #getLogger(String)
	 */
	public static Logger getLogger() {
		return getLogger((String) null);
	}

	/**
	 * Retrieve the Logger with the name of the given Class as its key (can be null).<br>
	 * This is a convenience method which calls {@link #getLogger(String)} and passes in
	 * <code>clazz.getName()</code>.<br>
	 * <b>The passed in Class object is not retained, it is only used to get its name.</b><br>
	 * Null values for clazz returns the default Logger, as with {@link #getLogger()}.
	 * @param clazz the Class to use to create the key that identifies the Logger, or null
	 * @return the Logger; never null
	 * @see #getLogger()
	 * @see #getLogger(String)
	 */
	public static Logger getLogger(Class<?> clazz) {
		return getLogger((clazz != null) ? clazz.getName() : null);
	}
	
	/**
	 * Retrieve the Logger with the given key (can be null).<br>
	 * If the Logger does not currently exist, it is created.
	 * Null values for key returns the default Logger, as with {@link #getLogger()}.
	 * @param key the key used to identify the Logger, or null
	 * @return the Logger; never null
	 * @see #getLogger()
	 * @see #getLogger(Class)
	 */
	public static Logger getLogger(String key) {
		Logger logger = loggers.get(key);
		if(logger == null) {
			synchronized(loggers) {
				logger = loggers.get(key);
				if(logger == null) {
					loggers.put(key, logger = new Logger());
				}
			}
		}
		return logger;
	}
	
	public static int getSystemConsoleLevel() {
		return parseLevel(System.getProperty(SYS_PROP_CONSOLE), NEVER);
	}
	
	public static int getSystemEmailLevel() {
		return parseLevel(System.getProperty(SYS_PROP_EMAIL), NEVER);
	}
	
	public static int getSystemFileLevel() {
		return parseLevel(System.getProperty(SYS_PROP_FILE), INFO);
	}
	
	private static boolean isLogging(int logLevel, int setLevel) {
		return setLevel != NEVER && logLevel <= setLevel;
	}
	
	public static boolean isLoggingToConsole(Bundle bundle, int level) {
		if(isBundle(bundle)) {
			return isLogging((level & 0x000000ff), ((level & 0xff000000) >> 24));
		} else {
			return isLogging(level, systemConsoleLevel);
		}
	}
	
	public static boolean isLoggingToEmail(Bundle bundle, int level) {
		if(isBundle(bundle)) {
			return isLogging((level & 0x000000ff), ((level & 0x0000ff00) >> 8));
		} else {
			return isLogging(level, systemEmailLevel);
		}
	}
	
	public static boolean isLoggingToFile(Bundle bundle, int level) {
		if(isBundle(bundle)) {
			return isLogging((level & 0x000000ff), ((level & 0x00ff0000) >> 16));
		} else {
			return isLogging(level, systemFileLevel);
		}
	}
	
	private static int parseLevel(String level, int defaultLevel) {
		if(level != null) {
			try {
				return Integer.parseInt(level);
			} catch(Exception e) {
				if("ERROR".equalsIgnoreCase(level)) {
					return ERROR;
				} else if("WARN".equalsIgnoreCase(level) || "WARNING".equalsIgnoreCase(level)) {
					return WARNING;
				} else if("INFO".equalsIgnoreCase(level) || "INFORMATION".equalsIgnoreCase(level)) {
					return INFO;
				} else if("DEBUG".equalsIgnoreCase(level)) {
					return DEBUG;
				} else if("TRACE".equalsIgnoreCase(level)) {
					return TRACE;
				} else if("NO".equalsIgnoreCase(level) || "NONE".equalsIgnoreCase(level)) {
					return NONE;
				} else if("NEVER".equalsIgnoreCase(level)) {
					return NEVER;
				} else {
					// always return a valid value, otherwise it's a major pain in the ass to debug. will confuse users - guaranteed!
					System.out.println("Unknown logging level: " + level + ". Using default.");
					return defaultLevel;
				}
			}
		}
		return defaultLevel;
	}
	
	public static void setBundle(Class<?> clazz, Bundle bundle) {
		getLogger(clazz).bundle = bundle;
	}
	

	private Bundle bundle;
	private int consoleLevel;
	private int fileLevel;
	private int emailLevel;
	
	private Logger() {
		consoleLevel = (systemConsoleLevel == NEVER) ? NEVER : systemConsoleLevel;
		fileLevel = (systemFileLevel == NEVER) ? NEVER : systemFileLevel;
		emailLevel = (systemEmailLevel == NEVER) ? NEVER : systemEmailLevel;
	}
	
	@Override
	public void debug(String message) {
		log(DEBUG, message, null);
	}

	@Override
	public void debug(String message, Throwable exception) {
		log(DEBUG, message, exception) ;
	}
	
	@Override
	public void debug(Throwable exception) {
		log(DEBUG, exception.getLocalizedMessage(), exception) ;
	}
	
	// 32bits split into 4 bytes: CONSOLE|FILE|EMAIL|LEVEL
	private int encode(int level) {
		return 	((consoleLevel	<< 24) & 0xff000000) | 
				((fileLevel		<< 16) & 0x00ff0000) |
				((emailLevel	<<  8) & 0x0000ff00) |
				(level				   & 0x000000ff);
	}

	@Override
	public void error(String message) {
		log(ERROR, message, null);
	}
	
	@Override
	public void error(String message, Throwable exception) {
		log(ERROR, message, exception) ;
	}
	
	@Override
	public void error(Throwable exception) {
		log(ERROR, exception.getLocalizedMessage(), exception) ;
	}
	
	@Override
	public void info(String message) {
		log(INFO, message, null);
	}
	
	@Override
	public void info(String message, Throwable exception) {
		log(INFO, message, exception) ;
	}
	
	@Override
	public void info(Throwable exception) {
		log(INFO, exception.getLocalizedMessage(), exception) ;
	}
	
	@Override
	public boolean isLogging(int level) {
		return isLogging(level, consoleLevel) || isLogging(level, fileLevel) || isLogging(level, emailLevel);
	}
	
	@Override
	public boolean isLoggingDebug() {
		return isLogging(DEBUG, consoleLevel) || isLogging(DEBUG, fileLevel) || isLogging(DEBUG, emailLevel);
	}
	
	@Override
	public boolean isLoggingError() {
		return isLogging(ERROR, consoleLevel) || isLogging(ERROR, fileLevel) || isLogging(ERROR, emailLevel);
	}
	
	@Override
	public boolean isLoggingInfo() {
		return isLogging(INFO, consoleLevel) || isLogging(INFO, fileLevel) || isLogging(INFO, emailLevel);
	}
	
	@Override
	public boolean isLoggingToConsole(int level) {
		return isLogging(level, consoleLevel);
	}
	
	@Override
	public boolean isLoggingToEmail(int level) {
		return isLogging(level, emailLevel);
	}
	
	@Override
	public boolean isLoggingToFile(int level) {
		return isLogging(level, fileLevel);
	}
	
	@Override
	public boolean isLoggingTrace() {
		return isLogging(TRACE, consoleLevel) || isLogging(TRACE, fileLevel) || isLogging(TRACE, emailLevel);
	}
	
	@Override
	public boolean isLoggingWarning() {
		return isLogging(WARNING, consoleLevel) || isLogging(WARNING, fileLevel) || isLogging(WARNING, emailLevel);
	}
	
	@Override
	public void log(int level, String message) {
		log(level, message, null);
	}
	
	@Override
	public synchronized void log(int level, String message, Throwable exception) {
		if(isLogging(level)) {
			
			if(hasLogTracker()) {
				LogService service = getLogService();
				if(service != null) {
					service.log(encode(level), format(bundle, message), exception);
					return;
				}
			}
			
			// not logging else where, make sure to log to console if correct level
			if(isLoggingToConsole(level)) {
				if(level <= WARNING) {
					System.err.print(format(bundle, level, message, exception));
				} else {
					System.out.print(format(bundle, level, message, exception));
				}
			}
		}
	}
	
	@Override
	public void log(int level, Throwable exception) {
		log(level, null, exception) ;
	}
	
	@Override
	public void setBundle(Bundle bundle) {
		this.bundle = bundle;
	}
	
	@Override
	public void setConsoleLevel(int level) {
		consoleLevel = level;
	}
	
	@Override
	public void setEmailLevel(int level) {
		emailLevel = level;
	}
	
	@Override
	public void setFileLevel(int level) {
		fileLevel = level;
	}
	
	@Override
	public void trace(String message) {
		log(TRACE, message, null);
	}
	
	@Override
	public void trace(String message, Throwable exception) {
		log(TRACE, message, exception) ;
	}
	
	@Override
	public void trace(Throwable exception) {
		log(TRACE, exception.getLocalizedMessage(), exception) ;
	}
	
	@Override
	public void warn(String message) {
		log(WARNING, message, null);
	}

	@Override
	public void warn(String message, Throwable exception) {
		log(WARNING, message, exception) ;
	}
	
	@Override
	public void warn(Throwable exception) {
		log(WARNING, exception.getLocalizedMessage(), exception) ;
	}
	
}
