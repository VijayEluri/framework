/*******************************************************************************
 * Copyright (c) 2011 Oobium, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Jeremy Dowdall <jeremy@oobium.com> - initial API and implementation
 ******************************************************************************/
package org.oobium.eclipse.designer.outline;

import static org.oobium.build.model.ModelRelation.*;

import java.util.Map.Entry;

import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.graphics.Image;
import org.oobium.build.model.ModelAttribute;
import org.oobium.build.model.ModelRelation;
import org.oobium.eclipse.designer.DesignerPlugin;
import org.oobium.eclipse.designer.editor.models.ModelElement;
import org.oobium.eclipse.designer.outline.Property.Type;

public class DesignerLabelProvider extends StyledCellLabelProvider {

	@Override
	public void update(ViewerCell cell) {
		Object element = cell.getElement();
		String base = getText(element);
		StyledString s = new StyledString(base);

		if(element instanceof ModelElement) {
			// do nothing... ?
		}
		else if(element instanceof ModelAttribute) {
			ModelAttribute a = (ModelAttribute) element;
			if("java.sql.Date".equals(a.type())) {
				s.append(": sql.Date", StyledString.DECORATIONS_STYLER);
			} else {
				s.append(": " + a.getSimpleType(), StyledString.DECORATIONS_STYLER);
			}
		}
		else if(element instanceof ModelRelation) {
			ModelRelation r = (ModelRelation) element;
			if(r.isThrough()) {
				s.append(" : " + r.getSimpleType() + " -> " + r.through(), StyledString.DECORATIONS_STYLER);
			}
			else if(r.hasOpposite()) {
				s.append(" : " + r.getSimpleType() + " (" + r.opposite() + ")", StyledString.DECORATIONS_STYLER);
			}
			else {
				s.append(" : " + r.getSimpleType(), StyledString.DECORATIONS_STYLER);
			}
		}
		else if(element instanceof Property) {
			String value = String.valueOf(((Property) element).value);
			if("dependent".equals(base)) {
				value = getDependentConstant(Integer.parseInt(value));
			}
			else if("onDelete".equals(base)) {
				value = getReferentialConstant(Integer.parseInt(value));
			}
			else if("onUpdate".equals(base)) {
				value = getReferentialConstant(Integer.parseInt(value));
			}
			s.append(" : " + value, StyledString.DECORATIONS_STYLER);
		}

		cell.setText(s.toString());
		cell.setStyleRanges(s.getStyleRanges());
		cell.setImage(getImage(element));
		super.update(cell);
	}

	public String getText(Object element) {
		if(element instanceof ModelElement) {
			return ((ModelElement) element).getName();
		}
		if(element instanceof ModelAttribute) {
			return ((ModelAttribute) element).name();
		}
		if(element instanceof ModelRelation) {
			return ((ModelRelation) element).name();
		}
		if(element instanceof Property) {
			return (String) ((Property) element).key;
		}
		return "";
	}

	public Image getImage(Object element) {
		if(element instanceof ModelElement) {
			return DesignerPlugin.getImage("/icons/model.gif");
		}
		if(element instanceof ModelAttribute) {
			return DesignerPlugin.getImage("/icons/attribute.gif");
		}
		if(element instanceof ModelRelation) {
			if(((ModelRelation) element).hasMany()) {
				return DesignerPlugin.getImage("/icons/has_many.gif");
			}
			else {
				return DesignerPlugin.getImage("/icons/has_one.gif");
			}
		}
		if(element instanceof Property) {
			if(((Property) element).type == Type.Validation) {
				return DesignerPlugin.getImage("/icons/validation.png");
			}
		}
		return null;
	}

}
