package org.oobium.build.esp.dom.common;

import java.util.List;

import org.oobium.build.esp.dom.EspPart;
import org.oobium.build.esp.dom.parts.MethodSigArg;

public interface MethodSignature {

	public abstract MethodSignature addSigArg(MethodSigArg arg);
	
	public abstract EspPart getPart();
	
	public abstract String getMethodName();
	
	public abstract String getReturnType();
	
	public abstract List<MethodSigArg> getSigArgs();
	
	public abstract boolean isConstructor();
	
	public abstract boolean isStatic();
	
}
