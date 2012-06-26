package org.oobium.build.esp.dom.elements;

import java.util.ArrayList;
import java.util.List;

import org.oobium.build.esp.dom.EspElement;
import org.oobium.build.esp.dom.EspPart;
import org.oobium.build.esp.dom.common.MethodSignature;
import org.oobium.build.esp.dom.parts.MethodSigArg;

public class Constructor extends EspElement implements MethodSignature {

	private List<MethodSigArg> methodSigArgs;

	public Constructor() {
		super(Type.Constructor);
	}
	
	@Override
	public Constructor addSigArg(MethodSigArg methodSigArg) {
		if(methodSigArgs == null) {
			methodSigArgs = new ArrayList<MethodSigArg>();
		}
		methodSigArgs.add(methodSigArg);
		return this;
	}
	
	public List<MethodSigArg> getSigArgs() {
		return (methodSigArgs != null) ? methodSigArgs : new ArrayList<MethodSigArg>(0);
	}
	
	public Constructor setMethodSigArgs(List<MethodSigArg> methodSigArgs) {
		this.methodSigArgs = methodSigArgs;
		return this;
	}

	@Override
	public EspPart getPart() {
		return this;
	}

	@Override
	public String getMethodName() {
		return getDom().getSimpleName();
	}

	@Override
	public String getReturnType() {
		return null;
	}

	@Override
	public void initSigArgs() {
		methodSigArgs = new ArrayList<MethodSigArg>();
	}
	
	@Override
	public boolean isConstructor() {
		return true;
	}

	@Override
	public boolean isStatic() {
		return false;
	}
	
}
