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
package org.oobium.build.util;


public class JarVersion {

	public static JarVersion parse(String str) {
		JarVersion jv = new JarVersion();
		jv.parseString(str);
		return jv;
	}
	
	private int major = -1;
	private int minor = -1;
	private int micro = -1;
	private String build = "";

	private void parseString(String version) {
		String str = version;
		try {
			if(str.endsWith(".jar")) {
				str = str.substring(0, str.length()-4);
			}
			while(!Character.isDigit(str.charAt(0))) {
				str = str.substring(1);
			}
			String[] sa = str.split("\\.");
			if(sa.length > 0) {
				major = Integer.parseInt(sa[0]);
				if(sa.length > 1) {
					minor = Integer.parseInt(sa[1]);
					if(sa.length > 2) {
						micro = Integer.parseInt(sa[2]);
						if(sa.length > 3) {
							build = sa[3];
						}
					}
				}
			}
		} catch(Exception e) {
			major = minor = micro = -1;
			build = version;
		}
	}

	public boolean after(JarVersion version) {
		if(major > version.major) {
			return true;
		} else if(major < version.major) {
			return false;
		} else {
			if(minor > version.minor) {
				return true;
			} else if(minor < version.minor) {
				return false;
			} else {
				if(micro > version.micro) {
					return true;
				} else if(micro < version.micro) {
					return false;
				} else {
					return build.compareTo(version.build) > 0;
				}
			}
		}
	}

	public boolean before(JarVersion version) {
		if(major > version.major) {
			return false;
		} else if(major < version.major) {
			return true;
		} else {
			if(minor > version.minor) {
				return false;
			} else if(minor < version.minor) {
				return true;
			} else {
				if(micro > version.micro) {
					return false;
				} else if(micro < version.micro) {
					return true;
				} else {
					return build.compareTo(version.build) < 0;
				}
			}
		}
	}
	
	public int compareTo(JarVersion version) {
		if(major > version.major) {
			return 1;
		} else if(major < version.major) {
			return -1;
		} else {
			if(minor > version.minor) {
				return 1;
			} else if(minor < version.minor) {
				return -1;
			} else {
				if(micro > version.micro) {
					return 1;
				} else if(micro < version.micro) {
					return -1;
				} else {
					return build.compareTo(version.build);
				}
			}
		}
	}

}
