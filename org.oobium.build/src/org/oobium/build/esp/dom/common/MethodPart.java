package org.oobium.build.esp.dom.common;

import org.oobium.build.esp.dom.EspPart;
import org.oobium.build.esp.dom.parts.MethodArg;

public interface MethodPart {

	public abstract EspPart addArg(MethodArg arg);

	public abstract void initArgs();
	
}
