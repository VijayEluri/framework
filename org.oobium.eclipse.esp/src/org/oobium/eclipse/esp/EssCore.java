package org.oobium.eclipse.esp;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.oobium.build.esp.EspDom;
import org.oobium.build.esp.EspElement;
import org.oobium.build.esp.EspPart;
import org.oobium.build.esp.EspPart.Type;
import org.oobium.build.esp.elements.StyleChildElement;
import org.oobium.build.esp.elements.StyleElement;
import org.oobium.eclipse.workspace.OobiumCore;
import org.oobium.utils.FileUtils;

public class EssCore {

	private static void addCssSelectors(EspDom dom, boolean all, boolean ids, boolean classes, List<EspPart> selectors) {
		for(EspPart part : dom.getParts()) {
			if(part.getType() == Type.StyleElement) {
				for(EspElement e : ((StyleElement) part).getChildren()) {
					if(e.getType() == Type.StyleChildElement) {
						StyleChildElement c = (StyleChildElement) e;
						for(EspPart selector : c.getSelectorGroups()) {
							char[] ca = selector.getText().toCharArray();
							if(all) {
								selectors.add(selector);
							} else if(ids && ca[0] == '#' && isSimple(ca)) {
								selectors.add(selector);
							} else if(classes && ca[0] == '.' && isSimple(ca)) {
								selectors.add(selector);
							}
						}
					}
				}
			}
		}
	}
	
	public static EspPart getCssClass(EspDom dom, String name) {
		if(name == null) return null;
		name = name.trim();
		if(name.length() < 1) return null;
		if(name.charAt(0) != '.') name = "." + name;
		return getCssSelector(dom, name);
	}
	
	public static List<EspPart> getCssClasses(IProject project, EspDom dom) {
		return getCssSelectors(project, dom, false, false, true);
	}
	
	public static EspPart getCssId(EspDom dom, String name) {
		if(name == null) return null;
		name = name.trim();
		if(name.length() < 1) return null;
		if(name.charAt(0) != '#') name = "#" + name;
		return getCssSelector(dom, name);
	}
	
	public static List<EspPart> getCssIds(IProject project, EspDom dom) {
		return getCssSelectors(project, dom, false, true, false);
	}

	private static EspPart getCssSelector(EspDom dom, String name) {
		
		// TODO search the visible / loaded StyleSheets
		
		for(EspPart part : dom.getParts()) {
			if(part.getType() == Type.StyleElement) {
				for(EspElement e : ((StyleElement) part).getChildren()) {
					if(e.getType() == Type.StyleChildElement) {
						StyleChildElement c = (StyleChildElement) e;
						for(EspPart selector : c.getSelectorGroups()) {
							if(name.equals(selector.getText())) {
								return selector;
							}
						}
					}
				}
			}
		}
		return null;
	}
	
	public static List<EspPart> getCssSelectors(IProject project, EspDom dom) {
		return getCssSelectors(project, dom, true, true, true);
	}

	private static List<EspPart> getCssSelectors(IProject project, EspDom dom, boolean all, boolean ids, boolean classes) {
		List<EspPart> selectors = new ArrayList<EspPart>();
		addCssSelectors(dom, all, ids, classes, selectors);
		for(EspDom d : getDoms(project)) {
			addCssSelectors(d, all, ids, classes, selectors);
		}
		return selectors;
	}
	
	private static List<EspDom> getDoms(IProject project) {
		List<EspDom> doms = new ArrayList<EspDom>();
		for(File file : OobiumCore.getStyleSheets(project)) {
			String name = file.getName();
			System.out.println(name);
			StringBuilder src = FileUtils.readFile(file);
			doms.add(new EspDom(name, src));
		}
		return doms;
	}
	
	private static boolean isSimple(char[] ca) {
		for(int i = 1; i < ca.length; i++) {
			if(!Character.isLetterOrDigit(ca[i]) && ca[i] != '_' && ca[i] != '-') {
				return false;
			}
		}
		return true;
	}
	
}
