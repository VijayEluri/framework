package org.oobium.persist;

import org.oobium.utils.json.JsonUtils;

public @interface Validate {

	public static final int CREATE 	= 1 << 0;
	public static final int DESTROY = 1 << 1;
	public static final int UPDATE 	= 1 << 2;

	
	/**
	 * The field (or comma separated list of fields) to be validated.
	 */
	String field() default "";

	/**
	 * Validate that the given field is blank (null or empty)
	 */
	boolean isBlank() default false;

	/**
	 * Validate that the value of the given field is in the provided list.
	 * <p>Note that the value of this method will be converted to a List<Object>
	 * using the {@link JsonUtils#toList(String)} method.</p>
	 * @see #isNotIn()
	 */
	String isIn() default "";

	/**
	 * Validate that the given field is NOT blank (is not null, and is not empty)
	 */
	boolean isNotBlank() default false;

	/**
	 * Validate that the value of the given field is NOT in the provided list.
	 * <p>Note that the value of this method will be converted to a List<Object>
	 * using the {@link JsonUtils#toList(String)} method.</p>
	 * @see #isIn()
	 */
	String isNotIn() default "";

	/**
	 * Validate that the given field is not null.
	 */
	boolean isNotNull() default false;
	
	/**
	 * Validate that the given field is not null.
	 */
	boolean isNull() default false;
	
	/**
	 * Not yet supported
	 */
	boolean isUnique() default false;

	/**
	 * Validate that the value of the given field is equal to the provided number. 
	 */
	int lengthIs() default -1;

	/**
	 * Validate that the value of the given field matches the provided regular expression. Uses
	 * standard regular expression syntax.
	 */
	String matches() default "";

	/**
	 * Validate that the value of the given field is not greater than the provided number. 
	 */
	int maxLength() default -1;
	
	/**
	 * The message to use when recording an error for this validation.
	 */
	String message() default "";
	
	/**
	 * Validate that the value of the given field is not less than the provided number. 
	 */
	int minLength() default -1;
	
	/**
	 * Specifies when the validation should happen. By setting this option you can limit (or expand,
	 * if adding destroy) this behavior. Options can be bitwise-OR'ed together.
	 * <dl>
	 * <dt>Valid Options:</dt>
	 * <dd>{@link #CREATE}</dd>
	 * <dd>{@link #DESTROY}</dd>
	 * <dd>{@link #UPDATE}</dd>
	 * </dl>
	 * <p>The default is {@link #CREATE} | {@link #UPDATE}.
	 */
	int on() default CREATE | UPDATE;
	
	/**
	 * When validating a length (min, max, or is), the tokenizer is used to split up a String before calculating its length.
	 * The value of the tokenizer can be any valid regular expression that you would normally pass to {@link String#split(String)}.
	 * <p>Has no affect if the field being validated is not a String</p>
	 */
	String tokenizer() default "";
	
	/**
	 * Run this validation unless the method returned by this property evaluates to true.
	 * <p>Example: @Validate(field="publishedAt", isNull=true, unless="isPublished")
	 */
	String unless() default "";

	/**
	 * Run this validation unless the value of the field is blank (null or zero length).
	 * <p>Example: @Validate(field="content", minLength=1000, unlessBlank=true)</p>
	 */
	boolean unlessBlank() default false;
	
	/**
	 * Run this validation unless the value of the field is null.
	 * <p>Example: @Validate(field="content", minLength=1000, unlessNull=true)</p>
	 */
	boolean unlessNull() default false;
	
	/**
	 * Run this validation only when the method returned by this property evaluates to true.
	 * <p>Example: @Validate(field="publishedAt", isNotNull=true, when="isPublished")</p>
	 */
	String when() default "";

	/**
	 * Validate this model with the Validator specified by the given class.
	 * <p>The given class <b>MUST</b> be of type {@link Validator}.</p>
	 * <p><b>This validation cannot be mixed with other validations.</b></p>
	 */
	Class<?> with() default Object.class;
	
	/**
	 * Validate this field with the method returned by this property. The given method must
	 * return a boolean indicating that it passes or fails validation, and may take no arguments
	 * or one argument: the field that is being validated. If multiple fields are being validated
	 * then the given method may accept any of them, but on one at a time (as achieved by 
	 * method overloading).
	 * <p>Example: @Validate(field="name,publishedAt", withMethod:"test")<br/>
	 * private boolean test(String name) { ... }<br/>
	 * private boolean test(Date publishedAt) { ... }<br/>
	 * </p>
	 * <p><b>This validation cannot be mixed with other validations.</b></p>
	 */
	String withMethod() default "";
	
}
