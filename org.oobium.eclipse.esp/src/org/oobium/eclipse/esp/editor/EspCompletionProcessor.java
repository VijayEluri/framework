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

import static org.oobium.build.esp.dom.EspPart.Type.Constructor;
import static org.oobium.build.esp.dom.EspPart.Type.ImportElement;

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
import org.oobium.build.esp.compiler.ESourceFile;
import org.oobium.build.esp.dom.EspDom;
import org.oobium.build.esp.dom.EspPart;
import org.oobium.build.esp.dom.EspPart.Type;
import org.oobium.build.esp.dom.parts.MethodArg;
import org.oobium.build.esp.dom.parts.style.Property;
import org.oobium.build.esp.dom.parts.style.Selector;
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
	
	private List<ICompletionProposal> computeCompletionProposals(IDocument doc, EspPart part, int offset) {
		switch(part.getType()) {
		case DOM:               return computeDomProposals(doc, part, offset);
		case JavaElement:
		case JavaContainer:     return computeJavaProposals(doc, part, offset);
		case JavaSource:		return computeJavaSourceProposals(doc, part, offset);
		case MarkupElement:
		case MarkupComment:     return computeMarkupProposals(doc, part, offset);
		case MarkupTag:         return computeMarkupTagProposals(doc, part, offset);
		case MarkupId:          return computeCssSelectorProposals(part, offset);
		case MarkupClass:       return computeCssSelectorProposals(part, offset);
		case MethodArg:         return computeVarNameProposals(part, offset);
		case ScriptPart:		return computeDynAssetProposals(doc, part, offset);
		case StylePropertyName: return computeStylePropertyNameProposals(part, offset);
		case VarName:           return computeVarNameProposals(part, offset);
		default:                return new ArrayList<ICompletionProposal>(0);
		}
	}

	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset) {
		IDocument doc = viewer.getDocument();
		offset--;
		if(offset < 0) {
			List<ICompletionProposal> proposals = computeDomProposals(doc, EspCore.get(doc), 0);
			return sorted(proposals);
		} else {
			EspPart part = EspCore.get(doc).getPart(offset);
			if(part != null) {
				List<ICompletionProposal> proposals = computeCompletionProposals(doc, part, offset);
				return sorted(proposals);
			}
			return new ICompletionProposal[0];
		}
	}

	public IContextInformation[] computeContextInformation(ITextViewer viewer, int documentOffset) {
		IContextInformation[] result= new IContextInformation[5];
		for (int i= 0; i < result.length; i++)
			result[i]= new ContextInformation(
				MessageFormat.format(MarkupEditorMessages.getString("CompletionProcessor.ContextInfo.display.pattern"), new Object[] { new Integer(i), new Integer(documentOffset) }),  //$NON-NLS-1$
				MessageFormat.format(MarkupEditorMessages.getString("CompletionProcessor.ContextInfo.value.pattern"), new Object[] { new Integer(i), new Integer(documentOffset - 5), new Integer(documentOffset + 5)})); //$NON-NLS-1$
		return result;
	}
	
	private List<ICompletionProposal> computeCssSelectorProposals(EspPart part, int offset) {
		int endIndex = offset - part.getStart() + 1;
		if(endIndex <= 0) {
			return new ArrayList<ICompletionProposal>(0);
		}
		
		List<Selector> selectors;
		if(part.charAt(0) == '#') {
			selectors = EssCore.getCssIds(editor.getProject(), part.getDom());
		} else if(part.charAt(0) == '.') {
			selectors = EssCore.getCssClasses(editor.getProject(), part.getDom());
		} else {
			selectors = EssCore.getCssSelectors(editor.getProject(), part.getDom());
		}

		List<ICompletionProposal> results = new ArrayList<ICompletionProposal>();
		String prefix = part.substring(0, endIndex);
		for(Selector selector : selectors) {
			if(selector.startsWith(prefix)) {
				String rstr = selector.getText();
				int rlength = part.length();
				int length = rstr.length();

				StyledString dstr = new StyledString(selector.getText());
				dstr.append(" - " + selector.getDom().getName(), StyledString.QUALIFIER_STYLER);

				StringBuilder info = new StringBuilder();
				info.append(selector.getText()).append(" {");
				for(Property prop : selector.getDeclaration().getProperties()) {
					info.append("\n  ").append(prop);
				}
				info.append("\n}");

				EspCompletionProposal proposal =
					new EspCompletionProposal(rstr, part.getStart(), rlength, length, null, dstr, null, info.toString());
				proposal.setAutoInsertable(false);
				
				results.add(proposal);
			}
		}
		
		return results;
	}
	
	private List<ICompletionProposal> computeDomProposals(IDocument doc, EspPart part, int offset) {
		EspDom dom = part.getDom();
		String imp = "import";
		String ctor = dom.getSimpleName();
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
					if(next.getElement().isA(Constructor)) {
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
					if(!parts.get(i).isA(Constructor)) {
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
				for(String tag : Constants.MARKUP_TAGS.keySet()) {
					Image image = EspPlugin.getImage(EspPlugin.IMG_HTML_TAG);
					EspCompletionProposal proposal = new EspCompletionProposal(tag, offset, 0, tag.length(), image, tag, null, Constants.MARKUP_TAGS.get(tag));
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
					for(String tag : Constants.MARKUP_TAGS.keySet()) {
						results.add(new EspCompletionProposal(tag, offset, 0, tag.length(), image, tag, null, Constants.MARKUP_TAGS.get(tag)));
					}
					return results;
				} else {
					String s = dom.subSequence(lineStart, offset).toLowerCase();
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

	private List<ICompletionProposal> computeJavaProposals(IDocument doc, EspPart part, int offset) {
		EspDom dom = part.getDom();
		int i = offset;
		while(i >= part.getStart() && Character.isJavaIdentifierPart(dom.charAt(i))) {
			i--;
		}
		if(i > part.getStart() && Character.isJavaIdentifierStart(dom.charAt(i))) {
			i--;
		}
		int start = i;
		ESourceFile jf = editor.getEspJavaFile(true);
		int javaOffset = jf.getJavaOffset(offset) + 1;
		if(javaOffset == 0) {
			System.out.println("javaOffset not found");
			return new ArrayList<ICompletionProposal>(0);
		}
		
		InputStream in = null;
		try {
			in = editor.getJavaResource().getContents(true);
			i = 0;
			char c;
			while((c = (char) in.read()) != -1) {
				if(i == javaOffset) {
					System.out.print('^');
					System.out.print(c);
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
		System.out.println();
		try {
			if(in != null) in.close();
		} catch(IOException e1) {
			e1.printStackTrace();
		}
		
		ICompilationUnit cu = (ICompilationUnit) JavaCore.create(editor.getJavaResource());
		
		try {
			EspJavaProposalCollector collector = new EspJavaProposalCollector(cu, start+1);
			cu.codeComplete(javaOffset, collector);
			ICompletionProposal[] proposals = collector.getJavaCompletionProposals();
			return Arrays.asList(proposals);
		} catch (JavaModelException e) {
			e.printStackTrace();
		}

		return new ArrayList<ICompletionProposal>(0);
	}
	
	private List<ICompletionProposal> computeJavaSourceProposals(IDocument doc, EspPart part, int offset) {
		EspPart p = part.getParent().getParent();
		if(p instanceof MethodArg) {
			MethodArg arg = (MethodArg) p;
			if( ! arg.hasName() && arg.getValue() == part.getParent()) {
				List<ICompletionProposal> proposals = computeVarNameProposals(part, offset);
				proposals.addAll(computeJavaProposals(doc, part, offset));
				return proposals;
			}
		}
		return computeJavaProposals(doc, part, offset);
	}
	
	private List<ICompletionProposal> computeMarkupProposals(IDocument doc, EspPart part, int offset) {
		return new ArrayList<ICompletionProposal>(0);
	}

	private List<ICompletionProposal> computeMarkupTagProposals(IDocument doc, EspPart part, int offset) {
		EspDom dom = part.getDom();
		if(part.isA(Type.MarkupTag)) {
			List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();
			int endIndex = offset - part.getStart() + 1;
			String prefix = part.getText().substring(0, endIndex);

			String ctor = dom.getSimpleName();
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
					if(!parts.get(i).isA(Constructor)) {
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
				String s = dom.subSequence(start, offset).toLowerCase();
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
			
			Image image = EspPlugin.getImage(EspPlugin.IMG_HTML_TAG);
			for(String tag : Constants.MARKUP_TAGS.keySet()) {
				if(tag.startsWith(prefix)) {
					int rstart = part.getStart();
					int rlength = part.length();
					int length = tag.length();
					String description = Constants.MARKUP_TAGS.get(tag);
					ICompletionProposal proposal = 
						new EspCompletionProposal(tag, rstart, rlength, length, image, tag, null, description);
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
				for(String tag : Constants.MARKUP_TAGS.keySet()) {
					proposals.add(new EspCompletionProposal(tag, offset, 0, tag.length(), image, tag, null, Constants.MARKUP_TAGS.get(tag)));
				}
				return proposals;
			}
		}
		return new ArrayList<ICompletionProposal>(0);
	}

	private List<ICompletionProposal> computeDynAssetProposals(IDocument doc, EspPart part, int offset) {
		EspDom dom = part.getDom();
		String imp = "import";
		String ctor = dom.getSimpleName();
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
					if(next.getElement().isA(Constructor)) {
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
					if(!parts.get(i).isA(Constructor)) {
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
			} else {
				String s = dom.subSequence(lineStart, offset).toLowerCase();
				if(imp != null && "import".startsWith(s)) {
					Image image = JavaUI.getSharedImages().getImage(ISharedImages.IMG_OBJS_PACKAGE);
					proposals.add(new EspCompletionProposal("import ", lineStart, offset-lineStart, 7, image, "import", null, "add a new import statement"));
				} else if(ctor != null && ctor.toLowerCase().startsWith(s)) {
					Image image = EspPlugin.getImage(EspPlugin.IMG_CTOR);
					proposals.add(new EspCompletionProposal(ctor+"()", lineStart, offset-lineStart, ctor.length()+1, image, ctor, null, "add a new constructor"));
				}
			}
		} catch(BadLocationException e) {
			e.printStackTrace();
		}

		return proposals;
	}

	private List<ICompletionProposal> computeStylePropertyNameProposals(EspPart part, int offset) {
		if(part.isA(Type.StylePropertyName)) {
			List<ICompletionProposal> results = new ArrayList<ICompletionProposal>();
			Set<String> tags = new TreeSet<String>(Constants.CSS_PROPERTIES.keySet());
			int endIndex = offset - part.getStart() + 1;
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
					int rlength = part.length();
					int length = tag.length();
					ICompletionProposal proposal = 
						new EspCompletionProposal(
								tag, 
								part.getStart(), 
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
			results.addAll(computeCssSelectorProposals(part, offset));
			return results;
		} else if(part.isA(Type.StyleProperty)) {
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

	private List<ICompletionProposal> computeVarNameProposals(EspPart part, int offset) {
		List<String> strings = new ArrayList<String>();
		strings.addAll(Constants.DATA_BINDING);
		strings.addAll(Constants.DOM_EVENTS);
		List<ICompletionProposal> results = new ArrayList<ICompletionProposal>();
		if(part.isA(Type.MethodArg)) {
			for(String string : strings) {
				String rtext = string + ": ";
				results.add(new EspCompletionProposal(rtext, offset+1, 0, rtext.length(), null, string));
			}
		} else {
			int rlen = offset - part.getStart() + 1;
			String prefix = part.substring(0, rlen);
			for(String string : strings) {
				if(string.startsWith(prefix)) {
					String rtext = part.isA(Type.JavaSource) ? (string + ": ") : string;
					results.add(new EspCompletionProposal(rtext, part.getStart(), rlen, rtext.length(), null, string));
				}
			}
		}
		return results;
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
