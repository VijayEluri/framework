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
public @interface Relation {

	public static final String[] DEPENDENT_CONSTANTS = { "UNDEFINED", "DESTROY", "DELETE", "NULLIFY" };
	public static final String[] REFERENTIAL_CONSTANTS = { "UNDEFINED", "CASCADE", "NO_ACTION", "RESTRICT", "SET_DEFAULT", "SET_NULL" };

	public static final int UNDEFINED = -1;
	
	/**
	 * Whenever rows in the parent (referenced) table are deleted, the respective rows of the child (referencing)
	 * table with a matching foreign key column will get deleted as well. This is called a cascade delete.
	 * <a href="http://en.wikipedia.org/wiki/Foreign_key#CASCADE">Wikipedia Entry</a>
	 * @see Relation#onDelete()
	 * @see Relation#onUpdate()
	 */
	public static final int CASCADE = 0;
	
	/**
	 * NO ACTION and RESTRICT are very much alike. The main difference between NO ACTION and RESTRICT is that with
	 * NO ACTION the referential integrity check is done after trying to alter the table. RESTRICT does the check 
	 * before trying to execute the UPDATE or DELETE statement. Both referential actions act the same if the referential
	 * integrity check fails: the UPDATE or DELETE statement will result in an error.
	 * <a href="http://en.wikipedia.org/wiki/Foreign_key#NO_ACTION">Wikipedia Entry</a>
	 * @see Relation#onDelete()
	 * @see Relation#onUpdate()
	 */
	public static final int NO_ACTION = 1;
	
	/**
	 * A value cannot be updated or deleted when a row exists in a foreign key table that references the value in the referenced table.
	 * <a href="http://en.wikipedia.org/wiki/Foreign_key#RESTRICT">Wikipedia Entry</a>
	 * @see Relation#onDelete()
	 * @see Relation#onUpdate()
	 */
	public static final int RESTRICT = 2;
	
	/**
	 * Similar to SET NULL, the foreign key values in the referencing row are set to the column default when the
	 * referenced row is updated or deleted.
	 * <a href="http://en.wikipedia.org/wiki/Foreign_key#SET_DEFAULT">Wikipedia Entry</a>
	 * @see Relation#onDelete()
	 * @see Relation#onUpdate()
	 */
	public static final int SET_DEFAULT = 3;
	
	/**
	 * The foreign key values in the referencing row are set to NULL when the referenced row is updated or deleted.
	 * <a href="http://en.wikipedia.org/wiki/Foreign_key#SET_NULL">Wikipedia Entry</a>
	 * @see Relation#onDelete()
	 * @see Relation#onUpdate()
	 */
	public static final int SET_NULL = 4;

	/**
	 * Destroy the related model object when this model is destroyed. This will instantiate the related model and then call
	 * its destroy method, thereby running any validations and/or observers.
	 * <p>This is handled at the Model level.</p>
	 * @see Relation#dependent()
	 */
	public static final int DESTROY = 0;

	/**
	 * Delete the related model object when this model is destroyed. The will delete the model from persistent storage
	 * without instantiating it or calling its destroy method, thereby skipping its validations and observers.
	 * <p>This may be better achieved by setting the opposite relation's onDelete property to CASCADE</p>
	 * <p>This is handled at the Persistor level.</p>
	 * @see Relation#dependent()
	 * @see Relation#onDelete()
	 */
	public static final int DELETE = 1;

	/**
	 * Similar to {@link Relation#DELETE}, this sets the foreign key of the related model to null when this
	 * model is destroyed.
	 * <p>This may be better achieved by setting the opposite relation's onDelete property to SET_NULL</p>
	 * <p>This is handled at the Persistor level.</p>
	 * @see Relation#dependent()
	 * @see Relation#onDelete()
	 */
	public static final int NULLIFY = 2;

	
	public static final int DEFAULT_DEPENDENT = UNDEFINED;
	public static final String DEFAULT_EMBED = "";
	public static final boolean DEFAULT_EMBEDDED = false;
	public static final boolean DEFAULT_HASKEY = false;
	public static final boolean DEFAULT_INCLUDE = false;
	public static final int DEFAULT_ONDELETE = UNDEFINED;
	public static final int DEFAULT_ONUPDATE = UNDEFINED;
	public static final String DEFAULT_OPPOSITE = "";
	public static final String DEFAULT_ORDERBY = "";
	public static final boolean DEFAULT_READONLY = false;
	public static final String DEFAULT_THROUGH = "";
	public static final boolean DEFAULT_UNIQUE = false;
	public static final boolean DEFAULT_VIRTUAL = false;

	/**
	 * Specify what to do with dependent (related) model objects when this model is destroyed.
	 * <p><b>Valid only for both has one and has many relationships</b></p>
	 * <dl>
	 *   <dt>options:</dt>
	 *   <dd>{@link Relation#DESTROY}</dd>
	 *   <dd>{@link Relation#DELETE}</dd>
	 *   <dd>{@link Relation#NULLIFY}</dd>
	 * </dl>
	 * <p>default is UNDEFINED, meaning that it is not used</p>
	 */
	int dependent() default DEFAULT_DEPENDENT;

	/**
	 * A comma separated list of the fields from the related Model to embed.
	 * If this property is not an empty string (indicates one or more fields),
	 * and if the related Model is not an embedded Model, then its id will also be included, whether it is
	 * in the provided list or not. Default is an empty string.
	 * <p>This setting will be overridden by {@link #embedded()}.</p>
	 * <p>Option and PersistService implementation specific.</p>
	 * @see #embedded()
	 * @see ModelDescription#embedded()
	 */
	String embed() default DEFAULT_EMBED;

	/**
	 * If set true, then the entire related Model will be embedded within this Model.
	 * <p>This setting will override {@link #embed()}.</p>
	 * <p>Option and PersistService implementation specific.</p>
	 * @see #embed()
	 * @see ModelDescription#embedded()
	 */
	boolean embedded() default DEFAULT_EMBEDDED;
	
	boolean hasKey() default false;
	
	/**
	 * If set true, specifies that this field should always be included (eagerly loaded) when loading the model.<br/>
	 * default is false.
	 */
	boolean include() default DEFAULT_INCLUDE;
	
	/**
	 * The name of the field that will represent this relationship in the defining class.
	 */
	String name();
	
	/**
	 * The referential action to take place when the referenced model is destroyed. The name comes from the ON DELETE clause
	 * in an SQL Foreign Key constraint and the meaning of the referential actions can be taken directly from SQL references.
	 * <p><b>Valid only for has one relationships</b></p>
	 * <dl>
	 *   <dt>actions:</dt>
	 *   <dd>{@link Relation#CASCADE}</dd>
	 *   <dd>{@link Relation#NO_ACTION}</dd>
	 *   <dd>{@link Relation#RESTRICT}</dd>
	 *   <dd>{@link Relation#SET_DEFAULT}</dd>
	 *   <dd>{@link Relation#SET_NULL}</dd>
	 * </dl>
	 * <p>default is UNDEFINED, meaning that this clause is not set</p>
	 */
	int onDelete() default DEFAULT_ONDELETE;

	/**
	 * The referential action to take place when the referenced model is destroyed. The name comes from the ON UPDATE clause
	 * in an SQL Foreign Key constraint and the meaning of the referential actions can be taken directly from SQL references.
	 * <p><b>Valid only for has one relationships</b></p>
	 * <dl>
	 *   <dt>actions:</dt>
	 *   <dd>{@link Relation#CASCADE}</dd>
	 *   <dd>{@link Relation#NO_ACTION}</dd>
	 *   <dd>{@link Relation#RESTRICT}</dd>
	 *   <dd>{@link Relation#SET_DEFAULT}</dd>
	 *   <dd>{@link Relation#SET_NULL}</dd>
	 * </dl>
	 * <p>default is UNDEFINED, meaning that this clause is not set</p>
	 */
	int onUpdate() default DEFAULT_ONUPDATE;

	/**
	 * The name of the field in the related class that points to this field
	 * (the opposite of 'parent', would be 'children')
	 * @return
	 */
	String opposite() default DEFAULT_OPPOSITE;

	String orderBy() default DEFAULT_ORDERBY;
	
	boolean readOnly() default false;
	
	String through() default DEFAULT_THROUGH;
	
	/**
	 * The class type that this relation points to
	 * @return class
	 */
	Class<? extends Model> type();
	
	/**
	 * <p>
	 * Set whether or not the value of the field must be unique for all instances of this model.
	 * </p>
	 * <p>
	 * Default is false.
	 * </p>
	 * @return the uniqueness of the attribute for this attribute
	 */
	boolean unique() default DEFAULT_UNIQUE;

	boolean virtual() default DEFAULT_VIRTUAL;

}
