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
package org.oobium.eclipse.esp.editor;

import org.eclipse.jface.text.*;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.projection.ProjectionViewer;

public class EspAnnotationHover implements IAnnotationHover {

	public String getHoverInfo(ISourceViewer sourceViewer, int lineNumber) {
		IDocument document = sourceViewer.getDocument();

		try {
			IRegion info = document.getLineInformation(lineNumber);
			return document.get(info.getOffset(), info.getLength());
		} catch (BadLocationException x) {
		}

		return null;
	}

}
