package org.oobium.build.esp.dom.parts;

import org.oobium.build.esp.dom.EspPart;

public class MethodSigArg extends EspPart {

	private EspPart varType;
	private EspPart varName;
	private EspPart defaultValue;
	private boolean isVarArgs;
	
	public MethodSigArg() {
		super(Type.MethodSigArg);
	}

	public EspPart getDefaultValue() {
		return defaultValue;
	}
	
	public EspPart getVarName() {
		return varName;
	}
	
	public EspPart getVarType() {
		return varType;
	}
	
	public boolean hasDefaultValue() {
		return defaultValue != null;
	}
	
	public boolean hasVarName() {
		return varName != null;
	}
	
	public boolean hasVarType() {
		return varType != null;
	}
	
	public boolean isVarArgs() {
		return isVarArgs;
	}
	
	public void setDefaultValue(EspPart defaultValue) {
		this.defaultValue = defaultValue;
	}
	
	public void setVarArgs(boolean isVarArgs) {
		this.isVarArgs = isVarArgs;
	}
	
	public void setVarName(EspPart varName) {
		this.varName = varName;
	}
	
	public void setVarType(EspPart varType) {
		this.varType = varType;
	}

}
