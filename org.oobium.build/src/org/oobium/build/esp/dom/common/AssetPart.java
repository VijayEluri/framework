package org.oobium.build.esp.dom.common;

import java.util.List;

import org.oobium.build.esp.dom.EspDom;
import org.oobium.build.esp.dom.EspPart;

public interface AssetPart {

	public abstract List<EspPart> getJavaContainers();
	
	public abstract EspDom getDom();
	
	public abstract int getEnd();
	
	public abstract EspPart getPart();

}
