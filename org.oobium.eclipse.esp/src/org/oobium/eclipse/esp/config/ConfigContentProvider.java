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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
import org.oobium.utils.Config.Mode;
import org.oobium.utils.json.JsonUtils;

/**
 * Divides the editor's document into ten segments and provides elements for them.
 */
class ConfigContentProvider implements ITreeContentProvider, PropertyChangeListener {

	private Comparator<Object> sorter = new Comparator<Object>() {
		private ConfigLabelProvider lp = new ConfigLabelProvider();
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
	
	private boolean sort;
	private boolean merge;
	
	public ConfigContentProvider(boolean sort, boolean merge) {
		this.sort = sort;
		this.merge = merge;
	}
	
	public void dispose() {
		if(document != null) {
//			EspCore.get(document).removeListener(this);
		}
	}

	public Object[] getChildren(Object element) {
		if(element instanceof Map) {
			return sorted(((Map<?,?>) element).entrySet().toArray());
		}
		if(element instanceof ConfigMode) {
			return sorted(((ConfigMode) element).config.entrySet().toArray());
		}
		if(element instanceof List) {
			return sorted(((List<?>) element).toArray());
		}
		if(element instanceof Entry) {
			return getChildren(((Entry<?,?>) element).getValue());
		}
		return new Object[0];
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object[] getElements(Object element) {
		if(element instanceof IDocument) {
			Map config = JsonUtils.toMap(document.get(), true);
			if(merge) {
				ConfigMode dev = new ConfigMode(Mode.DEV, config);
				ConfigMode test = new ConfigMode(Mode.TEST, config);
				ConfigMode prod = new ConfigMode(Mode.PROD, config);
				dev.config.putAll(config);
				test.config.putAll(config);
				prod.config.putAll(config);
				return new Object[] { dev, test, prod };
			} else {
				ConfigMode global = new ConfigMode(config);
				ConfigMode dev = new ConfigMode(Mode.DEV, config);
				ConfigMode test = new ConfigMode(Mode.TEST, config);
				ConfigMode prod = new ConfigMode(Mode.PROD, config);
				return new Object[] { global, dev, test, prod };
			}
		}
		return new Object[0];
	}

	public Object getParent(Object element) {
		return null;
	}

	public boolean hasChildren(Object element) {
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
	
	public void setMerge(boolean merge) {
		if(this.merge != merge) {
			this.merge = merge;
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					viewer.refresh();
				}
			});
		}
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
	
	private Object[] sorted(Object[] array) {
		if(sort) {
			Arrays.sort(array, sorter);
		}
		return array;
	}

}
