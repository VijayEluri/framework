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
package org.oobium.eclipse.esp;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.oobium.build.esp.EspDom;

public class EspCore {

	private static EspCore instance = new EspCore();
	
	public static EspDom get(IDocument document) {
		return instance.getDom(document);
	}

	public static EspDom remove(IDocument document) {
		return instance.removeDom(document);
	}
	
	
	private Map<IDocument, EspDom> domMap;
	private Set<IDocument> changed;
	private IDocumentListener listener;
	
	private EspCore() {
		domMap = new HashMap<IDocument, EspDom>();
		changed = new HashSet<IDocument>();
		listener = new IDocumentListener() {
			@Override
			public void documentAboutToBeChanged(DocumentEvent event) {
				addChanged(event.getDocument());
			}
			@Override
			public void documentChanged(DocumentEvent event) {
				// nothing to do
			}
		};
	}

	private synchronized void addChanged(IDocument document) {
		changed.add(document);
	}
	
	private synchronized EspDom getDom(IDocument document) {
		EspDom dom = domMap.get(document);
		if(dom == null) {
			dom = new EspDom(null, document.get());
			document.addPrenotifiedDocumentListener(listener);
			domMap.put(document, dom);
		} else {
			if(changed.remove(document)) {
				dom.setSource(document.get());
			}
		}
		return dom;
	}
	
	private synchronized EspDom removeDom(IDocument document) {
		document.removeDocumentListener(listener);
		changed.remove(document);
		return domMap.remove(document);
	}

}
