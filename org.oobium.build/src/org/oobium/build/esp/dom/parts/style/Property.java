package org.oobium.build.esp.dom.parts.style;

import static org.oobium.utils.StringUtils.methodName;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.oobium.build.esp.dom.EspPart;
import org.oobium.build.esp.dom.common.MethodPart;
import org.oobium.build.esp.dom.parts.MethodArg;

public class Property extends EspPart implements MethodPart {

	private EspPart name;
	private List<MethodArg> args;
	private Map<String, MethodArg> entries;
	private EspPart value;
	
	public Property() {
		super(Type.StyleProperty);
	}

	@Override
	public Property addArg(MethodArg arg) {
		if(arg.hasName()) {
			if(entries == null) {
				entries = new TreeMap<String, MethodArg>();
			}
			EspPart name = arg.getName();
			entries.put(name.getText(), arg);
		} else {
			if(args == null) {
				args = new ArrayList<MethodArg>();
			}
			args.add(arg);
		}
		return this;
	}

	public MethodArg getArg(int ix) {
		return (args != null) ? args.get(ix) : null;
	}
	
	public List<MethodArg> getArgs() {
		return args;
	}
	
	public Map<String, MethodArg> getEntries() {
		return entries;
	}
	
	public MethodArg getEntry(String name) {
		return (entries != null) ? entries.get(name) : null;
	}
	
	public String getMethodName() {
		return (name != null) ? methodName(name.getText()) : null;
	}

	public EspPart getName() {
		return name;
	}
	
	public EspPart getValue() {
		return value;
	}
	
	public boolean hasArg(int ix) {
		return args != null && ix < args.size() && args.get(ix) != null;
	}
	
	public boolean hasArgs() {
		return args != null && !args.isEmpty();
	}
	
	public boolean hasEntries() {
		return entries != null && !entries.isEmpty();
	}
	
	public boolean hasEntry(String name) {
		return entries != null && entries.get(name) != null;
	}
	
	public boolean hasName() {
		return name != null;
	}

	public boolean hasValue() {
		return value != null;
	}

	@Override
	public void initArgs() {
		args = new ArrayList<MethodArg>();
	}
	
	public boolean isMixin() {
		return value == null;
	}
	
	public boolean isParametric() {
		return args != null; // may be empty
	}
	
	public void setName(EspPart name) {
		this.name = name;
	}
	
	public void setValue(EspPart value) {
		this.value = value;
	}

}
