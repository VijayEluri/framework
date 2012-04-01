package org.oobium.build.esp;

import java.util.ArrayList;
import java.util.List;

import org.oobium.build.esp.EspPart.Type;
import org.oobium.build.esp.elements.StyleChildElement;
import org.oobium.build.esp.elements.StyleElement;

public class EspResolver {

	private static boolean isSimple(char[] ca) {
		for(int i = 1; i < ca.length; i++) {
			if(!Character.isLetterOrDigit(ca[i]) && ca[i] != '_' && ca[i] != '-') {
				return false;
			}
		}
		return true;
	}
	
	
	private List<EspDom> doms;
	
	public EspResolver() {
		doms = new ArrayList<EspDom>();		
	}
	
	public void add(EspDom dom) {
		if(!doms.contains(dom)) {
			doms.add(dom);
		}
	}
	
	private void addCssSelectors(EspDom dom, boolean all, boolean ids, boolean classes, List<EspPart> selectors) {
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
	
	public EspPart getCssClass(String name) {
		if(name == null) return null;
		name = name.trim();
		if(name.length() < 1) return null;
		if(name.charAt(0) != '.') name = "." + name;
		return getCssSelector(name);
	}
	
	public List<EspPart> getCssClasses() {
		return getCssSelectors(false, false, true);
	}
	
	public EspPart getCssId(String name) {
		if(name == null) return null;
		name = name.trim();
		if(name.length() < 1) return null;
		if(name.charAt(0) != '#') name = "#" + name;
		return getCssSelector(name);
	}
	
	public List<EspPart> getCssIds() {
		return getCssSelectors(false, true, false);
	}

	public EspPart getCssSelector(String name) {
		for(EspDom dom : doms) {
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
		}
		return null;
	}
	
	public List<EspPart> getCssSelectors() {
		return getCssSelectors(true, true, true);
	}

	private List<EspPart> getCssSelectors(boolean all, boolean ids, boolean classes) {
		List<EspPart> selectors = new ArrayList<EspPart>();
		for(EspDom dom : doms) {
			addCssSelectors(dom, all, ids, classes, selectors);
		}
		return selectors;
	}
	
	public boolean remove(EspDom dom) {
		return doms.remove(dom);
	}
	
}
