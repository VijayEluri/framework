/*******************************************************************************
 * Copyright (c) 2010 Oobium, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Jeremy Dowdall <jeremy@oobium.com> - initial API and implementation
 ******************************************************************************/
package org.oobium.app;

import static org.oobium.utils.json.JsonUtils.format;
import static org.oobium.utils.json.JsonUtils.toJson;
import static org.oobium.utils.json.JsonUtils.toMap;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.oobium.logging.LogProvider;
import org.oobium.utils.Config;
import org.oobium.utils.FileUtils;

public class MutableAppConfig extends Config {

	public static MutableAppConfig loadMutableConfiguration(File file) {
		if(file.exists()) {
			String fileName = file.getName();
			try {
				if(fileName.endsWith(".jar")) {
					throw new IllegalStateException("cannot load a configuration from a jar file as mutable");
				} else if(fileName.endsWith(".js") || fileName.endsWith(".json")) {
					Map<String, Object> map = toMap(FileUtils.readFile(file).toString(), true);
					return new MutableAppConfig(file, map);
				}
			} catch(Exception e) {
				LogProvider.getLogger().error("There was an error loading the configuration.", e);
			}
		}
		return new MutableAppConfig(file, new HashMap<String, Object>(0));
	}

	
	private final File file;
	
	private MutableAppConfig(File file, Map<String, Object> properties) {
		super(properties);
		this.file = file;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void add(Map map, String name, Object value) {
		Object o = map.get(name);
		if(o == null) {
			List<Object> list = new ArrayList<Object>();
			list.add(value);
			map.put(name, list);
		} else if(o instanceof List) {
			List list = (List) o;
			if(!list.contains(value)) {
				list.add(value);
			}
		} else if(o instanceof String) {
			List<Object> list = new ArrayList<Object>();
			list.add(o);
			list.add(value);
			map.put(name, list);
		} else {
			throw new IllegalStateException();
		}
	}
	
	@SuppressWarnings({ "rawtypes" })
	public void add(Mode mode, String name, Object value) {
		if(mode == null) {
			add(properties, name, value);
		} else {
			Map map = (Map) properties.get(mode.name().toLowerCase());
			if(map == null) {
				map = new LinkedHashMap();
				properties.put(mode.name().toLowerCase(), map);
			}
			add(map, name, value);
		}
	}

	public void add(String name, Object value) {
		add(properties, name, value);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object put(Mode mode, String name, Object value) {
		Object o = properties.get(mode.name().toLowerCase());
		if(o == null) {
			o = new LinkedHashMap();
			properties.put(mode.name().toLowerCase(), o);
		}
		return ((Map) o).put(name, value);
	}
	
	public Object put(String name, Object value) {
		return properties.put(name, value);
	}
	
	@SuppressWarnings({ "rawtypes" })
	private void remove(Map map, String name, Object value) {
		Object o = map.get(name);
		if(o != null) {
			if(o instanceof List) {
				List l = (List) o;
				l.remove(value);
				if(l.isEmpty()) {
					map.remove(name);
				}
			} else if(o instanceof String) {
				if(o.equals(value)) {
					map.remove(name);
				}
			} else {
				throw new IllegalStateException();
			}
		}
	}
	
	@SuppressWarnings({ "rawtypes" })
	public void remove(Mode mode, String name, Object value) {
		if(mode == null) {
			remove(properties, name, value);
		} else {
			Map map = (Map) properties.get(mode.name().toLowerCase());
			if(map == null) {
				map = new LinkedHashMap();
				properties.put(mode.name().toLowerCase(), map);
			}
			remove(map, name, value);
		}
	}
	
	public void remove(String name, Object value) {
		remove(properties, name, value);
	}

	public void save() {
		FileUtils.writeFile(file, "(" + format(toJson(properties)) + ");");
	}

}
