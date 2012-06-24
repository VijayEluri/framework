package org.oobium.build.esp.dom;

import java.util.ArrayList;
import java.util.List;

public class EspElement extends EspPart {

	private List<EspElement> children;
	
	public EspElement(Type type) {
		super(type);
	}
	
	private EspElement addElement(EspElement child) {
		if(children == null) {
			children = new ArrayList<EspElement>();
		}
		children.add(child);
		return this;
	}
	
	public List<EspElement> getChildren() {
		return (children != null) ? children : new ArrayList<EspElement>(0);
	}
	
	public boolean hasChildren() {
		return children != null && !children.isEmpty();
	}
	
	private EspElement removeElement(EspElement element) {
		if(children != null) {
			if(children.remove(element)) {
				element.element = null;
			}
		}
		return this;
	}

	public EspElement setChildren(List<EspElement> children) {
		this.children = children;
		return this;
	}
	
	@Override
	public EspElement setElement(EspElement newElement) {
		if(element != null) {
			element.removeElement(this);
		}
		element = newElement;
		if(element != null) {
			element.addElement(this);
		}
		return this;
	}

}
