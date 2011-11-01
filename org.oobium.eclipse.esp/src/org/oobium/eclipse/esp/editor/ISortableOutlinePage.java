package org.oobium.eclipse.esp.editor;

import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

public interface ISortableOutlinePage extends IContentOutlinePage {

	public abstract void setInput(IDocument document);

	public abstract void setSort(boolean sort);

}
