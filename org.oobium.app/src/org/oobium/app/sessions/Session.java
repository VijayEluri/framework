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
package org.oobium.app.sessions;

import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.oobium.http.HttpSession;
import org.oobium.persist.Attribute;
import org.oobium.persist.Model;
import org.oobium.persist.ModelDescription;

@ModelDescription(
	attrs = {
		@Attribute(name="uuid", type=String.class),
		@Attribute(name="data", type=Map.class),
		@Attribute(name="expiration", type=Date.class)
	}
)
public class Session extends Model implements HttpSession {

    public static Session retrieve(int id, String uuid) {
		if(id > 0 && uuid != null) {
			try {
				return Model.find(Session.class, "WHERE id=? AND uuid=? AND expiration>?", id, uuid, new Date());
			} catch(SQLException e) {
				Model.getLogger().warn(e);
			}
		}
		return null;
	}

	public Session() {
		this(-1, UUID.randomUUID().toString(), new HashMap<String, String>(), new Date(System.currentTimeMillis() + 30*60*1000));
	}
	
	private Session(int id, String uuid, Map<String, String> dataMap, Date expiration) {
		put("id", id);
		put("uuid", uuid);
		put("dataMap", dataMap);
		put("expiration", new Date(expiration.getTime()));
	}
	
	public Session(Date expiration) {
		this(-1, UUID.randomUUID().toString(), new HashMap<String, String>(), expiration);
	}
	
	@SuppressWarnings("unchecked")
	private Map<String, String> getData() {
		return (Map<String, String>) get("data");
	}
	
    @Override
	public void clearData() {
    	Map<String, String> data = getData();
    	if(data != null) {
    		data.clear();
    	}
	}

	@Override
	public String getData(String key) {
    	Map<String, String> data = getData();
    	if(data != null) {
    		return data.get(key);
    	}
    	return null;
	}

	@Override
	public Date getExpiration() {
		Date expiration = (Date) get("expiration");
		return (Date) expiration.clone();
	}

	@Override
	public String getUuid() {
		return (String) get("uuid");
	}
	
	@Override
	public boolean isDestroyed() {
		Date expiration = (Date) get("expiration");
		return expiration == null || expiration.before(new Date());
	}
	
	@Override
	public void putData(String key, boolean value) {
		putData(key, String.valueOf(value));
	}
	
	@Override
	public void putData(String key, double value) {
		putData(key, String.valueOf(value));
	}

	@Override
	public void putData(String key, long value) {
		putData(key, String.valueOf(value));
	}
	
	@Override
	public void putData(String key, String value) {
    	Map<String, String> data = getData();
    	if(data == null) {
    		data = new HashMap<String, String>();
    		put("data", data);
    	}
		data.put(key, value);
	}
	
	@Override
	public String removeData(String key) {
    	Map<String, String> data = getData();
    	if(data != null) {
    		return data.remove(key);
    	}
    	return null;
	}

	@Override
	public void setExpiration(Date expiration) {
		put("expiration", (Date) expiration.clone());
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{id=").append(getId()).append(", expires=").append(getExpiration()).append(", data=").append(getData()).append('}');
		return sb.toString();
	}
	
}
