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
package org.oobium.build.esp.compiler;

import static org.oobium.build.esp.dom.EspDom.DocType.EMT;
import static org.oobium.build.esp.dom.EspDom.DocType.ESP;
import static org.oobium.build.esp.dom.EspPart.Type.Constructor;
import static org.oobium.build.esp.dom.EspPart.Type.DOM;
import static org.oobium.build.esp.dom.EspPart.Type.ImportElement;
import static org.oobium.build.esp.dom.EspPart.Type.InnerTextElement;
import static org.oobium.build.esp.dom.EspPart.Type.JavaElement;
import static org.oobium.build.esp.dom.EspPart.Type.ScriptElement;
import static org.oobium.build.esp.dom.EspPart.Type.StyleElement;
import static org.oobium.utils.DateUtils.httpDate;
import static org.oobium.utils.StringUtils.blank;
import static org.oobium.utils.StringUtils.h;
import static org.oobium.utils.StringUtils.plural;
import static org.oobium.utils.StringUtils.titleize;
import static org.oobium.utils.StringUtils.underscored;
import static org.oobium.utils.StringUtils.varName;
import static org.oobium.utils.literal.Set;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.oobium.app.controllers.HttpController;
import org.oobium.app.http.Action;
import org.oobium.app.views.ScriptFile;
import org.oobium.app.views.StyleSheet;
import org.oobium.app.views.View;
import org.oobium.build.esp.compiler.ESourceFile.EspLocation;
import org.oobium.build.esp.compiler.ESourceFile.JavaSource;
import org.oobium.build.esp.dom.EspDom;
import org.oobium.build.esp.dom.EspElement;
import org.oobium.build.esp.dom.EspPart;
import org.oobium.build.esp.dom.EspPart.Type;
import org.oobium.build.esp.dom.EspResolver;
import org.oobium.build.esp.dom.common.MethodSignature;
import org.oobium.build.esp.dom.elements.Constructor;
import org.oobium.build.esp.dom.elements.ImportElement;
import org.oobium.build.esp.dom.elements.InnerText;
import org.oobium.build.esp.dom.elements.JavaElement;
import org.oobium.build.esp.dom.elements.MarkupComment;
import org.oobium.build.esp.dom.elements.MarkupElement;
import org.oobium.build.esp.dom.elements.ScriptElement;
import org.oobium.build.esp.dom.elements.StyleElement;
import org.oobium.build.esp.dom.parts.MethodArg;
import org.oobium.build.esp.dom.parts.MethodSigArg;
import org.oobium.build.esp.dom.parts.style.Declaration;
import org.oobium.build.esp.dom.parts.style.Ruleset;
import org.oobium.logging.LogProvider;
import org.oobium.logging.Logger;
import org.oobium.mailer.MailerTemplate;
import org.oobium.utils.ArrayUtils;
import org.oobium.utils.StringUtils;
import org.oobium.utils.Utils;
import org.oobium.utils.json.JsonUtils;

public class EspCompiler {

	private static final String SBHEAD = "__head__";
	private static final String SBBODY = "__body__";

	
	public static EspCompiler newEspCompiler(String packageName) {
		return newEspCompiler(packageName, null);
	}
	
	public static EspCompiler newEspCompiler(String packageName, Logger logger) {
		return new EspCompiler(packageName).setLogger(logger);
	}

	
	private EspResolver resolver;
	private FormCompiler formCompiler;
	private ScriptCompiler scriptCompiler;
	
	private String pkg;
	private EspDom dom;
	private ESourceFile esf;
	private StringBuilder body;
	private List<EspLocation> bodyLocations;
	
	private boolean inJava;
	private boolean inAsset;
	
	private int javaLevel;
	private String sbName;
	private EspElement captureElement;
	private String contentName;
	private boolean enableEscaping;
	private Map<StringBuilder, List<EspLocation>> locationsMap;
	
	Logger logger;
	
	private EspCompiler(String packageName) {
		this.pkg = packageName;
		this.sbName = SBBODY;
		this.enableEscaping = true;
		this.locationsMap = new HashMap<StringBuilder, List<EspLocation>>();
		this.formCompiler = new FormCompiler(this);
		this.scriptCompiler = new ScriptCompiler(this);
	}

	public EspCompiler setLogger(Logger logger) {
		this.logger = (logger != null) ? logger : LogProvider.getLogger(EspCompiler.class);
		return this;
	}
	
	private EspLocation addLocation(EspPart part, StringBuilder sb) {
		EspLocation location = new EspLocation(sb.length(), part);
		if(sb == body) {
			bodyLocations.add(location);
		} else {
			List<EspLocation> locations = locationsMap.get(sb);
			if(locations != null) {
				locations.add(location);
			}
		}
		return location;
	}

	private StringBuilder appendAttr(String name, EspPart value, boolean hidden) {
		body.append(' ').append(name);
		if(value != null) {
			body.append("=\\\"");
			build(value, body);
			body.append("\\\"");
			if(hidden && "style".equals(name)) {
				body.insert(body.length()-2, ";display:none");
			}
		}
		return body;
	}
	
	StringBuilder appendAttr(String name, MethodArg entry) {
		return appendAttr(name, entry, false);
	}
	
	/**
	 * append an HTML attribute, ensuring that double quotes are around the value,
	 * whether it is a simple string or Java expression
	 */
	private StringBuilder appendAttr(String name, MethodArg entry, boolean hidden) {
		if(entry.hasCondition()) {
			prepForJava(body);
			body.append("if(");
			build(entry.getCondition(), body);
			body.append(") {\n");
			indent(body).append('\t');
			prepForMarkup(body);
			appendAttr(name, entry.getValue(), hidden);
			prepForJava(body);
			body.append("}\n");
			prepForMarkup(body);
		}
		else {
			appendAttr(name, entry.getValue(), hidden);
		}
		return body;
	}
	
	private void appendConfirmCloser(MarkupElement element) {
		if(element.hasEntry("confirm")) {
			body.append("}");
		}
	}
	
	private void appendConfirmOpener(MarkupElement element) {
		if(element.hasEntry("confirm")) {
			body.append("if(confirm('");
			build(element.getEntry("confirm"), body);
			body.append("')) {");
		}
	}
	
	private void appendCreateJs(MarkupElement element, MethodArg target) {
		body.append(" href=\\\"\").append(pathTo(");
		build(target, body, true);
		body.append(", create)).append(\"\\\"");
		body.append(" onclick=\\\"");
		appendConfirmOpener(element);
		body.append("var f = document.createElement('form');");
		body.append("f.style.display = 'none';");
		body.append("this.parentNode.appendChild(f);");
		body.append("f.method = 'post';");
		body.append("f.action = '\").append(pathTo(");
		build(target, body, true);
		body.append(", create)).append(\"';");
		body.append("var m = document.createElement('input');");
		body.append("m.setAttribute('type', 'hidden');");
		body.append("m.setAttribute('name', '");
		build(element.getEntry("field"), body);
		body.append("');");
		body.append("m.setAttribute('value', '");
		build(element.getEntry("value"), body);
		body.append("');");
		body.append("f.appendChild(m);");
//					sb.append("var s = document.createElement('input');");
//					sb.append("s.setAttribute('type', 'hidden');");
//					sb.append("s.setAttribute('name', 'authenticity_token');");
//					sb.append("s.setAttribute('value', 'b9373bb3936620b9457ec2edc19f597b32faf6bf');");
//					sb.append("f.appendChild(s);");
		body.append("f.submit();");
		appendConfirmCloser(element);
		body.append("return false;\\\"");
	}

	private void appendDeleteJs(MarkupElement element, MethodArg target) {
		body.append(" href=\\\"\").append(pathTo(");
		build(target, body, true);
		body.append(", destroy)).append(\"\\\"");
		body.append(" onclick=\\\"");
		appendConfirmOpener(element);
		body.append("var f = document.createElement('form');");
		body.append("f.style.display = 'none';");
		body.append("this.parentNode.appendChild(f);");
		body.append("f.method = 'post';");
		body.append("f.action = '\").append(pathTo(");
		build(target, body, true);
		body.append(", destroy)).append(\"';");
		body.append("var m = document.createElement('input');");
		body.append("m.setAttribute('type', 'hidden');");
		body.append("m.setAttribute('name', '_method');");
		body.append("m.setAttribute('value', 'delete');");
		body.append("f.appendChild(m);");
//					sb.append("var s = document.createElement('input');");
//					sb.append("s.setAttribute('type', 'hidden');");
//					sb.append("s.setAttribute('name', 'authenticity_token');");
//					sb.append("s.setAttribute('value', 'b9373bb3936620b9457ec2edc19f597b32faf6bf');");
//					sb.append("f.appendChild(s);");
		body.append("f.submit();");
		appendConfirmCloser(element);
		body.append("return false;\\\"");
	}

	private void appendDeleteJs(MarkupElement element, String target) {
		body.append(" onclick=\\\"");
		appendConfirmOpener(element);
		body.append("var f = document.createElement('form');");
		body.append("f.style.display = 'none';");
		body.append("this.parentNode.appendChild(f);");
		body.append("f.method = 'post';");
		body.append("f.action = '\").append(");
		body.append(target);
		body.append(").append(\"';");
		body.append("var m = document.createElement('input');");
		body.append("m.setAttribute('type', 'hidden');");
		body.append("m.setAttribute('name', '_method');");
		body.append("m.setAttribute('value', 'delete');");
		body.append("f.appendChild(m);");
//					sb.append("var s = document.createElement('input');");
//					sb.append("s.setAttribute('type', 'hidden');");
//					sb.append("s.setAttribute('name', 'authenticity_token');");
//					sb.append("s.setAttribute('value', 'b9373bb3936620b9457ec2edc19f597b32faf6bf');");
//					sb.append("f.appendChild(s);");
		body.append("f.submit();");
		appendConfirmCloser(element);
		body.append("return false;\\\"");
	}

	void appendEscaped(StringBuilder sb, String text) {
		appendEscaped(sb, text, 0, text.length());
	}
	
	void appendEscaped(StringBuilder sb, String text, int start, int end) {
		if(inJava || !enableEscaping) {
			sb.append(text, start, end);
		} else {
			for(int i = start; i < end; i++) {
				char c = text.charAt(i);
				switch(c) {
				case '"':	if(i == 0 || text.charAt(i-1) != '\\') { sb.append("\\\""); } break;
				case '\t':	if(i == 0 || text.charAt(i-1) != '\\') { sb.append("\\t"); } break;
				case '\n':  if(i == 0 || text.charAt(i-1) != '\\') { sb.append("\\n"); } break;
				default:	sb.append(c); break;
				}
			}
		}
	}
	
	private void appendFieldError(String model, List<MethodArg> fields, String str, boolean newAttr) {
		prepForJava(body);
		body.append("if(").append(model).append(".hasErrors(");
		for(int i = 0; i < fields.size(); i++) {
			if(i != 0) body.append(", ");
			build(fields.get(i), body);
		}
		body.append(")) {\n");
		indent(body);
		body.append('\t').append(sbName).append(".append(\"").append(str);
		body.append(" error=\\\"\").append(");
		body.append(model).append(".getError(");
		for(int i = 0; i < fields.size(); i++) {
			if(i != 0) body.append(", ");
			build(fields.get(i), body);
		}
		if(newAttr) {
			body.append(")).append(\"\\\"\");\n");
			indent(body);
			body.append("}\n");
		} else {
			body.append("));\n");
			indent(body);
			body.append("}\n");
			indent(body);
			body.append(sbName).append(".append(\"\\\"\");\n");
		}
		prepForMarkup(body);
	}
	
	void buildJavaContainer(EspPart container, StringBuilder sb) {
		if(inAsset()) {
			sb.append(getCodeVar(container));
		} else {
			buildJavaContainer(container, sb, false);
		}
	}
	
	void buildJavaContainer(EspPart container, StringBuilder sb, boolean forceInJava) {
		if(container.hasParts()) {
			String simpleString = getSimpleString(container);
			if(simpleString != null) {
				sb.append(simpleString);
				return;
			}
			
			if(!inJava && !forceInJava) {
				sb.append("\");\n");
				indent(sb);
				sb.append(sbName).append(".append(");
			}
			
			char escapeChar = getEscapeChar(container);
			if(escapeChar != 0) sb.append(escapeChar).append('(');
			
			List<EspPart> parts = container.getParts();
			for(int i = 0; i < parts.size(); i++) {
				EspPart part = parts.get(i);
				if(part.isA(Type.JavaEscape)) {
					continue;
				}
				if(part.isA(Type.JavaString)) {
					buildJavaString(part, escapeChar != 0, sb);
				}
				else {
					addLocation(part, sb);
					sb.append(part.getText());
				}
			}
			
			if(escapeChar != 0) sb.append(')');
			
			if(!inJava && !forceInJava) {
				sb.append(");\n");
				indent(sb);
				sb.append(sbName).append(".append(\"");
			}
		}
	}

	private void appendUpdateJs(MarkupElement element, MethodArg target) {
		body.append(" href=\\\"\").append(pathTo(");
		build(target, body, true);
		body.append(", update)).append(\"\\\"");
		body.append(" onclick=\\\"");
		appendConfirmOpener(element);
		body.append("var f = document.createElement('form');");
		body.append("f.style.display = 'none';");
		body.append("this.parentNode.appendChild(f);");
		body.append("f.method = 'post';");
		body.append("f.action = '\").append(pathTo(");
		build(target, body, true);
		body.append(", update)).append(\"';");
		body.append("var m = document.createElement('input');");
		body.append("m.setAttribute('type', 'hidden');");
		body.append("m.setAttribute('name', '_method');");
		body.append("m.setAttribute('value', 'put');");
		body.append("f.appendChild(m);");
		body.append("var m = document.createElement('input');");
		body.append("m.setAttribute('type', 'hidden');");
		body.append("m.setAttribute('name', '");
		build(element.getEntry("field"), body);
		body.append("');");
		body.append("m.setAttribute('value', '");
		build(element.getEntry("value"), body);
		body.append("');");
		body.append("f.appendChild(m);");
//					sb.append("var s = document.createElement('input');");
//					sb.append("s.setAttribute('type', 'hidden');");
//					sb.append("s.setAttribute('name', 'authenticity_token');");
//					sb.append("s.setAttribute('value', 'b9373bb3936620b9457ec2edc19f597b32faf6bf');");
//					sb.append("f.appendChild(s);");
		body.append("f.submit();");
		appendConfirmCloser(element);
		body.append("return false;\\\"");
	}
	
	void build(EspPart part, StringBuilder sb) {
		build(part, sb, false);
	}

	/**
	 * @param forceInJava if true, then will behave as if inJava == true
	 */
	void build(EspPart part, StringBuilder sb, boolean forceInJava) {
		if(part == null) {
			return; // occurs when part is supposed to be the value of an entry part, but it has not been created yet: "key:"
		}
		else if(part instanceof MethodArg) {
			build(((MethodArg) part).getValue(), sb, forceInJava);
		}
		else if(part.isA(Type.JavaContainer)) {
			if(forceInJava) {
				boolean wasInJava = inJava;
				inJava = true;
				buildJavaContainer(part, sb);
				inJava = wasInJava;
			} else {
				buildJavaContainer(part, sb);
			}
		}
		else {
			String text = part.getText();
			int startAdjustment = 0;
			if(part.isA(Type.MarkupId) || part.isA(Type.MarkupClass)) {
				text = text.substring(1);
				startAdjustment = 1;
			}
			if(part.hasParts()) {
				List<EspPart> parts = part.getParts();
				for(int i = 0; i < parts.size(); i++) {
					EspPart sub = parts.get(i);
					int s1 = sub.getStart() - part.getStart() - startAdjustment;
					int s2 = s1 + sub.length();
					if(i == 0) { // if first
						if(s1 > 0) appendEscaped(sb, text, 0, s1);
					} else {
						int s0 = parts.get(i-1).getEnd() - part.getStart();
						if(s0 < s1) appendEscaped(sb, text, s0, s1);
					}
					if(sub.isA(Type.JavaContainer)) {
						buildJavaContainer(sub, sb);
					}
					else if(sub.isA(Type.Comment)) {
						if(sub.startsWith("/**")) { // JavaDoc
							appendEscaped(sb, text, s1, s2);
						}
					}
					else {
						appendEscaped(sb, text, s1, s2);
					}
					if(i == parts.size() - 1) { // if last
						if(s2 < text.length()) {
							appendEscaped(sb, text, s2, text.length());
						}
					}
				}
			}
			else {
				appendEscaped(sb, text);
			}
		}
	}
	
	void buildAttrs(MarkupElement element, String...skip) {
		boolean hidden = element.isHidden();
		Set<String> skipSet = Set(skip);
		if(element.hasEntries()) {
			for(MethodArg entry : element.getEntries().values()) {
				String name = entry.getName().getText().trim();
				if(!skipSet.contains(name)) {
					appendAttr(name, entry, hidden);
				}
			}
			if(hidden && !element.hasEntry("style")) {
				body.append(" style=\\\"display:none\\\"");
			}
		} else if(hidden) {
			body.append(" style=\\\"display:none\\\"");
		}
	}
	
	void buildChildren(MarkupElement element) {
		if(element.hasChildren()) {
			for(EspElement child : element.getChildren()) {
				if(child.isA(JavaElement)) {
					buildJava((JavaElement) child);
				}
				else if(child.isA(InnerTextElement)) {
					buildInnerText(child);
				}
				else {
					buildElement(child);
				}
			}
		}
	}

	void buildClasses(MarkupElement element) {
		if(element.hasClasses()) {
			body.append(" class=\\\"");
			for(Iterator<EspPart> ci = element.getClasses().iterator(); ci.hasNext(); ) {
				build(ci.next(), body);
				if(ci.hasNext()) body.append(' ');
			}
			body.append("\\\"");
		}
	}
	
	void buildClasses(MarkupElement element, String model, List<MethodArg> fields) {
		if(!"hidden".equals(element.getTag().getText())) {
			String cssClass = "label".equals(element.getTag().getText()) ? "labelWithErrors\\\"" : "fieldWithErrors\\\"";
			if(element.hasClasses()) {
				body.append(" class=\\\"");
				for(Iterator<EspPart> ci = element.getClasses().iterator(); ci.hasNext(); ) {
					build(ci.next(), body);
					if(ci.hasNext()) body.append(' ');
				}
				
				appendFieldError(model, fields, " " + cssClass, false);
			} else {
				appendFieldError(model, fields, " class=\\\"" + cssClass, true);
			}
		}
	}
	
	void buildClosingTag(String tag) {
		if(inJava) {
			indent(body);
			body.append(sbName).append(".append(\"");
		}
		body.append('<').append('/').append(tag).append('>');
		inJava = false;
	}
	
	private void buildComment(MarkupComment comment) {
		prepForMarkup(body);
		String oldSection = prepFor(comment);

		body.append("<!--");
		if(comment.hasEntry("if")) {
			body.append("[if \").append(");
			build(comment.getEntry("if"), body, true);
			body.append(").append(\"]>");
		}

		if(comment.hasInnerText()) {
			build(comment.getInnerText(), body);
		}

		buildChildren(comment);
		
		prepForMarkup(body);
		if(comment.hasEntry("if")) {
			body.append("<![endif]");
		}
		body.append("-->");
		
		prepFor(oldSection);
	}
	
	private void buildElement(EspElement element) {
		switch(element.getType()) {
		case MarkupComment:
			buildComment((MarkupComment) element);
			break;
		case MarkupElement:
			buildHtml((MarkupElement) element);
			break;
		case ScriptElement:
			scriptCompiler.compile((ScriptElement) element);
			break;
		case StyleElement:
			buildStyle((StyleElement) element);
			break;
		}
	}
	
	private void buildHtml(MarkupElement element) {
		String tag = element.getTag().getText();

		if("capture".equals(tag)) {
			startCapture(element);
			buildChildren(element);
			stopCapture();
		}
		else if("contentFor".equals(tag)) {
			startContent(element);
			buildChildren(element);
			stopContent();
		}
		else if("models".equals(tag)) {
			scriptCompiler.buildModels();
		}
		else {
			if("textarea".equalsIgnoreCase(tag)) tag = "textarea";
	
			buildHtml(tag, element);
		}
	}

	private void buildHtml(String tag, MarkupElement element) {
		if("!--".equals(tag))      { buildComment((MarkupComment) element);    return; }
		if("errors".equals(tag))   { formCompiler.buildErrors(element);        return; }
		if("form".equals(tag))     { formCompiler.buildForm(element);          return; }
		if("label".equals(tag))    { formCompiler.buildLabel(element);         return; }
		if("messages".equals(tag)) { buildMessages(element);                   return; }
		if("options".equals(tag))  { formCompiler.buildSelectOptions(element); return; }
		if("title".equals(tag))    { buildTitle(element);                      return; }
		if("view".equals(tag))     { buildView(element);                       return; }
		if("yield".equals(tag))    { buildYield((MarkupElement) element);      return; }

		prepForMarkup(body);
		
		     if("a".equals(tag))        { buildLink(element);                      }
		else if("link".equals(tag))     { buildLink(element);                      }
		else if("button".equals(tag))   { buildLink(element);                      }
		else if("img".equals(tag))      { buildImage(element);                     }
		else if("number".equals(tag))   { formCompiler.buildNumber(element);       }
		else if("decimal".equals(tag))  { formCompiler.buildDecimal(element);      }
		else if("fields".equals(tag))   { formCompiler.buildFields(element);       }
		else if("date".equals(tag))     { formCompiler.buildDateInputs(element);   }
		else if("textarea".equals(tag)) { formCompiler.buildTextArea(element);     }
		else if("check".equals(tag))    { formCompiler.buildCheck(element);        }
		else if("radio".equals(tag))    { formCompiler.buildInput(element);        }
		else if("file".equals(tag))     { formCompiler.buildInput(element);        }
		else if("input".equals(tag))    { formCompiler.buildInput(element);        }
		else if("hidden".equals(tag))   { formCompiler.buildInput(element);        }
		else if("number".equals(tag))   { formCompiler.buildInput(element);        }
		else if("password".equals(tag)) { formCompiler.buildInput(element);        }
		else if("text".equals(tag))     { formCompiler.buildInput(element);        }
		else if("select".equals(tag))   { formCompiler.buildSelect(element);       }
		else if("submit".equals(tag))   { formCompiler.buildSubmit(element);       }
		else if("reset".equals(tag))    { formCompiler.buildResetInput(element);   }
		else if("option".equals(tag))   { formCompiler.buildSelectOption(element); }
		else if(!"head".equals(tag)) {	
			body.append('<').append(tag);
			buildId(element);
			buildClasses(element);
			buildAttrs(element);
			body.append('>');
			if(element.hasInnerText()) {
				build(element.getInnerText(), body);
			}
		}
		inJava = false;
		
		buildChildren(element);
		
		if(hasClosingTag(tag)) {
			buildClosingTag(tag);
		}
	}
	
	void buildId(MarkupElement element) {
		if(element.hasId()) {
			body.append(" id=\\\"");
			build(element.getId(), body);
			body.append("\\\"");
		}
	}
	
	private void buildImage(MarkupElement image) {
		body.append("<img");
		buildId(image);
		buildClasses(image);
		buildAttrs(image, "src");
		if(image.hasEntry("src")) {
			appendAttr("src", image.getEntry("src"));
		} else if(image.hasArgs()) {
			appendAttr("src", image.getArgs().get(0));
		}
		body.append('>');
		if(image.hasInnerText()) {
			build(image.getInnerText(), body);
		}
	}
	
	private void buildImport(ImportElement element) {
		if(element.hasImportPart()) {
			JavaSource source = new JavaSource(element.getImportPart());
			if(element.isStatic()) {
				esf.addStaticImport(source);
			} else {
				esf.addImport(source);
			}
		}
	}

	private void buildInnerText(EspElement element) {
		InnerText innerText = (InnerText) element;
		if(innerText.hasInnerText()) {
			if(innerText.isWordGroup()) {
				if(body.length() > 0 && !Character.isWhitespace(body.charAt(body.length()-1))) {
					body.append(' ');
				}
				appendEscaped(body, innerText.getInnerText().getText());
			} else if(innerText.isPromptLine()) {
				appendEscaped(body, h(innerText.getInnerText().getText()));
				if(body.length() > 1 && (body.charAt(body.length()-2) != '\\' || body.charAt(body.length()-1) != 'n')) {
					body.append('\\').append('n');
				}
			} else if(innerText.isLiteral()) {
				appendEscaped(body, innerText.getInnerText().getText());
			} else {
				build(innerText.getInnerText(), body);
			}
		} else if(innerText.isPromptLine()) {
			body.append('\\').append('n');
		}
	}
	
	private void buildJava(JavaElement element) {
		prepForJava(body);
		indent(body);
		buildJavaContainer(element.getSource(), body);
		body.append("\n");

		if(element.hasChildren()) {
			javaLevel++;
			for(EspElement child : element.getChildren()) {
				if(child.isA(JavaElement)) {
					buildJava((JavaElement) child);
				} else {
					buildElement(child);
				}
			}
			javaLevel--;
		}

		if(!inJava) {
			body.append("\");\n");
			inJava = true;
		}
	}
	
	private void buildJavaString(EspPart part, boolean escaped, StringBuilder sb) {
		String text = part.getText();
		if(part.hasParts()) {
			if(!escaped) sb.append('(');
			List<EspPart> parts = part.getParts();
			for(int i = 0; i < parts.size(); i++) {
				EspPart sub = parts.get(i);
				int s1 = sub.getStart() - part.getStart();
				int s2 = s1 + sub.length();

				if(i == 0) {
					if(s1 > 0) {
						if(s1 > 1) {
							sb.append(text, 0, s1);
							sb.append("\" + ");
						}
					}
				} else {
					int s0 = parts.get(i-1).getEnd() - part.getStart();
					if(s0 < s1) {
						sb.append(" + \"");
						sb.append(text, s0, s1);
						sb.append("\" + ");
					}
				}
				
				if(sub.isA(Type.JavaContainer)) {
					if(sub.hasParts()) {
						String subtext = sub.getText();
						boolean exp = 1 < subtext.length() && subtext.charAt(1) == '{';
						if(exp) sb.append('(');
						for(EspPart subpart : sub.getParts()) {
							addLocation(subpart, sb);
							sb.append(subpart.getText());
						}
						if(exp) sb.append(')');
					}
				} else {
					sb.append(text, s1, s2);
				}
				
				if(i == parts.size() - 1) { // if last
					if(s2 < text.length()-1) {
						sb.append(" + \"");
						sb.append(text, s2, text.length());
					}
				}
			}
			if(!escaped) sb.append(')');
		}
		else {
			sb.append(text);
		}
	}
	
	private void buildLink(MarkupElement link) {
		body.append('<').append(link.getTag().getText());
		buildId(link);
		buildClasses(link);
		MethodArg target = null;
		String action = null;
		if(link.hasArgs() && link.getArgs().size() <= 2) {
			buildAttrs(link, "href", "action", "confirm", "update", "field", "value", "method");
			target = link.getArg(0);
			if(link.getArgs().size() == 1) {
				appendAttr("href", target);
				if(link.hasEntryValue("method")) {
					String method = link.getEntryValue("method").getText();
					if("\"delete\"".equalsIgnoreCase(method)) {
						appendDeleteJs(link, target.getText());
					}
				}
			} else if(dom.is(ESP)) { // size == 2
				MethodArg part = link.getArg(1);
				if(part == null) {
					body.append(" href=\\\"\").append(").append("pathTo(");
					build(target, body, true);
					body.append(", null)).append(\"\\\"");
				} else {
					action = part.getText().trim();
					if("create".equals(action)) {
						esf.addStaticImport(Action.class.getCanonicalName() + ".create");
						appendCreateJs(link, target);
					} else if("update".equals(action)) {
						esf.addStaticImport(Action.class.getCanonicalName() + ".update");
						appendUpdateJs(link, target);
					} else if("destroy".equals(action)) {
						esf.addStaticImport(Action.class.getCanonicalName() + ".destroy");
						appendDeleteJs(link, target);
					} else {
						body.append(" href=\\\"\").append(").append("pathTo(");
						build(target, body, true);
						body.append(", ");
						if("show".equals(action) || "showAll".equals(action) || "showEdit".equals(action) || "showNew".equals(action)) {
							esf.addStaticImport(Action.class.getCanonicalName() + "." + action);
							body.append(action);
						} else {
							build(part, body, true);
						}
						body.append(")).append(\"\\\"");
					}
				}
			} else if(dom.is(EMT)) { // size == 2
				// mailers can't use JavaScript or pathTo (uses urlTo instead)
				MethodArg part = link.getArg(1);
				if(part == null) {
					body.append(" href=\\\"\").append(").append("urlTo(");
					build(target, body, true);
					body.append(", null)).append(\"\\\"");
				} else {
					action = part.getText().trim();
					body.append(" href=\\\"\").append(").append("urlTo(");
					build(target, body, true);
					body.append(", ");
					if("show".equals(action) || "showAll".equals(action) || "showEdit".equals(action) || "showNew".equals(action)) {
						esf.addStaticImport(Action.class.getCanonicalName() + "." + action);
						body.append(action);
					} else {
						build(part, body, true);
					}
					body.append(")).append(\"\\\"");
				}
			}
		} else if(link.getEntryCount() == 1 && link.hasEntry("onclick")) {
			body.append(" href=\\\"#\\\" onclick=\\\"");
			build(link.getEntry("onclick"), body);
			body.append(";return false;\\\"");
		} else {
			buildAttrs(link);
		}
		body.append('>');

		if(link.hasInnerText()) {
			build(link.getInnerText(), body);
		} else if(target != null && !link.hasChildren()) {
			if(action != null) {
				if(action.length() > 4 && action.startsWith("show")) {
					body.append(titleize(action.substring(4))).append(' ');
				} else {
					body.append(titleize(action)).append(' ');
				}
				String txt = target.getText();
				if(txt.endsWith(".class")) {
					if(action.equals("showNew")) {
						txt = txt.substring(0, txt.length()-6);
					} else {
						txt = plural(txt.substring(0, txt.length()-6));
					}
				}
				body.append(titleize(txt));
			} else {
				String txt = target.getText();
				if(txt.length() > 1 && txt.charAt(0) == '"' && txt.charAt(txt.length()-1) == '"') {
					txt = txt.substring(1, txt.length()-1);
				}
				body.append(txt);
			}
		}
	}
	
	private void buildMessages(MarkupElement element) {
		prepForJava(body);

		body.append("messagesBlock(").append(sbName).append(");\n");
		
		inJava = true;
	}
	
	private void buildMethod(String name, String sig) {
		StringBuilder sb = new StringBuilder(sig.length() + body.length() + 15);
		sb.append(sig).append(body);
		if(inJava) {
			sb.append("\t}");
		} else {
			int pos = sb.length()-1;
			boolean empty = true;
			char[] ca = ".append(\"".toCharArray();
			for(int i = ca.length-1; empty && i >= 0; i--, pos--) {
				if(ca[i] != sb.charAt(pos)) {
					empty = false;
				}
			}
			if(empty) {
				ca = sbName.toCharArray();
				for(int i = ca.length-1; empty && i >= 0; i--, pos--) {
					if(ca[i] != sb.charAt(pos)) {
						empty = false;
					}
				}
				pos++;
				if(empty) { // delete the previous section
					while(pos >= 0 && sb.charAt(pos) != '\n') {
						pos--;
					}
					sb.delete(pos+1, sb.length());
				} else { // delete the previous start and close the section
					sb.delete(pos, sb.length());
					sb.append(";\n");
				}
			} else { // close the section
				sb.append("\");\n");
			}
			sb.append("\t}");
		}
		
		for(EspLocation location : bodyLocations) {
			location.offset += sig.length();
		}
		
		esf.addMethod(name, sb.toString(), bodyLocations);
	}

	void buildMethodSignature(MethodSignature method) {
		String rtype = method.getReturnType();
		String mname = method.getMethodName();
		
		StringBuilder sb;
		List<MethodSigArg> args = method.getSigArgs();
		if(canBuildPartMethodSignatures(args)) {
			for(int i = 0; i < args.size(); i++) {
				if(args.get(i).hasDefaultValue()) {
					sb = new StringBuilder();
					sb.append("\tpublic ");
					if(method.isStatic()) sb.append("static ");
					if(rtype != null && rtype.length() > 0) {
						sb.append(rtype).append(' ');
					}
					sb.append(mname).append('(');
					for(int j = 0; j < i; j++) {
						MethodSigArg arg = args.get(j);
						String declaration = arg.getVarType().getText() + " " + arg.getVarName().getText();
						if(j != 0) sb.append(", ");
						sb.append(declaration);
					}
					sb.append(") {\n\t\t");
					if(method.isConstructor()) {
						sb.append("this");
					} else {
						if(rtype != null && rtype.length() > 0) {
							sb.append("return ");
						}
						sb.append(mname);
					}
					sb.append("(");
					for(int j = 0; j < args.size(); j++) {
						MethodSigArg arg = args.get(j);
						if(j != 0) sb.append(", ");
						if(j < i) {
							sb.append(arg.getVarName().getText());
						} else {
							boolean wasInJava = inJava;
							inJava = true;
							buildJavaContainer(arg.getDefaultValue(), sb);
							inJava = wasInJava;
						}
					}
					sb.append(");\n");
					sb.append("\t}");
					JavaSource source = new JavaSource(sb.toString());
					if(method.isConstructor()) {
						esf.addConstructor(source);
					} else {
						esf.addMethod(mname + i, source);
					}
				}
			}
		}

		sb = new StringBuilder();
		locationsMap.put(sb, new ArrayList<EspLocation>());
		sb.append("\tpublic ");
		if(method.isStatic()) sb.append("static ");
		if(!method.isConstructor()) sb.append(rtype).append(' ');
		addLocation(method.getPart(), sb);
		sb.append(mname).append('(');
		for(int i = 0; i < args.size(); i++) {
			MethodSigArg arg = args.get(i);
			if(i != 0) sb.append(", ");
			addLocation(arg, sb);
			if(arg.hasVarType()) {
				String vtype = arg.getVarType().getText();
				addLocation(arg.getVarType(), sb);
				sb.append(vtype);
				if(arg.isVarArgs()) {
					vtype = vtype + "[]";
					sb.append('.').append('.').append('.');
				} else {
					sb.append(' ');
				}
				if(arg.hasVarName()) {
					addLocation(arg.getVarName(), sb);
					String name = arg.getVarName().getText();
					sb.append(name);
					if(method.isConstructor()) esf.addVariable(name, "public " + vtype + " " + name);
				}
			}
		}
		sb.append(") {\n");
		if(method.isConstructor()) {
			for(int j = 0; j < args.size(); j++) {
				MethodSigArg arg = args.get(j);
				if(arg.hasVarName()) {
					sb.append("\t\tthis.").append(arg.getVarName().getText()).append(" = ");
					if(j <= args.size()) {
						sb.append(arg.getVarName().getText());
					} else if(arg.hasDefaultValue()) {
						sb.append(arg.getDefaultValue().getText());
					}
					sb.append(";\n");
				}
			}
		} else {
			offsetLocations(sb, mname.startsWith("id") ? 1 : 4);
			Ruleset rule = (Ruleset) method;
			Declaration declaration = rule.getDeclaration();
			if(declaration.hasProperties()) {
				sb.append("\t\tStringBuilder __body__ = new StringBuilder();\n");
				sb.append("\t\t__body__.append(\"");
				StyleCompiler styleCompiler = new StyleCompiler(this);
				styleCompiler.buildStyleProperties(rule, sb);
				sb.append("\");\n");
				sb.append("\t\treturn __body__.toString();\n");
			} else {
				sb.append("\t\treturn \"\";\n");
			}
		}
		sb.append("\t}");
		JavaSource source = new JavaSource(sb.toString(), locationsMap.remove(sb));
		if(method.isConstructor()) {
			esf.addConstructor(source);
		} else {
			esf.addMethod(mname + args.size(), source);
		}
	}
	
	private void buildStyle(StyleElement element) {
		StyleCompiler compiler = new StyleCompiler(this);
		compiler.buildStyle(element);
	}

	private void buildTitle(MarkupElement element) {
		prepForJava(body);
		if(element.hasInnerText()) {
			body.append("setTitle(\"");
			build(element.getInnerText(), body);
			body.append("\");\n");
		} else {
			body.append("setTitle(null);\n");
		}
	}

	private void buildView(MarkupElement view) {
		if(view.hasJavaType()) {
			if(view.hasEntries()) {
				Entry<String, MethodArg> entry = view.getEntries().entrySet().iterator().next();
				MethodArg part = entry.getValue();
				if(!blank(part)) {
					String itype = entry.getKey();
					String var = varName(itype) + "$" + view.getStart();
					
					prepForJava(body);

					EspPart value = part.getValue();
					String val = (value != null) ? value.getText() : "";
					body.append("for(").append(itype).append(' ').append(var).append(" : ").append(val).append(") {\n");
					indent(body);
					body.append("\tyield(new ");
					build(view.getJavaType(), body);
					body.append('(').append(var).append("));\n");
					indent(body);
					body.append("}\n");
					
					inJava = true;
				}
			} else {
				prepForJava(body);
				body.append("yield(new ");
				build(view.getJavaType(), body);
				if(view.hasArgs()) {
					body.append("(");
					for(Iterator<MethodArg> iter = view.getArgs().iterator(); iter.hasNext(); ) {
						build(iter.next(), body);
						if(iter.hasNext()) body.append(',').append(' ');
					}
					body.append("));\n");
				} else {
					body.append("());\n");
				}
				inJava = true;
			}
		}
	}

	private void buildYield(MarkupElement element) {
		prepForJava(body);

		if(dom.is(ESP)) {
			if(element.hasArgs()) {
				body.append("yield(");
				build(element.getArg(0), body);
				if(captureElement != null) {
					body.append(", ").append(sbName);
				}
				body.append(");\n");
			} else {
				body.append("yield(");
				if(captureElement != null) {
					body.append(sbName);
				}
				body.append(");\n");
			}
		} else {
			body.append("yield(");
			if(captureElement != null) {
				body.append(sbName);
			}
			body.append(");\n");
		}

		inJava = true;
	}
	
	private boolean canBuildPartMethodSignatures(List<MethodSigArg> args) {
		for(MethodSigArg arg : args) {
			if(!arg.hasVarType() || !arg.hasVarName()) {
				return false;
			}
		}
		return true;
	}
	
	public ESourceFile compile(EspDom dom) {
		this.dom = dom;
		if(resolver == null) {
			resolver = new EspResolver();
			resolver.add(dom);
		}
		switch(this.dom.getDocType()) {
		case ESP: return compileEsp();
		case EMT: return compileEmt();
		case ESS: return compileEss();
		case EJS: return compileEjs();
		default:
			throw new IllegalArgumentException("don't know how to compile DocType: " + dom.getDocType());
		}
	}
	
	private ESourceFile compileEjs() {
		esf = new ESourceFile();
		body = new StringBuilder();
		bodyLocations = new ArrayList<EspLocation>();

		esf.setPackage(pkg);
		esf.addStaticImport(StringUtils.class.getCanonicalName() + ".*");
		esf.addImport(ScriptFile.class.getCanonicalName());
		esf.setSimpleName(dom.getSimpleName());
		esf.setSuperName(ScriptFile.class.getSimpleName());

		int ix = 0;
		List<EspPart> parts = dom.getParts();
		
		for( ; ix < parts.size(); ix++) {
			EspPart part = parts.get(ix);
			if(part.isA(ImportElement)) {
				buildImport((ImportElement) part);
			} else if(part instanceof EspElement) {
				break;
			}
		}
		
		for( ; ix < parts.size(); ix++) {
			EspPart part = parts.get(ix);
			if(part.isA(Constructor)) {
				buildMethodSignature((Constructor) part);
			} else if(part instanceof EspElement) {
				break;
			}
		}

		body.append("\t\t").append(SBBODY).append(".append(\"");
		for( ; ix < parts.size(); ix++) {
			EspPart part = parts.get(ix);
			if(part.isA(JavaElement)) {
				buildJava((JavaElement) part);
			} else if(part instanceof EspElement) {
				break;
			}
		}
		
		boolean hasInitializer = false;
		for(int i = ix; i < parts.size(); i++) {
			EspPart part = parts.get(ix);
			if(part.isA(ScriptElement)) {
				if(scriptCompiler.buildInitializer((ScriptElement) part)) {
					hasInitializer = true;
				}
			}
		}
		
		esf.addMethod(
				"hasInitializer",
				"\t@Override\n\tpublic boolean hasInitializer() {\n\t\treturn " + hasInitializer + ";\n\t}"
		);
		
		buildMethod(
				"doRender",
				"\t@Override\n\tprotected void doRender(StringBuilder " + SBBODY + ") throws Exception {\n"
		);

		esf.finalizeSource();

		String asset = underscored(pkg.replace('.','/')+"/"+dom.getSimpleName()) + ".js";
		body = new StringBuilder();
		body.append("/**\n");
		body.append(" * Auto-generated from ").append(pkg).append('.').append(dom.getSimpleName()).append(".ejs\n");
		body.append(" * ").append(httpDate(System.currentTimeMillis())).append("\n");
		body.append(" */\n");
		enableEscaping = false;
		for( ; ix < parts.size(); ix++) {
			EspPart part = parts.get(ix);
			if(part instanceof EspElement) {
				buildElement((EspElement) part);
			}
		}
		esf.addAsset(asset, body.toString());
		
		return esf;
	}

	private ESourceFile compileEmt() {
		esf = new ESourceFile();
		body = new StringBuilder();
		bodyLocations = new ArrayList<EspLocation>();

		esf.setPackage(pkg);
		esf.addStaticImport(ArrayUtils.class.getCanonicalName() + ".*");
		esf.addStaticImport(JsonUtils.class.getCanonicalName() + ".*");
		esf.addStaticImport(StringUtils.class.getCanonicalName() + ".*");
		esf.addStaticImport(Utils.class.getCanonicalName() + ".*");
		esf.addImport(Action.class.getCanonicalName());
		esf.addImport(MailerTemplate.class.getCanonicalName());
		esf.addClassAnnotation("@SuppressWarnings(\"unused\")");
		esf.setSimpleName(dom.getSimpleName());
		esf.setSuperName(MailerTemplate.class.getSimpleName());

		if(dom.hasParts()) {
			int ix = 0;
			List<EspPart> parts = dom.getParts();
			
			while(ix < parts.size()) {
				EspPart part = parts.get(ix);
				if(part.isA(ImportElement)) {
					buildImport((ImportElement) part);
					ix++;
				} else {
					break;
				}
			}
			
			while(ix < parts.size()) {
				EspPart part = parts.get(ix);
				if(part.isA(Constructor)) {
					buildMethodSignature((Constructor) part);
					ix++;
				} else {
					break;
				}
			}

			body.append("\t\t").append(SBBODY).append(".append(\"");
			for( ; ix < parts.size(); ix++) {
				EspElement element = (EspElement) parts.get(ix);
				if(element.isA(JavaElement)) {
					buildJava((JavaElement) element);
				} else {
					buildElement(element);
				}
			}
		
			buildMethod(
					"doRender",
					"\t@Override\n\tprotected void doRender(StringBuilder " + SBBODY + ") throws Exception {\n"
			);
		}
		
		esf.finalizeSource();
		
		return esf;
	}
	
	private ESourceFile compileEsp() {
		esf = new ESourceFile();
		body = new StringBuilder();
		body.append("\t\t").append(SBBODY).append(".append(\"");
		bodyLocations = new ArrayList<EspLocation>();

		esf.setPackage(pkg);
		esf.addStaticImport(ArrayUtils.class.getCanonicalName() + ".*");
		esf.addStaticImport(JsonUtils.class.getCanonicalName() + ".*");
		esf.addStaticImport(StringUtils.class.getCanonicalName() + ".*");
		esf.addStaticImport(Utils.class.getCanonicalName() + ".*");
		esf.addImport(Action.class.getCanonicalName());
		esf.addImport(View.class.getCanonicalName());
		esf.addImport(HttpController.class.getCanonicalName());
		esf.addClassAnnotation("@SuppressWarnings(\"unused\")");
		esf.setSimpleName(dom.getSimpleName());
		esf.setSuperName(View.class.getSimpleName());

		if(dom.hasParts()) {
			int ix = 0;
			List<EspPart> parts = dom.getParts();
			
			while(ix < parts.size()) {
				EspPart part = parts.get(ix);
				if(part.isA(ImportElement)) {
					buildImport((ImportElement) part);
				} else if(part instanceof EspElement) {
					break;
				}
				ix++;
			}
			
			while(ix < parts.size()) {
				EspPart part = parts.get(ix);
				if(part.isA(Constructor)) {
					buildMethodSignature((Constructor) part);
				} else if(part instanceof EspElement) {
					break;
				}
				ix++;
			}

			for( ; ix < parts.size(); ix++) {
				EspPart part = parts.get(ix);
				if(part instanceof EspElement) {
					EspElement element = (EspElement) part;
					if(element.isA(JavaElement)) {
						buildJava((JavaElement) element);
					} else {
						buildElement(element);
					}
				}
			}

			buildMethod(
					"render",
					"\t@Override\n\tprotected void render(StringBuilder " + SBHEAD + ", StringBuilder " + SBBODY + ") throws Exception {\n"
			);
		}
		
		esf.finalizeSource();
		
		return esf;
	}
	
	private ESourceFile compileEss() {
		esf = new ESourceFile();
		body = new StringBuilder();
		bodyLocations = new ArrayList<EspLocation>();

		esf.setPackage(pkg);
		esf.addStaticImport(StringUtils.class.getCanonicalName() + ".*");
		esf.addImport(StyleSheet.class.getCanonicalName());
		esf.setSimpleName(dom.getSimpleName());
		esf.setSuperName(StyleSheet.class.getSimpleName());

		int ix = 0;
		List<EspPart> parts = dom.getParts();
		
		for( ; ix < parts.size(); ix++) {
			EspPart part = parts.get(ix);
			if(part.isA(ImportElement)) {
				buildImport((ImportElement) part);
			} else if(part instanceof EspElement) {
				break;
			}
		}
		
		for( ; ix < parts.size(); ix++) {
			EspPart part = parts.get(ix);
			if(part.isA(Constructor)) {
				buildMethodSignature((Constructor) part);
			} else if(part instanceof EspElement) {
				break;
			}
		}

		body.append("\t\t").append(SBBODY).append(".append(\"");
		for( ; ix < parts.size(); ix++) {
			EspPart part = parts.get(ix);
			if(part.isA(JavaElement)) {
				buildJava((JavaElement) part);
			} else if(part instanceof EspElement) {
				break;
			}
		}
		for(int i = ix; i < parts.size(); i++) {
			EspPart part = parts.get(ix);
			if(part.isA(StyleElement)) {
				buildStyle((StyleElement) part);
			}
		}

		buildMethod(
				"doRender",
				"\t@Override\n\tprotected void doRender(StringBuilder " + SBBODY + ") throws Exception {\n"
		);
		
		esf.finalizeSource();
		
		return esf;
	}

	void decJavaLevel() {
		javaLevel--;
	}

	EspDom getDom() {
		return dom;
	}
	
	StringBuilder getBody() {
		return body;
	}

	String getCodeVar(EspPart var) {
		String name = varName(var.getDom().getSimpleName().replace('.', '_'), false);
		return "$oobenv." + name + "Var" + var.getStart();
	}
	
	private char getEscapeChar(EspPart container) {
		if(inJava) {
			return 0;
		}
		
		String text = container.getText();
		if(text.isEmpty()) {
			return 0;
		}
		if(text.charAt(0) ==  '$') {
			return getEscapeChar(text, 1);
		}
		if(text.length() < 3) { // no room for an escape char (return default)
			if(inForm())   return 'f';
			if(inScript()) return 'j';
			return 'h';
		}

		return getEscapeChar(text, 0);
	}

	private char getEscapeChar(String text, int ix) {
		if(ix+2 < text.length()) {
			if(text.charAt(ix) == '{' && Character.isWhitespace(text.charAt(ix+2))) {
				switch(text.charAt(ix+1)) {
				case 'n': return 'n'; // null
				case 'h': return 'h'; // HTML
				case 'j': return 'j'; // JSON
				case 'f': return 'f'; // form
				case 'r': return 0;   // raw (aka none)
				}
			}
			if(inForm())   return 'f';
			if(inScript()) return 'j';
			return 'h';
		}
		return 0;
	}
	
	String getJavaTypeName(EspPart type) {
		return getJavaTypeName(type, null);
	}
	
	String getJavaTypeName(EspPart type, Class<?> defaultType) {
		if(type != null && type.hasParts()) {
			for(EspPart part : type.getParts()) {
				if(part.isA(Type.JavaContainer)) {
					return getJavaTypeName(part, defaultType);
				}
				else if(part.isA(Type.JavaSource)) {
					return part.getText();
				}
			}
		}
		return (defaultType != null) ? defaultType.getSimpleName() : null;
	}
	
	EspResolver getResolver() {
		return resolver;
	}
	
	String getSimpleString(EspPart container) {
		List<EspPart> parts = container.getParts();
		if(parts.size() == 1) {
			EspPart part = parts.get(0);
			if(part.isA(Type.JavaString) && ! part.hasParts()) {
				if(inJava) {
					return part.getText();
				} else {
					String s = part.getText();
					return s.substring(1, s.length()-1);
				}
			}
		}
		return null;
	}
	
	ESourceFile getSourceFile() {
		return esf;
	}
	
	private boolean hasClosingTag(String tag) {
		if("capture".equals(tag))    return false;
		if("check".equals(tag))      return false;
		if("contentFor".equals(tag)) return false;
		if("date".equals(tag))       return false;
		if("decimal".equals(tag))    return false;
		if("errors".equals(tag))     return false;
		if("fields".equals(tag))     return false;
		if("file".equals(tag))       return false;
		if("head".equals(tag))       return false;
		if("hidden".equals(tag))     return false;
		if("input".equals(tag))      return false;
		if("messages".equals(tag))   return false;
		if("number".equals(tag))     return false;
		if("options".equals(tag))    return false;
		if("password".equals(tag))   return false;
		if("radio".equals(tag))      return false;
		if("reset".equals(tag))      return false;
		if("submit".equals(tag))     return false;
		if("text".equals(tag))       return false;
		if("title".equals(tag))      return false;
		if("view".equals(tag))       return false;
		if("yield".equals(tag))      return false;
		return true;
	}
	
	void incJavaLevel() {
		javaLevel++;
	}

	StringBuilder indent(StringBuilder sb) {
		int level = 0;
		for(int i = sb.length()-1; i >= 0; i--) {
			if(sb.charAt(i) == '\t') {
				level++;
			} else {
				break;
			}
		}
		for(int i = level; i < javaLevel+2; i++) {
			sb.append('\t');
		}
		return sb;
	}
	
	private boolean inForm() {
		return formCompiler.inForm();
	}
	
	boolean inHead(EspPart part) {
		EspPart parent = part.getParent();
		while(parent != null && !parent.isA(DOM)) {
			switch(parent.getType()) {
			case DOM:
				return false;
			case MarkupElement:
				if("head".equals(((MarkupElement) parent).getTag().getText())) {
					return true;
				}
			default:
				parent = parent.getParent();
			}
		}
		return false;
	}
	
	boolean inJava() {
		return inJava;
	}
	
	private boolean inAsset() {
		return inAsset;
	}
	
	void inAsset(boolean inAsset) {
		this.inAsset = inAsset;
	}
	
	private boolean inScript() {
		return scriptCompiler.inScript();
	}
	
	private void offsetLocations(StringBuilder sb, int offset) {
		for(EspLocation location : locationsMap.get(sb)) {
			location.offset += offset;
		}
	}
	
	String prepFor(EspPart part) {
		String oldSection = sbName;
		if(inHead(part)) {
			prepForHead();
		} else {
			prepForBody();
		}
		return oldSection;
	}
	
	boolean prepFor(String section) {
		if(sbName != section) {
			sbName = section;
			if(!inJava) {
				int pos = body.length()-1;
				boolean empty = true;
				char[] ca = ".append(\"".toCharArray();
				for(int i = ca.length-1; empty && i >= 0; i--, pos--) {
					if(ca[i] != body.charAt(pos)) {
						empty = false;
					}
				}
				if(empty) {
					ca = ((section == SBBODY) ? SBHEAD : SBBODY).toCharArray();
					for(int i = ca.length-1; empty && i >= 0; i--, pos--) {
						if(ca[i] != body.charAt(pos)) {
							empty = false;
						}
					}
					pos++;
					if(empty) {
						// just change the section name and exit
						body.replace(pos, pos+section.length(), section);
						return true;
					} else {
						// delete the previous start, close the section and fall through
						body.delete(pos, body.length());
						body.append(";\n");
					}
				} else {
					// close the section and fall through
					body.append("\");\n");
				}
			}
			indent(body);
			body.append(sbName).append(".append(\"");
			return true;
		}
		return false;
	}
	
	boolean prepForBody() {
		return prepFor(SBBODY);
	}
	
	boolean prepForHead() {
		return prepFor(SBHEAD);
	}
	
	/**
	 * @return true if set to inJava to true; false if was inJava was already true
	 */
	boolean prepForJava(StringBuilder sb) {
		if(inJava) {
			indent(sb);
			return false;
		} else {
			inJava = true;
			String s = sbName + ".append(\"";
			if(sb.length() < s.length()) {
				sb.append("\");\n");
				indent(sb);
			} else {
				for(int i = 0; i < s.length(); i++) {
					if(sb.charAt(sb.length()-i-1) != s.charAt(s.length()-i-1)) {
						sb.append("\");\n");
						indent(sb);
						return true;
					}
				}
				sb.delete(sb.length()-s.length(), sb.length());
			}
			return true;
		}
	}
	
	void prepForMarkup(StringBuilder sb) {
		if(inJava) {
			indent(sb);
			sb.append(sbName(sb)).append(".append(\"");
			inJava = false;
		}
	}
	
	String sbName(StringBuilder sb) {
		if(sb == body) return sbName;
		return SBBODY;
	}
	
	public void setResolver(EspResolver resolver) {
		this.resolver = resolver;
	}
	
	private void startCapture(MarkupElement capture) {
		prepForJava(body);

		if(capture.hasArgs()) {
			sbName = capture.getArg(0).getText().trim();
		}
		sbName = sbName + "$" + capture.getStart();
		body.append("StringBuilder ").append(sbName).append(" = new StringBuilder();\n");

		captureElement = capture;
		
		inJava = true;
	}
	
	private void startContent(MarkupElement content) {
		prepForJava(body);

		if(content.hasArgs()) {
			sbName = content.getArg(0).getText().trim();
		}
		
		contentName = sbName;

		if(sbName.charAt(0) == '"' && sbName.charAt(sbName.length()-1) == '"') {
			sbName = underscored(sbName.substring(1, sbName.length()-1));
		}
		
		sbName = sbName + "$" + content.getStart();
		body.append("StringBuilder ").append(sbName).append(" = new StringBuilder();\n");

		captureElement = content;
		
		inJava = true;
	}

	private void stopCapture() {
		if(contentName != null) {
			stopContent();
		} else {
			prepForJava(body);
	
			String var = sbName.substring(0, sbName.lastIndexOf('$'));
			body.append("String ").append(var).append(" = ").append(sbName).append(".toString();\n");
			indent(body);
			body.append(sbName).append(" = null;\n");
			
			captureElement = null;
			sbName = SBBODY;
			
			inJava = true;
		}
	}
	
	private void stopContent() {
		prepForJava(body);

		body.append("putContent(").append(contentName).append(", ").append(sbName).append(".toString());\n");
		indent(body);
		body.append(sbName).append(" = null;\n");
		
		captureElement = null;
		contentName = null;
		sbName = SBBODY;
		
		inJava = true;
	}
	
}
