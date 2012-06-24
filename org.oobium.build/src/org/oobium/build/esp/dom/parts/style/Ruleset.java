package org.oobium.build.esp.dom.parts.style;

import java.util.ArrayList;
import java.util.List;

import org.oobium.build.esp.dom.EspPart;
import org.oobium.build.esp.dom.parts.style.Declaration;
import org.oobium.build.esp.dom.parts.style.Selector;
import org.oobium.build.esp.dom.parts.style.SelectorGroup;

public class Ruleset extends EspPart {

	private SelectorGroup selectorGroup;
	private Declaration declaration;
	private boolean merged;
	
	private List<Ruleset> nestedRules;
	
	public Ruleset() {
		super(Type.StyleRuleset);
	}

	public void addNestedRule(Ruleset rule) {
		if(nestedRules == null) {
			nestedRules = new ArrayList<Ruleset>();
		}
		nestedRules.add(rule);
	}
	
	public Declaration getDeclaration() {
		return declaration;
	}
	
	public Ruleset getParentRuleset() {
		return (parent instanceof Ruleset) ? (Ruleset) parent : null;
	}
	
	public Selector getSelector() {
		return (selectorGroup.hasSelectors()) ? selectorGroup.getSelectors().get(0) : null;
	}
	
	public List<Selector> getSelectors() {
		return (selectorGroup != null) ? selectorGroup.getSelectors() : new ArrayList<Selector>(0);
	}
	
	public SelectorGroup getSelectorGroup() {
		return selectorGroup;
	}

	public boolean isMerged() {
		return merged;
	}
	
	public boolean isNested() {
		return (parent instanceof Ruleset);
	}

	public boolean hasDeclaration() {
		return declaration != null;
	}
	
	public boolean hasSelectors() {
		return selectorGroup != null && selectorGroup.hasSelectors();
	}
	
	public void setDeclaration(Declaration declaration) {
		this.declaration = declaration;
	}
	
	public void setMerged(boolean merged) {
		this.merged = merged;
	}
	
	public void setSelectorGroup(SelectorGroup selectorGroup) {
		this.selectorGroup = selectorGroup;
	}
	
}
