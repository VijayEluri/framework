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
import java.util.Set;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Relation {

	public static final int UNDEFINED = -1;
	public static final int CASCADE = 0;
	public static final int RESTRICT = 1;
	public static final int SET_NULL = 2;
	public static final int NO_ACTION = 3;
	
	/**
	 * Not yet implemented
	 */
	Class<?> backedBy() default Set.class;

	String through() default "";
	
	/**
	 * If set true, specifies that this field should always be included (eagerly loaded) when loading the model.<br/>
	 * default is false.
	 */
	boolean include() default false;
	
	Class<?> key() default String.class;

	int limit() default -1;
	
	/**
	 * The name of the field that will represent this relationship in the defining class.
	 */
	String name();

	/**
	 * <p>options: {@link Relation#CASCADE}, {@link Relation#RESTRICT}, {@link Relation#SET_NULL}, {@link Relation#NO_ACTION}</p>
	 * <p>default is UNDEFINED</p>
	 * <p>If onDelete is set to UNDEFINED, the the actual ON DELETE type will be set according to the model definition: if the 
	 * parent object is required it will be CASCADE; otherwise it will be SET NULL</p>
	 */
	int onDelete() default UNDEFINED;

	/**
	 * The name of the field in the related class that points to this field
	 * (the opposite of 'parent', would be 'children')
	 * @return
	 */
	String opposite() default "";
	
	String orderBy() default "";
	
	boolean readOnly() default false;
	
	/**
	 * Sets whether or not this field is required, and therefore not allowed to be null.
	 * <p>Only valid for hasOne relations.</p>
	 * <p>Default is false.</p>
	 * @return true if this field is required, false otherwise
	 */
	boolean required() default false;
	
	/**
	 * The class type that this relation points to
	 * @return class
	 */
	Class<?> type();
	
	/**
	 * <p>
	 * Set whether or not the value of the field must be unique for all instances of this model.
	 * </p>
	 * <p>
	 * Default is false.
	 * </p>
	 * @return the uniqueness of the attribute for this attribute
	 */
	boolean unique() default false;

	boolean virtual() default false;

}
