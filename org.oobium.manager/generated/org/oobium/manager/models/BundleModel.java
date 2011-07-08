/*
@ModelDescription()
*/
package org.oobium.manager.models;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import org.oobium.persist.Model;
import org.oobium.persist.Paginator;
import org.oobium.utils.json.JsonModel;

public abstract class BundleModel extends Model {

	/**
	 * Create a new instance of Bundle and set its id to the given value
	*/
	public static Bundle newInstance(int id) {
		Bundle bundle = new Bundle();
		bundle.setId(id);
		return bundle;
	}

	/**
	 * Create a new instance of Bundle and initialize it with the given fields
	*/
	public static Bundle newInstance(Map<String, Object> fields) {
		Bundle bundle = new Bundle();
		bundle.setAll(fields);
		return bundle;
	}

	/**
	 * Create a new instance of Bundle and initialize it with the given json data
	*/
	public static Bundle newInstance(String json) {
		Bundle bundle = new Bundle();
		bundle.setAll(json);
		return bundle;
	}

	/**
	 * Find the Bundle with the given id
	*/
	public static Bundle find(int id) throws SQLException {
		return Model.find(Bundle.class, id);
	}

	/**
	 * Find the Bundle with the given id and include the given fields.
	 * The include option can start with 'include:', but it is not required.
	*/
	public static Bundle find(int id, String include) throws SQLException {
		String sql = (include.startsWith("include:") ? "where id=? " : "where id=? include:") + include;
		return Model.find(Bundle.class, sql, id);
	}

	/**
	 * Find the Bundle with using the given sql query and values.  Note that only one instance will be returned.
	 * Prepend the query with 'where' to enter only the where clause.
	*/
	public static Bundle find(String sql, Object...values) throws SQLException {
		return Model.find(Bundle.class, sql, values);
	}

	public static List<Bundle> findAll() throws SQLException {
		return Model.findAll(Bundle.class);
	}

	public static List<Bundle> findAll(String sql, Object...values) throws SQLException {
		return Model.findAll(Bundle.class, sql, values);
	}

	public static Paginator<Bundle> paginate(int page, int perPage) throws SQLException {
		return Paginator.paginate(Bundle.class, page, perPage);
	}

	public static Paginator<Bundle> paginate(int page, int perPage, String sql, Object...values) throws SQLException {
		return Paginator.paginate(Bundle.class, page, perPage, sql, values);
	}

	@Override
	public Bundle put(String field, Object value) {
		return (Bundle) super.put(field, value);
	}

	@Override
	public Bundle putAll(JsonModel model) {
		return (Bundle) super.putAll(model);
	}

	@Override
	public Bundle putAll(Map<String, Object> data) {
		return (Bundle) super.putAll(data);
	}

	@Override
	public Bundle putAll(String json) {
		return (Bundle) super.putAll(json);
	}

	@Override
	public Bundle set(String field, Object value) {
		return (Bundle) super.set(field, value);
	}

	@Override
	public Bundle setAll(Map<String, Object> data) {
		return (Bundle) super.setAll(data);
	}

	@Override
	public Bundle setAll(String json) {
		return (Bundle) super.setAll(json);
	}

	@Override
	public Bundle setId(int id) {
		return (Bundle) super.setId(id);
	}

}