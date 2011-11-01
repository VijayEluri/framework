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
package org.oobium.eclipse.esp.config;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.graphics.Image;
import org.oobium.utils.StringUtils;
import org.oobium.utils.Config.Mode;

public class ConfigLabelProvider extends StyledCellLabelProvider {

	@Override
	public void update(ViewerCell cell) {
		Object element = cell.getElement();
		String base = getText(element);
		StyledString s = new StyledString(base);

		if(element instanceof Entry && !hasChildren(element)) {
			s.append(" : " + ((Entry<?,?>) element).getValue(), StyledString.DECORATIONS_STYLER);
		}

		cell.setText(s.toString());
		cell.setStyleRanges(s.getStyleRanges());
		cell.setImage(getImage(element));
		super.update(cell);
	}

	private boolean hasChildren(Object element) {
		if(element instanceof Map) {
			return !((Map<?,?>) element).isEmpty();
		}
		if(element instanceof ConfigMode) {
			return !((ConfigMode) element).isEmpty();
		}
		if(element instanceof List) {
			return !((List<?>) element).isEmpty();
		}
		if(element instanceof Entry) {
			return hasChildren(((Entry<?,?>) element).getValue());
		}
		return false;
	}

	public String getText(Object element) {
		if(element instanceof ConfigMode) {
			Mode mode = ((ConfigMode) element).mode;
			return (mode == null) ? "Global" : StringUtils.titleize(mode.name());
		}
		if(element instanceof Entry) {
			return (String) ((Entry<?,?>) element).getKey();
		}
		if(element instanceof String) {
			return (String) element;
		}
		return "";
	}

	public Image getImage(Object element) {
		return null;
	}

}
