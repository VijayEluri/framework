package org.oobium.build.esp.compiler;

import static org.oobium.build.esp.dom.EspDom.DocType.ESS;

import java.util.List;

import org.oobium.build.esp.dom.EspResolver;
import org.oobium.build.esp.dom.elements.StyleElement;
import org.oobium.build.esp.dom.parts.MethodArg;
import org.oobium.build.esp.dom.parts.StylePart;
import org.oobium.build.esp.dom.parts.style.Declaration;
import org.oobium.build.esp.dom.parts.style.ParametricRuleset;
import org.oobium.build.esp.dom.parts.style.Property;
import org.oobium.build.esp.dom.parts.style.Ruleset;
import org.oobium.build.esp.dom.parts.style.Selector;

public class StyleCompiler extends AssetCompiler {

	private StyleElement element;
	
	public StyleCompiler(EspCompiler parent) {
		super(parent);
	}

	public void buildStyle(StyleElement element) {
		this.element = element;
		buildStyle();
	}
	
	private void buildStyle() {
		if(element.hasJavaType()) {
			if("true".equals(element.getEntry("inline"))) {
				buildInlineEss(element);
			} else {
				buildExternalEss(element);
			}
		} else {
			StringBuilder body = parent.getBody();
			if(element.hasArgs()) {
				parent.prepForJava(body);
				for(MethodArg arg : element.getArgs()) {
					body.append("addExternalStyle(");
					parent.build(arg, body);
					body.append(");\n");
				}
			}
			if(element.hasAsset()) {
				parent.prepForMarkup(body);
				String oldSection = parent.prepFor(element);
				if(!parent.getDom().is(ESS)) {
					if(element.hasEntry("media")) {
						body.append("<style media=");
						parent.appendAttr("media", element.getEntry("media"));
						body.append(">");
					} else {
						body.append("<style>");
					}
				}
				buildStylePart(element.getAsset(), body);
				if(!parent.getDom().is(ESS)) {
					body.append("</style>");
				}
				parent.prepFor(oldSection);
			}
		}
	}
	
	private void buildMethod(Property mixin) {
		StringBuilder body = parent.getBody();
		parent.prepFor(mixin);
		parent.prepForJava(body);
		body.append(parent.sbName(body)).append(".append(");
		body.append(mixin.getName()).append(" (");
		if(mixin.hasArgs()) {
			List<MethodArg> args = mixin.getArgs();
			for(int i = 0; i < args.size(); i++) {
				if(i != 0) body.append(", ");
				parent.build(args.get(i), body, true);
			}
		}
		body.append(")));\n");
		parent.prepForMarkup(body);
	}
	
	private void buildStylePart(StylePart style, StringBuilder sb) {
		if(style.hasParametricRules()) {
			for(ParametricRuleset rule : style.getParametricRules()) {
				parent.buildMethodSignature(rule);
			}
		}
		if(style.hasRules()) {
			for(Ruleset rule : style.getRules()) {
				buildStyleRuleset(rule, sb);
			}
		}
	}

	void buildStyleProperties(Declaration declaration, StringBuilder sb) {
		buildStyleProperties(declaration, sb, true);
	}
	
	boolean buildStyleProperties(Declaration declaration, StringBuilder sb, boolean first) {
		for(Property property : declaration.getProperties()) {
			first = buildStyleProperty(property, sb, first);
		}
		return first;
	}
	
	private boolean buildStyleProperty(Property property, StringBuilder sb, boolean first) {
		if(property.isMixin()) {
			EspResolver resolver = parent.getResolver();
			String name = property.getName().getText();
			Selector selector = (resolver != null) ? resolver.getCssSelector(name) : null;
			if(selector == null) {
				// TODO create warning
			} else {
				buildStyleProperties(selector.getDeclaration(), sb, first);
			}
		}
		else if(property.isParametric()) {
			buildMethod(property);
		}
		else {
			if(first) { first = false; } else { sb.append(';'); }
			sb.append(property.getName().getText());
			if(property.hasValue()) {
				sb.append(':');
				parent.build(property.getValue(), sb);
			}
		}
		return first;
	}
	
	private void buildStyleRuleset(Ruleset rule, StringBuilder sb) {
		if(rule.hasDeclaration() && rule.hasSelectors()) {
			List<Selector> selectors = rule.getSelectors();
			for(int i = 0; i < selectors.size(); i++) {
				if(i != 0) sb.append(',');
				int ix = sb.length();
				Selector selector = selectors.get(i);
				sb.append(selector.getText().trim());
				Ruleset parent = rule.getParentRuleset();
				while(parent != null) {
					if(!rule.isMerged()) sb.insert(ix, ' ');
					sb.insert(ix, parent.getSelector());
					parent = (rule = parent).getParentRuleset();
				}
			}
			sb.append('{');
			buildStyleProperties(rule.getDeclaration(), sb);
			sb.append('}');
		}
	}

}
