package org.oobium.eclipse.esp.config;

import java.util.HashMap;
import java.util.Map;

import org.oobium.utils.Config.Mode;

public class ConfigMode {

	public final Mode mode;
	public final Map<?,?> config;
	
	public ConfigMode(Map<?,?> config) {
		this.mode = null;
		this.config = config;
	}
	
	public ConfigMode(Mode mode, Map<?,?> config) {
		this.mode = mode;
		String name = mode.name().toLowerCase();
		Map<?,?> tmp = null;
		for(String key : config.keySet().toArray(new String[config.size()])) {
			if(name.equalsIgnoreCase(key)) {
				tmp = (Map<?,?>) config.remove(key);
				break;
			}
		}
		this.config = (tmp != null) ? tmp : new HashMap<Object, Object>(0);
	}
	
	public boolean isEmpty() {
		return config != null && config.isEmpty();
	}
	
}
