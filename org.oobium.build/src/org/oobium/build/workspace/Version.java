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
package org.oobium.build.workspace;

import static org.oobium.utils.coercion.TypeCoercer.*;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Version implements Comparable<Version> {

	public final int major;
	public final int minor;
	public final int micro;
	public final String qualifier;
	
	Version() {
		major = minor = micro = 0;
		qualifier = "";
	}
	
	public Version(int major, int minor, int micro, String qualifier) {
		this.major = major;
		this.minor = minor;
		this.micro = micro;
		this.qualifier = qualifier;
	}
	
	public Version(String str) {
		if(str != null && str.length() > 0) {
			String[] sa = str.split("\\.");
			major = coerce(sa[0], int.class);
			if(sa.length > 1) {
				minor = coerce(sa[1], int.class);
				if(sa.length > 2) {
					micro = coerce(sa[2], int.class);
					if(sa.length > 3) {
						qualifier = sa[3];
					} else {
						qualifier = "";
					}
				} else {
					micro = 0;
					qualifier = "";
				}
			} else {
				minor = micro = 0;
				qualifier = "";
			}
		} else {
			major = minor = micro = 0;
			qualifier = "";
		}
	}
	
	@Override
	public int compareTo(Version version) {
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
					return qualifier.compareTo(version.qualifier);
				}
			}
		}
	}

	/**
	 * This Version is considered equal to the given object if:<br/>
	 * 1. the object is also a Version with the same components (including qualifier), or<br/>
	 * 2. the object is a String that is equal to the returned value of this Version's toString() method
	 */
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof Version) {
			Version v = (Version) obj;
			return major == v.major && minor == v.minor && micro == v.micro && qualifier.equals(v.qualifier);
		} else if(obj instanceof String) {
			return this.toString().equals(obj);
		}
		return false;
	}

	public boolean isAfter(Version version, boolean inclusive) {
		if(inclusive && version.equals(this)) {
			return true;
		}
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
					return qualifier.compareTo(version.qualifier) > 0;
				}
			}
		}
	}
	
	public boolean isBefore(Version version, boolean inclusive) {
		if(inclusive && version.equals(this)) {
			return true;
		}
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
					return qualifier.compareTo(version.qualifier) < 0;
				}
			}
		}
	}

	public boolean resolves(VersionRange range) {
		if(range == null) {
			return true;
		}
		if(range.start == null) {
			return true;
		}
		if(range.end == null) {
			return range.start.isBefore(this, true);
		}
		return (range.start.isBefore(this, range.startInclusive) && range.end.isAfter(this, range.endInclusive));
	}
	
	@Override
	public String toString() {
		return toString(false);
	}

	public String toString(boolean ommitQualifier) {
		StringBuilder sb = new StringBuilder(10 + (qualifier != null ? qualifier.length() : 0));
		sb.append(major).append('.').append(minor).append('.').append(micro);
		if(!ommitQualifier && qualifier.length() > 0) {
			sb.append('.').append(qualifier);
		}
		return sb.toString();
	}

	public Version resolve(Date date) {
		if(qualifier.equals("qualifier")) {
			SimpleDateFormat sdf = new SimpleDateFormat("'v'yyyyMMdd-HHmmss");
			String qualifier = sdf.format(date);
			return new Version(major, minor, micro, qualifier);
		} else {
			return this;
		}
	}

}
