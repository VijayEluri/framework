/*
@ModelDescription(
	attrs = {
		@Attribute(name="host", type=String.class, required=true),		// the host that triggered the event
		@Attribute(name="service", type=String.class, required=true),	// the service that triggered the event
		@Attribute(name="eventName", type=String.class, required=true),	// the name of the event
		@Attribute(name="data", type=String.class, required=true)		// any data required to describe the event
	},
	timestamps = true,
	allowDelete = false,
	allowUpdate = false
)
*/
package org.oobium.events.models;

import static org.oobium.utils.StringUtils.blank;

import java.lang.String;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.oobium.persist.Model;
import org.oobium.persist.Paginator;
import org.oobium.utils.json.JsonModel;

public abstract class EventModel extends Model {

	public static final String CREATED_AT = "createdAt";
	public static final String DATA = "data";
	public static final String EVENT_NAME = "eventName";
	public static final String HOST = "host";
	public static final String SERVICE = "service";
	public static final String UPDATED_AT = "updatedAt";

	/**
	 * Create a new instance of Event and set its id to the given value
	*/
	public static Event newInstance(int id) {
		Event event = new Event();
		event.setId(id);
		return event;
	}

	/**
	 * Create a new instance of Event and initialize it with the given fields
	*/
	public static Event newInstance(Map<String, Object> fields) {
		Event event = new Event();
		event.setAll(fields);
		return event;
	}

	/**
	 * Create a new instance of Event and initialize it with the given json data
	*/
	public static Event newInstance(String json) {
		Event event = new Event();
		event.setAll(json);
		return event;
	}

	/**
	 * Find the Event with the given id
	*/
	public static Event find(int id) throws SQLException {
		return Model.find(Event.class, id);
	}

	/**
	 * Find the Event with the given id and include the given fields.
	 * The include option can start with 'include:', but it is not required.
	*/
	public static Event find(int id, String include) throws SQLException {
		String sql = (include.startsWith("include:") ? "where id=? " : "where id=? include:") + include;
		return Model.find(Event.class, sql, id);
	}

	/**
	 * Find the Event with using the given sql query and values.  Note that only one instance will be returned.
	 * Prepend the query with 'where' to enter only the where clause.
	*/
	public static Event find(String sql, Object...values) throws SQLException {
		return Model.find(Event.class, sql, values);
	}

	public static List<Event> findAll() throws SQLException {
		return Model.findAll(Event.class);
	}

	public static List<Event> findAll(String sql, Object...values) throws SQLException {
		return Model.findAll(Event.class, sql, values);
	}

	public static Paginator<Event> paginate(int page, int perPage) throws SQLException {
		return Paginator.paginate(Event.class, page, perPage);
	}

	public static Paginator<Event> paginate(int page, int perPage, String sql, Object...values) throws SQLException {
		return Paginator.paginate(Event.class, page, perPage, sql, values);
	}

	@Override
	protected void validateSave() {
		if((isNew() || isSet(DATA)) && blank(get(DATA))) {
			addError(DATA, "cannot be blank");
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

	public Date getCreatedAt() {
		return get(CREATED_AT, Date.class);
	}

	public String getData() {
		return get(DATA, String.class);
	}

	public String getEventName() {
		return get(EVENT_NAME, String.class);
	}

	public String getHost() {
		return get(HOST, String.class);
	}

	public String getService() {
		return get(SERVICE, String.class);
	}

	public Date getUpdatedAt() {
		return get(UPDATED_AT, Date.class);
	}

	public boolean hasCreatedAt() {
		return get(CREATED_AT) != null;
	}

	public boolean hasData() {
		return get(DATA) != null;
	}

	public boolean hasEventName() {
		return get(EVENT_NAME) != null;
	}

	public boolean hasHost() {
		return get(HOST) != null;
	}

	public boolean hasService() {
		return get(SERVICE) != null;
	}

	public boolean hasUpdatedAt() {
		return get(UPDATED_AT) != null;
	}

	@Override
	public Event put(String field, Object value) {
		return (Event) super.put(field, value);
	}

	@Override
	public Event putAll(JsonModel model) {
		return (Event) super.putAll(model);
	}

	@Override
	public Event putAll(Map<String, Object> data) {
		return (Event) super.putAll(data);
	}

	@Override
	public Event putAll(String json) {
		return (Event) super.putAll(json);
	}

	@Override
	public Event set(String field, Object value) {
		return (Event) super.set(field, value);
	}

	@Override
	public Event setAll(Map<String, Object> data) {
		return (Event) super.setAll(data);
	}

	@Override
	public Event setAll(String json) {
		return (Event) super.setAll(json);
	}

	public Event setCreatedAt(Date createdAt) {
		return set(CREATED_AT, createdAt);
	}

	public Event setData(String data) {
		return set(DATA, data);
	}

	public Event setEventName(String eventName) {
		return set(EVENT_NAME, eventName);
	}

	public Event setHost(String host) {
		return set(HOST, host);
	}

	@Override
	public Event setId(int id) {
		return (Event) super.setId(id);
	}

	public Event setService(String service) {
		return set(SERVICE, service);
	}

	public Event setUpdatedAt(Date updatedAt) {
		return set(UPDATED_AT, updatedAt);
	}

}