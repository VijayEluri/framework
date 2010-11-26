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

import javax.mail.Message;

public enum RecipientType {

	TO, CC, BCC;
	
	Message.RecipientType getMessageRecipientType() {
		switch(this) {
		case TO:	return Message.RecipientType.TO;
		case CC:	return Message.RecipientType.CC;
		case BCC:	return Message.RecipientType.BCC;
		}
		throw new IllegalStateException("unknown RecipientType " + this);
	}
	
}
