package org.oobium.build.esp.dom.parts.style;

import java.util.ArrayList;
import java.util.List;

import org.oobium.build.esp.dom.EspPart;

public class SelectorGroup extends EspPart {

	private List<Selector> selectors;
	
	public SelectorGroup() {
		super(Type.StyleSelectorGroup);
	}
	
	public void addSelector(Selector selector) {
		if(selectors == null) {
			selectors = new ArrayList<Selector>();
		}
		selectors.add(selector);
	}
	
	@Override
	public Ruleset getParent() {
		return (Ruleset) super.getParent();
	}
	
	public List<Selector> getSelectors() {
		return (selectors != null) ? selectors : new ArrayList<Selector>(0);
	}
	
	public boolean hasSelectors() {
		return selectors != null && !selectors.isEmpty();
	}
	
}
