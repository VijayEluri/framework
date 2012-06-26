package org.oobium.build.esp.dom.parts;

import static org.oobium.build.esp.dom.internal.Utils.findJavaContainers;

import java.util.List;

import org.oobium.build.esp.dom.EspPart;
import org.oobium.build.esp.dom.common.AssetPart;

public class ScriptPart extends EspPart implements AssetPart {

	public ScriptPart() {
		super(Type.ScriptPart);
	}

	@Override
	public List<EspPart> getJavaContainers() {
		return findJavaContainers(this);
	}

	@Override
	public EspPart getPart() {
		return this;
	}
	
}
