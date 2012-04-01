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

import static org.oobium.utils.StringUtils.join;

import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.swt.graphics.Point;
import org.oobium.build.esp.Constants;
import org.oobium.build.esp.EspPart;
import org.oobium.build.esp.EspPart.Type;
import org.oobium.build.esp.elements.StyleChildElement;
import org.oobium.eclipse.esp.EspCore;
import org.oobium.eclipse.esp.EssCore;
import org.oobium.utils.StringUtils;

public class EspTextHover implements ITextHover {

	private EspEditor editor;
	
	public EspTextHover(EspEditor editor) {
		this.editor = editor;
	}
	
	public String getHoverInfo(ITextViewer viewer, IRegion region) {
		if (region != null) {
			try {
				IDocument doc = viewer.getDocument();
				int offset = region.getOffset();

				IMarker[] markers = editor.getMarkers(offset);
				if(markers != null) {
					Set<String> set = null;
					for(IMarker marker : markers) {
						try {
							Object o = marker.getAttribute(IMarker.CHAR_START);
							if(o instanceof Integer) {
								int start = (Integer) o;
								o = marker.getAttribute(IMarker.CHAR_END);
								if(o instanceof Integer) {
									int end = (Integer) o;
									if(start <= offset && offset < end) {
										if(set == null) {
											set = new TreeSet<String>();
										}
										set.add(String.valueOf(marker.getAttribute(IMarker.MESSAGE)));
									}
								}
							}
						} catch(CoreException e) {
							// discard
						}
					}
					if(set != null) {
						return join(set, '\n');
					}
				}

				// if no message from markers, return info about the part
				EspPart part = EspCore.get(doc).getPart(offset);
				if(region.getLength() > 0) {
					if(part != EspCore.get(doc).getPart(offset)) {
						part = null;
					}
				}
				if(part != null) {
					switch(part.getType()) {
					case ClassPart:
						return getCssClassHover(part);
					case IdPart:
						return getCssIdHover(part);
					default:
						return getDefaultHover(part);
					}
				} else {
					return doc.get(offset, region.getLength());
				}
			} catch (BadLocationException x) {
			}
		}
		return EspEditorMessages.getString("JavaTextHover.emptySelection"); //$NON-NLS-1$
	}

	private String getCssClassHover(EspPart part) {
		EspPart selector = EssCore.getCssClass(part.getDom(), part.getText());
		if(selector == null) {
			return "";
		} else {
			return getCssSelectorHover(selector);
		}
	}
	
	private String getCssIdHover(EspPart part) {
		EspPart selector = EssCore.getCssId(part.getDom(), part.getText());
		if(selector == null) {
			return "";
		} else {
			return getCssSelectorHover(selector);
		}
	}
	
	private String getCssSelectorHover(EspPart selector) {
		StringBuilder hover = new StringBuilder();
		for(StyleChildElement prop : ((StyleChildElement) selector.getParent()).getProperties()) {
			hover.append(prop).append('\n');
		}
		hover.deleteCharAt(hover.length()-1);
		return hover.toString();
	}
	
	private String getDefaultHover(EspPart part) {
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
	}
	
	public IRegion getHoverRegion(ITextViewer textViewer, int offset) {
		Point selection= textViewer.getSelectedRange();
		if (selection.x <= offset && offset < selection.x + selection.y) {
			return new Region(selection.x, selection.y);
		}
		return new Region(offset, 0);
	}
	
}
