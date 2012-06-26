package org.oobium.build.esp.dom.parts.style;

import java.util.ArrayList;
import java.util.List;

import org.oobium.build.esp.dom.EspPart;

public class Declaration extends EspPart {

	private List<Property> properties;
	
	public Declaration() {
		super(Type.StyleDeclaration);
	}
	
	public void addProperty(Property property) {
		if(properties == null) {
			properties = new ArrayList<Property>();
		}
		properties.add(property);
	}
	
	@Override
	public Ruleset getParent() {
		return (Ruleset) super.getParent();
	}
	
	public List<Property> getProperties() {
		return (properties != null) ? properties : new ArrayList<Property>(0);
	}
	
	public boolean hasProperties() {
		return properties != null && !properties.isEmpty();
	}

}
