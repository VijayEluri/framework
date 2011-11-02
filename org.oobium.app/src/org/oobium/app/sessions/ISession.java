package org.oobium.app.sessions;

import java.util.Date;

public interface ISession {

	public static final String SESSION_ID_KEY = "oobium_session_id";
	public static final String SESSION_UUID_KEY = "oobium_session_uuid";

	public abstract void clearData();

	public abstract String getData(String key);

	public abstract Date getExpiration();

	public abstract Object getId();
	
	public abstract String getUuid();

	public abstract boolean isDestroyed();

	public abstract void putData(String key, Object value);

	public abstract void putData(String key, String value);

	public abstract String removeData(String key);

	public abstract boolean save();
	
	public abstract void setExpiration(Date expiration);

}