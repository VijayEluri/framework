/*
@ModelDescription(
	attrs = {
		@Attribute(name="host", type=String.class, required=true),		// the host to listen to for events (domain & port)
		@Attribute(name="service", type=String.class, required=true),	// the service to listen to for events
		@Attribute(name="eventName", type=String.class, required=true),	// the name of the event to listen for
		@Attribute(name="callback", type=String.class, required=true),	// the URL to notify when an event occurs (send events here)
		@Attribute(name="remoteUrl", type=String.class),				// if this is a local listener: the URL for listeners on the remote server
		@Attribute(name="remoteId", type=int.class),					// if this is a local listener: the id of the remote listener, if it has been created
		@Attribute(name="oneShot", type=boolean.class)					// if true, automatically delete this listener after the first event has occurred
	},
	timestamps = true,
	allowUpdate = false
)
@Indexes("{Unique-host,service,eventName,callback}")
*/
package org.oobium.events.models;

import static org.oobium.utils.StringUtils.blank;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.oobium.persist.Model;
import org.oobium.persist.Paginator;
import org.oobium.utils.json.JsonModel;

public abstract class ListenerModel extends Model {

	public static final String CALLBACK = "callback";
	public static final String CREATED_AT = "createdAt";
	public static final String EVENT_NAME = "eventName";
	public static final String HOST = "host";
	public static final String ONE_SHOT = "oneShot";
	public static final String REMOTE_ID = "remoteId";
	public static final String REMOTE_URL = "remoteUrl";
	public static final String SERVICE = "service";
	public static final String UPDATED_AT = "updatedAt";

	/**
	 * Create a new instance of Listener and set its id to the given value
	*/
	public static Listener newInstance(int id) {
		Listener listener = new Listener();
		listener.setId(id);
		return listener;
	}

	/**
	 * Create a new instance of Listener and initialize it with the given fields
	*/
	public static Listener newInstance(Map<String, Object> fields) {
		Listener listener = new Listener();
		listener.setAll(fields);
		return listener;
	}

	/**
	 * Create a new instance of Listener and initialize it with the given json data
	*/
	public static Listener newInstance(String json) {
		Listener listener = new Listener();
		listener.setAll(json);
		return listener;
	}

	/**
	 * Find the Listener with the given id
	*/
	public static Listener find(int id) throws Exception {
		return getPersistService(Listener.class).findById(Listener.class, id);
	}

	/**
	 * Find the Listener with the given id and include the given fields.
	 * The include option can start with 'include:', but it is not required.
	*/
	public static Listener find(int id, String include) throws Exception {
		String sql = (include.startsWith("include:") ? "where id=? " : "where id=? include:") + include;
		return getPersistService(Listener.class).find(Listener.class, sql, id);
	}

	/**
	 * Find the Listener with using the given sql query and values.  Note that only one instance will be returned.
	 * Prepend the query with 'where' to enter only the where clause.
	*/
	public static Listener find(String sql, Object...values) throws Exception {
		return getPersistService(Listener.class).find(Listener.class, sql, values);
	}

	public static List<Listener> findAll() throws Exception {
		return getPersistService(Listener.class).findAll(Listener.class);
	}

	public static List<Listener> findAll(String sql, Object...values) throws Exception {
		return getPersistService(Listener.class).findAll(Listener.class, sql, values);
	}

	public static Paginator<Listener> paginate(int page, int perPage) throws Exception {
		return Paginator.paginate(Listener.class, page, perPage);
	}

	public static Paginator<Listener> paginate(int page, int perPage, String sql, Object...values) throws Exception {
		return Paginator.paginate(Listener.class, page, perPage, sql, values);
	}

	@Override
	protected void validateSave() {
		if((isNew() || isSet(CALLBACK)) && blank(get(CALLBACK))) {
			addError(CALLBACK, "cannot be blank");
		}
		if((isNew() || isSet(EVENT_NAME)) && blank(get(EVENT_NAME))) {
			addError(EVENT_NAME, "cannot be blank");
		}
		if((isNew() || isSet(HOST)) && blank(get(HOST))) {
			addError(HOST, "cannot be blank");
		}
		if((isNew() || isSet(SERVICE)) && blank(get(SERVICE))) {
			addError(SERVICE, "cannot be blank");
		}
	}

	public String getCallback() {
		return get(CALLBACK, String.class);
	}

	public Date getCreatedAt() {
		return get(CREATED_AT, Date.class);
	}

	public String getEventName() {
		return get(EVENT_NAME, String.class);
	}

	public String getHost() {
		return get(HOST, String.class);
	}

	public boolean getOneShot() {
		return get(ONE_SHOT, boolean.class);
	}

	public int getRemoteId() {
		return get(REMOTE_ID, int.class);
	}

	public String getRemoteUrl() {
		return get(REMOTE_URL, String.class);
	}

	public String getService() {
		return get(SERVICE, String.class);
	}

	public Date getUpdatedAt() {
		return get(UPDATED_AT, Date.class);
	}

	public boolean hasCallback() {
		return get(CALLBACK) != null;
	}

	public boolean hasCreatedAt() {
		return get(CREATED_AT) != null;
	}

	public boolean hasEventName() {
		return get(EVENT_NAME) != null;
	}

	public boolean hasHost() {
		return get(HOST) != null;
	}

	public boolean hasOneShot() {
		return get(ONE_SHOT) != null;
	}

	public boolean hasRemoteId() {
		return get(REMOTE_ID) != null;
	}

	public boolean hasRemoteUrl() {
		return get(REMOTE_URL) != null;
	}

	public boolean hasService() {
		return get(SERVICE) != null;
	}

	public boolean hasUpdatedAt() {
		return get(UPDATED_AT) != null;
	}

	public boolean isOneShot() {
		return Boolean.TRUE.equals(getOneShot());
	}

	@Override
	public Listener put(String field, Object value) {
		return (Listener) super.put(field, value);
	}

	@Override
	public Listener putAll(JsonModel model) {
		return (Listener) super.putAll(model);
	}

	@Override
	public Listener putAll(Map<String, Object> data) {
		return (Listener) super.putAll(data);
	}

	@Override
	public Listener putAll(String json) {
		return (Listener) super.putAll(json);
	}

	@Override
	public Listener set(String field, Object value) {
		return (Listener) super.set(field, value);
	}

	@Override
	public Listener setAll(Map<String, Object> data) {
		return (Listener) super.setAll(data);
	}

	@Override
	public Listener setAll(String json) {
		return (Listener) super.setAll(json);
	}

	public Listener setCallback(String callback) {
		return set(CALLBACK, callback);
	}

	public Listener setCreatedAt(Date createdAt) {
		return set(CREATED_AT, createdAt);
	}

	public Listener setEventName(String eventName) {
		return set(EVENT_NAME, eventName);
	}

	public Listener setHost(String host) {
		return set(HOST, host);
	}

	@Override
	public Listener setId(Object id) {
		return (Listener) super.setId(id);
	}

	public Listener setOneShot(boolean oneShot) {
		return set(ONE_SHOT, oneShot);
	}

	public Listener setRemoteId(int remoteId) {
		return set(REMOTE_ID, remoteId);
	}

	public Listener setRemoteUrl(String remoteUrl) {
		return set(REMOTE_URL, remoteUrl);
	}

	public Listener setService(String service) {
		return set(SERVICE, service);
	}

	public Listener setUpdatedAt(Date updatedAt) {
		return set(UPDATED_AT, updatedAt);
	}

}