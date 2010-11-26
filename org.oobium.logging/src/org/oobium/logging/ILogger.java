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

import org.osgi.framework.Bundle;
import org.osgi.service.log.LogService;

public interface ILogger {

	/**
	 * A level indicating that no logging should occur, although loggers requiring
	 * initialization will still be initialized in case the level is changed later (value: 0)
	 */
	public static final int NONE = 0;
	
	/**
	 * A level indicating an error that could be critical to the system
	 * (value: <cod>LogService.LOG_ERROR</code> = 1)
	 */
	public static final int ERROR = LogService.LOG_ERROR;
	
	/**
	 * A level indicating a warning that can be handled by the system
	 * (value: <cod>LogService.LOG_WARNING</code> = 2)
	 */
	public static final int WARNING = LogService.LOG_WARNING;
	
	/**
	 * A level indicating an informative message which occurs during normal system operation
	 * (value: <cod>LogService.LOG_INFO</code> = 3)
	 */
	public static final int INFO = LogService.LOG_INFO;
	
	/**
	 * A level indicating a message that could be useful in debugging the system
	 * (value: <cod>LogService.LOG_DEBUG</code> = 4)
	 */
	public static final int DEBUG = LogService.LOG_DEBUG;
	
	/**
	 * A level indicating a message that could be useful in debugging minute parts of the system
	 * (value: <cod>LogService.LOG_DEBUG + 1</code> = 5)
	 */
	public static final int TRACE = LogService.LOG_DEBUG + 1;
	
	/**
	 * A level indicating that no logging will occur.
	 * Loggers requiring initialization will <b>not</b> be initialized and will not perform as
	 * expected if the level is changed later.
	 * <p>Only valid if used at the system level; set by a system property</p>
	 * (value: 255)
	 */
	public static final int NEVER = 255; // -1 in the unsigned world...
	

	public abstract void debug(String message);

	public abstract void debug(String message, Throwable exception);

	public abstract void debug(Throwable exception);

	public abstract void error(String message);

	public abstract void error(String message, Throwable exception);

	public abstract void error(Throwable exception);

	public abstract void info(String message);

	public abstract void info(String message, Throwable exception);

	public abstract void info(Throwable exception);

	public abstract boolean isLogging(int level);

	public abstract boolean isLoggingDebug();

	public abstract boolean isLoggingError();

	public abstract boolean isLoggingInfo();

	public abstract boolean isLoggingToConsole(int level);

	public abstract boolean isLoggingToEmail(int level);

	public abstract boolean isLoggingToFile(int level);

	public abstract boolean isLoggingTrace();

	public abstract boolean isLoggingWarning();

	public abstract void log(int level, String message);

	public abstract void log(int level, String message, Throwable exception);

	public abstract void log(int level, Throwable exception);

	public abstract void setBundle(Bundle bundle);

	public abstract void setConsoleLevel(int level);

	public abstract void setEmailLevel(int level);

	public abstract void setFileLevel(int level);

	public abstract void trace(String message);

	public abstract void trace(String message, Throwable exception);

	public abstract void trace(Throwable exception);

	public abstract void warn(String message);

	public abstract void warn(String message, Throwable exception);

	public abstract void warn(Throwable exception);

}
