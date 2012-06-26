package org.oobium.build.esp.dom.parts.style;

import static org.oobium.utils.StringUtils.*;

import java.util.ArrayList;
import java.util.List;

import org.oobium.build.esp.dom.EspPart;
import org.oobium.build.esp.dom.common.MethodSignature;
import org.oobium.build.esp.dom.parts.MethodSigArg;
import org.oobium.build.esp.dom.parts.style.Declaration;
import org.oobium.build.esp.dom.parts.style.Selector;
import org.oobium.build.esp.dom.parts.style.SelectorGroup;

public class Ruleset extends EspPart implements MethodSignature {

	private SelectorGroup selectorGroup;
	private List<MethodSigArg> sigArgs;
	private Declaration declaration;
	private boolean merged;
	
	private List<Ruleset> nestedRules;
	
	public Ruleset() {
		super(Type.StyleRuleset);
	}

	@Override
	public MethodSignature addSigArg(MethodSigArg arg) {
		if(sigArgs == null) {
			sigArgs = new ArrayList<MethodSigArg>();
		}
		sigArgs.add(arg);
		return null;
	}

	@Override
	public EspPart getPart() {
		return this;
	}

	@Override
	public String getMethodName() {
		return (selectorGroup != null) ? methodName(selectorGroup.getText()) : null;
	}

	@Override
	public String getReturnType() {
		return "String";
	}

	@Override
	public List<MethodSigArg> getSigArgs() {
		return (sigArgs != null) ? sigArgs : new ArrayList<MethodSigArg>(0);
	}

	@Override
	public void initSigArgs() {
		sigArgs = new ArrayList<MethodSigArg>();
	}
	
	@Override
	public boolean isConstructor() {
		return false;
	}

	public boolean isParametric() {
		return sigArgs != null;
	}
	
	@Override
	public boolean isStatic() {
		return true;
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
	
	public List<Ruleset> getNestedRules() {
		return (nestedRules != null) ? nestedRules : new ArrayList<Ruleset>(0);
	}
	
	public Ruleset getParentRuleset() {
		return (parent instanceof Declaration) ? ((Declaration) parent).getParent() : null;
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
		return (parent instanceof Declaration);
	}

	public boolean hasDeclaration() {
		return declaration != null;
	}

	public boolean hasNestedRules() {
		return nestedRules != null;
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
