package org.oobium.build.esp.dom;

import java.util.ArrayList;
import java.util.List;

import org.oobium.build.esp.dom.EspPart.Type;
import org.oobium.build.esp.dom.elements.StyleElement;
import org.oobium.build.esp.dom.parts.StylePart;
import org.oobium.build.esp.dom.parts.style.Ruleset;
import org.oobium.build.esp.dom.parts.style.Selector;

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
	
	public EspResolver(EspDom dom) {
		this();
		add(dom);
	}
	
	public EspResolver add(EspDom dom) {
		if(!doms.contains(dom)) {
			doms.add(dom);
		}
		return this;
	}
	
	public EspResolver addAll(Iterable<EspDom> doms) {
		for(EspDom dom : doms) {
			add(dom);
		}
		return this;
	}
	
	private void addCssSelectors(EspDom dom, boolean all, boolean ids, boolean classes, List<Selector> selectors) {
		for(EspPart part : dom.getParts()) {
			if(part.isA(Type.StyleElement)) {
				StyleElement element = (StyleElement) part;
				if(element.hasAsset()) {
					StylePart style = element.getAsset();
					for(Ruleset rule : style.getRules()) {
						Selector selector = rule.getSelector(); // TODO handle multiple selectors
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
	
	public Selector getCssClass(String name) {
		if(name == null) return null;
		name = name.trim();
		if(name.length() < 1) return null;
		if(name.charAt(0) != '.') name = "." + name;
		return getCssSelector(name);
	}
	
	public List<Selector> getCssClasses() {
		return getCssSelectors(false, false, true);
	}
	
	public Selector getCssId(String name) {
		if(name == null) return null;
		name = name.trim();
		if(name.length() < 1) return null;
		if(name.charAt(0) != '#') name = "#" + name;
		return getCssSelector(name);
	}
	
	public List<Selector> getCssIds() {
		return getCssSelectors(false, true, false);
	}

	public Selector getCssSelector(String name) {
		for(EspDom dom : doms) {
			for(EspPart part : dom.getParts()) {
				if(part.isA(Type.StyleElement)) {
					StyleElement element = (StyleElement) part;
					if(element.hasAsset()) {
						StylePart style = element.getAsset();
						for(Ruleset rule : style.getRules()) {
							Selector selector = rule.getSelector(); // TODO handle multiple selectors
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
	
	public List<Selector> getCssSelectors() {
		return getCssSelectors(true, true, true);
	}

	public List<Selector> getCssSelectors(boolean all, boolean ids, boolean classes) {
		List<Selector> selectors = new ArrayList<Selector>();
		for(EspDom dom : doms) {
			addCssSelectors(dom, all, ids, classes, selectors);
		}
		return selectors;
	}
	
	public boolean remove(EspDom dom) {
		return doms.remove(dom);
	}
	
}
