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
import org.eclipse.swt.graphics.Point;
import org.oobium.build.esp.Constants;
import org.oobium.build.esp.EspPart;
import org.oobium.build.esp.EspPart.Type;
import org.oobium.eclipse.esp.EspCore;
import org.oobium.utils.StringUtils;

public class EspTextHover implements ITextHover {

	public String getHoverInfo(ITextViewer viewer, IRegion region) {
		if (region != null) {
			try {
				IDocument doc = viewer.getDocument();
				EspPart part = EspCore.get(doc).getPart(region.getOffset());
				if(region.getLength() > 0) {
					if(part != EspCore.get(doc).getPart(region.getOffset())) {
						part = null;
					}
				}
				if(part != null) {
					String name = StringUtils.titleize(part.getType().name());
					String text = part.getText();
					String info = name + ": \"" + text + "\"";
					if(part.isA(Type.TagPart)) {
						String description = Constants.HTML_TAGS.get(text);
						if(description == null) {
							return info + " - Unknown HTML Tag";
						} else {
							return info + " - " + description;
						}
					} else {
						return info;
					}
				} else {
					return doc.get(region.getOffset(), region.getLength());
				}
			} catch (BadLocationException x) {
			}
		}
		return EspEditorMessages.getString("JavaTextHover.emptySelection"); //$NON-NLS-1$
	}
	
	public IRegion getHoverRegion(ITextViewer textViewer, int offset) {
		Point selection= textViewer.getSelectedRange();
		if (selection.x <= offset && offset < selection.x + selection.y) {
			return new Region(selection.x, selection.y);
		}
		return new Region(offset, 0);
	}
	
}
