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

import static org.oobium.build.esp.dom.EspPart.Type.Comment;
import static org.oobium.build.esp.dom.EspPart.Type.Constructor;
import static org.oobium.build.esp.dom.EspPart.Type.ImportElement;
import static org.oobium.build.esp.dom.EspPart.Type.StyleElement;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
import org.oobium.build.esp.dom.EspDom;
import org.oobium.build.esp.dom.EspElement;
import org.oobium.build.esp.dom.EspPart;
import org.oobium.build.esp.dom.elements.ImportElement;
import org.oobium.build.esp.dom.elements.JavaElement;
import org.oobium.build.esp.dom.elements.MarkupElement;
import org.oobium.build.esp.dom.elements.StyleElement;
import org.oobium.build.esp.dom.parts.StylePart;
import org.oobium.build.esp.dom.parts.style.Ruleset;
import org.oobium.build.esp.dom.parts.style.Selector;
import org.oobium.eclipse.esp.EspCore;

class EspContentProvider implements ITreeContentProvider, PropertyChangeListener {

	private Comparator<Object> sorter = new Comparator<Object>() {
		private EspLabelProvider lp = new EspLabelProvider();
		@Override
		public int compare(Object o1, Object o2) {
			String s1 = lp.getText(o1).toLowerCase();
			String s2 = lp.getText(o2).toLowerCase();
			return s1.compareTo(s2);
		}
	};

	private final static String SEGMENTS= "__esp_segments"; //$NON-NLS-1$
	private IPositionUpdater positionUpdater= new DefaultPositionUpdater(SEGMENTS);
	
	private TreeViewer viewer;
	private IDocument document;
	private Imports imports;
	
	private boolean sort;
	
	public EspContentProvider(boolean sort) {
		this.sort = sort;
	}
	
	public void dispose() {
		if(document != null) {
//			EspCore.get(document).removeListener(this);
		}
	}

	public Object[] getChildren(Object element) {
		if(element instanceof StyleElement) {
			List<Object> selectors = new ArrayList<Object>();
			StylePart style = ((StyleElement) element).getAsset();
			for(Ruleset rule : style.getRules()) {
				for(Selector selector : rule.getSelectorGroup().getSelectors()) {
					selectors.add(selector);
				}
			}
			return sorted(selectors).toArray();
		}
		if(element instanceof MarkupElement) {
			return ((MarkupElement) element).getChildren().toArray();
		}
		if(element instanceof JavaElement) {
			return ((JavaElement) element).getChildren().toArray();
		}
		if(element instanceof Imports) {
			return sorted(((Imports) element).getChildren()).toArray();
		}
		return new Object[0];
	}

	public Object[] getElements(Object element) {
		if(element instanceof IDocument) {
			List<Object> ctors = new ArrayList<Object>();
			List<Object> elements = new ArrayList<Object>();
			imports = null;
			EspDom dom = EspCore.get(document);
			for(EspPart part : dom) {
				if(part.isA(ImportElement)) {
					if(imports == null) imports = new Imports();
					imports.addChild((EspElement) part);
				}
				else if(part.isA(Constructor)) {
					ctors.add(part);
				}
				else if(part.isA(Comment)) {
					continue;
				}
				else {
					if(part.isA(StyleElement) && dom.isStyle()) {
						StylePart style = ((StyleElement) part).getAsset();
						for(Ruleset rule : style.getRules()) {
							for(Selector selector : rule.getSelectorGroup().getSelectors()) {
								elements.add(selector);
							}
						}
					} else {
						elements.add(part);
					}
				}
			}
			if(dom.isStyle()) {
				elements = sorted(elements);
			}
			if(!ctors.isEmpty()) {
				elements.addAll(0,sorted(ctors));
			}
			if(imports != null) {
				elements.add(0, imports);
			}
			return elements.toArray();
		}
		return new Object[0];
	}

	public Object getParent(Object element) {
		if(element instanceof ImportElement) {
			return imports;
		}
		if(element instanceof EspPart) {
			return ((EspPart) element).getParent();
		}
		if(element instanceof Imports) {
			return imports;
		}
		return null;
	}

	public boolean hasChildren(Object element) {
		if(element instanceof MarkupElement) {
			return ((MarkupElement) element).hasChildren();
		}
		if(element instanceof JavaElement) {
			return ((JavaElement) element).hasChildren();
		}
		if(element instanceof StyleElement) {
			return ((StyleElement) element).hasChildren();
		}
		if(element instanceof Imports) {
			return ((Imports) element).hasChildren();
		}
		return false;
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

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				viewer.refresh();
			}
		});
	}
	
	public void setSort(boolean sort) {
		if(this.sort != sort) {
			this.sort = sort;
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					viewer.refresh();
				}
			});
		}
	}
	
	private <T> List<T> sorted(List<T> list) {
		if(sort) {
			Collections.sort(list, sorter);
		}
		return list;
	}
	
}
