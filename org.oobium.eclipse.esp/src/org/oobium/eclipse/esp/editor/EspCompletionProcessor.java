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

import static org.oobium.build.esp.EspPart.Type.*;
import static org.oobium.build.esp.EspPart.Type.DOM;
import static org.oobium.build.esp.EspPart.Type.ImportElement;

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ContextInformation;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;
import org.oobium.build.esp.Constants;
import org.oobium.build.esp.ESourceFile;
import org.oobium.build.esp.EspDom;
import org.oobium.build.esp.EspElement;
import org.oobium.build.esp.EspPart;
import org.oobium.build.esp.EspPart.Type;
import org.oobium.build.esp.elements.StyleChildElement;
import org.oobium.eclipse.esp.EspCore;
import org.oobium.eclipse.esp.EspPlugin;
import org.oobium.eclipse.esp.EssCore;
import org.oobium.eclipse.esp.editor.completions.ContextInformationValidator;
import org.oobium.eclipse.esp.editor.completions.EspCompletionProposal;
import org.oobium.eclipse.esp.editor.completions.EspCompletionProposalComparator;
import org.oobium.eclipse.esp.editor.completions.EspJavaProposalCollector;
import org.oobium.eclipse.esp.editor.completions.MarkupEditorMessages;

public class EspCompletionProcessor implements IContentAssistProcessor {

	private EspEditor editor;
	protected IContextInformationValidator validator = new ContextInformationValidator();
	
	public EspCompletionProcessor(EspEditor editor) {
		this.editor = editor;
	}
	
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset) {
		IDocument doc = viewer.getDocument();
		EspPart part = EspCore.get(doc).getPart(offset);
		if(part != null) {
			List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();
			if(part.isA(DOM) && offset > 0) {
				EspPart prev = part.getPart(offset-1);
				if(prev != null && !prev.isA(DOM)) {
					part = prev;
				}
			}
			EspElement element = part.getElement();
			if(element == null) {
				if(part.isA(DOM)) {
					proposals.addAll(computeDomProposals(doc, part, offset));
				}
			} else {
				switch(element.getType()) {
				case MarkupCommentElement:
				case MarkupElement:
					proposals.addAll(computeMarkupProposals(doc, element, part, offset));
					break;
				case ConstructorElement:
				case ImportElement:
				case JavaElement:
					proposals.addAll(computeJavaProposals(doc, element, part, offset));
					break;
				case StyleElement:
					if(part.isA(TagPart)) {
						proposals.addAll(computeMarkupProposals(doc, element, part, offset));
						break;
					} // else, fall through
				case StyleChildElement:
					proposals.addAll(computeStyleProposals(element, part, offset));
					break;
				default:
					switch(part.getType()) {
					case TagPart:
						proposals.addAll(computeMarkupProposals(doc, element, part, offset));
						break;
					case JavaPart:
					case JavaSourcePart:
						proposals.addAll(computeJavaProposals(doc, element, part, offset));
						break;
					}
					break;
				}
			}
			return sorted(proposals);
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
	
	private List<ICompletionProposal> computeCssClassProposals(EspPart part, int offset) {
		return computeCssSelectorProposals(part, offset, ".", EssCore.getCssClasses(editor.getProject(), part.getDom()));
	}
	
	private List<ICompletionProposal> computeCssIdProposals(EspPart part, int offset) {
		return computeCssSelectorProposals(part, offset, "#", EssCore.getCssIds(editor.getProject(), part.getDom()));
	}

	private List<ICompletionProposal> computeCssPropertyNameProposals(EspPart part, int offset) {
		if(part.isA(Type.StylePropertyNamePart)) {
			List<ICompletionProposal> results = new ArrayList<ICompletionProposal>();
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
				for(String tag : tags) {
					int rlength = part.getLength();
					int length = tag.length();
					ICompletionProposal proposal = 
						new EspCompletionProposal(
								tag, 
								offset - prefix.length(), 
								rlength, 
								length, 
								null, 
								tag, 
								null, 
								Constants.CSS_PROPERTIES.get(tag)
							);
					results.add(proposal);
				}
			}
			results.addAll(computeMixinProposals(part, offset));
			return results;
		} else if(part.isA(Type.StyleEntryPart)) {
			EspDom dom = part.getDom();
			int i = offset-1;
			while(i >= 0 && dom.charAt(i) != '"' && Character.isWhitespace(dom.charAt(i))) {
				i--;
			}
			char c = dom.charAt(i);
			if(c == '{' || c == '"' || c == ';') {
				List<ICompletionProposal> results = new ArrayList<ICompletionProposal>();
				for(String tag : Constants.CSS_PROPERTIES.keySet()) {
					results.add(new EspCompletionProposal(tag, offset, 0, tag.length(), null, tag, null, Constants.CSS_PROPERTIES.get(tag)));
				}
				return results;
			}
		} else {
			EspDom dom = part.getDom();
			int i = offset-1;
			while(i >= 0 && dom.charAt(i) != '\n' && Character.isWhitespace(dom.charAt(i))) {
				i--;
			}
			char c = dom.charAt(i);
			if(c == '{' || c == '\n' || c == ';') {
				List<ICompletionProposal> results = new ArrayList<ICompletionProposal>();
				for(String tag : Constants.CSS_PROPERTIES.keySet()) {
					results.add(new EspCompletionProposal(tag, offset, 0, tag.length(), null, tag, null, Constants.CSS_PROPERTIES.get(tag)));
				}
				return results;
			}
		}
		return new ArrayList<ICompletionProposal>(0);
	}
	
	private List<ICompletionProposal> computeCssSelectorProposals(EspPart part, int offset, String prefix, List<EspPart> selectors) {
		if(selectors.isEmpty()) {
			return new ArrayList<ICompletionProposal>(0);
		}
		
		int startIndex = offset;
		int endIndex = offset - part.getStart();
		if(endIndex > 0) {
			if(part.isA(Type.ClassPart) || part.isA(Type.IdPart)) {
				prefix += part.getText().substring(0, endIndex);
			}
			for(Iterator<EspPart> iter = selectors.iterator(); iter.hasNext(); ) {
				if(!iter.next().startsWith(prefix)) {
					iter.remove();
				}
			}
			startIndex = startIndex - prefix.length() + 1;
		}

		if(selectors.isEmpty()) {
			return new ArrayList<ICompletionProposal>(0);
		} else {
			List<ICompletionProposal> results = new ArrayList<ICompletionProposal>();
			for(EspPart selector : selectors) {
				String rstr = selector.substring(1);
				int rlength = part.getLength();
				int length = rstr.length();

				StyledString dstr = new StyledString(selector.getText());
				dstr.append(" - " + selector.getDom().getFileName(), StyledString.QUALIFIER_STYLER);

				StringBuilder info = new StringBuilder();
				info.append(selector.getText()).append(" {");
				for(StyleChildElement prop : ((StyleChildElement) selector.getParent()).getProperties()) {
					info.append("\n  ").append(prop);
				}
				info.append("\n}");

				EspCompletionProposal proposal =
					new EspCompletionProposal(
							rstr,
							startIndex,
							rlength,
							length,
							null,
							dstr,
							null,
							info.toString()
						);
				proposal.setAutoInsertable(false);
				results.add(proposal);
			}
			return results;
		}
	}
	
	private List<ICompletionProposal> computeDomProposals(IDocument doc, EspPart part, int offset) {
		EspDom dom = part.getDom();
		String imp = "import";
		String ctor = dom.getName();
		List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();
		try {
			int lineStart = doc.getLineOffset(doc.getLineOfOffset(offset));
			if(dom.hasParts()) {
				EspPart next = dom.getNextSubPart(offset);
				if(next != null) {
					if(next.getElement().isA(ImportElement)) {
						if(offset == lineStart) {
							Image image = JavaUI.getSharedImages().getImage(ISharedImages.IMG_OBJS_PACKAGE);
							EspCompletionProposal proposal = new EspCompletionProposal("import ", offset, 0, 7, image, "import", null, "add a new import statement");
							proposal.setRelevance(100);
							proposals.add(proposal);
						}
						return proposals;
					}
					if(next.getElement().isA(ConstructorElement)) {
						if(offset == lineStart) {
							Image image = JavaUI.getSharedImages().getImage(ISharedImages.IMG_OBJS_PACKAGE);
							EspCompletionProposal proposal = new EspCompletionProposal("import ", offset, 0, 7, image, "import", null, "add a new import statement");
							proposal.setRelevance(100);
							proposals.add(proposal);
							image = EspPlugin.getImage(EspPlugin.IMG_CTOR);
							proposal = new EspCompletionProposal(ctor, offset, 0, ctor.length(), image, ctor, null, "add a new constructor");
							proposal.setRelevance(90);
							proposals.add(proposal);
						}
						return proposals;
					}
				}
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
			if(offset == lineStart) {
				if(imp != null) {
					Image image = JavaUI.getSharedImages().getImage(ISharedImages.IMG_OBJS_PACKAGE);
					EspCompletionProposal proposal = new EspCompletionProposal("import ", offset, 0, 7, image, "import", null, "add a new import statement");
					proposal.setRelevance(100);
					proposals.add(proposal);
				}
				if(ctor != null) {
					Image image = EspPlugin.getImage(EspPlugin.IMG_CTOR);
					EspCompletionProposal proposal = new EspCompletionProposal(ctor, offset, 0, ctor.length(), image, ctor, null, "add a new constructor");
					proposal.setRelevance(90);
					proposals.add(proposal);
				}
				for(String tag : Constants.HTML_TAGS.keySet()) {
					Image image = EspPlugin.getImage(EspPlugin.IMG_HTML_TAG);
					EspCompletionProposal proposal = new EspCompletionProposal(tag, offset, 0, tag.length(), image, tag, null, Constants.HTML_TAGS.get(tag));
					proposal.setRelevance(50);
					proposals.add(proposal);
				}
			} else {
				int i = offset - 1;
				while(i >= 0 && dom.charAt(i) != '\n' && Character.isWhitespace(dom.charAt(i))) {
					i--;
				}
				char c = dom.charAt(i);
				if(c == '\n' || (c == '-' && i > 0 && dom.charAt(i-1) == '<')) {
					List<ICompletionProposal> results = new ArrayList<ICompletionProposal>();
					Image image = EspPlugin.getImage(EspPlugin.IMG_HTML_TAG);
					for(String tag : Constants.HTML_TAGS.keySet()) {
						results.add(new EspCompletionProposal(tag, offset, 0, tag.length(), image, tag, null, Constants.HTML_TAGS.get(tag)));
					}
					return results;
				} else {
					String s = dom.substring(lineStart, offset).toLowerCase();
					if(imp != null && "import".startsWith(s)) {
						Image image = JavaUI.getSharedImages().getImage(ISharedImages.IMG_OBJS_PACKAGE);
						proposals.add(new EspCompletionProposal("import ", lineStart, offset-lineStart, 7, image, "import", null, "add a new import statement"));
					} else if(ctor != null && ctor.toLowerCase().startsWith(s)) {
						Image image = EspPlugin.getImage(EspPlugin.IMG_CTOR);
						proposals.add(new EspCompletionProposal(ctor+"()", lineStart, offset-lineStart, ctor.length()+1, image, ctor, null, "add a new constructor"));
					}
				}
			}
		} catch(BadLocationException e) {
			e.printStackTrace();
		}

		return proposals;
	}

	private List<ICompletionProposal> computeJavaProposals(IDocument doc, EspElement element, EspPart part, int espOffset) {
		EspDom dom = part.getDom();
		int i = espOffset - 1;
		while(i >= part.getStart() && Character.isJavaIdentifierPart(dom.charAt(i))) {
			i--;
		}
		if(i > part.getStart() && Character.isJavaIdentifierStart(dom.charAt(i))) {
			i--;
		}
		int start = i + 1;
		ESourceFile jf = editor.getEspJavaFile();
		int javaOffset = jf.getJavaOffset(espOffset);
		
		InputStream in = null;
		try {
			in = editor.getJavaResource().getContents(true);
			i = 0;
			char c;
			while((c = (char) in.read()) != -1) {
				if(i == javaOffset) {
					System.out.print('{');
					System.out.print(c);
					System.out.print('}');
				}
				else if(i > javaOffset-20 && i < javaOffset+20) {
					System.out.print(c);
				}
				else if(i > javaOffset+20) {
					break;
				}
				i++;
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		try {
			if(in != null) in.close();
		} catch(IOException e1) {
			e1.printStackTrace();
		}
		
		ICompilationUnit cu = (ICompilationUnit) JavaCore.create(editor.getJavaResource());
		
		try {
			EspJavaProposalCollector collector = new EspJavaProposalCollector(cu, start);
			cu.codeComplete(javaOffset, collector);
			ICompletionProposal[] proposals = collector.getJavaCompletionProposals();
			return Arrays.asList(proposals);
		} catch (JavaModelException e) {
			e.printStackTrace();
		}

		return new ArrayList<ICompletionProposal>(0);
	}

	private List<ICompletionProposal> computeMarkupProposals(IDocument doc, EspElement element, EspPart part, int offset) {
		switch(part.getType()) {
		case ClassPart:
			return computeCssClassProposals(part, offset);
		case IdPart:
			return computeCssIdProposals(part, offset);
		case JavaPart:
		case JavaSourcePart:
		case JavaTypePart:
			return computeJavaProposals(doc, element, part, offset);
		case MarkupElement:
			if(offset > 0) {
				char c = part.getDom().charAt(offset-1);
				switch(c) {
				case '.': return computeCssClassProposals(part, offset);
				case '#': return computeCssIdProposals(part, offset);
				// default: fall through
				}
				if(!Character.isWhitespace(c)) {
					EspPart prev = part.getPart(offset-1);
					if(prev != part) {
						return computeMarkupProposals(doc, element, prev, offset);
					} // else, fall through
				}
			} // else, fall through
		case TagPart:
			return computeMarkupTagProposals(doc, part, offset);
		case StyleElement:
		case StyleChildElement:
			return computeStyleProposals(element, part, offset);
		case StylePart:
			return new ArrayList<ICompletionProposal>(0); // TODO style part completion
		case StyleEntryPart:
		case StylePropertyPart:
		case StylePropertyNamePart:
			return computeCssPropertyNameProposals(part, offset);
		}
		return new ArrayList<ICompletionProposal>(0);
	}

	private List<ICompletionProposal> computeMarkupTagProposals(IDocument doc, EspPart part, int offset) {
		EspDom dom = part.getDom();
		if(part.isA(Type.TagPart)) {
			List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();
			int endIndex = offset - part.getStart();
			String prefix = part.getText().substring(0, endIndex);

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
				while(start < part.getEnd() && Character.isWhitespace(dom.charAt(start))) {
					start++;
				}
				String s = dom.substring(start, offset).toLowerCase();
				if(imp != null && "import".startsWith(s)) {
					Image image = JavaUI.getSharedImages().getImage(ISharedImages.IMG_OBJS_PACKAGE);
					proposals.add(new EspCompletionProposal("import ", start, offset-start, 7, image, "import", null, "add a new import statement"));
				} else if(ctor != null && ctor.toLowerCase().startsWith(s)) {
					Image image = EspPlugin.getImage(EspPlugin.IMG_CTOR);
					proposals.add(new EspCompletionProposal(ctor+"()", start, offset-start, ctor.length()+1, image, ctor, null, "add a new constructor"));
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
				Image image = EspPlugin.getImage(EspPlugin.IMG_HTML_TAG);
				for(String tag : tags) {
					int rlength = part.getLength();
					int length = tag.length();
					ICompletionProposal proposal = 
						new EspCompletionProposal(tag, offset - prefix.length(), rlength, length, image, tag, null, Constants.HTML_TAGS.get(tag));
					proposals.add(proposal);
				}
			}
			if(!proposals.isEmpty()) {
				return proposals;
			}
		} else {
			
			int i = offset-1;
			while(i >= 0 && dom.charAt(i) != '\n' && Character.isWhitespace(dom.charAt(i))) {
				i--;
			}
			char c = dom.charAt(i);
			if(c == '\n' || (c == '-' && i > 0 && dom.charAt(i-1) == '<')) {
				Image image = EspPlugin.getImage(EspPlugin.IMG_HTML_TAG);
				List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();
				for(String tag : Constants.HTML_TAGS.keySet()) {
					proposals.add(new EspCompletionProposal(tag, offset, 0, tag.length(), image, tag, null, Constants.HTML_TAGS.get(tag)));
				}
				return proposals;
			}
		}
		return new ArrayList<ICompletionProposal>(0);
	}

	private List<ICompletionProposal> computeMixinProposals(EspPart part, int offset) {
		List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();
		if(part.charAt(0) == '.') {
			proposals.addAll(computeCssClassProposals(part, offset));
		}
		if(part.charAt(0) == '#') {
			proposals.addAll(computeCssIdProposals(part, offset));
		}
		return proposals;
	}

	private List<ICompletionProposal> computeStyleProposals(EspElement element, EspPart part, int offset) {
		switch(part.getType()) {
		case StyleMixinNamePart:
			return computeMixinProposals(part, offset);
		case StyleElement:
		case StylePropertyPart:
			EspPart prev = part.getPart(offset-1);
			if(prev.isA(StyleMixinNamePart)) {
				return computeMixinProposals(prev, offset);
			} // else, fall through
			if(prev.isA(StylePropertyNamePart)) {
				return computeCssPropertyNameProposals(prev, offset);
			} // else, fall through
		case StyleChildElement:
		case StylePropertyNamePart:
			return computeCssPropertyNameProposals(part, offset);
		}
		return new ArrayList<ICompletionProposal>(0);
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
	
	private ICompletionProposal[] sorted(List<ICompletionProposal> proposals) {
		ICompletionProposal[] pa = proposals.toArray(new ICompletionProposal[proposals.size()]);
		Arrays.sort(pa, new EspCompletionProposalComparator());
		return pa;
	}
	
}
