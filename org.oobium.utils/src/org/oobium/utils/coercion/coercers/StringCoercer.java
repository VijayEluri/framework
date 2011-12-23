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
package org.oobium.utils.coercion.coercers;

import static org.oobium.utils.json.JsonUtils.serialize;

import java.io.File;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URL;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.oobium.utils.Base64;
import org.oobium.utils.json.JsonModel;
import org.oobium.utils.json.JsonUtils;

public class StringCoercer extends AbstractCoercer {

	public String coerce(byte[] ba, Class<?> toType) {
		StringBuilder sb = new StringBuilder(new String(Base64.encode(ba)));
		sb.insert(0, "/Base64(").append(")/");
		return sb.toString();
	}
	
	public String coerce(JsonModel model, Class<?> toType) {
		return model.toJson();
	}

	public String coerce(Map<?,?> map, Class<?> toType) {
		return JsonUtils.toJson(map);
	}
	
	public String coerce(File file, Class<?> toType) {
		return file.getAbsolutePath();
	}
	
	public String coerce(Boolean b, Class<?> toType) {
		return b.toString();
	}
	
	public String coerce(Byte b, Class<?> toType) {
		return b.toString();
	}
	
	public String coerce(Character c, Class<?> toType) {
		return c.toString();
	}
	
	public String coerce(Double d, Class<?> toType) {
		return d.toString();
	}
	
	public String coerce(Enum<?> e, Class<?> toType) {
		return e.name();
	}
	
	public String coerce(Float f, Class<?> toType) {
		return f.toString();
	}
	
	public String coerce(Integer i, Class<?> toType) {
		return i.toString();
	}
	
	public String coerce(Long l, Class<?> toType) {
		return l.toString();
	}
	
	public String coerce(Short s, Class<?> toType) {
		return s.toString();
	}
	
	public String coerce(BigDecimal bd, Class<?> toType) {
		return bd.toPlainString();
	}
	
	public String coerce(URI u, Class<?> toType) {
		return u.toString();
	}
	
	public String coerce(URL u, Class<?> toType) {
		return u.toString();
	}
	
	public String coerce(Date d, Class<?> toType) {
		return d.toString();
	}
	
	public String coerce(java.sql.Date d, Class<?> toType) {
		return d.toString();
	}
	
	public String coerce(Time t, Class<?> toType) {
		return t.toString();
	}
	
	public String coerce(Timestamp ts, Class<?> toType) {
		return ts.toString();
	}
	
	public String coerce(Locale l, Class<?> toType) {
		return l.toString();
	}
	
	public String coerce(TimeZone tz, Class<?> toType) {
		return tz.getID();
	}
	
	public String coerce(Object object, Class<?> toType) {
		if(object instanceof Map<?,?>) {
			return coerce((Map<?,?>) object, toType);
		}
		if(object instanceof JsonModel) {
			return coerce((JsonModel) object, toType);
		}
		if(object instanceof Enum<?>) {
			return coerce((Enum<?>) object, toType);
		}
		return serialize(object);
	}

	@Override
	public String coerceNull(Class<?> toType) {
		return null;
	}

	@Override
	public Class<?> getType() {
		return String.class;
	}
	
}
