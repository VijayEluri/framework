package org.oobium.eclipse.esp.editor.completions;

import java.lang.reflect.Field;

import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.LazyJavaTypeCompletionProposal;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.TextEdit;
import org.oobium.eclipse.esp.EspCore;

@SuppressWarnings("restriction")
public class EspJavaTypeCompletionProposal extends LazyJavaTypeCompletionProposal {

	private boolean addImport;
	
	public EspJavaTypeCompletionProposal(CompletionProposal proposal, JavaContentAssistInvocationContext context) {
		super(proposal, context);
	}

	@Override
	protected String computeReplacementString() {
		String s = super.computeReplacementString();
		try {
			Field f = LazyJavaTypeCompletionProposal.class.getDeclaredField("fImportRewrite");
			f.setAccessible(true);
			addImport = (f.get(this) != null);
			f.set(this, null);
		} catch(Exception e) {
			e.printStackTrace();
		}
		return s;
	}
	
	@Override
	public void apply(IDocument document, char trigger, int offset) {
		int charsFromEnd = 0;
		String replacement = getReplacementString();
		if("List".equals(replacement)) {
			setReplacementString("List<>");
			charsFromEnd = 1;
		}
		super.apply(document, trigger, offset);
		if(addImport && super.allowAddingImports()) {
			try {
				int importOffset = EspCore.get(document).getNextImportOffset();
				String txt = "import " + getQualifiedTypeName() + "\n";
				if(importOffset < document.getLength() && document.getChar(importOffset) != '\n') {
					txt = txt + "\n";
				}
				TextEdit edit = new InsertEdit(importOffset, txt);
				edit.apply(document);
				setReplacementOffset(getReplacementOffset() + txt.length() - charsFromEnd);
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}

}
