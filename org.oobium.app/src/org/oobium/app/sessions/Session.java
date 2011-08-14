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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.oobium.persist.Attribute;
import org.oobium.persist.Model;
import org.oobium.persist.ModelDescription;
import org.oobium.persist.PersistException;

@ModelDescription(
	attrs = {
		@Attribute(name="uuid", type=String.class),
		@Attribute(name="data", type=Map.class),
		@Attribute(name="expiration", type=Date.class)
	}
)
public class Session extends Model {

	public static final String SESSION_ID_KEY = "oobium_session_id";
	public static final String SESSION_UUID_KEY = "oobium_session_uuid";

    public static Session retrieve(int id, String uuid) {
		if(id > 0 && uuid != null) {
			try {
				return Model.getPersistService(Session.class).find(Session.class, "id:?,uuid:?,expiration:{gt:?}", id, uuid, new Date());
			} catch(PersistException e) {
				Model.getLogger().warn(e);
			}
		}
		return null;
	}

    /**
     * @param expiresIn seconds
     */
	public Session(int expiresIn) {
		this(-1, UUID.randomUUID().toString(), new HashMap<String, String>(), new Date(System.currentTimeMillis() + expiresIn*1000));
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
	
	public void clearData() {
    	Map<String, String> data = getData();
    	if(data != null) {
    		data.clear();
    	}
	}

	public String getData(String key) {
    	Map<String, String> data = getData();
    	if(data != null) {
    		return data.get(key);
    	}
    	return null;
	}

	public Date getExpiration() {
		Date expiration = (Date) get("expiration");
		return (Date) expiration.clone();
	}

	public String getUuid() {
		return (String) get("uuid");
	}
	
	public boolean isDestroyed() {
		Date expiration = (Date) get("expiration");
		return expiration == null || expiration.before(new Date());
	}
	
	public void putData(String key, Object value) {
		putData(key, String.valueOf(value));
	}
	
	public void putData(String key, String value) {
    	Map<String, String> data = getData();
    	if(data == null) {
    		data = new HashMap<String, String>();
    		put("data", data);
    	}
		data.put(key, value);
	}
	
	public String removeData(String key) {
    	Map<String, String> data = getData();
    	if(data != null) {
    		return data.remove(key);
    	}
    	return null;
	}

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
