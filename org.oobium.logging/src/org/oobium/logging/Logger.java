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


public interface Logger {

	/**
	 * The system property for setting the logging level of the console output.
	 */
	public static final String SYS_PROP_CONSOLE = "org.oobium.logging.console";
	
	/**
	 * The system property for setting the logging level of the email output.
	 */
	public static final String SYS_PROP_EMAIL = "org.oobium.logging.email";
	
	/**
	 * The system property for setting the logging level of the file output.
	 */
	public static final String SYS_PROP_FILE = "org.oobium.logging.file";
	
	/**
	 * A level indicating that no logging should occur, although loggers requiring
	 * initialization will still be initialized in case the level is changed later (value: 0)
	 */
	public static final int NONE = 0;
	
	/**
	 * A level indicating an error that could be critical to the system
	 * (value equal to <cod>LogService.LOG_ERROR</code> = 1)
	 */
	public static final int ERROR = 1;
	
	/**
	 * A level indicating a warning that can be handled by the system
	 * (value equal to <cod>LogService.LOG_WARNING</code> = 2)
	 */
	public static final int WARNING = 2;
	
	/**
	 * A level indicating an informative message which occurs during normal system operation
	 * (value equal to <cod>LogService.LOG_INFO</code> = 3)
	 */
	public static final int INFO = 3;
	
	/**
	 * A level indicating a message that could be useful in debugging the system
	 * (value equal to <cod>LogService.LOG_DEBUG</code> = 4)
	 */
	public static final int DEBUG = 4;
	
	/**
	 * A level indicating a message that could be useful in debugging minute parts of the system
	 * (value equal to <cod>LogService.LOG_DEBUG + 1</code> = 5)
	 */
	public static final int TRACE = 5;
	
	/**
	 * A level indicating that no logging will occur.
	 * Loggers requiring initialization will <b>not</b> be initialized and will not perform as
	 * expected if the level is changed later.
	 * <p>Only valid if used at the system level; set by a system property</p>
	 * (value: 255)
	 */
	public static final int NEVER = 255; // -1 in the unsigned world...
	

	public abstract void debug(String message);

	/**
	 * Log the given message at the {@link #DEBUG} level.<br/>
	 * Message can contain anchors, denoted by an opening brace immediately followed by a closing brace - {},
	 * which will be replaced by the String representation of the corresponding value in the given values.
	 * See <a href="http://www.slf4j.org/faq.html#logging_performance">SLF4J FAQ</a> for more information.
	 */
	public abstract void debug(String message, Object...values);

	public abstract void debug(String message, Throwable exception);

	/**
	 * Log the given message and Throwable at the {@link #DEBUG} level.<br/>
	 * Message can contain anchors, denoted by an opening brace immediately followed by a closing brace - {},
	 * which will be replaced by the String representation of the corresponding value in the given values.
	 * See <a href="http://www.slf4j.org/faq.html#logging_performance">SLF4J FAQ</a> for more information.
	 */
	public abstract void debug(String message, Throwable exception, Object...values);

	public abstract void debug(Throwable exception);

	public abstract void error(String message);

	/**
	 * Log the given message at the {@link #ERROR} level.<br/>
	 * Message can contain anchors, denoted by an opening brace immediately followed by a closing brace - {},
	 * which will be replaced by the String representation of the corresponding value in the given values.
	 * See <a href="http://www.slf4j.org/faq.html#logging_performance">SLF4J FAQ</a> for more information.
	 */
	public abstract void error(String message, Object...values);

	public abstract void error(String message, Throwable exception);

	/**
	 * Log the given message and Throwable at the {@link #ERROR} level.<br/>
	 * Message can contain anchors, denoted by an opening brace immediately followed by a closing brace - {},
	 * which will be replaced by the String representation of the corresponding value in the given values.
	 * See <a href="http://www.slf4j.org/faq.html#logging_performance">SLF4J FAQ</a> for more information.
	 */
	public abstract void error(String message, Throwable exception, Object...values);

	public abstract void error(Throwable exception);

	public abstract void info(String message);

	/**
	 * Log the given message at the {@link #INFO} level.<br/>
	 * Message can contain anchors, denoted by an opening brace immediately followed by a closing brace - {},
	 * which will be replaced by the String representation of the corresponding value in the given values.
	 * See <a href="http://www.slf4j.org/faq.html#logging_performance">SLF4J FAQ</a> for more information.
	 */
	public abstract void info(String message, Object...values);

	public abstract void info(String message, Throwable exception);

	/**
	 * Log the given message and Throwable at the {@link #INFO} level.<br/>
	 * Message can contain anchors, denoted by an opening brace immediately followed by a closing brace - {},
	 * which will be replaced by the String representation of the corresponding value in the given values.
	 * See <a href="http://www.slf4j.org/faq.html#logging_performance">SLF4J FAQ</a> for more information.
	 */
	public abstract void info(String message, Throwable exception, Object...values);

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

	/**
	 * Log the given message at the given level.<br/>
	 * Message can contain anchors, denoted by an opening brace immediately followed by a closing brace - {},
	 * which will be replaced by the String representation of the corresponding value in the given values.
	 * See <a href="http://www.slf4j.org/faq.html#logging_performance">SLF4J FAQ</a> for more information.
	 * @see #ERROR
	 * @see #WARN
	 * @see #INFO
	 * @see #DEBUG
	 * @see #TRACE
	 */
	public abstract void log(int level, String message, Object...values);
	
	public abstract void log(int level, String message, Throwable exception);
	
	public abstract void log(int level, String message, Throwable exception, Object...values);

	public abstract void log(int level, Throwable exception);

	public abstract void setConsoleLevel(int level);

	public abstract void setEmailLevel(int level);

	public abstract void setFileLevel(int level);
	
	public abstract void setTag(String tag);

	public abstract void trace(String message);

	public abstract void trace(String message, Object...values);

	public abstract void trace(String message, Throwable exception);

	public abstract void trace(String message, Throwable exception, Object...values);

	public abstract void trace(Throwable exception);

	public abstract void warn(String message);

	/**
	 * Log the given message at the {@link #WARN} level.<br/>
	 * Message can contain anchors, denoted by an opening brace immediately followed by a closing brace - {},
	 * which will be replaced by the String representation of the corresponding value in the given values.
	 * See <a href="http://www.slf4j.org/faq.html#logging_performance">SLF4J FAQ</a> for more information.
	 */
	public abstract void warn(String message, Object...values);

	public abstract void warn(String message, Throwable exception);

	/**
	 * Log the given message and Throwable at the {@link #WARN} level.<br/>
	 * Message can contain anchors, denoted by an opening brace immediately followed by a closing brace - {},
	 * which will be replaced by the String representation of the corresponding value in the given values.
	 * See <a href="http://www.slf4j.org/faq.html#logging_performance">SLF4J FAQ</a> for more information.
	 */
	public abstract void warn(String message, Throwable exception, Object...values);

	public abstract void warn(Throwable exception);

}
