package org.oobium.build.esp.compiler;

import java.util.List;

import org.oobium.build.esp.dom.EspResolver;
import org.oobium.build.esp.dom.elements.StyleElement;
import org.oobium.build.esp.dom.parts.MethodArg;
import org.oobium.build.esp.dom.parts.StylePart;
import org.oobium.build.esp.dom.parts.style.Declaration;
import org.oobium.build.esp.dom.parts.style.Property;
import org.oobium.build.esp.dom.parts.style.Ruleset;
import org.oobium.build.esp.dom.parts.style.Selector;

public class StyleAssetCompiler extends AssetCompiler {

	private StyleElement element;
	private StringBuilder body;
	
	public StyleAssetCompiler(EspCompiler parent) {
		super(parent);
	}

	public void buildStyle(StyleElement element) {
		if(element.hasAsset()) {
			StylePart style = element.getAsset();
			if(style.hasRules()) {
				this.element = element;
				this.body = parent.getBody();
				for(Ruleset rule : style.getRules()) {
//					if(rule.isParametric()) {
//						parent.buildMethodSignature(rule);
//					} else {
						buildStyleRuleset(rule);
//					}
				}
			}
		}
	}

	private void buildMethod(Property mixin) {
		body.append(mixin.getName()).append(" (");
		if(mixin.hasArgs()) {
			List<MethodArg> args = mixin.getArgs();
			for(int i = 0; i < args.size(); i++) {
				if(i != 0) body.append(", ");
				parent.build(args.get(i), body, true);
			}
		}
	}
	
	void buildStyleProperties(Ruleset rule) {
		buildStyleProperties(rule, true);
	}
	
	boolean buildStyleProperties(Ruleset rule, boolean first) {
		if(rule.hasDeclaration()) {
			for(Property property : rule.getDeclaration().getProperties()) {
				first = buildStyleProperty(property, first);
			}
		}
		return first;
	}
	
	private boolean buildStyleProperty(Property property, boolean first) {
		if(property.isMixin()) {
			EspResolver resolver = parent.getResolver();
			String name = property.getName().getText();
			Selector selector = (resolver != null) ? resolver.getCssSelector(name) : null;
			if(selector == null) {
				parent.logger.debug("could not find mixin with the name: '{}'", name);
			} else {
				buildStyleProperties(selector.getRuleset(), first);
			}
		}
		else if(property.isParametric()) {
			buildMethod(property);
		}
		else {
			if(first) { first = false; } else { body.append(';'); }
			body.append(property.getName().getText());
			if(property.hasValue()) {
				body.append(':');
				parent.build(property.getValue(), body);
			}
		}
		return first;
	}
	
	private void buildStyleRuleset(Ruleset ruleset) {
		if(ruleset.hasDeclaration() && ruleset.hasSelectors()) {
			List<Selector> selectors = ruleset.getSelectors();
			for(int i = 0; i < selectors.size(); i++) {
				if(i != 0) body.append(',');
				int ix = body.length();
				Selector selector = selectors.get(i);
				body.append(selector.getText().trim());
				Ruleset rule = ruleset;
				Ruleset parent = rule.getParentRuleset();
				while(parent != null) {
					if(!rule.isMerged()) body.insert(ix, ' ');
					body.insert(ix, parent.getSelector());
					parent = (rule = parent).getParentRuleset();
				}
			}
			body.append('{');
			buildStyleProperties(ruleset);
			body.append('}');
		}
		if(ruleset.hasNestedRules()) {
			for(Ruleset nested : ruleset.getNestedRules()) {
				buildStyleRuleset(nested);
			}
		}
	}

}
