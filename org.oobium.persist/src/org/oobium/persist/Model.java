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
package org.oobium.persist;

import static org.oobium.persist.ModelAdapter.getAdapter;
import static org.oobium.utils.StringUtils.blank;
import static org.oobium.utils.StringUtils.titleize;
import static org.oobium.utils.coercion.TypeCoercer.coerce;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.oobium.logging.Logger;
import org.oobium.utils.StringUtils;
import org.oobium.utils.coercion.TypeCoercer;
import org.oobium.utils.json.JsonModel;
import org.oobium.utils.json.JsonUtils;


public abstract class Model implements JsonModel {

	private static final ThreadLocal<Logger> logService = new ThreadLocal<Logger>();
	private static final ThreadLocal<PersistServices> persistServices = new ThreadLocal<PersistServices>();
	
	
	public static int count(Class<? extends Model> clazz, String where, Object...values) throws SQLException {
		return getPersistService(clazz).count(clazz, where, values);
	}
	
	public static List<Map<String, Object>> executeQuery(Class<? extends Model> clazz, String sql, Object...values) throws SQLException {
		return getPersistService(clazz).executeQuery(sql, values);
	}

	public static int executeUpdate(Class<? extends Model> clazz, String sql, Object...values) throws SQLException {
		return getPersistService(clazz).executeUpdate(sql, values);
	}
	
	public static <T extends Model> T find(Class<T> clazz, int id) throws SQLException {
		return getPersistService(clazz).find(clazz, id);
	}

	public static <T extends Model> T find(Class<T> clazz, String sql, Object...values) throws SQLException {
		return getPersistService(clazz).find(clazz, sql, values);
	}
	
	public static <T extends Model> List<T> findAll(Class<T> clazz) throws SQLException {
		return getPersistService(clazz).findAll(clazz);
	}

	public static <T extends Model> List<T> findAll(Class<T> clazz, String sql, Object...values) throws SQLException {
		return getPersistService(clazz).findAll(clazz, sql, values);
	}
	
	public static Logger getLogger() {
		Logger service = logService.get();
		if(service == null) {
			service = Logger.getLogger();
			logService.set(service);
		}
		return service;
	}
	
	public static PersistService getPersistService(Class<? extends Model> clazz) {
		return getPersistServices().getFor(clazz);
	}
	
	public static PersistServices getPersistServices() {
		PersistServices services = persistServices.get();
		if(services == null) {
			services = new PersistServices();
			persistServices.set(services);
		}
		return persistServices.get();
	}
	
	protected static boolean notEquals(Object o1, Object o2) {
		return (o1 != null && !o1.equals(o2)) || o2 != null; 
	}
	
	public static void setLogger(Logger service) {
		logService.set(service);
	}

	public static PersistServices setPersistService(PersistService service) {
		PersistServices services = new PersistServices(service);
		setPersistServices(services);
		return services;
	}
	
	public static void setPersistServices(PersistServices services) {
		persistServices.set(services);
	}
	
	private PersistService persistor;
	protected Logger logger;
	
	private int id;
	private Map<String, Object> fields;
	private Map<String, ArrayList<String>> errors;

	public Model() {
		logger = getLogger();
		fields = new HashMap<String, Object>();
	}

	public final void addError(String message) {
		addError(null, message);
	}
	
	public final void addError(String subject, String message) {
		if(errors == null) {
			errors = new LinkedHashMap<String, ArrayList<String>>();
		}
		ArrayList<String> list = errors.get(subject);
		if(list == null) {
			list = new ArrayList<String>();
			errors.put(subject, list);
		}
		list.add(message);
		if(logger.isLoggingDebug()) {
			logger.debug("added error to " + asSimpleString() + ": " + subject + " -> "+ message);
		}
	}
	
	public String asSimpleString() {
		return asSimpleString(false);
	}
	
	public String asSimpleString(boolean includeErrors) {
		StringBuilder sb = new StringBuilder();
		sb.append(getClass().getSimpleName());
		sb.append(" {id:").append(id);
		if(fields.isEmpty()) {
			sb.append("-empty");
		}
		if(includeErrors && !errors.isEmpty()) {
			if(fields.isEmpty()) {
				sb.append(',').append(' ');
			}
			sb.append("errors: [");
			List<String> errors = getErrorsList();
			for(int i = 0; i < errors.size(); i++) {
				if(i != 0) sb.append(',').append(' ');
				sb.append(errors.get(i));
			}
			sb.append(']');
		}
		sb.append("}");
		return sb.toString();
	}
	
	public String asString() {
		return asString(false);
	}
	
	public String asString(boolean includeErrors) {
		StringBuilder sb = new StringBuilder();
		sb.append(getClass().getSimpleName());
		sb.append(" {id:").append(id);
		if(fields.isEmpty()) {
			sb.append("-empty");
		} else {
			for(Entry<String, Object> entry : fields.entrySet()) {
				sb.append(",").append(entry.getKey()).append(":").append(String.valueOf(entry.getValue()));
			}
		}
		if(includeErrors && !errors.isEmpty()) {
			sb.append(',').append(' ');
			sb.append("errors: [");
			List<String> errors = getErrorsList();
			for(int i = 0; i < errors.size(); i++) {
				if(i != 0) sb.append(',').append(' ');
				sb.append(errors.get(i));
			}
			sb.append(']');
		}
		sb.append("}");
		return sb.toString();
	}
	
	public final boolean canCreate() {
		clearErrors();
		Observer.runBeforeValidateCreate(this);
		validateCreate();
		Observer.runAfterValidateCreate(this);
		return !hasErrors();
	}
	
	public final boolean canDestroy() {
		clearErrors();
		Observer.runBeforeValidateDestroy(this);
		validateDestroy();
		Observer.runAfterValidateDestroy(this);
		return !hasErrors();
	}
	
	public final boolean canSave() {
		clearErrors();
		Observer.runBeforeValidateSave(this);
		validateSave();
		Observer.runAfterValidateSave(this);
		return !hasErrors();
	}
	
	public final boolean canUpdate() {
		clearErrors();
		Observer.runBeforeValidateUpdate(this);
		validateUpdate();
		Observer.runAfterValidateUpdate(this);
		return !hasErrors();
	}

	/**
	 * Clear this model's internal data map.
	 */
	public void clear() {
		fields.clear();
	}

	protected final void clearErrors() {
		if(hasErrors()) {
			errors.clear();
			errors = null;
		}
	}
	
	public boolean create() {
		if(!isNew()) {
			addError("model has already been created");
			return false;
		}
		if(canCreate()) {
			return doCreate();
		}
		return false;
	}
	
	public boolean destroy() {
		if(id == 0) {
			addError("cannot destroy a model that has not been saved");
		} else {
			if(getAdapter(getClass()).isDeletable()) {
				if(canDestroy()) {
					Observer.runBeforeDestroy(this);
					if(!hasErrors()) {
						try {
							getPersistor().destroy(this);
							id = 0;
							Observer.runAfterDestroy(this);
						} catch(SQLException e) {
							logger.warn("failed to destroy " + asSimpleString(), e);
							addError(e.getLocalizedMessage());
						}
					}
					return id == 0;
				}
			} else {
				addError("Destroy is not permitted.");
			}
		}
		return false;
	}

	private boolean doCreate() {
		boolean saved = false;
		Observer.runBeforeCreate(this);
	
		if(!hasErrors()) {
			try {
				getPersistor().create(this);
				saved = true;
				Observer.runAfterCreate(this);
				Observer.runAfterSave(this);
			} catch(SQLException e) {
				logger.warn("failed to save " + asSimpleString(), e);
				addError(e.getLocalizedMessage());
			}
		}
		return saved;
	}
	
	private boolean doUpdate() {
		boolean saved = false;
		if(getAdapter(getClass()).isUpdatable()) {
			Observer.runBeforeUpdate(this);
			
			if(!hasErrors()) {
				try {
					getPersistor().update(this);
					saved = true;
					Observer.runAfterUpdate(this);
					Observer.runAfterSave(this);
				} catch(SQLException e) {
					logger.warn("failed to save " + asSimpleString(), e);
					addError(e.getLocalizedMessage());
				}
			}
		} else {
			addError("Updates are not permitted.");
		}
		return saved;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == this) {
			return true;
		}
		if(obj != null && obj.getClass() == getClass() && ((Model) obj).getId() == id) {
			return id != 0;
		}
		return false;
	}
	
	/**
	 * Get a data field from the model object.  If the model is not new then 
	 * the field will be resolved if it is not already resolved.
	 * @param field the name of the field to be returned
	 * @return the value of the field
	 * @see #get(String, boolean)
	 */
	public Object get(String field) {
		return get(field, true);
	}
	
	/**
	 * Get a data field from the model object.  If load is true, and 
	 * the model is not new, then the field will be resolved if it 
	 * is not already resolved.
	 * @param field the name of the field to be returned
	 * @param load true to make sure the field is resolved, false otherwise
	 * @return the value of the field
	 */
	public Object get(String field, boolean load) {
		if("id".equals(field)) {
			return getId();
		} else {
			if(fields.containsKey(field)) {
				Class<?> type = getAdapter(getClass()).getClass(field);
				if(type == null) {
					return fields.get(field);
				} else {
					Object value = fields.get(field);
					if(value == null || value.getClass() == type) { // TODO also check if correct type for primitive
						return value;
					}
					return set(field, value, type);
				}
			} else {
				if(hasMany(field)) {
					Set<?> set;
					if(isThrough(field)) {
						set = new LinkedHashSet<Model>();
					} else if(isManyToNone(field)) {
						set = new LinkedHashSet<Model>();
					} else if(isOppositeRequired(field)) {
						set = new RequiredSet<Model>(this, field, getOpposite(field));
					} else {
						set = new ActiveSet<Model>(this, field, getOpposite(field), isManyToMany(field));
					}
					fields.put(field, set);
				}
				if(load && !isNew()) {
					if(hasContained(field)) {
						// TODO include the requested field if it is a hasOne...?
						load();
					} else if(hasMany(field)) {
						try {
							Model model = getPersistor().find(getClass(), "where id=? include:?", id, field);
							if(model != null) {
								set(field, model.get(field));
							}
						} catch(SQLException e) {
							logger.warn("failed to load relation " + field + " in " + asSimpleString(), e);
						}
					}
				}
				return fields.get(field);
			}
		}
	}
	
	public <T> T get(String field, Class<T> type) {
		return coerce(get(field), type);
	}
	
	public <T> T get(String field, Class<T> type, boolean load) {
		return coerce(get(field, load), type);
	}

	/**
	 * Get a map of all of the internal data fields that
	 * are currently set in this model.  Does not include the
	 * model's ID, and does not resolve the model.
	 * <p>The returned map is a copy - changes to it will not
	 * affect the model.</p>
	 * @return a map of internal data fields
	 */
	public Map<String, Object> getAll() {
		return new HashMap<String, Object>(fields);
	}

	public Map<String, List<String>> getErrors() {
		if(errors != null) {
			return new LinkedHashMap<String, List<String>>(errors);
		}
		return new LinkedHashMap<String, List<String>>(0);
	}
	
	public List<String> getErrorsList() {
		if(errors == null) {
			return new ArrayList<String>(0);
		}
		List<String> list = new ArrayList<String>();
		for(Entry<String, ArrayList<String>> entry : errors.entrySet()) {
			String subject = entry.getKey();
			for(String message : entry.getValue()) {
				if(subject == null) {
					list.add(message);
				} else {
					list.add(titleize(subject) + " " + message);
				}
			}
		}
		return list;
	}
	
	public final int getId() {
		return getId(false);
	}
	
	public final int getId(boolean saveFirst) {
		if(saveFirst && isNew()) {
			save();
		}
		return id;
	}

	private String getOpposite(String field) {
		return getAdapter(getClass()).getOpposite(field);
	}
	
	public PersistService getPersistor() {
		if(persistor == null) {
			persistor = getPersistService(getClass());
		}
		return persistor;
	}
	
	private boolean hasContained(String field) {
		ModelAdapter adapter = getAdapter(getClass());
		return adapter.hasAttribute(field) || adapter.hasOne(field);
	}
	
	public boolean hasErrors() {
		return errors != null && !errors.isEmpty();
	}
	
	@Override
	public int hashCode() {
		int hash = id + 2;
		hash = hash * 31 + getClass().getCanonicalName().hashCode();
		return hash;
	}
	
	private boolean hasMany(String field) {
		return getAdapter(getClass()).hasMany(field);
	}
	
	@Override
	public final boolean isBlank() {
		return isNew() && blank(getAll());
	}

	@Override
	public final boolean isEmpty() {
		return fields.isEmpty();
	}
	
	private boolean isManyToMany(String field) {
		return getAdapter(getClass()).isManyToMany(field);
	}

	private boolean isManyToNone(String field) {
		return getAdapter(getClass()).isManyToNone(field);
	}
	
	@Override
	public final boolean isNew() {
		return id <= 0;
	}
	
	private boolean isOppositeRequired(String field) {
		return getAdapter(getClass()).isOppositeRequired(field);
	}

	public final boolean isRequired(String field) {
		return getAdapter(getClass()).isRequired(field);
	}
	
    @Override
	public final boolean isSet(String field) {
		return fields.containsKey(field);
	}
	
    private boolean isThrough(String field) {
		return getAdapter(getClass()).isManyToNone(field);
	}
	
	public final boolean load() {
		try {
			getPersistor().retrieve(this);
			return true;
		} catch(SQLException e) {
			logger.warn("failed to load " + asSimpleString(), e);
		}
		return false;
	}
	
	/**
	 * Put the given field, and its given value, directly into this model's underlying
	 * data map.  The model will not be resolved if it isn't already.  If the field
	 * already exists in the data map, it will be over-written.  Other fields in the
	 * data map will remain unchanged.
	 * @param field
	 * @param value
	 */
	@Override
	public Model put(String field, Object value) {
		if("id".equals(field)) {
			setId(coerce(id, int.class));
		}
		fields.put(field, value);
		return this;
	}
	
	/**
	 * Sets this model's fields to those of the given model.
	 * The existing fields will first be cleared and then the
	 * given model's fields will be passed into the {@link #setAll(Map)}
	 * method of this model.
	 * @param model
	 * @see #setAll(Map)
	 * @return this, for method chaining
	 */
	@Override
	public Model putAll(JsonModel model) {
		fields.clear();
		if(model != null) {
			putAll(model.getAll());
		}
		return this;
	}
	
	/**
	 * Put all of the given fields, and their given values, directly into this model's underlying
	 * data map.  The model will not be resolved if it isn't already.  If any of the fields
	 * already exist in the data map, they will be over-written.  Fields in the data map that
	 * do not exist in the given fields will remain unchanged.  If one of the fields in the data
	 * map is "id", then it will be used to set this model's id and <b>not</b> put into the data map.
	 * @param fields
	 */
	@Override
	public Model putAll(Map<String, Object> fields) {
		if(fields.containsKey("id")) {
			Object id = fields.remove("id");
			setId(coerce(id, int.class));
		}
		this.fields.putAll(fields);
		return this;
	}
	
	/**
	 * Put all of the given fields, and their given values, directly into this model's underlying
	 * data map.  The model will not be resolved if it isn't already.  If any of the fields
	 * already exist in the data map, they will be over-written.  Fields in the data map that
	 * do not exist in the given fields will remain unchanged.  If one of the fields in the data
	 * map is "id", then it will be used to set this model's id and <b>not</b> put into the data map.
	 * @param json a Map of fields in JSON
	 * @see #putAll(Map)
	 */
	@Override
	public Model putAll(String json) {
		return putAll(JsonUtils.toMap(json));
	}
	
	/**
	 * Remove the field from this model's internal data map.
	 * @param field the field to be removed
	 * @return the previous value of the field, or null if there was value for the field
	 */
	public Object remove(String field) {
		return fields.remove(field);
	}
	
	/**
     * Removes the error at the specified position in this list.
     * Shifts any subsequent elements to the left (subtracts one from their indices).
     *
     * @param index the index of the error to be removed
     * @return the error that was removed from the list
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
	public final String removeError(int index) {
		return removeError(null, index);
	}
	
	public final boolean removeError(String error) {
		return removeError(null, error);
	}

	/**
     * Removes the error at the specified position in this list.
     * Shifts any subsequent elements to the left (subtracts one from their indices).
     *
     * @param index the index of the error to be removed
     * @return the error that was removed from the list
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
	public final String removeError(String subject, int index) {
		if(errors != null) {
			List<String> list = errors.get(subject);
			if(list != null) {
				String s = list.remove(index);
				if(list.isEmpty()) {
					errors.remove(subject);
					if(errors.isEmpty()) {
						errors = null;
					}
				}
				return s;
			}
		}
	    throw new IndexOutOfBoundsException("Index: " + index + ", Size: 0");
	}
	
	/**
	 * Removes the first occurrence of the specified error from this list,
	 * if it is present.  If the list does not contain the error, it is
	 * unchanged.  More formally, removes the error with the lowest index
	 * <tt>i</tt> such that
	 * <tt>(o==null&nbsp;?&nbsp;get(i)==null&nbsp;:&nbsp;o.equals(get(i)))</tt>
	 * (if such an error exists).  Returns <tt>true</tt> if this list
	 * contained the specified error (or equivalently, if this list
	 * changed as a result of the call).
	 *
	 * @param error the error to be removed from this list, if present
	 * @return <tt>true</tt> if this list contained the specified error; false otherwise
	 */
	public final boolean removeError(String subject, String message) {
		if(errors != null) {
			List<String> list = errors.get(subject);
			if(list != null) {
				list.remove(message);
				if(list.isEmpty()) {
					errors.remove(subject);
					if(errors.isEmpty()) {
						errors = null;
					}
				}
				return true;
			}
		}
		return false;
	}

	public final List<String> removeErrors(String subject) {
		if(errors != null) {
			List<String> list = errors.remove(subject);
			if(errors.isEmpty()) {
				errors = null;
			}
			return list;
		}
		return new ArrayList<String>(0);
	}
	
	public boolean save() {
		if(canSave()) {
			Observer.runBeforeSave(this);
			if(isNew()) {
				return doCreate();
			} else {
				return doUpdate();
			}
		}
		return false;
	}
	
	/**
	 * Set the given field to the coersed type of the given value.
	 * <p>The model will be resolved if:
	 * <ul>
	 *   <li>it isn't already resolved</li>
	 *   <li>the given value is an instance of {@link Model}</li>
	 *   <li>the opposite of the given field is either an {@link ActiveSet} or {@link RequiredSet}
	 * </ul>
	 * </p>
	 * If the field already exists in the data map, it will be over-written.
	 * @param field
	 * @param value
	 * @return this, for method chaining
	 * @see TypeCoercer#coerce(Object, Class)
	 */
	@Override
	public Model set(String field, Object value) {
		if("id".equals(field)) {
			setId(coerce(value, int.class));
		} else {
			Class<?> type = getAdapter(getClass()).getClass(field);
			set(field, value, type);
		}
		return this;
	}
	
	private Object set(String field, Object value, Class<?> type) {
		if(type == null) {
			// not a persisted field that we know about
			fields.put(field, value);
			return value;
		} else if(Collection.class.isAssignableFrom(type)) {
			Class<? extends Model> mtype = getAdapter(this).getHasManyMemberClass(field);
			Model[] models = (Model[]) coerce(value, Array.newInstance(mtype, 0).getClass());
			if(type == ActiveSet.class || type == RequiredSet.class) {
				Set<Model> set;
				if(type == ActiveSet.class) {
					set = new ActiveSet<Model>(this, field, getOpposite(field), isManyToMany(field), models);
				} else {
					set = new RequiredSet<Model>(this, field, getOpposite(field), models);
				}
				fields.put(field, set);
				return set;
			} else {
				return coerce(models, type);
			}
		} else {
			Object coersedValue = coerce(value, type);
			if(Model.class.isAssignableFrom(type)) {
				String opposite = getOpposite(field);
				if(getAdapter(type.asSubclass(Model.class)).hasMany(opposite)) {
					Model oldModel = (Model) (fields.containsKey(field) ? fields.get(field) : null);
					if(oldModel != null) {
						Object o = oldModel.get(opposite);
						if(o instanceof ActiveSet<?>) {
							((ActiveSet<?>) oldModel.get(opposite)).doRemove(this);
						} else if(o instanceof RequiredSet<?>) {
							((RequiredSet<?>) oldModel.get(opposite)).doRemove(this);
						}
					}
					Model newModel = (Model) coersedValue;
					if(newModel != null) {
						Object o = newModel.get(opposite);
						if(o instanceof ActiveSet<?>) {
							((ActiveSet<?>) o).doAdd(this);
						} else if(o instanceof RequiredSet<?>) {
							((RequiredSet<?>) o).doAdd(this);
						}
					}
				}
			}
			fields.put(field, coersedValue);
			return coersedValue;
		}
	}

	/**
	 * Sets this model's fields to those of the given map.
	 * The existing fields will first be cleared and then
	 * each entry in the Map will be used to set a corresponding
	 * field in this object through one of two methods:
	 * 1. the setter method will be called, if it exists; otherwise
	 * 2. it will be passed to the {@link #set(String, Object)} method.
	 * @param fields
	 * @see #set(String, Object)
	 * @return this, for method chaining
	 */
	@Override
	public Model setAll(Map<String, Object> fields) {
		this.fields.clear();
		if(fields != null) {
			for(Entry<String, Object> entry : fields.entrySet()) {
				String key = entry.getKey();
				Object val = entry.getValue();
				setField(key, val);
			}
		}
		return this;
	}

	/**
	 * Sets this model's fields to those of the given map.
	 * The existing fields will first be cleared and then the
	 * given map will be passed into the {@link #setAll(Map)}
	 * method of this model.
	 * @param json a String that contains a map object in JSON format
	 * @see #setAll(Map)
	 * @return this, for method chaining
	 */
	@Override
	public Model setAll(String json) {
		fields.clear();
		if(json != null) {
			setAll(JsonUtils.toMap(json));
		}
		return this;
	}
	
	private void setField(String field, Object value) {
		if("id".equals(field)) {
			setId(coerce(value, int.class));
		} else {
			Class<?> type = getAdapter(getClass()).getClass(field);
			if(type == null || Collection.class.isAssignableFrom(type)) {
				set(field, value);
			} else {
				try {
					Method setter = getClass().getMethod(StringUtils.setterName(field), type);
					setter.invoke(this, coerce(value, type));
				} catch(NoSuchMethodException e) {
					set(field, value);
				} catch(SecurityException e) {
					logger.warn(e);
				} catch(IllegalArgumentException e) {
					logger.warn(e);
				} catch(IllegalAccessException e) {
					logger.warn(e);
				} catch(InvocationTargetException e) {
					logger.warn(e);
				}
			}
		}
	}
	
	@Override
	public Model setId(int id) {
		this.id = id;
		return this;
	}
	
	public void setPersistor(PersistService service) {
		persistor = service;
	}
	
	@Override
	public String toJson() {
		Map<String, Object> map = new TreeMap<String, Object>();
		map.put("id", getId());
//		TODO toJson: include canonical class name?
		ModelAdapter adapter = ModelAdapter.getAdapter(getClass());
		for(String field : adapter.getFields()) {
			if(isSet(field)) {
				if(adapter.hasAttribute(field)) {
					map.put(field, get(field));
				} else if(adapter.hasOne(field)) {
					Model model = (Model) get(field);
					ModelAdapter fadapter = ModelAdapter.getAdapter(model.getClass());
					Map<String, Object> fmap = new TreeMap<String, Object>();
					fmap.put("id", model.getId());
					for(String ffield : fadapter.getFields()) {
						if(model.isSet(ffield)) {
							if(fadapter.hasAttribute(ffield)) {
								fmap.put(ffield, model.get(ffield));
							} else if(fadapter.hasOne(ffield)) {
								fmap.put(ffield, ((Model) model.get(ffield)).getId());
							} else if(fadapter.hasMany(ffield)) {
								Collection<?> collection = (Collection<?>) model.get(ffield);
								List<Object> list = new ArrayList<Object>();
								for(Object o : collection) {
									list.add(Collections.singletonMap("id", ((Model) o).getId()));
								}
								fmap.put(ffield, list);
							}
						}
					}
					map.put(field, fmap);
				} else if(adapter.hasMany(field)) {
					Collection<?> collection = (Collection<?>) get(field);
					List<Object> list = new ArrayList<Object>();
					for(Object o : collection) {
						Model model = (Model) o;
						ModelAdapter fadapter = ModelAdapter.getAdapter(model.getClass());
						Map<String, Object> fmap = new TreeMap<String, Object>();
						fmap.put("id", model.getId());
						for(String ffield : fadapter.getFields()) {
							if(model.isSet(ffield)) {
								if(fadapter.hasAttribute(ffield)) {
									fmap.put(ffield, model.get(ffield));
								} else if(fadapter.hasOne(ffield)) {
									fmap.put(ffield, ((Model) model.get(ffield)).getId());
								} else if(fadapter.hasMany(ffield)) {
									Collection<?> fcollection = (Collection<?>) get(ffield);
									List<Object> flist = new ArrayList<Object>();
									for(Object fo : fcollection) {
										flist.add(Collections.singletonMap("id", ((Model) fo).getId()));
									}
									fmap.put(ffield, list);
								}
							}
						}
						list.add(fmap);
					}
					map.put(field, list);
				}
			}
		}
		String json = JsonUtils.toJson(map);
		if(logger.isLoggingTrace()) {
			logger.trace(this + ".toJson() -> \n  " + json);
		}
		return json;
	}

	@Override
	public String toString() {
		return asSimpleString();
	}
	
	public boolean update() {
		if(isNew()) {
			addError("cannot update a model that has not been created");
			return false;
		}
		if(canUpdate()) {
			return doUpdate();
		}
		return false;
	}
	
	protected void validateCreate() {
		// subclasses to override
	}
	
	protected void validateDestroy() {
		// subclasses to override
	}

	protected void validateSave() {
		if(isNew()) {
			validateCreate();
		} else {
			validateUpdate();
		}
	}
	
	protected void validateUpdate() {
		// subclasses to override
	}
	
}
