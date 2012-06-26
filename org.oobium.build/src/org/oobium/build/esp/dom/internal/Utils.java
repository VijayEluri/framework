package org.oobium.build.esp.dom.internal;

import java.util.ArrayList;
import java.util.List;

import org.oobium.build.esp.dom.EspPart;
import org.oobium.build.esp.dom.EspPart.Type;

public class Utils {

	private static void addJavaContainers(EspPart part, List<EspPart> containers) {
		if(part.isA(Type.JavaContainer)) {
			containers.add(part);
		}
		for(EspPart sub : part.getParts()) {
			addJavaContainers(sub, containers);
		}
	}
	
	public static List<EspPart> findJavaContainers(EspPart part) {
		List<EspPart> containers = new ArrayList<EspPart>();
		addJavaContainers(part, containers);
		return containers;
	}
	
}
