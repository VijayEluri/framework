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
package org.oobium.eclipse.esp.editor;

import static org.oobium.build.esp.EspPart.Type.ConstructorElement;
import static org.oobium.build.esp.EspPart.Type.DOM;
import static org.oobium.build.esp.EspPart.Type.ImportElement;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.CompletionRequestor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ContextInformation;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.oobium.build.esp.Constants;
import org.oobium.build.esp.EspDom;
import org.oobium.build.esp.EspElement;
import org.oobium.build.esp.ESourceFile;
import org.oobium.build.esp.EspPart;
import org.oobium.build.esp.EspPart.Type;
import org.oobium.eclipse.esp.EspCore;
import org.oobium.eclipse.esp.editor.completions.ContextInformationValidator;
import org.oobium.eclipse.esp.editor.completions.HtmlCompletionProposal;
import org.oobium.eclipse.esp.editor.completions.MarkupEditorMessages;

public class EspCompletionProcessor implements IContentAssistProcessor {

	protected IContextInformationValidator validator = new ContextInformationValidator();

	private EspEditor editor;
	
	public EspCompletionProcessor(EspEditor editor) {
		this.editor = editor;
	}
	
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset) {
		IDocument doc = viewer.getDocument();
		EspPart part = EspCore.get(doc).getPart(offset-1);
		if(part != null) {
			EspElement element = part.getElement();
			if(element == null) {
				if(part.isA(DOM)) return computeDomProposals(doc, part, offset);
			} else {
				switch(element.getType()) {
				case ConstructorElement:
					return computeConstructorProposals(doc, element, part, offset);
				case HtmlElement:
					return computeHtmlProposals(doc, element, part, offset);
				case ImportElement:
					return computeImportProposals(doc, element, part, offset);
				case JavaElement:
					return computeJavaProposals(doc, element, part, offset);
				case StyleElement:
				case StyleChildElement:
					return computeStyleProposals(element, part, offset);
				}
			}
		}
		return new ICompletionProposal[0];
	}

	private ICompletionProposal[] computeConstructorProposals(IDocument doc, EspElement element, EspPart part, int offset) {
		switch(part.getType()) {
		case CtorArgPart:
		case VarTypePart:
		case VarNamePart:
		case DefaultValuePart:
			return computeJavaProposals(doc, element, part, offset);
		}
		return new ICompletionProposal[0];
	}
	
	public IContextInformation[] computeContextInformation(ITextViewer viewer, int documentOffset) {
		IContextInformation[] result= new IContextInformation[5];
		for (int i= 0; i < result.length; i++)
			result[i]= new ContextInformation(
				MessageFormat.format(MarkupEditorMessages.getString("CompletionProcessor.ContextInfo.display.pattern"), new Object[] { new Integer(i), new Integer(documentOffset) }),  //$NON-NLS-1$
				MessageFormat.format(MarkupEditorMessages.getString("CompletionProcessor.ContextInfo.value.pattern"), new Object[] { new Integer(i), new Integer(documentOffset - 5), new Integer(documentOffset + 5)})); //$NON-NLS-1$
		return result;
	}

	private ICompletionProposal[] computeCssPropertyNameProposals(EspPart part, int offset) {
		if(part.isA(Type.StylePropertyNamePart)) {
			Set<String> tags = new TreeSet<String>(Constants.CSS_PROPERTIES.keySet());
			int endIndex = offset - part.getStart();
			String prefix = part.getText().substring(0, endIndex);
			if(endIndex > 0) {
				for(Iterator<String> iter = tags.iterator(); iter.hasNext(); ) {
					if(!iter.next().startsWith(prefix)) {
						iter.remove();
					}
				}
			}
			if(!tags.isEmpty()) {
				List<ICompletionProposal> results = new ArrayList<ICompletionProposal>();
				for(String tag : tags) {
					int rlength = part.getLength();
					int length = tag.length();
					ICompletionProposal proposal = 
						new HtmlCompletionProposal(tag, offset - prefix.length(), rlength, length, null, tag, null, Constants.CSS_PROPERTIES.get(tag));
					results.add(proposal);
				}
				return results.toArray(new ICompletionProposal[results.size()]);
			}
		} else if(part.isA(Type.StyleEntryPart)) {
			int i = offset-1;
			while(i >= 0 && part.charAt(i) != '"' && Character.isWhitespace(part.charAt(i))) {
				i--;
			}
			char c = part.charAt(i);
			if(c == '{' || c == '"' || c == ';') {
				List<ICompletionProposal> results = new ArrayList<ICompletionProposal>();
				for(String tag : Constants.CSS_PROPERTIES.keySet()) {
					results.add(new HtmlCompletionProposal(tag, offset, 0, tag.length(), null, tag, null, Constants.CSS_PROPERTIES.get(tag)));
				}
				return results.toArray(new ICompletionProposal[results.size()]);
			}
		} else {
			int i = offset-1;
			while(i >= 0 && part.charAt(i) != '\n' && Character.isWhitespace(part.charAt(i))) {
				i--;
			}
			char c = part.charAt(i);
			if(c == '{' || c == '\n' || c == ';') {
				List<ICompletionProposal> results = new ArrayList<ICompletionProposal>();
				for(String tag : Constants.CSS_PROPERTIES.keySet()) {
					results.add(new HtmlCompletionProposal(tag, offset, 0, tag.length(), null, tag, null, Constants.CSS_PROPERTIES.get(tag)));
				}
				return results.toArray(new ICompletionProposal[results.size()]);
			}
		}
		return new ICompletionProposal[0];
	}

	private ICompletionProposal[] computeDomProposals(IDocument doc, EspPart part, int offset) {
		EspDom dom = part.getDom();
		String ctor = dom.getName();
		String imp = "import";
		if(dom.hasParts()) {
			int i = 0;
			List<EspPart> parts = dom.getParts();
			while(i < parts.size() && parts.get(i).getEnd() < offset) {
				if(!parts.get(i).isA(ImportElement)) {
					imp = null;
					break;
				}
				i++;
			}
			while(i < parts.size() && parts.get(i).getEnd() < offset) {
				if(!parts.get(i).isA(ConstructorElement)) {
					ctor = null;
					break;
				}
				i++;
			}
		}
		List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();
		try {
			int lineStart = doc.getLineOffset(doc.getLineOfOffset(offset));
			if(offset == lineStart) {
				if(imp != null) {
					proposals.add(new HtmlCompletionProposal("import ", offset, 0, 7, null, "import", null, "add a new import statement"));
				}
				if(ctor != null) {
					proposals.add(new HtmlCompletionProposal(ctor, offset, 0, ctor.length(), null, ctor, null, "add a new constructor"));
				}
				for(String tag : Constants.HTML_TAGS.keySet()) {
					proposals.add(new HtmlCompletionProposal(tag, offset, 0, tag.length(), null, tag, null, Constants.HTML_TAGS.get(tag)));
				}
			} else {
				int i = offset - 1;
				while(i >= 0 && part.charAt(i) != '\n' && Character.isWhitespace(part.charAt(i))) {
					i--;
				}
				char c = part.charAt(i);
				if(c == '\n' || (c == '-' && i > 0 && part.charAt(i-1) == '<')) {
					List<ICompletionProposal> results = new ArrayList<ICompletionProposal>();
					for(String tag : Constants.HTML_TAGS.keySet()) {
						results.add(new HtmlCompletionProposal(tag, offset, 0, tag.length(), null, tag, null, Constants.HTML_TAGS.get(tag)));
					}
					return results.toArray(new ICompletionProposal[results.size()]);
				} else {
					String s = part.substring(lineStart, offset).toLowerCase();
					if(imp != null && "import".startsWith(s)) {
						proposals.add(new HtmlCompletionProposal("import ", lineStart, offset-lineStart, 7, null, "import", null, "add a new import statement"));
					} else if(ctor != null && ctor.toLowerCase().startsWith(s)) {
						proposals.add(new HtmlCompletionProposal(ctor+"()", lineStart, offset-lineStart, ctor.length()+1, null, ctor, null, "add a new constructor"));
					}
				}
			}
		} catch(BadLocationException e) {
			e.printStackTrace();
		}

		return proposals.toArray(new ICompletionProposal[proposals.size()]);
	}

	private ICompletionProposal[] computeHtmlProposals(IDocument doc, EspElement element, EspPart part, int offset) {
		switch(part.getType()) {
		case JavaPart:
		case JavaSourcePart:
			return computeJavaProposals(doc, element, part, offset);
		case TagPart:
			return computeHtmlTagProposals(doc, part, offset);
		case StylePart:
			return new ICompletionProposal[0]; // TODO style part completion
		case StyleEntryPart:
		case StylePropertyPart:
		case StylePropertyNamePart:
			return computeCssPropertyNameProposals(part, offset);
		}
		return new ICompletionProposal[0];
	}

	private ICompletionProposal[] computeHtmlTagProposals(IDocument doc, EspPart part, int offset) {
		if(part.isA(Type.TagPart)) {
			List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();
			int endIndex = offset - part.getStart();
			String prefix = part.getText().substring(0, endIndex);

			EspDom dom = part.getDom();
			String ctor = dom.getName();
			String imp = "import";
			if(dom.hasParts()) {
				int i = 0;
				List<EspPart> parts = dom.getParts();
				while(i < parts.size() && parts.get(i).getEnd() < offset) {
					if(!parts.get(i).isA(ImportElement)) {
						imp = null;
						break;
					}
					i++;
				}
				while(i < parts.size() && parts.get(i).getEnd() < offset) {
					if(!parts.get(i).isA(ConstructorElement)) {
						ctor = null;
						break;
					}
					i++;
				}
			}

			try {
				int start = doc.getLineOffset(doc.getLineOfOffset(offset));
				while(start < part.getEnd() && Character.isWhitespace(part.charAt(start))) {
					start++;
				}
				String s = part.substring(start, offset).toLowerCase();
				if(imp != null && "import".startsWith(s)) {
					proposals.add(new HtmlCompletionProposal("import ", start, offset-start, 7, null, "import", null, "add a new import statement"));
				} else if(ctor != null && ctor.toLowerCase().startsWith(s)) {
					proposals.add(new HtmlCompletionProposal(ctor+"()", start, offset-start, ctor.length()+1, null, ctor, null, "add a new constructor"));
				}
			} catch(BadLocationException e) {
				e.printStackTrace();
			}
			
			Set<String> tags = new TreeSet<String>(Constants.HTML_TAGS.keySet());
			if(endIndex > 0) {
				for(Iterator<String> iter = tags.iterator(); iter.hasNext(); ) {
					if(!iter.next().startsWith(prefix)) {
						iter.remove();
					}
				}
			}
			if(!tags.isEmpty()) {
				for(String tag : tags) {
					int rlength = part.getLength();
					int length = tag.length();
					ICompletionProposal proposal = 
						new HtmlCompletionProposal(tag, offset - prefix.length(), rlength, length, null, tag, null, Constants.HTML_TAGS.get(tag));
					proposals.add(proposal);
				}
			}
			if(!proposals.isEmpty()) {
				return proposals.toArray(new ICompletionProposal[proposals.size()]);
			}
		} else {
			int i = offset-1;
			while(i >= 0 && part.charAt(i) != '\n' && Character.isWhitespace(part.charAt(i))) {
				i--;
			}
			char c = part.charAt(i);
			if(c == '\n' || (c == '-' && i > 0 && part.charAt(i-1) == '<')) {
				List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();
				for(String tag : Constants.HTML_TAGS.keySet()) {
					proposals.add(new HtmlCompletionProposal(tag, offset, 0, tag.length(), null, tag, null, Constants.HTML_TAGS.get(tag)));
				}
				return proposals.toArray(new ICompletionProposal[proposals.size()]);
			}
		}
		return new ICompletionProposal[0];
	}

	private ICompletionProposal[] computeImportProposals(IDocument doc, EspElement element, EspPart part, int espOffset) {
		int i = espOffset - 1;
		while(i >= part.getStart() && isJavaImportPart(part.charAt(i))) {
			i--;
		}
		final int start = i + 1;
		i = espOffset;
		while(i < part.getEnd() && isJavaImportPart(part.charAt(i))) {
			i++;
		}
		final int end = i;
		ESourceFile jf = editor.getEspJavaFile();
		final int javaOffset = jf.getJavaOffset(espOffset - 1);
		ICompilationUnit cu = (ICompilationUnit) JavaCore.create(editor.getJavaResource());
		final List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();
		try {
			((ICompilationUnit) cu).codeComplete(javaOffset + 1, new CompletionRequestor() {
				@Override
				public void accept(CompletionProposal completion) {
					String text;
					char[] ca = completion.getCompletion();
					if(ca.length > 0 && ca[ca.length-1] == ';') {
						text = new String(ca, 0, ca.length-1);
					} else {
						text = new String(ca);
					}
					proposals.add(new HtmlCompletionProposal(text, start, end-start, text.length(), null, text, null, completion.toString()));
				}
			});
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
		return proposals.toArray(new ICompletionProposal[proposals.size()]);
	}
	
	private ICompletionProposal[] computeJavaProposals(IDocument doc, EspElement element, EspPart part, int espOffset) {
		int i = espOffset - 1;
		while(i >= part.getStart() && Character.isLetterOrDigit(part.charAt(i))) {
			i--;
		}
		final int start = i + 1;
		i = espOffset;
		while(i < part.getEnd() && Character.isLetterOrDigit(part.charAt(i))) {
			i++;
		}
		final int end = i;
//		if(part.isA(Type.JavaSourcePart)) {
			ESourceFile jf = editor.getEspJavaFile();
			final int javaOffset = jf.getJavaOffset(espOffset - 1);
			ICompilationUnit cu = (ICompilationUnit) JavaCore.create(editor.getJavaResource());
			final List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();
			try {
				((ICompilationUnit) cu).codeComplete(javaOffset + 1, new CompletionRequestor() {
					@Override
					public void accept(CompletionProposal completion) {
						String text = new String(completion.getCompletion());
						proposals.add(new HtmlCompletionProposal(text, start, end-start, text.length(), null, text, null, completion.toString()));
					}
				});
			} catch (JavaModelException e) {
				e.printStackTrace();
			}
			return proposals.toArray(new ICompletionProposal[proposals.size()]);
//		} else {
//			List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();
//			for(String tag : Constants.HTML_TAGS.keySet()) {
//				proposals.add(new HtmlCompletionProposal(tag, start, end-start, tag.length(), null, tag, null, Constants.HTML_TAGS.get(tag)));
//			}
//			return proposals.toArray(new ICompletionProposal[proposals.size()]);
//		}
	}
	
	private ICompletionProposal[] computeStyleProposals(EspElement element, EspPart part, int offset) {
		switch(part.getType()) {
		case StyleChildElement:
		case StylePropertyPart:
		case StylePropertyNamePart:
			return computeCssPropertyNameProposals(part, offset);
		}
		return new ICompletionProposal[0];
	}
	
	public char[] getCompletionProposalAutoActivationCharacters() {
		return null;
	}
	
	public char[] getContextInformationAutoActivationCharacters() {
		return null;
	}
	
	public IContextInformationValidator getContextInformationValidator() {
		return validator;
	}
	
	public String getErrorMessage() {
		return "This is the error message";
	}
	
	private boolean isJavaImportPart(char c) {
		return c == '.' || Character.isJavaIdentifierPart(c);
	}
}
