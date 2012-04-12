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
package org.oobium.build.esp;

import static org.oobium.build.esp.EspPart.Type.ConstructorElement;
import static org.oobium.build.esp.EspPart.Type.DOM;
import static org.oobium.build.esp.EspPart.Type.ImportElement;
import static org.oobium.build.esp.EspPart.Type.InnerTextElement;
import static org.oobium.build.esp.EspPart.Type.JavaElement;
import static org.oobium.build.esp.EspPart.Type.MarkupElement;
import static org.oobium.utils.StringUtils.blank;
import static org.oobium.utils.StringUtils.className;
import static org.oobium.utils.StringUtils.getterName;
import static org.oobium.utils.StringUtils.h;
import static org.oobium.utils.StringUtils.hasserName;
import static org.oobium.utils.StringUtils.plural;
import static org.oobium.utils.StringUtils.titleize;
import static org.oobium.utils.StringUtils.underscored;
import static org.oobium.utils.StringUtils.varName;
import static org.oobium.utils.coercion.TypeCoercer.coerce;
import static org.oobium.utils.literal.Set;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.oobium.app.controllers.HttpController;
import org.oobium.app.http.Action;
import org.oobium.app.views.ScriptFile;
import org.oobium.app.views.StyleSheet;
import org.oobium.app.views.View;
import org.oobium.build.esp.ESourceFile.EspLocation;
import org.oobium.build.esp.ESourceFile.JavaSource;
import org.oobium.build.esp.elements.ConstructorElement;
import org.oobium.build.esp.elements.ImportElement;
import org.oobium.build.esp.elements.InnerTextElement;
import org.oobium.build.esp.elements.JavaElement;
import org.oobium.build.esp.elements.MarkupCommentElement;
import org.oobium.build.esp.elements.MarkupElement;
import org.oobium.build.esp.elements.MethodSignatureElement;
import org.oobium.build.esp.elements.ScriptElement;
import org.oobium.build.esp.elements.StyleChildElement;
import org.oobium.build.esp.elements.StyleElement;
import org.oobium.build.esp.parts.MethodSignatureArg;
import org.oobium.build.esp.parts.EmbeddedJavaPart;
import org.oobium.build.esp.parts.EntryPart;
import org.oobium.build.esp.parts.JavaPart;
import org.oobium.build.esp.parts.JavaSourcePart;
import org.oobium.build.esp.parts.ScriptEntryPart;
import org.oobium.build.esp.parts.ScriptPart;
import org.oobium.build.esp.parts.StyleEntryPart;
import org.oobium.build.esp.parts.StylePropertyPart;
import org.oobium.build.esp.parts.StylePropertyValuePart;
import org.oobium.mailer.MailerTemplate;
import org.oobium.persist.Model;
import org.oobium.utils.ArrayUtils;
import org.oobium.utils.StringUtils;
import org.oobium.utils.Utils;
import org.oobium.utils.json.JsonUtils;

public class EspCompiler {

	private static final String SBHEAD = "__head__";
	private static final String SBBODY = "__body__";

	
	private static String getFormModelNameVar(int start) {
		return "formModelName$" + start;
	}
	
	private static String getFormModelVar(int start) {
		return "formModel$" + start;
	}
	
	private static String getFormModelVar(MarkupElement form) {
		if(form.hasJavaType()) {
			return getFormModelVar(form.getStart());
		} else {
			String action = (form.getArgs().size() == 2) ? form.getArg(1).getText().trim() : null;
			boolean hasMany = !(action == null || "create".equalsIgnoreCase(action) || "update".equalsIgnoreCase(action) || "delete".equalsIgnoreCase(action));
			if(hasMany) {
				return getFormModelVar(form.getStart());
			} else {
				String arg = form.getArg(0).getText().trim();
				for(int i = 0; i < arg.length(); i++) {
					if(!Character.isJavaIdentifierPart(arg.charAt(i))) {
						return getFormModelVar(form.getStart());
					}
				}
				return arg;
			}
		}
	}
	
	
	private EspResolver resolver;
	
	private final String pkg;
	private final EspDom dom;
	private ESourceFile esf;
	private StringBuilder body;
	private List<EspLocation> bodyLocations;
	private boolean inJava;
	private int javaLevel;
	private String sbName;
	private int captureLevel;
	
	private String contentName;
	
	private Map<StringBuilder, List<EspLocation>> locationsMap = new HashMap<StringBuilder, List<EspLocation>>();
	
	public EspCompiler(EspResolver resolver, String packageName, EspDom dom) {
		this.resolver = resolver;
		this.pkg = packageName;
		this.dom = dom;
		this.sbName = SBBODY;
		this.captureLevel = -1;
	}

	public EspCompiler(String packageName, EspDom dom) {
		this(null, packageName, dom);
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

	private StringBuilder appendAttr(String name, EntryPart entry) {
		return appendAttr(name, entry, false);
	}
	
	/**
	 * append an HTML attribute, ensuring that double quotes are around the value,
	 * whether it is a simple string or Java expression
	 */
	private StringBuilder appendAttr(String name, EntryPart entry, boolean hidden) {
		if(entry.isConditional()) {
			prepForJava(body);
			body.append("if(");
			build(entry.getCondition(), body);
			body.append(") {\n\t");
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
	
	private StringBuilder appendAttr(String name, EspPart value) {
		return appendAttr(name, value, false);
	}
	
	private StringBuilder appendAttr(String name, EspPart value, boolean hidden) {
		body.append(' ').append(name);
		if(value != null) {
			body.append('=');
			int q1 = body.length();
			build(value, body);
			ensureQuotes(body, q1, true);
			if(hidden && "style".equals(name)) {
				body.insert(body.length()-2, ";display:none");
			}
		}
		return body;
	}
	
	private void appendConfirmCloser(MarkupElement element) {
		if(element.hasEntryValue("confirm")) {
			body.append("}");
		}
	}
	
	private void appendConfirmOpener(MarkupElement element) {
		if(element.hasEntryValue("confirm")) {
			JavaSourcePart part = (JavaSourcePart) element.getEntryValue("confirm");
			body.append("if(confirm('");
			if(part.isSimple()) {
				String text = part.getText();
				body.append(text.substring(1, text.length()-1));
			} else {
				build(part, body);
			}
			body.append("')) {");
		}
	}
	
	private void appendCreateJs(MarkupElement element, JavaSourcePart target) {
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
		build(element.getEntryValue("field"), body);
		body.append("');");
		body.append("m.setAttribute('value', '");
		build(element.getEntryValue("value"), body);
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

	private void appendDeleteJs(MarkupElement element, JavaSourcePart target) {
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

	private void appendEntryValueWithoutQuotes(MarkupElement element, String key) {
		JavaSourcePart part = (JavaSourcePart) element.getEntryValue(key);
		if(part.isSimple()) {
			build(part, body, true);
		} else {
			JavaSourcePart part2 = new JavaSourcePart(part);
			build(part2, body, true);
		}
	}
	
	private void appendEscaped(StringBuilder sb, String text) {
		appendEscaped(sb, text, 0, text.length());
	}
	
	private void appendEscaped(StringBuilder sb, String text, int start, int end) {
		if(inJava) {
			sb.append(text, start, end);
		} else {
			for(int i = start; i < end; i++) {
				char c = text.charAt(i);
				switch(c) {
				case '"':	if(i == 0 || text.charAt(i-1) != '\\') { sb.append("\\\""); } break;
				case '\t':	if(i == 0 || text.charAt(i-1) != '\\') { sb.append("\\t"); } break;
				default:	sb.append(c); break;
				}
			}
		}
	}
	
	private void appendFieldError(String model, List<JavaSourcePart> fields, String str, boolean newAttr) {
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
	
	private void appendFormFieldName(String name, List<JavaSourcePart> fields) {
		if(inJava) {
			body.append(name).append(" + \"");
		} else {
			body.append("\").append(").append(name).append(").append(\"");
		}
		for(JavaSourcePart field : fields) {
			body.append('[');
			if(field.isSimple()) {
				String text = field.getText();
				body.append(text.substring(1, text.length()-1));
			} else {
				build(field, body);
			}
			body.append(']');
		}
		if(inJava) {
			body.append("\"");
		}
	}
	
	private String appendJavaType(MarkupElement element, Class<?> defaultType) {
		String type;
		if(element.hasJavaType()) {
			type = element.getJavaType();
			addLocation(element.getJavaTypePart(), body);
			body.append(type);
		} else if(defaultType != null) {
			type = defaultType.getSimpleName();
			body.append(type);
			String name = defaultType.getCanonicalName();
			if(!name.startsWith("java.lang.")) {
				esf.addImport(name);
			}
		} else {
			type = null;
		}
		return type;
	}
	
	private void appendUpdateJs(MarkupElement element, JavaSourcePart target) {
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
		build(element.getEntryValue("field"), body);
		body.append("');");
		body.append("m.setAttribute('value', '");
		build(element.getEntryValue("value"), body);
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
	
	private void appendValueGetter(String model, List<JavaSourcePart> fields) {
		if(fields.size() == 1) {
			body.append(model).append('.').append(getter(fields.get(0)));
		} else {
			int last = fields.size() - 1;
			for(int i = 0; i < fields.size(); i++) {
				body.append(model);
				for(int j = 0; j <= i; j++) {
					body.append('.').append((j == i && j != last) ? hasser(fields.get(j)) : getter(fields.get(j)));
				}
				if(i < last) {
					body.append("?(");
				}
			}
			for(int i = 0; i < last; i++) {
				body.append("):\"\"");
			}
		}
	}

	private void build(EspPart part, StringBuilder sb) {
		build(part, sb, false);
	}
	
	/**
	 * @param forceLastIsJava if true, then will behave as if inJava == true
	 */
	private void build(EspPart part, StringBuilder sb, boolean forceLastIsJava) {
		if(part instanceof JavaSourcePart) {
			build((JavaSourcePart) part, sb, forceLastIsJava);
		}
		else if(part instanceof ScriptPart) {
			build((ScriptPart) part, sb);
		}
		else if(part instanceof StyleEntryPart) {
			build((StyleEntryPart) part, sb);
		}
		else if(part instanceof StylePropertyValuePart) {
			build((StylePropertyValuePart) part, sb);
		}
		else {
			if(part == null) {
				return; // occurs when part is supposed to be the value of an entry part, but it has not been created yet: "key:"
			}
			else if(part.hasParts()) {
				String text = part.getText();
				List<EspPart> parts = part.getParts();
				for(int i = 0; i < parts.size(); i++) {
					EspPart sub = parts.get(i);
					if(sub instanceof JavaPart) {
						JavaPart jpart = (JavaPart) sub;
						int s1 = jpart.getStart() - part.getStart();
						int s2 = s1 + jpart.getLength();
						if(i == 0) {
							if(s1 > 0) {
								appendEscaped(sb, text, 0, s1);
							}
						} else {
							int s0 = parts.get(i-1).getEnd() - part.getStart();
							if(s0 < s1) {
								appendEscaped(sb, text, s0, s1);
							}
						}
						sb.append("\").append(");
						if(jpart.isEscaped()) {
							sb.append(jpart.getEscapeChar()).append('(');
						}
						EspPart spart = jpart.getSourcePart();
						if(spart != null) {
							addLocation(spart, sb);
						}
						sb.append(jpart.getSource());
						if(jpart.isEscaped()) {
							sb.append(')');
						}
						sb.append(").append(\"");
						if(i == parts.size() - 1) {
							if(s2 < text.length()) {
								appendEscaped(sb, text, s2, text.length());
							}
						}
					} else {
						int s1 = sub.getStart() - part.getStart();
						int s2 = s1 + sub.getLength();
						if(i == 0) {
							if(s1 > 0) {
								appendEscaped(sb, text, 0, s1);
							}
						} else {
							int s0 = parts.get(i-1).getEnd() - part.getStart();
							if(s0 < s1) {
								appendEscaped(sb, text, s0, s1);
							}
						}
						appendEscaped(sb, text, s1, s2);
						if(i == parts.size() - 1) {
							if(s2 < text.length()) {
								appendEscaped(sb, text, s2, text.length());
							}
						}
					}
				}
			}
			else {
				appendEscaped(sb, part.getText());
			}
		}
	}
	
	private void build(JavaSourcePart jpart, StringBuilder sb, boolean forceLastIsJava) {
		if(jpart == null) {
			return; // occurs when part is supposed to be the value of an entry part, but it has not been created yet: "key:"
		}
		String text = jpart.getText();
		if(jpart.isSimple()) {
			if(forceLastIsJava || inJava) {
				addLocation(jpart, sb);
				sb.append(text);
			} else {
				addLocation(jpart, sb);
				appendEscaped(sb, text);
			}
		} else {
			if(jpart.hasParts()) {
				List<EspPart> parts = jpart.getParts();
				for(int i = 0; i < parts.size(); i++) {
					EspPart sub = parts.get(i);
					if(sub instanceof EmbeddedJavaPart) {
						boolean instring = false;
						EmbeddedJavaPart jsspart = (EmbeddedJavaPart) sub;
						int s1 = jsspart.getStart() - jpart.getStart();
						int s2 = s1 + jsspart.getLength();
						if(i == 0) {
							if(s1 > 0) {
								if(forceLastIsJava || inJava) {
									sb.append('"');
								}
								appendEscaped(sb, text, 0, s1);
								instring = true;
							}
						} else {
							int s0 = parts.get(i-1).getEnd() - jpart.getStart();
							if(s0 < s1) {
								sb.append(").append(\"");
								appendEscaped(sb, text, s0, s1);
								instring = true;
							}
						}
						if(instring) {
							sb.append("\").append(");
						}
						if(jpart instanceof ScriptEntryPart) {
							sb.append("j(");
						} else {
							sb.append("h(");
						}
						EspPart spart = jsspart.getSourcePart();
						if(spart != null) {
							addLocation(spart, sb);
						}
						sb.append(jsspart.getSource());
						sb.append(")");
						if(i == parts.size() - 1) {
							if(s2 < text.length()) {
								sb.append(").append(\"");
								appendEscaped(sb, text, s2, text.length());
								if(forceLastIsJava || inJava) {
									sb.append('"');
								}
							}
						}
					} else {
						build(sub, sb);
					}
				}
			} else {
				if(forceLastIsJava || inJava) {
					addLocation(jpart, sb);
					sb.append(text);
				} else {
					sb.append("\").append(h(");
					addLocation(jpart, sb);
					sb.append(text);
					sb.append(")).append(\"");
				}
			}
		}
	}
	
	private void build(ScriptPart jpart, StringBuilder sb) {
		if(jpart == null) {
			return; // occurs when part is supposed to be the value of an entry part, but it has not been created yet: "key:"
		}
		String text = jpart.getText();
		if(jpart.isSimple()) {
			appendEscaped(sb, text);
		} else {
			List<EspPart> parts = jpart.getParts();
			for(int i = 0; i < parts.size(); i++) {
				EspPart sub = parts.get(i);
				if(sub instanceof EmbeddedJavaPart) {
					EmbeddedJavaPart embedded = (EmbeddedJavaPart) sub;
					int s1 = embedded.getStart() - jpart.getStart();
					int s2 = s1 + embedded.getLength();
					if(i == 0) {
						if(s1 > 0) {
							appendEscaped(sb, text, 0, s1);
						}
					} else {
						int s0 = parts.get(i-1).getEnd() - jpart.getStart();
						if(s0 < s1) {
							appendEscaped(sb, text, s0, s1);
						}
					}
					sb.append("\").append(j(");
					EspPart spart = embedded.getSourcePart();
					if(spart != null) {
						addLocation(spart, sb);
					}
					sb.append(embedded.getSource());
					sb.append(")).append(\"");
					if(i == parts.size() - 1) {
						if(s2 < text.length()) {
							appendEscaped(sb, text, s2, text.length());
						}
					}
				} else {
					int s1 = sub.getStart() - jpart.getStart();
					int s2 = s1 + sub.getLength();
					if(i == 0) {
						if(s1 > 0) {
							appendEscaped(sb, text, 0, s1);
						}
					} else {
						int s0 = parts.get(i-1).getEnd() - jpart.getStart();
						if(s0 < s1) {
							appendEscaped(sb, text, s0, s1);
						}
					}
					appendEscaped(sb, sub.getText());
					if(i == parts.size() - 1) {
						if(s2 < text.length()) {
							appendEscaped(sb, text, s2, text.length());
						}
					}
				}
			}
		}
	}
	
	private void build(StyleEntryPart part, StringBuilder sb) {
		if(part.isJava()) {
			build(part.getJava(), sb);
		} else {
			List<StylePropertyPart> properties = part.getProperties();
			if(properties != null) {
				for(int i = 0; i < properties.size(); i++) {
					StylePropertyPart property = properties.get(i);
					if(property.hasName() && property.hasValue()) {
						if(i != 0) sb.append(';');
						sb.append(property.getName().getText());
						sb.append(':');
						build(property.getValue(), sb);
					}
				}
			}
		}
	}
	
	private void build(StylePropertyValuePart part, StringBuilder sb) {
		if(part == null) {
			return;
		}
		String text = part.getText();
		if(part.isSimple()) {
			appendEscaped(sb, text);
		} else {
			List<EspPart> parts = part.getParts();
			for(int i = 0; i < parts.size(); i++) {
				EspPart sub = parts.get(i);
				if(sub instanceof EmbeddedJavaPart) {
					EmbeddedJavaPart embedded = (EmbeddedJavaPart) sub;
					int s1 = embedded.getStart() - part.getStart();
					int s2 = s1 + embedded.getLength();
					if(i == 0) {
						if(s1 > 0) {
							appendEscaped(sb, text, 0, s1);
						}
					} else {
						int s0 = parts.get(i-1).getEnd() - part.getStart();
						if(s0 < s1) {
							appendEscaped(sb, text, s0, s1);
						}
					}
					sb.append("\").append(h(");
					EspPart spart = embedded.getSourcePart();
					if(spart != null) {
						addLocation(spart, sb);
					}
					sb.append(embedded.getSource());
					sb.append(")).append(\"");
					if(i == parts.size() - 1) {
						if(s2 < text.length()) {
							appendEscaped(sb, text, s2, text.length());
						}
					}
				} else {
					build(sub, sb);
				}
			}
		}
	}
	
	private void buildAttrs(MarkupElement element, String...skip) {
		boolean hidden = element.isHidden();
		Set<String> skipSet = Set(skip);
		if(element.hasEntries()) {
			Map<String, EntryPart> entries = element.getEntries();
			for(EntryPart entry : entries.values()) {
				String key = entry.getKey().getText().trim();
				if(!skipSet.contains(key)) {
					appendAttr(key, entry, hidden);
				}
			}
			if(hidden && !entries.containsKey("style")) {
				body.append(" style=\\\"display:none\\\"");
			}
		} else if(hidden) {
			body.append(" style=\\\"display:none\\\"");
		}
	}

	private void buildCheck(MarkupElement check) {
		if(check.hasArgs()) {
			List<JavaSourcePart> fields = check.getArgs();
			String model = getFormModel(check);
			String modelName = getFormModelName(check);
			if(!blank(modelName)) {
				body.append("<input type=\\\"hidden\\\" name=\\\"");
				if(check.hasEntry("name")) {
					build(check.getEntry("name").getValue(), body);
				} else {
					appendFormFieldName(modelName, fields);
				}
				body.append("\\\" value=\\\"false\\\" />");
				
				body.append("<input type=\\\"checkbox\\\"");
				body.append(" id=\\\"");
				if(check.hasId()) {
					build(check.getId(), body);
				} else if(check.hasEntry("id")) {
					build(check.getEntry("id").getValue(), body);
				} else {
					appendFormFieldName(modelName, fields);
				}
				body.append("\\\"");
				buildClasses(check, model, fields);
				body.append(" name=\\\"");
				if(check.hasEntry("name")) {
					build(check.getEntry("name").getValue(), body);
				} else {
					appendFormFieldName(modelName, fields);
				}
				body.append("\\\" value=\\\"true\\\"");
				buildAttrs(check, "id", "name", "value", "type");
				body.append("\");\n");
				indent(body);
				body.append("if(");
				appendValueGetter(model, fields);
				body.append(") {\n");
				javaLevel++;
				indent(body);
				body.append(sbName).append(".append(\" CHECKED\");\n");
				javaLevel--;
				indent(body);
				body.append("}\n");
				indent(body);
				body.append(sbName).append(".append(\"");
			}
		} else {
			body.append("<input type=\\\"checkbox\\\"");
			buildId(check);
			buildClasses(check);
			buildAttrs(check, "type");
		}
		body.append(" />");
	}
	
	private void buildChildren(MarkupElement element) {
		if(element.hasChildren()) {
			for(EspElement child : element.getChildren()) {
				if(child.isElementA(JavaElement)) {
					buildJava((JavaElement) child, true);
				} else if(child.isElementA(InnerTextElement)) {
					InnerTextElement innerText = (InnerTextElement) child;
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
				} else {
					buildElement(child);
				}
			}
		}
	}

	private void buildClasses(MarkupElement element) {
		if(element.hasClassNames()) {
			body.append(" class=\\\"");
			for(Iterator<EspPart> ci = element.getClassNames().iterator(); ci.hasNext(); ) {
				build(ci.next(), body);
				if(ci.hasNext()) body.append(' ');
			}
			body.append("\\\"");
		}
	}
	
	private void buildClasses(MarkupElement element, String model, List<JavaSourcePart> fields) {
		if(!"hidden".equals(element.getTag())) {
			String cssClass = "label".equals(element.getTag()) ? "labelWithErrors\\\"" : "fieldWithErrors\\\"";
			if(element.hasClassNames()) {
				body.append(" class=\\\"");
				for(Iterator<EspPart> ci = element.getClassNames().iterator(); ci.hasNext(); ) {
					build(ci.next(), body);
					if(ci.hasNext()) body.append(' ');
				}
				
				appendFieldError(model, fields, " " + cssClass, false);
			} else {
				appendFieldError(model, fields, " class=\\\"" + cssClass, true);
			}
		}
	}
	
	private void buildComment(MarkupCommentElement comment) {
		prepForMarkup(body);

		body.append("<!--");
		if(comment.hasEntryValue("if")) {
			body.append("[if \").append(");
			build(comment.getEntryValue("if"), body, true);
			body.append(").append(\"]>");
		}

		if(comment.hasInnerText()) {
			build(comment.getInnerText(), body);
		}

		buildChildren(comment);
		
		if(comment.hasEntryValue("if")) {
			body.append("<![endif]");
		}
		body.append("-->");

		inJava = false;
	}
	
	private void buildDateInputs(MarkupElement date) {
		body.append("<span");
		buildFormField(date, false);
		body.append(">\");\n");
		indent(body);
		body.append(sbName).append(".append(dateTimeTags(");
		inJava = true;
		if(date.hasEntry("name")) {
			build(date.getEntryValue("name"), body);
		}
		else if(date.hasId()) {
			build(date.getId(), body);
		}
		else if(date.hasEntry("id")) {
			build(date.getEntryValue("id"), body);
		}
		else if(date.hasArgs()) {
			String modelName = getFormModelName(date);
			if(!blank(modelName)) {
				appendFormFieldName(modelName, date.getArgs());
			}else {
				body.append("\"datetime\"");
			}
		}
		else {
			body.append("\"datetime\"");
		}
		body.append(", ");
		if(date.hasEntry("format")) {
			build(date.getEntryValue("format"), body);
		} else {
			body.append("\"MMM/dd/yyyy\"");
		}
		if(date.hasArgs()) {
			body.append(", ");
			String model = getFormModel(date);
			if(blank(model)) {
				build(date.getArg(0), body);
			} else {
				appendValueGetter(model, date.getArgs());
			}
		}
		body.append("));\n");
		indent(body);
		body.append(sbName).append(".append(\"</span>");
		inJava = false;
	}
	
	private void buildDecimal(MarkupElement input) {
		
		// TODO incomplete - based on buildNumber - does not allow setting scale
		
		body.append("<input type=\\\"text\\\"");
		if(input.hasArgs()) {
			List<JavaSourcePart> fields = input.getArgs();
			String model = getFormModel(input);
			String modelName = getFormModelName(input);
			if(!blank(modelName)) {
				body.append(" id=");
				int q1 = body.length();
				if(input.hasId()) {
					build(input.getId(), body);
				} else if(input.hasEntry("id")) {
					build(input.getEntry("id").getValue(), body);
				} else {
					appendFormFieldName(modelName, fields);
				}
				ensureQuotes(body, q1, true);
				buildClasses(input, model, fields);
				body.append(" name=");
				q1 = body.length();
				if(input.hasEntry("name")) {
					build(input.getEntry("name").getValue(), body);
				} else {
					appendFormFieldName(modelName, fields);
				}
				ensureQuotes(body, q1, true);
				if(input.hasEntry("value")) {
					body.append(" value=");
					q1 = body.length();
					build(input.getEntry("value").getValue(), body);
					ensureQuotes(body, q1, true);
				} else {
					body.append(" value=\\\"\").append(f(");
					appendValueGetter(model, fields);
					body.append(")).append(\"\\\"");
				}
				buildAttrs(input, "id", "name", "value", "onkeypress", "scale");
			}
		} else {
			if(input.hasEntry("type")) {
				body.append(" type=\\\"");
				build(input.getEntry("type").getValue(), body);
				body.append("\\\"");
			}
			buildId(input);
			buildClasses(input);
			buildAttrs(input, "type", "onkeypress", "scale");
		}
		
		String js = "var k=window.event?event.keyCode:event.which;" +
					"if(k==127||k<32){return true}" +
					"if(k==46||(k>47&&k<58)){" +
						"var ix=this.value.indexOf('.');" +
						"if(ix==-1){return true}" +
						"if(k!=46){" +
							"if(document.selection && document.selection.createRange){var pos=document.selection.createRange().getBookmark().charCodeAt(2)-2;}" +
							"else if(this.setSelectionRange){var pos=this.selectionStart;}" +
							"if(pos<=ix||this.value.length<(ix+{scale}+1)){return true;}" +
						"}" +
					"}" +
					"return false;";
		
		js = js.replace("{scale}", coerce(input.getEntryText("scale"), "2"));
		
		if(input.hasEntryValue("onkeypress")) {
			JavaSourcePart part = (JavaSourcePart) input.getEntry("onkeypress").getValue();
			if(part.isSimple()) {
				String text = part.getText();
				body.append(" onkeypress=\\\"").append(text.substring(1, text.length()-1));
			} else {
				body.append(" onkeypress=\\\"");
				build(part, body);
			}
			if(body.charAt(body.length()-1) != ';') {
				body.append(';');
			}
			body.append(js).append("\\\"");
		} else {
			body.append(" onkeypress=\\\"").append(js).append("\\\"");
		}
		body.append(" />");
	}

	private void buildElement(EspElement element) {
		switch(element.getType()) {
		case MarkupCommentElement:
			buildComment((MarkupCommentElement) element);
			break;
		case MarkupElement:
			buildHtml((MarkupElement) element);
			break;
		case ScriptElement:
			buildScript((ScriptElement) element);
			break;
		case StyleElement:
			buildStyle((StyleElement) element);
			break;
		case YieldElement:
			buildYield((MarkupElement) element);
			inJava = true;
			break;
		}
	}
	
	private void buildErrors(MarkupElement element) {
		prepForJava(body);

		body.append("errorsBlock(").append(sbName).append(", ");

		if(element.hasArgs()) {
			build(element.getArg(0), body);
		} else {
			String model = getFormModel(element);
			if(!blank(model)) {
				body.append(model);
			}
		}

		if(element.hasEntryValue("title")) {
			body.append(", ");
			build(element.getEntryValue("title"), body);
		} else {
			body.append(", null");
		}
		
		if(element.hasEntryValue("message")) {
			body.append(", ");
			build(element.getEntryValue("message"), body);
		} else {
			body.append(", null");
		}
		
		body.append(");\n");
		
		inJava = true;
	}
	
	private void buildExternalEjs(StringBuilder sb, JavaSourcePart type, ScriptElement element) {
		prepForJava(sb);
		int pos = element.getStart();
		sb.append("String path$").append(pos).append(" = underscored(");
		build(type, sb);
		sb.append(".class.getName()).replace('.', '/');\n");
		prepForMarkup(sb);
		sb.append("<script src='/\").append(path$").append(pos).append(").append(\".js'></script>");
	}
	
	private void buildExternalEss(StringBuilder sb, JavaSourcePart type, StyleElement element) {
		prepForJava(sb);
		int pos = element.getStart();
		sb.append("String path$").append(pos).append(" = underscored(");
		build(type, sb);
		sb.append(".class.getName()).replace('.', '/');\n");
		prepForMarkup(sb);
		boolean revertSection = prepForHead();
		sb.append("<link rel='stylesheet' type='text/css' href='/\").append(path$").append(pos).append(").append(\".css' />");
		if(revertSection) {
			prepForBody();
		}
	}

	private void buildFields(MarkupElement fields) {
		List<JavaSourcePart> args = fields.getArgs();
		if(args != null && args.size() == 1) {
			String modelName = getFormModelNameVar(fields.getStart());

			prepForJava(body);
			body.append("String ").append(modelName).append(" = ").append(getFormModelNameValue(fields)).append(";\n");
			prepForMarkup(body);

			body.append("<input type=\\\"hidden\\\" name=\\\"\").append(").append(modelName).append(").append(\"[id]\\\"");
			body.append(" value=\\\"\").append(f(");
			build(fields.getArg(0), body, true);
			body.append(")).append(\"").append("\\\" />");
		}
	}
	
	private void buildForm(MarkupElement form) {
		List<JavaSourcePart> args = form.getArgs();
		if(args != null && (args.size() == 1 || args.size() == 2)) {
			String action = (args.size() == 2) ? args.get(1).getText().trim() : null;
			boolean hasMany = !(action == null || "create".equalsIgnoreCase(action) || "update".equalsIgnoreCase(action) || "delete".equalsIgnoreCase(action));

			String type = form.hasJavaType() ? form.getJavaType() : (hasMany ? className(action.replaceAll("\"", "")) : "Model");
			String model = getFormModelVar(form);
			String modelVal = args.get(0).getText().trim();
			String modelName = getFormModelNameVar(form.getStart());
			
			if(type == "Model") { // try to figure it out from the modelVal
				Pattern p = Pattern.compile("\\s*new\\s+(\\w+)\\s*\\(");
				Matcher m = p.matcher(modelVal);
				if(m.find()) {
					type = m.group(1);
				}
			}
			
			prepForJava(body);
			if(!model.equals(modelVal)) {
				if(type.equals("Model")) {
					esf.addImport(Model.class.getCanonicalName());
				}
				body.append(type).append(' ').append(model).append(" = ");
				if(hasMany) {
					body.append("new ").append(type).append("()");
				} else {
					body.append(modelVal);
				}
				body.append(";\n");
				indent(body);
			}
			body.append("String ").append(modelName).append(" = ");
			if(hasMany) {
				body.append('"').append(varName(type)).append('"');
			} else {
				body.append(getFormModelNameValue(form));
			}
			body.append(";\n");
			prepForMarkup(body);
			
			body.append('<').append(form.getTag());
			buildId(form);
			buildClasses(form);
			buildAttrs(form, "action", "method");

			String method = null;
			
			if(action != null) {
				if("create".equalsIgnoreCase(action)) {
					method = "post";
					esf.addStaticImport(Action.class.getCanonicalName() + ".create");
					body.append(" action=\\\"\").append(pathTo(").append(model).append(", create)).append(\"\\\"");
				} else if("update".equalsIgnoreCase(action)) {
					method = "put";
					esf.addStaticImport(Action.class.getCanonicalName() + ".update");
					body.append(" action=\\\"\").append(pathTo(").append(model).append(", update)).append(\"\\\"");
				} else if("destroy".equalsIgnoreCase(action)) {
					method = "delete";
					esf.addStaticImport(Action.class.getCanonicalName() + ".destroy");
					body.append(" action=\\\"\").append(pathTo(").append(model).append(", destroy)).append(\"\\\"");
				} else {
					method = "post";
					body.append(" action=\\\"\").append(pathTo(").append(modelVal).append(", ").append(action).append(")).append(\"\\\"");
				}
			}
			if(method == null && form.hasEntry("method")) {
				EspPart value = form.getEntry("method").getValue();
				if(value != null) {
					method = value.getText();
					if(form.hasEntry("action")) {
						body.append(" action=\\\"");
						build(form.getEntry("action").getValue(), body);
						body.append("\\\"");
					} else {
						if("post".equalsIgnoreCase(method)) {
							esf.addStaticImport(Action.class.getCanonicalName() + ".create");
							body.append(" action=\\\"\").append(pathTo(").append(model).append(", create)).append(\"\\\"");
						} else if("put".equalsIgnoreCase(method)) {
							esf.addStaticImport(Action.class.getCanonicalName() + ".update");
							body.append(" action=\\\"\").append(pathTo(").append(model).append(", update)).append(\"\\\"");
						} else if("delete".equalsIgnoreCase(method)) {
							esf.addStaticImport(Action.class.getCanonicalName() + ".destroy");
							body.append(" action=\\\"\").append(pathTo(").append(model).append(", destroy)).append(\"\\\"");
						} else {
							esf.addStaticImport(Action.class.getCanonicalName() + ".create");
							body.append(" action=\\\"\").append(pathTo(").append(model).append(", create)).append(\"\\\"");
						}
					}
				}
			}
			if(method == null && form.hasEntry("action")) {
				EspPart value = form.getEntry("action").getValue();
				if(value != null) {
					action = value.getText();
					if("create".equalsIgnoreCase(action)) {
						method = "post";
						esf.addStaticImport(Action.class.getCanonicalName() + ".create");
						body.append(" action=\\\"\").append(pathTo(").append(model).append(", create)).append(\"\\\"");
					} else if("update".equalsIgnoreCase(action)) {
						method = "put";
						esf.addStaticImport(Action.class.getCanonicalName() + ".update");
						body.append(" action=\\\"\").append(pathTo(").append(model).append(", update)).append(\"\\\"");
					} else if("destroy".equalsIgnoreCase(action)) {
						method = "delete";
						esf.addStaticImport(Action.class.getCanonicalName() + ".destroy");
						body.append(" action=\\\"\").append(pathTo(").append(model).append(", destroy)).append(\"\\\"");
					} else {
						method = "post";
						body.append(" action=\\\"");
						build(value, body);
						body.append("\\\"");
					}
				}
			}
			if(method == null) {
				esf.addImport(Action.class.getCanonicalName());
				body.append(" action=\\\"\").append(pathTo(").append(model).append(", ").append(model).append(".isNew() ? Action.create : Action.update)).append(\"\\\"");
				body.append(" method=\\\"post\\\"");
			} else {
				body.append(" method=\\\"").append(method).append("\\\"");
			}
			if(form.hasEntry("enctype")) {
				body.append(" enctype=\\\"");
				build(form.getEntry("enctype").getValue(), body);
				body.append("\\\"");
			} else {
				if(containsFileInput(form)) {
					body.append(" enctype=\\\"multipart/form-data\\\"");
				}
			}
			body.append('>');
			if(method == null) {
				prepForJava(body);
				body.append("if(!").append(model).append(".isNew()) {\n");
				indent(body);
				body.append('\t').append(sbName).append(".append(\"<input type=\\\"hidden\\\" name=\\\"_method\\\" value=\\\"PUT\\\" />\");\n");
				indent(body);
				body.append("}\n");
				prepForMarkup(body);
			} else if(method.equalsIgnoreCase("put")) {
				body.append("<input type=\\\"hidden\\\" name=\\\"_method\\\" value=\\\"PUT\\\" />");
			} else if(method.equalsIgnoreCase("delete")) {
				body.append("<input type=\\\"hidden\\\" name=\\\"_method\\\" value=\\\"DELETE\\\" />");
			}
			if(hasMany) {
				body.append("<input type=\\\"hidden\\\" name=\\\"id\\\" value=\\\"\").append(f(");
				build(args.get(0), body, true);
				body.append(")).append(\"").append("\\\" />");
			} else {
				body.append("<input type=\\\"hidden\\\" name=\\\"\").append(").append(modelName).append(").append(\"[id]\\\"");
				body.append(" value=\\\"\").append(f(");
				build(args.get(0), body, true);
				body.append(")).append(\"").append("\\\" />");
			}
		} else {
			body.append('<').append(form.getTag());
			buildId(form);
			buildClasses(form);
			buildAttrs(form);
			body.append('>');
		}
	}
	
	private void buildFormField(MarkupElement input, boolean hasValue) {
		if(input.hasArgs()) {
			List<JavaSourcePart> fields = input.getArgs();
			String model = getFormModel(input);
			String modelName = getFormModelName(input);
			if(!blank(modelName)) {
				body.append(" id=\\\"");
				if(input.hasId()) {
					build(input.getId(), body);
				} else if(input.hasEntry("id")) {
					build(input.getEntry("id").getValue(), body);
				} else {
					appendFormFieldName(modelName, fields);
				}
				body.append("\\\"");
				buildClasses(input, model, fields);
				body.append(" name=\\\"");
				if(input.hasEntry("name")) {
					build(input.getEntry("name").getValue(), body);
				} else {
					appendFormFieldName(modelName, fields);
				}
				body.append("\\\"");
				if(hasValue) {
					if(input.hasEntry("value")) {
						appendAttr("value", input.getEntry("value"));
					} else {
						body.append(" value=\\\"\").append(f(");
						appendValueGetter(model, fields);
						body.append(")).append(\"\\\"");
					}
				}
				buildAttrs(input, "id", "name", "value", "type");
			}
		} else {
			buildId(input);
			buildClasses(input);
			buildAttrs(input, "type");
		}
	}
	
	private void buildHtml(MarkupElement element) {
		String tag = element.getTag();
		if("view".equals(tag)) {
			buildView(element);
		} else if("options".equals(tag)) {
			buildSelectOptions(element);
		} else if("label".equals(tag)) {
			buildLabel(element);
		} else if("errors".equals(tag)) {
			buildErrors(element);
		} else if("messages".equals(tag)) {
			buildMessages(element);
		} else if("capture".equals(tag)) {
			startCapture(element);
		} else if("contentFor".equals(tag)) {
			startContent(element);
		} else {
			prepForMarkup(body);
			if("a".equals(tag) || "link".equals(tag) || "button".equals(tag)) {
				buildLink(element);
			} else if("img".equals(tag)) {
				buildImage(element);
			} else if("number".equals(tag)) {
				buildNumber(element);
			} else if("decimal".equals(tag)) {
				buildDecimal(element);
			} else if("form".equals(tag)) {
				buildForm(element);
			} else if("fields".equals(tag)) {
				buildFields(element);
			} else if("date".equals(tag)) {
				buildDateInputs(element);
			} else if("textArea".equals(tag)) {
				tag = "textarea";
				buildTextArea(element);
			} else if("check".equals(tag)) {
				buildCheck(element);
			} else if("radio".equals(tag) || "file".equals(tag) || "input".equals(tag) || "hidden".equals(tag) || "number".equals(tag) || "password".equals(tag) || "text".equals(tag)) {
				buildInput(tag, element);
			} else if("select".equals(tag)) {
				buildSelect(element);
			} else if("submit".equals(tag)) {
				buildSubmit(element);
			} else if("reset".equals(tag)) {
				buildResetInput(element);
			} else if("title".equals(tag)) {
				buildTitle(element);
			} else if("option".equals(tag)) {
				buildSelectOption(element);
			} else if(!"head".equals(tag)) {
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
		}

		buildChildren(element);
		
		if(element.hasClosingTag()) {
			if(inJava) {
				indent(body);
				body.append(sbName).append(".append(\"");
			}
			body.append('<').append('/').append(tag).append('>');
			inJava = false;
		}
	}
	
	private void buildId(MarkupElement element) {
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
		if(element.hasImport()) {
			JavaSource source = new JavaSource(element.getImport(), element.getImportPart());
			if(element.isStatic()) {
				esf.addStaticImport(source);
			} else {
				esf.addImport(source);
			}
		}
	}

	private void buildInlineDynamicAsset(StringBuilder sb, JavaSourcePart type, int start, List<JavaSourcePart> args) {
		String typeName = type.getText();
		String varName = varName(typeName) + "$" + start;
		build(type, sb);
		sb.append(' ').append(varName).append(" = new ").append(typeName);
		if(args != null && !args.isEmpty()) {
			sb.append("(");
			for(Iterator<JavaSourcePart> iter = args.iterator(); iter.hasNext(); ) {
				build(iter.next(), sb);
				if(iter.hasNext()) sb.append(',').append(' ');
			}
			sb.append(");\n");
		} else {
			sb.append("();\n");
		}
		indent(sb);
		sb.append(varName).append(".render(" + sbName + ");\n");
	}

	private void buildInlineEjs(StringBuilder sb, JavaSourcePart type, ScriptElement element) {
		prepForMarkup(sb);
		sb.append("<script>");
		prepForJava(sb);
		buildInlineDynamicAsset(sb, type, element.getStart(), element.getArgs());
		prepForMarkup(sb);
		sb.append("</script>");
	}

	private void buildInlineEss(StringBuilder sb, JavaSourcePart type, StyleElement element) {
		prepForMarkup(sb);
		sb.append("<style>");
		prepForJava(sb);
		buildInlineDynamicAsset(sb, type, element.getStart(), element.getArgs());
		prepForMarkup(sb);
		sb.append("</style>");
	}
	
	private void buildInput(String tag, MarkupElement input) {
		body.append("<input");
		if(input.hasEntry("type")) {
			appendAttr("type", input.getEntry("type"));
		} else {
			if("hidden".equals(tag))		body.append(" type=\\\"hidden\\\"");
			else if("radio".equals(tag))	body.append(" type=\\\"radio\\\"");
			else if("file".equals(tag))		body.append(" type=\\\"file\\\"");
			else if("number".equals(tag))	body.append(" type=\\\"text\\\"");
			else if("password".equals(tag))	body.append(" type=\\\"password\\\"");
			else if("text".equals(tag))		body.append(" type=\\\"text\\\"");
		}
		buildFormField(input, !"password".equals(tag));
		body.append(" />");
	}
	
	private void buildJava(JavaElement element, boolean isStart) {
		JavaSourcePart source = element.getSourcePart();
		if(source != null) {
			prepForJava(body);
			addLocation(element.getSourcePart(), body);
			buildJavaLine(source, body);
			body.append("\n");
			inJava = true;
		}

		if(element.hasChildren()) {
			javaLevel++;
			for(EspElement child : element.getChildren()) {
				if(child.isElementA(JavaElement)) {
					buildJava((JavaElement) child, true);
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

	/**
	 * called _only_ from {@link #buildJava(org.oobium.build.esp.elements.JavaElement, boolean)}
	 */
	private void buildJavaLine(JavaSourcePart jpart, StringBuilder sb) {
		if(jpart == null) {
			return; // occurs when part is supposed to be the value of an entry part, but it has not been created yet: "key:"
		}
		String text = jpart.getText();
		if(jpart.isSimple()) {
			addLocation(jpart, sb);
			sb.append(text);
		} else {
			if(jpart.hasParts()) {
				List<EspPart> parts = jpart.getParts();
				for(int i = 0; i < parts.size(); i++) {
					EspPart sub = parts.get(i);
					if(sub instanceof EmbeddedJavaPart) {
						EmbeddedJavaPart jsspart = (EmbeddedJavaPart) sub;
						int s1 = jsspart.getStart() - jpart.getStart();
						int s2 = s1 + jsspart.getLength();
						if(i == 0) {
							if(s1 > 0) {
								sb.append(text.substring(0, s1));
							}
						} else {
							int s0 = parts.get(i-1).getEnd() - jpart.getStart();
							if(s0 < s1) {
								sb.append(text.substring(s0, s1));
							}
						}
						sb.append("\" + ");
						EspPart spart = jsspart.getSourcePart();
						if(spart != null) {
							addLocation(spart, sb);
						}
						sb.append(jsspart.getSource());
						sb.append(" + \"");
						if(i == parts.size() - 1) {
							if(s2 < text.length()) {
								sb.append(text.substring(s2, text.length()));
							}
						}
					} else {
						build(sub, sb);
					}
				}
			} else {
				addLocation(jpart, sb);
				sb.append(text);
			}
		}
	}
	
	private void buildLabel(MarkupElement label) {
		prepForMarkup(body);
		inJava = false; // set true below if necessary
		body.append("<label");
		buildId(label);
		if(label.hasArgs()) {
			List<JavaSourcePart> fields = label.getArgs();
			String model = getFormModel(label);
			String modelName = getFormModelName(label);
			if(!blank(modelName)) {
				buildClasses(label, model, fields);
				buildAttrs(label, "for", "text");
				body.append(" for=");
				int q1 = body.length();
				if(label.hasEntry("for")) {
					build(label.getEntry("for").getValue(), body);
				} else {
					appendFormFieldName(modelName, fields);
				}
				ensureQuotes(body, q1, true);
				
				body.append('>');
				if(label.hasEntry("text")) {
					int pos = body.length();
					build(label.getEntry("text").getValue(), body);
					if(pos < body.length()) {
						if(body.charAt(pos) == '\\' && pos+1 < body.length() && body.charAt(pos+1) == '"') {
							body.delete(pos, pos+2);
						}
						if(body.charAt(body.length()-2) == '\\' && body.charAt(body.length()-1) == '"') {
							body.delete(body.length()-2, body.length());
						}
					}
				}
				else if(!label.hasInnerText()) {
					JavaSourcePart jpart = fields.get(fields.size()-1);
					if(jpart.isSimple()) {
						String text = jpart.getText();
						body.append(titleize(text.substring(1, text.length()-1)));
					} else {
						body.append("\").append(h(titleize(");
						build(jpart, body, true);
						body.append("))).append(\"");
					}
				}
				if(label.hasInnerText()) {
					build(label.getInnerText(), body);
				}
				
				body.append("\");\n");
				indent(body);
				body.append("if(");
				if(label.hasEntry("required")) {
					build(label.getEntry("required").getValue(), body, true);
				} else {
					body.append(model).append(".isRequired(");
					inJava = true;
					for(int i = 0; i < fields.size(); i++) {
						if(i != 0) body.append(", ");
						build(fields.get(i), body);
					}
					body.append(")");
				}
				body.append(") {\n");
				if(label.hasEntry("requiredClass")) {
					indent(body);
					body.append('\t').append(sbName).append(".append(\"<span class=\\\"");
					build(label.getEntry("requiredClass").getValue(), body);
					body.append("\\\">*</span>\");\n");
				} else {
					indent(body);
					body.append('\t').append(sbName).append(".append(\"<span class=\\\"required\\\">*</span>\");\n");
				}
				indent(body);
				body.append("}\n");
				inJava = true;
			}
		} else {
			buildClasses(label);
			buildAttrs(label, "required", "requiredClass");
			if(label.hasEntryValue("required")) {
				
			}
			body.append('>');
			if(label.hasInnerText()) {
				build(label.getInnerText(), body);
			}
			if(label.hasEntryValue("required")) {
				body.append("\");\n");
				indent(body);
				body.append("if(");
				build(label.getEntryValue("required"), body, true);
				body.append(") {\n");
				if(label.hasEntryValue("requiredClass")) {
					indent(body);
					body.append('\t').append(sbName).append(".append(\"<span class=\\\"");
					build(label.getEntryValue("requiredClass"), body);
					body.append("\\\">*</span>\");\n");
				} else {
					indent(body);
					body.append('\t').append(sbName).append(".append(\"<span class=\\\"required\\\">*</span>\");\n");
				}
				indent(body);
				body.append("}\n");
				inJava = true;
			}
		}
	}
	
	private void buildLink(MarkupElement link) {
		body.append('<').append(link.getTag());
		buildId(link);
		buildClasses(link);
		JavaSourcePart target = null;
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
			} else if(dom.isEsp()) { // size == 2
				JavaSourcePart part = link.getArg(1);
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
			} else if(dom.isEmt()) { // size == 2
				// mailers can't use JavaScript or pathTo (uses urlTo instead)
				JavaSourcePart part = link.getArg(1);
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

	private void buildMethod(StringBuilder sb, String name, String sig, List<EspLocation> locations, boolean lastIsJava) {
		String s = sb.toString();
		if(("\t\t" + SBBODY + ".append(\"").equals(s)) {
			sb.delete(0, sb.length());
		} else {
			sb = new StringBuilder(sig.length() + s.length() + 15);
			sb.append(sig).append(s);
			if(lastIsJava) {
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
			for(EspLocation location : locations) {
				location.offset += sig.length();
			}
			esf.addMethod(name, sb.toString(), locations);
		}
	}
	
	private void buildMethod(StyleChildElement element) {
		prepFor(element);
		body.append("\").append(h(");
		String mname = element.getMethodName();
		addLocation(element.getFirstSelector(), body).length = mname.length();
		body.append(mname).append('(');
		for(JavaSourcePart arg : element.getArgs()) {
			if(body.charAt(body.length()-1) != '(') body.append(", ");
			build(arg, body, true);
		}
		body.append("))).append(\"");
	}

	private void buildMethodSignature(MethodSignatureElement element) {
		String rtype = element.getReturnType();
		String mname = element.getMethodName();
		boolean ctor = (element instanceof ConstructorElement);
		
		StringBuilder sb;
		List<MethodSignatureArg> args = element.hasSignatureArgs() ? element.getSignatureArgs() : new ArrayList<MethodSignatureArg>(0);
		if(canBuildPartMethodSignatures(args)) {
			for(int i = 0; i < args.size(); i++) {
				if(args.get(i).hasDefaultValue()) {
					sb = new StringBuilder();
					sb.append("\tpublic ");
					if(element.isStatic()) sb.append("static ");
					if(rtype != null && rtype.length() > 0) {
						sb.append(rtype).append(' ');
					}
					sb.append(mname).append('(');
					for(int j = 0; j < i; j++) {
						MethodSignatureArg arg = args.get(j);
						String declaration = arg.getVarType() + " " + arg.getVarName();
						if(j != 0) sb.append(", ");
						sb.append(declaration);
					}
					sb.append(") {\n\t\t");
					if(ctor) {
						sb.append("this");
					} else {
						if(rtype != null && rtype.length() > 0) {
							sb.append("return ");
						}
						sb.append(mname);
					}
					sb.append("(");
					for(int j = 0; j < args.size(); j++) {
						MethodSignatureArg arg = args.get(j);
						if(j != 0) sb.append(", ");
						if(j < i) {
							sb.append(arg.getVarName());
						} else {
							sb.append(arg.getDefaultValue());
						}
					}
					sb.append(");\n");
					sb.append("\t}");
					JavaSource source = new JavaSource(sb.toString());
					if(ctor) {
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
		if(element.isStatic()) sb.append("static ");
		if(!blank(rtype)) sb.append(rtype).append(' ');
		addLocation(element, sb);
		sb.append(mname).append('(');
		for(int i = 0; i < args.size(); i++) {
			MethodSignatureArg arg = args.get(i);
			if(i != 0) sb.append(", ");
			addLocation(arg, sb);
			if(arg.hasVarType()) {
				String vtype = arg.getVarType();
				addLocation(arg.getVarTypePart(), sb);
				sb.append(vtype);
				if(arg.isVarArgs()) {
					vtype = vtype + "[]";
					sb.append('.').append('.').append('.');
				} else {
					sb.append(' ');
				}
				if(arg.hasVarName()) {
					addLocation(arg.getVarNamePart(), sb);
					String name = arg.getVarName();
					sb.append(name);
					if(ctor) esf.addVariable(name, "public " + vtype + " " + name);
				}
			}
		}
		sb.append(") {\n");
		if(ctor) {
			for(int j = 0; j < args.size(); j++) {
				MethodSignatureArg arg = args.get(j);
				sb.append("\t\tthis.").append(arg.getVarName()).append(" = ");
				if(j <= args.size()) {
					sb.append(arg.getVarName());
				} else {
					sb.append(arg.getDefaultValue());
				}
				sb.append(";\n");
			}
		} else {
			offsetLocations(sb, mname.startsWith("id") ? 1 : 4);
			StyleChildElement sce = (StyleChildElement) element;
			if(sce.hasProperties()) {
				sb.append("\t\tStringBuilder __sb__ = new StringBuilder();\n");
				sb.append("\t\t__sb__.append(\"");
				buildStyleProperties(sb, sce);
				sb.append("\");\n");
				sb.append("\t\treturn __sb__.toString();\n");
			} else {
				sb.append("\t\treturn \"\";\n");
			}
		}
		sb.append("\t}");
		JavaSource source = new JavaSource(sb.toString(), locationsMap.remove(sb));
		if(ctor) {
			esf.addConstructor(source);
		} else {
			esf.addMethod(mname + args.size(), source);
		}
	}
	
	private void buildNumber(MarkupElement input) {
		body.append("<input type=\\\"text\\\"");
		if(input.hasArgs()) {
			List<JavaSourcePart> fields = input.getArgs();
			String model = getFormModel(input);
			String modelName = getFormModelName(input);
			if(!blank(modelName)) {
				body.append(" id=");
				int q1 = body.length();
				if(input.hasId()) {
					build(input.getId(), body);
				} else if(input.hasEntry("id")) {
					build(input.getEntry("id").getValue(), body);
				} else {
					appendFormFieldName(modelName, fields);
				}
				ensureQuotes(body, q1, true);
				buildClasses(input, model, fields);
				body.append(" name=");
				q1 = body.length();
				if(input.hasEntry("name")) {
					build(input.getEntry("name").getValue(), body);
				} else {
					appendFormFieldName(modelName, fields);
				}
				ensureQuotes(body, q1, true);
				if(input.hasEntry("value")) {
					body.append(" value=");
					q1 = body.length();
					build(input.getEntry("value").getValue(), body);
					ensureQuotes(body, q1, true);
				} else {
					body.append(" value=\\\"\").append(f(");
					appendValueGetter(model, fields);
					body.append(")).append(\"\\\"");
				}
				buildAttrs(input, "id", "name", "value", "onkeypress");
			}
		} else {
			if(input.hasEntry("type")) {
				body.append(" type=\\\"");
				build(input.getEntry("type").getValue(), body);
				body.append("\\\"");
			}
			buildId(input);
			buildClasses(input);
			buildAttrs(input, "type", "onkeypress");
		}
		
		String js = "var k=window.event?event.keyCode:event.which;return (k==127||k<32||(k>47&&k<58));";

		if(input.hasEntryValue("onkeypress")) {
			JavaSourcePart part = (JavaSourcePart) input.getEntry("onkeypress").getValue();
			if(part.isSimple()) {
				String text = part.getText();
				body.append(" onkeypress=\\\"").append(text.substring(1, text.length()-1));
			} else {
				body.append(" onkeypress=\\\"");
				build(part, body);
			}
			if(body.charAt(body.length()-1) != ';') {
				body.append(';');
			}
			body.append(js).append("\\\"");
		} else {
			body.append(" onkeypress=\\\"").append(js).append("\\\"");
		}
		body.append(" />");
	}
	
	private void buildResetInput(MarkupElement element) {
		body.append("<input");
		body.append(" type=\\\"reset\\\"");
		buildId(element);
		buildClasses(element);
		buildAttrs(element, "type");
		if(!element.hasEntry("value")) {
			body.append(" value=\\\"Reset\\\"");
		}
		body.append(" />");
	}

	private void buildScript(ScriptElement element) {
		prepFor(element);
		
		JavaSourcePart type = element.getJavaTypePart();
		if(type != null) {
			if("true".equals(element.getEntryText("inline"))) {
				buildInlineEjs(body, type, element);
			} else {
				buildExternalEjs(body, type, element);
			}
		} else {
			prepForMarkup(body);
			
			if(element.hasArgs()) {
				for(JavaSourcePart arg : element.getArgs()) {
					if(arg.isSimple()) {
						String txt = arg.getText();
						body.append("<script src='").append(txt.substring(1, txt.length()-1)).append("'></script>");
					} else {
						body.append("<script src='\").append(");
						build(arg, body, true);
						body.append(").append(\"'></script>");
					}
				}
			}
			if(element.hasLines()) {
				List<ScriptPart> lines = element.getLines();
				if(!dom.isEjs()) {
					body.append("<script>");
				}
				for(int i = 0; i < lines.size(); i++) {
					ScriptPart line = lines.get(i);
					if(i != 0) body.append("\\n");
					build(line, body);
				}
				if(!dom.isEjs()) {
					body.append("</script>");
				}
			}
			
			inJava = false;
		}
	}
	
	private void buildSelect(MarkupElement select) {
		body.append("<select");
		buildFormField(select, false);
		body.append(">");
	}
	
	private void buildSelectOption(MarkupElement option) {
		body.append("<option");
		buildId(option);
		buildClasses(option);
		if(option.hasArgs()) {
			buildAttrs(option, "value");
			body.append(" value=");
			build(option.getArg(0), body);
		} else {
			buildAttrs(option);
		}
		body.append('>');
		if(option.hasInnerText()) {
			build(option.getInnerText(), body);
		}
	}
	
	// options<Member>(findAllMembers(), text:"option.getNameLF()", value:"option.getId(), required: "false", sort:"option.getNameLF()")
	private void buildSelectOptions(MarkupElement element) {
		if(element.hasArgs()) {
			prepForJava(body);

			JavaSourcePart options = element.getArg(0);
			Object selection = element.hasArg(1) ? element.getArg(1) : getSelectionGetter(element);
			if(element.hasEntry("text") || element.hasEntry("value")) {
				if(blank(selection)) {
					body.append("for(");
					String var = varName(appendJavaType(element, Object.class));
					body.append(" ").append(var).append(" : ");
					build(options, body, true);
					body.append(") {\n");
					indent(body);
					if(element.hasEntryValue("title")) {
						body.append('\t').append(sbName).append(".append(\"<option title=\\\"\").append(");
						appendEntryValueWithoutQuotes(element, "title");
						body.append(").append(\"\\\" value=\\\"\").append(");
					} else {
						body.append('\t').append(sbName).append(".append(\"<option value=\\\"\").append(");
					}
					if(element.hasEntryValue("value")) {
						appendEntryValueWithoutQuotes(element, "value");
					} else {
						body.append("f(").append(var).append(')');
					}
					body.append(").append(\"\\\" >\").append(");
					if(element.hasEntryValue("text")) {
						appendEntryValueWithoutQuotes(element, "text");
					} else {
						body.append("h(String.valueOf(").append(var).append("))");
					}
					body.append(").append(\"</option>\");\n");
					indent(body);
					body.append("}\n");
				} else {
					String selectionVar = "selection$" + element.getStart();
					String selectedVar = "selected$" + element.getStart();
					
					body.append("Object ").append(selectionVar).append(" = ").append(selection).append(";\n");
					indent(body);
					body.append("for(");
					String var = varName(appendJavaType(element, Object.class));
					body.append(" ").append(var).append(" : ");
					build(options, body, true);
					body.append(") {\n");
					indent(body);
					body.append("\tboolean ").append(selectedVar).append(" = isEqual(");
					if(element.hasEntryValue("value")) {
						appendEntryValueWithoutQuotes(element, "value");
					} else {
						body.append(var);
					}
					body.append(", ").append(selectionVar).append(");\n");
					if(element.hasEntryValue("title")) {
						indent(body);
						body.append('\t').append(sbName).append(".append(\"<option title=\\\"\").append(");
						appendEntryValueWithoutQuotes(element, "title");
						body.append(").append(\"\\\" value=\\\"\").append(f(");
					} else {
						indent(body);
						body.append('\t').append(sbName).append(".append(\"<option value=\\\"\").append(");
					}
					if(element.hasEntryValue("value")) {
						appendEntryValueWithoutQuotes(element, "value");
					} else {
						body.append("f(").append(var).append(")");
					}
					body.append(").append(\"\\\" \").append(").append(selectedVar).append(" ? \"selected >\" : \">\").append(");
					if(element.hasEntryValue("text")) {
						appendEntryValueWithoutQuotes(element, "text");
					} else {
						body.append("h(String.valueOf(").append(var).append("))");
					}
					body.append(").append(\"</option>\");\n");
					indent(body);
					body.append("}\n");
				}
			} else {
				String required = null;
				if(element.hasEntry("required")) {
					required = element.hasEntryValue("required") ? toArg(element.getEntryValue("required").getText()) : "false";
				}
				if(blank(selection)) {
					body.append(sbName).append(".append(optionTags(");
					build(options, body, true);
					if(blank(required)) {
						body.append("));\n");
					} else {
						body.append("), ").append(required).append(");\n");
					}
				} else {
					body.append(sbName).append(".append(optionTags(");
					build(options, body, true);
					if(selection instanceof JavaSourcePart) {
						body.append(", ");
						build((JavaSourcePart) selection, body);
					} else {
						body.append(", ").append(selection);
					}
					if(blank(required)) {
						String model = getFormModel(element);
						if(!blank(model)) {
							List<JavaSourcePart> fields = ((MarkupElement) element.getParent()).getArgs();
							if(fields == null) {
								body.append("));\n");
							} else {
								body.append(", ").append(model).append(".isRequired(");
								inJava = true;
								for(int i = 0; i < fields.size(); i++) {
									if(i != 0) body.append(", ");
									build(fields.get(i), body);
								}
								body.append(")));\n");
							}
						} else {
							body.append("));\n");
						}
					} else {
						body.append(", ").append(required).append("));\n");
					}
				}
			}

			inJava = true;
		}
	}
	
	private void buildStyle(StyleElement element) {
		prepFor(element);
		
		JavaSourcePart type = element.getJavaTypePart();
		if(type != null) {
			if("true".equals(element.getEntryText("inline"))) {
				buildInlineEss(body, type, element);
			} else {
				buildExternalEss(body, type, element);
			}
		} else {
			prepForMarkup(body);
			
			if(element.hasArgs()) {
				boolean revertSection = prepForHead();
				for(EspPart arg : element.getArgs()) {
					body.append("<link rel='stylesheet' type='text/css'");
					if(element.hasEntryValue("media")) {
						body.append(" media=");
						appendAttr("media", element.getEntryValue("media"));
					}
					String file = arg.getText();
					if("defaults".equals(file)) {
						body.append(" href='/application.css' />");
					} else {
						body.append(" href='/").append(file);
						if(file.endsWith(".css")) {
							body.append("' />");
						} else {
							body.append(".css' />");
						}
					}
				}
				if(revertSection) {
					prepForBody();
				}
			}
			if(element.hasChildren()) {
				if(!dom.isEss()) {
					if(element.hasEntryValue("media")) {
						body.append("<style media=");
						appendAttr("media", element.getEntryValue("media"));
						body.append(">");
					} else {
						body.append("<style>");
					}
				}
				buildStyleChildren(body, element.getChildren());
				if(!dom.isEss()) {
					body.append("</style>");
				}
			}
			
			inJava = false;
		}
	}

	private void buildStyleChild(StringBuilder sb, StyleChildElement element) {
		List<EspPart> selectors = element.getSelectorGroups();
		for(int j = 0; j < selectors.size(); j++) {
			if(j != 0) sb.append(',');
			int ix = sb.length();
			String selector = selectors.get(j).getText();
			boolean space = selector.charAt(0) != '&';
			sb.append(space ? selector : selector.substring(1));
			EspPart parent = element.getParent();
			while(parent instanceof StyleChildElement) {
				selector = ((StyleChildElement) parent).getLastSelectorText();
				if(space) sb.insert(ix, ' ');
				sb.insert(ix, (selector.charAt(0) != '&') ? selector : selector.substring(1));
				space = selector.charAt(0) != '&';
				parent = parent.getParent();
			}
		}
		sb.append('{');
		buildStyleProperties(sb, element);
		sb.append('}');
	}

	private void buildStyleChildren(StringBuilder sb, List<EspElement> children) {
		for(EspElement childElement : children) {
			StyleChildElement child = (StyleChildElement) childElement;
			if(child.hasSelectors()) {
				if(child.isParameterized()) {
					buildMethodSignature(child);
				} else {
					if(child.hasProperties()) {
						buildStyleChild(sb, child);
					}
					if(child.hasChildren()) {
						buildStyleChildren(sb, child.getChildren());
					}
				}
			}
		}
	}

	private void buildStyleProperties(StringBuilder sb, StyleChildElement child) {
		buildStyleProperties(sb, child, true);
	}

	private boolean buildStyleProperties(StringBuilder sb, StyleChildElement child, boolean isFirst) {
		for(StyleChildElement element : child.getProperties()) {
			if(element.isProperty()) {
				if(isFirst) {
					isFirst = false;
				} else {
					sb.append(';');
				}
				sb.append(element.getName().getText());
				EspPart value = element.getValue();
				if(value != null) {
					sb.append(':');
					build(value, sb);
				}
			}
			else if(element.isMixin()) {
				if(element.isParameterized()) {
					buildMethod(element);
				} else {
					String name = element.getLastSelectorText();
					EspPart selector = (resolver != null) ? resolver.getCssSelector(name) : null;
					if(selector == null) {
						// TODO create warning
					} else {
						StyleChildElement mixin = (StyleChildElement) selector.getParent();
						buildStyleProperties(sb, mixin, isFirst);
					}
				}
			}
		}
		return isFirst;
	}
	
	private void buildSubmit(MarkupElement element) {
		body.append("<input");
		body.append(" type=\\\"submit\\\"");
		buildId(element);
		buildClasses(element);
		buildAttrs(element, "type");
		if(!element.hasEntry("value")) {
			if(element.hasInnerText()) {
				body.append(" value=\\\"");
				build(element.getInnerText(), body);
				body.append("\\\"");
			} else {
				String model = getFormModel(element);
				if(blank(model)) {
					body.append(" value=\\\"Submit\\\"");
				} else {
					String modelName = getFormModelName(element);
					String action = getFormAction(element);
					if("create".equalsIgnoreCase(action)) {
						body.append(" value=\\\"Create ");
					} else if("update".equalsIgnoreCase(action)) {
						body.append(" value=\\\"Update ");
					} else {
						body.append(" value=\\\"\").append(").append(model).append(".isNew() ? \"Create \" : \"Update ");
					}
					body.append("\").append(titleize(").append(modelName).append(")).append(\"\\\"");
				}
			}
		}
		body.append(" />");
	}
	
	private void buildTextArea(MarkupElement textarea) {
		body.append("<textarea");
		buildFormField(textarea, false);
		body.append(">");
		if(textarea.hasArgs()) {
			body.append("\").append(f(");
			appendValueGetter(getFormModel(textarea), textarea.getArgs());
			body.append(")).append(\"");
		} else {
			if(textarea.hasInnerText()) {
				build(textarea.getInnerText(), body);
			}
		}
	}
	
	private void buildTitle(MarkupElement element) {
		prepForMarkup(body);
		prepForHead();

		body.append("<title>");
		if(element.hasInnerText()) {
			build(element.getInnerText(), body);
		}
		body.append("</title>");
	}

	private void buildView(MarkupElement view) {
		if(view.hasJavaType()) {
			if(view.hasEntries()) {
				Entry<String, EntryPart> entry = view.getEntries().entrySet().iterator().next();
				EntryPart part = entry.getValue();
				if(!blank(part)) {
					String itype = entry.getKey();
					String var = varName(itype) + "$" + view.getStart();
					
					prepForJava(body);

					EspPart value = part.getValue();
					String val = (value != null) ? value.getText() : "";
					body.append("for(").append(itype).append(' ').append(var).append(" : ").append(val).append(") {\n");
					indent(body);
					body.append("\tyield(new ");
					build(view.getJavaTypePart(), body);
					body.append('(').append(var).append("));\n");
					indent(body);
					body.append("}\n");
					
					inJava = true;
				}
			} else {
				prepForJava(body);
				body.append("yield(new ");
				build(view.getJavaTypePart(), body);
				if(view.hasArgs()) {
					body.append("(");
					for(Iterator<JavaSourcePart> iter = view.getArgs().iterator(); iter.hasNext(); ) {
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

		if(dom.isEsp()) {
			if(element.hasArgs()) {
				body.append("yield(");
				build(element.getArg(0), body);
				if(captureLevel >= 0) {
					body.append(", ").append(sbName);
				}
				body.append(");\n");
			} else {
				body.append("yield(");
				if(captureLevel >= 0) {
					body.append(sbName);
				}
				body.append(");\n");
			}
		} else {
			body.append("yield(");
			if(captureLevel >= 0) {
				body.append(sbName);
			}
			body.append(");\n");
		}

		inJava = true;
	}
	
	private boolean canBuildPartMethodSignatures(List<MethodSignatureArg> args) {
		for(MethodSignatureArg arg : args) {
			if(!arg.hasVarType() || !arg.hasVarName()) {
				return false;
			}
		}
		return true;
	}

	public ESourceFile compile() {
		switch(dom.getDocType()) {
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
		esf.setSimpleName(dom.getName());
		esf.setSuperName(ScriptFile.class.getSimpleName());

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
				if(part.isA(ConstructorElement)) {
					buildMethodSignature((ConstructorElement) part);
					ix++;
				} else {
					break;
				}
			}

			body.append("\t\t").append(SBBODY).append(".append(\"");
			for( ; ix < parts.size(); ix++) {
				EspElement element = (EspElement) parts.get(ix);
				if(element.isA(JavaElement)) {
					buildJava((JavaElement) element, true);
				} else {
					buildElement(element);
				}
			}
		
			buildMethod(
					body,
					"doRender",
					"\t@Override\n\tprotected void doRender(StringBuilder " + SBBODY + ") throws Exception {\n",
					bodyLocations,
					inJava
			);
		}
		
		esf.finalizeSource();
		
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
		esf.setSimpleName(dom.getName());
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
				if(part.isA(ConstructorElement)) {
					buildMethodSignature((ConstructorElement) part);
					ix++;
				} else {
					break;
				}
			}

			body.append("\t\t").append(SBBODY).append(".append(\"");
			for( ; ix < parts.size(); ix++) {
				EspElement element = (EspElement) parts.get(ix);
				if(element.isA(JavaElement)) {
					buildJava((JavaElement) element, true);
				} else {
					buildElement(element);
				}
			}
		
			buildMethod(
					body,
					"doRender",
					"\t@Override\n\tprotected void doRender(StringBuilder " + SBBODY + ") throws Exception {\n",
					bodyLocations,
					inJava
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
		esf.setSimpleName(dom.getName());
		esf.setSuperName(View.class.getSimpleName());

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
				if(part.isA(ConstructorElement)) {
					buildMethodSignature((ConstructorElement) part);
					ix++;
				} else {
					break;
				}
			}

			for( ; ix < parts.size(); ix++) {
				EspElement element = (EspElement) parts.get(ix);
				if(captureLevel != -1 && element.getLevel() <= captureLevel) {
					stopCapture();
				}
				if(element.isA(JavaElement)) {
					buildJava((JavaElement) element, true);
				} else {
					buildElement(element);
				}
			}
			if(captureLevel != -1) {
				stopCapture();
			}

			buildMethod(
					body,
					"render",
					"\t@Override\n\tprotected void render(StringBuilder " + SBHEAD + ", StringBuilder " + SBBODY + ") throws Exception {\n",
					bodyLocations,
					inJava
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
		esf.setSimpleName(dom.getName());
		esf.setSuperName(StyleSheet.class.getSimpleName());

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
				if(part.isA(ConstructorElement)) {
					buildMethodSignature((ConstructorElement) part);
					ix++;
				} else {
					break;
				}
			}

			body.append("\t\t").append(SBBODY).append(".append(\"");
			for( ; ix < parts.size(); ix++) {
				EspElement element = (EspElement) parts.get(ix);
				if(element.isA(JavaElement)) {
					buildJava((JavaElement) element, true);
				} else {
					buildElement(element);
				}
			}
		
			buildMethod(
					body,
					"doRender",
					"\t@Override\n\tprotected void doRender(StringBuilder " + SBBODY + ") throws Exception {\n",
					bodyLocations,
					inJava
			);
		}
		
		esf.finalizeSource();
		
		return esf;
	}
	
	private boolean containsFileInput(EspElement element) {
		switch(element.getType()) {
		case MarkupElement:
			MarkupElement h = (MarkupElement) element;
			if("file".equals(h.getTag())) {
				return true;
			}
			if(h.hasChildren()) {
				for(EspElement child : h.getChildren()) {
					if(containsFileInput(child)) {
						return true;
					}
				}
			}
			return false;
		case JavaElement:
			JavaElement j = (JavaElement) element;
			if(j.hasChildren()) {
				for(EspElement child : j.getChildren()) {
					if(containsFileInput(child)) {
						return true;
					}
				}
			}
			return false;
		default:
			return false;
		}
	}
	
	/**
	 * @param sb the StringBuilder to modify
	 * @param q1 the position of the first quote
	 * @param escaped escape the quotes so they can be used inside a string (like with HTML attributes)
	 */
	private void ensureQuotes(StringBuilder sb, int q1, boolean escaped) {
		if(q1 < sb.length()) {
			if(sb.charAt(q1) != '\\' || (q1+1 < sb.length() && sb.charAt(q1+1) != '"')) {
				sb.insert(q1, escaped ? "\\\"" : "\"");
			}
			if(sb.charAt(sb.length()-2) != '\\' || sb.charAt(sb.length()-1) != '"') {
				sb.append(escaped ? "\\\"" : "\"");
			}
		} else {
			sb.append(escaped ? "\\\"\\\"" : "\"\"");
		}
	}
	
	private MarkupElement getForm(MarkupElement formField) {
		EspPart parent = formField.getParent();
		while(parent != null) {
			if(parent.isA(MarkupElement)) {
				MarkupElement element = (MarkupElement) parent;
				if("form".equals(element.getTag())) {
					List<JavaSourcePart> args = element.getArgs();
					if(args != null && (args.size() == 1 || args.size() == 2)) {
						return element;
					}
					break; // forms shouldn't be nested...
				} else if("fields".equals(element.getTag())) {
					List<JavaSourcePart> args = element.getArgs();
					if(args != null && args.size() == 1) {
						return element; // args.get(0).getText().trim();
					}
					// fields section may be nested...
				}
			}
			parent = parent.getParent();
		}
		return null;
	}

	private String getFormAction(MarkupElement formField) {
		EspPart parent = formField.getParent();
		while(parent != null) {
			if(parent.isA(MarkupElement)) {
				MarkupElement h = (MarkupElement) parent;
				if("form".equals(h.getTag())) {
					List<JavaSourcePart> args = h.getArgs();
					if(args != null && args.size() == 2) {
						return args.get(1).getText().trim();
					}
					if(h.hasEntry("method")) {
						EspPart part = h.getEntry("method").getValue();
						if(part != null) {
							String method = part.getText().trim();
							if("post".equalsIgnoreCase(method)) {
								return "create";
							} else if("put".equalsIgnoreCase(method)) {
								return "update";
							}
						}
						return null;
					}
					if(h.hasEntry("action")) {
						EspPart part = h.getEntry("action").getValue();
						if(part != null) {
							return part.getText().trim();
						}
						return null;
					}
					break; // forms shouldn't be nested...
				}
			}
			parent = parent.getParent();
		}
		return null;
	}
	
	private String getFormModel(MarkupElement formField) {
		MarkupElement form = getForm(formField);
		if(form != null) {
			return getFormModelVar(form);
		}
		return null;
	}
	
	private String getFormModelName(MarkupElement formField) {
		MarkupElement form = getForm(formField);
		if(form != null) {
			return getFormModelNameVar(form.getStart());
		}
		return null;
	}
	
	private String getFormModelNameValue(MarkupElement form) {
		if(form.hasEntry("as")) {
			return form.getEntryValue("as").getText().trim();
		}
		String arg = form.getArg(0).getText().trim();
		for(int i = 0; i < arg.length(); i++) {
			if(!Character.isJavaIdentifierPart(arg.charAt(i))) {
				return "varName((" + arg + ").getClass())";
			}
		}
		return "\"" + arg + "\"";
	}
	
	private String getSelectionGetter(MarkupElement options) {
		EspPart parent = options.getParent();
		if(parent instanceof MarkupElement && "select".equals(((MarkupElement) parent).getTag())) {
			MarkupElement select = (MarkupElement) parent;
			String model = getFormModel(select);
			if(!blank(model)) {
				StringBuilder sb = new StringBuilder();
				List<JavaSourcePart> fields = select.getArgs();
				if(fields != null) {
					if(fields.size() == 1) {
						sb.append(model).append('.').append(getter(fields.get(0)));
					} else {
						int last = fields.size() - 1;
						for(int i = 0; i < fields.size(); i++) {
							sb.append(model);
							for(int j = 0; j <= i; j++) {
								sb.append('.').append((j == i && j != last) ? hasser(fields.get(j)) : getter(fields.get(j)));
							}
							if(i < last) {
								sb.append("?(");
							}
						}
						for(int i = 0; i < last; i++) {
							sb.append("):\"\"");
						}
					}
				}
				return sb.toString();
			}
		}
		return null;
	}
	
	private String getter(JavaSourcePart part) {
		String text = part.getText();
		if(part.isSimple()) {
			return getterName(text.substring(1, text.length()-1)) + "()";
		}
		return "get(" + text + ")";
	}
	
	private String hasser(JavaSourcePart part) {
		String text = part.getText();
		if(part.isSimple()) {
			return hasserName(text.substring(1, text.length()-1)) + "()";
		}
		return "isSet(" + text + ")";
	}

	private StringBuilder indent(StringBuilder sb) {
		for(int i = 0; i < javaLevel+2; i++) {
			sb.append('\t');
		}
		return sb;
	}
	
	private boolean isInHead(EspElement element) {
		EspPart parent = element.getParent();
		while(parent != null && !parent.isA(DOM)) {
			switch(parent.getType()) {
			case DOM:
				return false;
			case MarkupElement:
				if("head".equals(((MarkupElement) parent).getTag())) {
					return true;
				}
			default:
				parent = parent.getParent();
			}
		}
		return false;
	}
	
	private void offsetLocations(StringBuilder sb, int offset) {
		for(EspLocation location : locationsMap.get(sb)) {
			location.offset += offset;
		}
	}
	
	private void prepFor(EspElement element) {
		if(isInHead(element)) {
			prepForHead();
		} else {
			prepForBody();
		}
	}
	
	private boolean prepFor(String section) {
		if(sbName != section) {
			sbName = section;
			
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
			indent(body);
			body.append(sbName).append(".append(\"");
			return true;
		}
		return false;
	}
	
	private boolean prepForBody() {
		return prepFor(SBBODY);
	}
	
	private boolean prepForHead() {
		return prepFor(SBHEAD);
	}
	
	private void prepForJava(StringBuilder sb) {
		if(inJava) {
			indent(sb);
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
						return;
					}
				}
				sb.delete(sb.length()-s.length(), sb.length());
			}
		}
	}
	
	private void prepForMarkup(StringBuilder sb) {
		if(inJava) {
			indent(sb);
			sb.append(sbName(sb)).append(".append(\"");
			inJava = false;
		}
	}
	
	private String sbName(StringBuilder sb) {
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

		captureLevel = capture.getLevel();
		
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

		captureLevel = content.getLevel();
		
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
			
			captureLevel = -1;
			sbName = SBBODY;
			
			inJava = true;
		}
	}
	
	private void stopContent() {
		prepForJava(body);

		body.append("putContent(").append(contentName).append(", ").append(sbName).append(".toString());\n");
		indent(body);
		body.append(sbName).append(" = null;\n");
		
		captureLevel = -1;
		contentName = null;
		sbName = SBBODY;
		
		inJava = true;
	}

	/**
	 * Strip the quotes from an entry value to use it as a Java argument
	 */
	private String toArg(String val) {
		if(val != null && val.length() > 1) {
			if(val.charAt(0) == '"' && val.charAt(val.length()-1) == '"') {
				return val.substring(1, val.length()-1);
			}
		}
		return val;
	}

}
