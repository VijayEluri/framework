package org.oobium.eclipse.esp;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.oobium.build.esp.dom.EspDom;
import org.oobium.build.esp.dom.EspResolver;
import org.oobium.build.esp.dom.parts.style.Selector;
import org.oobium.build.esp.parser.EspBuilder;
import org.oobium.eclipse.workspace.OobiumCore;
import org.oobium.utils.FileUtils;

public class EssCore {

	public static Selector getCssClass(EspDom dom, String name) {
		return new EspResolver(dom).getCssClass(name);
	}
	
	public static List<Selector> getCssClasses(IProject project, EspDom dom) {
		return new EspResolver(dom).addAll(getDoms(project)).getCssClasses();
	}
	
	public static Selector getCssId(EspDom dom, String name) {
		return new EspResolver(dom).getCssId(name);
	}
	
	public static List<Selector> getCssIds(IProject project, EspDom dom) {
		return new EspResolver(dom).addAll(getDoms(project)).getCssIds();
	}

	public static List<Selector> getCssSelectors(IProject project, EspDom dom) {
		return new EspResolver(dom).addAll(getDoms(project)).getCssSelectors();
	}
	
	private static List<EspDom> getDoms(IProject project) {
		List<EspDom> doms = new ArrayList<EspDom>();
		for(File file : OobiumCore.getStyleSheets(project)) {
			String name = file.getName();
			System.out.println(name);
			StringBuilder src = FileUtils.readFile(file);
			doms.add(EspBuilder.newEspBuilder(name).parse(src));
		}
		return doms;
	}
	
}
