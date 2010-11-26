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

import static org.oobium.utils.literal.*;

import org.junit.Ignore;
import org.junit.Test;

public class MailerTests {
	
	@Ignore
	@Test
	public void testSendMail() throws Exception {
		AbstractMailer mailer = new AbstractMailer() {
		};

		// TODO enter username, password, and To address
		String username = "";
		String password = "";
		String to = "";
		
		mailer.setProperties(Map(e("service", "gmail"), e("username", username), e("password", password)));
		
		mailer.setFrom("Oobium Support <support@oobium.com>");
		mailer.setTo(to);
		mailer.setSubject("[oobium] Thank you for signing up!");
		mailer.render("sorry, just a test here...");
		
		mailer.send();
	}
	
}
