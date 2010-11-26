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
package org.oobium.mailer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Mailer {

	/**
	 * The 'BCC' addresses of the email
	 */
	String[] bcc() default {};
	
	/**
	 * The body of the email.<br>
	 * This attribute is ignored if the 'template' attribute is set.
	 */
	String body() default "";
	
	/**
	 * The 'CC' addresses of the email
	 */
	String[] cc() default {};

//	/**
//	 * The char set to use for the email. Defaults to "ISO-8859-1".
//	 */
//	String charset() default "ISO-8859-1";
	
	/**
	 * The content type for the email. This defaults to “text/plain” but the filename may specify it.
	 * This attribute is ignored if the 'template' attribute is set.
	 */
	String contentType() default "text/plain";

	/**
	 * The from address of the email
	 */
	String from() default "";

	/**
	 * The address (if different than the “from” address) to direct replies to this email
	 */
	String replyTo() default "";
	
	/**
	 * Additional headers to be added to the email (in JSON format)
	 */
	String headers() default "";
	
	/**
	 * The 'To' addresses of the email
	 */
	String[] to() default {};
	
	/**
	 * The subject of the email
	 */
	String subject() default "";

	/**
	 * The template to use to create the body of this email.
	 * <p>Note: the class <b>must</b> extend {@link MailerTemplate}.</p>
	 */
	Class<?> template() default Object.class;

}
