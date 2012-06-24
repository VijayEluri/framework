package org.oobium.build.esp.dom.parts.style;

import java.util.ArrayList;
import java.util.List;

import org.oobium.build.esp.dom.EspPart;
import org.oobium.build.esp.dom.common.MethodSignature;
import org.oobium.build.esp.dom.parts.MethodSigArg;

public class ParametricRuleset extends EspPart implements MethodSignature {

	private String name;
	private List<MethodSigArg> sigArgs;
	private Declaration declaration;
	
	public ParametricRuleset() {
		super(Type.StyleRuleset);
	}

	public String getName() {
		return name;
	}
	
	public void setName(EspPart name) {
		this.name = name.getText().trim();
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public Declaration getDeclaration() {
		return declaration;
	}
	
	public void setDeclaration(Declaration declaration) {
		this.declaration = declaration;
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
		return name;
	}

	@Override
	public String getReturnType() {
		return null;
	}

	@Override
	public List<MethodSigArg> getSigArgs() {
		return (sigArgs != null) ? sigArgs : new ArrayList<MethodSigArg>(0);
	}

	@Override
	public boolean isConstructor() {
		return false;
	}

	@Override
	public boolean isStatic() {
		return true;
	}
	
}
