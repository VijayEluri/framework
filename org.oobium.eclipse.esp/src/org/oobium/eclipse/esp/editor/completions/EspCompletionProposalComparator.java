package org.oobium.eclipse.esp.editor.completions;

import java.util.Comparator;

import org.eclipse.jdt.internal.ui.text.java.AbstractJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.templates.TemplateProposal;

@SuppressWarnings("restriction")
public class EspCompletionProposalComparator implements Comparator<ICompletionProposal> {

	public int compare(ICompletionProposal p1, ICompletionProposal p2) {
		int r1 = getRelevance(p1);
		int r2 = getRelevance(p2);
		int diff = r2 - r1;
		if(diff != 0) {
			return diff;
		}
		boolean o1 = p1.toString().contains(" org.oobium.");
		boolean o2 = p2.toString().contains(" org.oobium.");
		if(o1 == o2) {
			return getSortKey(p1).compareToIgnoreCase(getSortKey(p2));
		}
		if(o1) return -1;
		return 1;
	}

	private String getSortKey(ICompletionProposal p) {
		if(p instanceof AbstractJavaCompletionProposal) {
			return ((AbstractJavaCompletionProposal) p).getSortString();
		}
		return p.getDisplayString();
	}

	private int getRelevance(ICompletionProposal proposal) {
		if(proposal instanceof IJavaCompletionProposal) {
			return ((IJavaCompletionProposal) proposal).getRelevance();
		}
		else if(proposal instanceof TemplateProposal) {
			return ((TemplateProposal) proposal).getRelevance();
		}
		else if(proposal instanceof EspCompletionProposal) {
			return ((EspCompletionProposal) proposal).getRelevance();
		}
		return 0;
	}

}
