package org.oobium.build.esp.dom.parts;

import org.oobium.build.esp.dom.EspPart;

public class MethodArg extends EspPart {

	private EspPart name;
	private EspPart value;
	private EspPart condition;
	
	public MethodArg() {
		super(Type.MethodArg);
	}
	
	@Override
	public boolean equals(Object obj) {
		if((obj instanceof String) && (value != null)) {
			return obj.equals(value.getText());
		}
		return super.equals(obj);
	}
	
	public EspPart getCondition() {
		return condition;
	}
	
	public EspPart getName() {
		return name;
	}
	
	public EspPart getValue() {
		return value;
	}
	
	public boolean hasCondition() {
		return condition != null;
	}
	
	public boolean hasName() {
		return name != null;
	}
	
	public boolean hasValue() {
		return value != null;
	}
	
	public void setCondition(EspPart condition) {
		this.condition = condition;
	}
	
	public void setName(EspPart key) {
		this.name = key;
	}
	
	public void setValue(EspPart value) {
		this.value = value;
	}
	
}
