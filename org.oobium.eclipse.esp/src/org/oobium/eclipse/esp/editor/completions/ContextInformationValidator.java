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
package org.oobium.eclipse.esp.editor.completions;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationPresenter;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;

/**
 * Simple content assist tip closer. The tip is valid in a range
 * of 5 characters around its popup location.
 */
public class ContextInformationValidator implements IContextInformationValidator, IContextInformationPresenter {

	protected int installOffset;

	/*
	 * @see IContextInformationValidator#isContextInformationValid(int)
	 */
	public boolean isContextInformationValid(int offset) {
		return Math.abs(installOffset - offset) < 5;
	}

	/*
	 * @see IContextInformationValidator#install(IContextInformation, ITextViewer, int)
	 */
	public void install(IContextInformation info, ITextViewer viewer, int offset) {
		installOffset = offset;
	}
	
	/*
	 * @see org.eclipse.jface.text.contentassist.IContextInformationPresenter#updatePresentation(int, TextPresentation)
	 */
	public boolean updatePresentation(int documentPosition, TextPresentation presentation) {
		return false;
	}
}
