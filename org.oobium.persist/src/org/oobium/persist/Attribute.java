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
package org.oobium.persist;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Attribute {

	public static final String DEFAULT_CHECK = "";
	public static final boolean DEFAULT_INDEXED = false;
	public static final String DEFAULT_INIT = "";
	public static final boolean DEFAULT_JSON = true;
	public static final int DEFAULT_PRECISION = 8;
	public static final boolean DEFAULT_READONLY = false;
	public static final int DEFAULT_SCALE = 2;
	public static final boolean DEFAULT_UNIQUE = false;
	public static final boolean DEFAULT_VIRTUAL = false;

	
	String check() default DEFAULT_CHECK;
	
	boolean indexed() default DEFAULT_INDEXED;

	/**
	 * <p>Initialize the value of this field when instantiating the Java class.<br/>
	 * The value can be any valid Java code that will result in the correct type
	 * for the field, but should not be null.</p>
	 * <p>Has no affect on the database schema.</p>
	 */
	String init() default DEFAULT_INIT;
	
	/**
	 * Sets whether or not this field will be included in the output from the
	 * {@link Model#toJson()} and {@link Model#toJson(String, Object...)} methods.
	 * <p>Default is true.</p>
	 * @return boolean set to true if this field should be included in the output JSON when set;
	 * set false otherwise (helpful for sensitive fields such as passwords). Can be overridden by
	 * explicitly including the field using {@link Model#toJson(String, Object...)}
	 */
	boolean json() default DEFAULT_JSON;
	
	/**
	 * The name of the field in the Java class.
	 * @return the name of this attribute
	 */
	String name();

	/**
	 * Only of interest for type BigDecimal
	 * @return the precision
	 */
	int precision() default DEFAULT_PRECISION;
	
	/**
	 * Set whether or not the field can be set through a public setter method.
	 * If readOnly is set to true then the public setter method will not be generated.
	 * The field will still exist and (like other fields) is protected, so there are
	 * other ways that it can be modified.
	 * <p>
	 * Default is false.
	 * </p>
	 * @return true if this field is read only, false otherwise.
	 */
	boolean readOnly() default DEFAULT_READONLY;

	/**
	 * Only of interest for type BigDecimal
	 * @return the scale
	 */
	int scale() default DEFAULT_SCALE;
	
	/**
	 * The runtime type of this attribute is also the type of the field in the java class.
	 * @return the runtime type of this attribute
	 */
	Class<?> type();

	/**
	 * <p>Set whether or not the value of the field must be unique for all instances of this model.
	 * This is enforced at the persistor level, usually through use of an index (for databases).</p>
	 * <p>Default is false.</p>
	 * <p><b>Note:</b> if unique is true, then the field must also be declared as required</p>
	 * @return the uniqueness of the attribute for this attribute
	 * @see #required()
	 */
	boolean unique() default DEFAULT_UNIQUE;

	/**
	 * not currently implemented...
	 * @return
	 */
	boolean virtual() default DEFAULT_VIRTUAL;

}
