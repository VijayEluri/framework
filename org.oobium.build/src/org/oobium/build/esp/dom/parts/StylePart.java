package org.oobium.build.esp.dom.parts;

import static org.oobium.build.esp.dom.internal.Utils.findJavaContainers;

import java.util.ArrayList;
import java.util.List;

import org.oobium.build.esp.dom.EspPart;
import org.oobium.build.esp.dom.common.AssetPart;
import org.oobium.build.esp.dom.elements.StyleElement;
import org.oobium.build.esp.dom.parts.style.ParametricRuleset;
import org.oobium.build.esp.dom.parts.style.Ruleset;

public class StylePart extends EspPart implements AssetPart {

	private List<Ruleset> rules;
	private List<ParametricRuleset> parametricRules;
	
	public StylePart() {
		super(Type.StylePart);
	}

	public void addParametricRuleset(ParametricRuleset rule) {
		if(parametricRules ==  null) {
			parametricRules = new ArrayList<ParametricRuleset>();
		}
		parametricRules.add(rule);
	}
	
	public void addRuleset(Ruleset rule) {
		if(rules ==  null) {
			rules = new ArrayList<Ruleset>();
		}
		rules.add(rule);
	}
	
	@Override
	public List<EspPart> getJavaContainers() {
		return findJavaContainers(this);
	}
	
	public List<ParametricRuleset> getParametricRules() {
		return (parametricRules != null) ? parametricRules : new ArrayList<ParametricRuleset>(0);
	}
	
	@Override
	public StyleElement getParent() {
		return (StyleElement) super.getParent();
	}
	
	@Override
	public EspPart getPart() {
		return this;
	}
	
	public List<Ruleset> getRules() {
		return (rules != null) ? rules : new ArrayList<Ruleset>(0);
	}
	
	public boolean hasParametricRules() {
		return parametricRules != null;
	}
	
	public boolean hasRules() {
		return rules != null;
	}

}
