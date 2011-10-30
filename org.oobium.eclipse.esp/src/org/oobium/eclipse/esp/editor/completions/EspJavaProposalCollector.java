package org.oobium.eclipse.esp.editor.completions;

import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.internal.ui.text.java.AbstractJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.CompletionProposalCollector;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;

@SuppressWarnings("restriction")
public class EspJavaProposalCollector extends CompletionProposalCollector {

	private final int replacementOffset;
	
	public EspJavaProposalCollector(ICompilationUnit cu, int replacementOffset) {
		super(cu);
		this.replacementOffset = replacementOffset;
	}

	@Override
	protected IJavaCompletionProposal createJavaCompletionProposal(CompletionProposal proposal) {
		if(proposal.getKind() == CompletionProposal.TYPE_REF) {
			EspJavaTypeCompletionProposal p = new EspJavaTypeCompletionProposal(proposal, getInvocationContext());
			p.setReplacementOffset(replacementOffset);
			p.setReplacementLength(getLength(proposal));
			return p;
		} else {
			IJavaCompletionProposal jp = super.createJavaCompletionProposal(proposal);
			if(jp instanceof AbstractJavaCompletionProposal) {
				((AbstractJavaCompletionProposal) jp).setReplacementOffset(replacementOffset);
			}
			return jp;
		}
	}
	
}
