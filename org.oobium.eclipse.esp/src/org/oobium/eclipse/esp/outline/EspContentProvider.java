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
package org.oobium.eclipse.esp.outline;

import static org.oobium.build.esp.EspPart.Type.*;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.DefaultPositionUpdater;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IPositionUpdater;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Display;
import org.oobium.build.esp.EspElement;
import org.oobium.eclipse.esp.EspCore;

/**
 * Divides the editor's document into ten segments and provides elements for them.
 */
class EspContentProvider implements ITreeContentProvider, PropertyChangeListener {

	private final static String SEGMENTS= "__esp_segments"; //$NON-NLS-1$
	private IPositionUpdater positionUpdater= new DefaultPositionUpdater(SEGMENTS);
	
	private TreeViewer viewer;
	private IDocument document;
	private Imports imports;
	
	protected void parse(IDocument document) {
		int lines= document.getNumberOfLines();
		int increment= Math.max(Math.round(lines / 10), 10);

		for (int line= 0; line < lines; line += increment) {

			int length= increment;
			if (line + increment > lines)
				length= lines - line;

			try {

				int offset= document.getLineOffset(line);
				int end= document.getLineOffset(line + length);
				length= end - offset;
				Position p= new Position(offset, length);
				document.addPosition(SEGMENTS, p);
//				content.add(new Segment(MessageFormat.format(EspEditorMessages.getString("OutlinePage.segment.title_pattern"), new Object[] { new Integer(offset) }), p)); //$NON-NLS-1$

			} catch (BadPositionCategoryException x) {
			} catch (BadLocationException x) {
			}
		}
	}

	
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		this.viewer = (TreeViewer) viewer;
		
		if (oldInput != null) {
//			EspCore.get(document).removeListener(this);
			if (document != null) {
				try {
					document.removePositionCategory(SEGMENTS);
				} catch (BadPositionCategoryException x) {
				}
				document.removePositionUpdater(positionUpdater);
			}
		}

		document = (IDocument) newInput;
		
		if (newInput != null) {
//			EspCore.get(document).addListener(this);
			if (document != null) {
				document.addPositionCategory(SEGMENTS);
				document.addPositionUpdater(positionUpdater);
			}
		}
	}

	public void dispose() {
		if(document != null) {
//			EspCore.get(document).removeListener(this);
		}
	}

	public Object[] getElements(Object element) {
		if(element instanceof IDocument) {
			List<Object> elements = new ArrayList<Object>();
			imports = null;
//			for(EspLine line : EspCore.get(document).lines()) {
//				if(line.isElementA(ImportElement)) {
//					if(imports == null) imports = new Imports();
//					imports.addChild(line.get(0));
//				} else if(!line.isElementA(BlankElement) && line.getLevel() == 0) {
//					elements.add(line.get(0));
//				}
//			}
			if(imports != null) {
				elements.add(0, imports);
			}
			return elements.toArray();
		}
		return new Object[0];
	}

	public boolean hasChildren(Object element) {
//		if(element instanceof EspElement) {
//			return ((EspElement) element).hasChildren();
//		}
		if(element instanceof Imports) {
			return ((Imports) element).hasChildren();
		}
		return false;
	}

	public Object getParent(Object element) {
		if(element instanceof EspElement) {
			return ((EspElement) element).getParent();
		}
		if(element instanceof Imports) {
			return imports;
		}
		return null;
	}

	public Object[] getChildren(Object element) {
//		if(element instanceof EspElement) {
//			return ((EspElement) element).getChildren().toArray();
//		}
		if(element instanceof Imports) {
			return ((Imports) element).getChildren().toArray();
		}
		return new Object[0];
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
//		Display.getDefault().asyncExec(new Runnable() {
//			@Override
//			public void run() {
//				viewer.refresh();
//			}
//		});
	}
	
}
