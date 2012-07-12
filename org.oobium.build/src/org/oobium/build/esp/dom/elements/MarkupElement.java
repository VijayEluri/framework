package org.oobium.build.esp.dom.elements;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.oobium.build.esp.dom.EspElement;
import org.oobium.build.esp.dom.EspPart;
import org.oobium.build.esp.dom.common.MethodPart;
import org.oobium.build.esp.dom.parts.MethodArg;

public class MarkupElement extends EspElement implements MethodPart {

	private EspPart tag;
	private EspPart javaType;
	private EspPart id;
	private List<EspPart> classes;
	private List<MethodArg> args;
	private Map<String, MethodArg> entries;
	private EspPart innerText;
	private boolean hidden;
	
	public MarkupElement() {
		this(Type.MarkupElement);
	}
	
	protected MarkupElement(Type type) {
		super(type);
	}
	
	public MarkupElement addClass(EspPart cssClass) {
		if(classes == null) {
			classes = new ArrayList<EspPart>();
		}
		classes.add(cssClass);
		return this;
	}
	
	@Override
	public MarkupElement addArg(MethodArg arg) {
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
	
	public int getArgCount() {
		return (args != null) ? args.size() : 0;
	}
	
	public List<MethodArg> getArgs() {
		return args;
	}
	
	public List<EspPart> getClasses() {
		return classes;
	}

	public int getEntryCount() {
		return (entries != null) ? entries.size() : 0;
	}
	
	public Map<String, MethodArg> getEntries() {
		return entries;
	}

	public MethodArg getEntry(String name) {
		return (entries != null) ? entries.get(name) : null;
	}

	public EspPart getEntryValue(String name) {
		if(entries != null) {
			MethodArg arg = entries.get(name);
			if(arg != null) return arg.getValue();
		}
		return null;
	}

	public EspPart getId() {
		return id;
	}
	
	public EspPart getInnerText() {
		return innerText;
	}
	
	public EspPart getJavaType() {
		return javaType;
	}
	
	public EspPart getTag() {
		return tag;
	}
	
	public boolean hasArg(int ix) {
		return args != null && ix < args.size() && args.get(ix) != null;
	}
	
	public boolean hasArgs() {
		return args != null && !args.isEmpty();
	}
	
	public boolean hasClasses() {
		return classes != null && !classes.isEmpty();
	}
	
	public boolean hasEntries() {
		return entries != null && !entries.isEmpty();
	}
	
	public boolean hasEntry(String name) {
		return entries != null && entries.get(name) != null;
	}

	public boolean hasEntryValue(String name) {
		if(entries != null) {
			MethodArg arg = entries.get(name);
			if(arg != null) return arg.hasValue();
		}
		return false;
	}

	public boolean hasId() {
		return id != null;
	}
	
	public boolean hasInnerText() {
		return innerText != null;
	}
	
	public boolean hasJavaType() {
		return javaType != null;
	}
	
	@Override
	public void initArgs() {
		args = new ArrayList<MethodArg>();
	}
	
	public boolean isHidden() {
		return hidden;
	}
	
	public void setArgs(List<MethodArg> args) {
		this.args = args;
	}
	
	public void setClasses(List<EspPart> classes) {
		this.classes = classes;
	}
	
	public void setEntries(Map<String, MethodArg> entries) {
		this.entries = entries;
	}
	
	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}
	
	public void setId(EspPart id) {
		this.id = id;
	}
	
	public void setInnerText(EspPart innerText) {
		this.innerText = innerText;
	}
	
	public void setJavaType(EspPart javaType) {
		this.javaType = javaType;
	}
	
	public void setTag(EspPart tag) {
		this.tag = tag;
	}

}
