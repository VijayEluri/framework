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
	
	public EspElement getElement() {
		return (EspElement) parent;
	}

	public boolean hasChildren() {
		return children != null && !children.isEmpty();
	}
	
	private EspElement removeElement(EspElement element) {
		if(children != null) {
			children.remove(element);
		}
		return this;
	}

	public EspElement setChildren(List<EspElement> children) {
		this.children = children;
		return this;
	}
	
	@Override
	public EspPart setParent(EspPart newParent) {
		if(newParent != null && !(newParent instanceof EspElement)) {
			throw new IllegalArgumentException("parent of an element must also be an element; not " + newParent);
		}
		
		EspElement oldElement = (EspElement) parent;
		if(oldElement != null) {
			oldElement.removeElement(this);
		}
		EspElement newElement = (EspElement) newParent;
		if(newElement != null) {
			newElement.addElement(this);
		}
		
		return super.setParent(newParent);
	}
	
}
