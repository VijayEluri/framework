package org.oobium.build.workspace;

import java.util.List;

import org.oobium.utils.Config;
import org.oobium.utils.Config.Mode;

public class Site {

	private final Application container;
	private Mode mode;
	private Config config;
	
	Site(Application application) {
		this.container = application;
	}

	private Config config() {
		if(config == null) {
			config = Config.loadConfiguration(container.site, mode);
		}
		return config;
	}

	public String[] getApplications() {
		Object o = config().get("apps");
		if(o == null) {
			return new String[] { container.name };
		}
		if(o instanceof List) {
			List<?> l = (List<?>) o;
			String[] tmp = l.toArray(new String[l.size()]);
			String[] apps = new String[tmp.length];
			System.arraycopy(tmp, 0, apps, 1, tmp.length);
			apps[0] = container.name;
			return apps;
		}
		throw new IllegalArgumentException("'apps' property in site.js can only be an array");
	}
	
	public String getHost() {
		return config().getString("host");
	}
	
	public String getPath() {
		return config().getString("path");
	}
	
	public String getUsername() {
		return config().getString("username");
	}
	
	public String getPassword() {
		return config().getString("password");
	}
	
	public boolean useSudo() {
		return config().get("sudo", boolean.class);
	}
	
	public Mode getMode() {
		return mode;
	}
	
	public void setMode(Mode mode) {
		this.mode = mode;
		this.config = null;
	}
	
}
