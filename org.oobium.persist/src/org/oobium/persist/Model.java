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
import static org.oobium.utils.json.JsonUtils.toList;
import static org.oobium.utils.literal.Map;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.oobium.logging.LogProvider;
import org.oobium.logging.Logger;
import org.oobium.utils.StringUtils;
import org.oobium.utils.coercion.TypeCoercer;
import org.oobium.utils.json.JsonModel;
import org.oobium.utils.json.JsonUtils;


public abstract class Model implements JsonModel {

	public static class ModelUtils {

		@SuppressWarnings("unchecked")
		public static <T extends Model> List<T> collectHasManyThrough(List<?> models, String field, Class<T> type) {
			List<T> list = new ArrayList<T>();
			for(Object o : models) {
				if(o instanceof Model) {
					Object value = ((Model) o).get(field);
					if(value instanceof List) {
						list.addAll((List<T>) value);
					}
					else if(value != null) {
						list.add((T) value);
					}
				}
			}
			return list;
		}
		
		@SuppressWarnings("unchecked")
		public static <T extends Model> List<T> collectHasManyThrough(Model model, String field, Class<T> type) {
			if(model == null) {
				return new ArrayList<T>(0);
			}
			List<T> list = new ArrayList<T>();
			Object value = model.get(field);
			if(value instanceof List) {
				list.addAll((List<T>) value);
			}
			else if(value != null) {
				list.add((T) value);
			}
			return list;
		}

	}
	
	private static final ThreadLocal<Logger> logService = new ThreadLocal<Logger>();
	private static final ThreadLocal<PersistServiceProvider> persistServiceProvider = new ThreadLocal<PersistServiceProvider>();
	
	private static PersistServiceProvider globalPersistServiceProvider;

	public static void activateObservers() {
		Observer.active = true;
	}
	
	public static void addObserver(Class<? extends Observer<?>> observerClass) {
		Observer.addObserver(observerClass);
	}
	
	public static void addObserver(Observer<?> observer) {
		Observer.addObserver(observer);
	}
	
	public static void deactivateObservers() {
		Observer.active = false;
	}
	
	public static Logger getLogger() {
		Logger service = logService.get();
		if(service == null) {
			service = LogProvider.getLogger(PersistService.class);
			logService.set(service);
		}
		return service;
	}
	
	public static PersistService getPersistService(Class<? extends Model> clazz) {
		return getPersistServiceProvider().getFor(clazz);
	}
	
	public static PersistServiceProvider getPersistServiceProvider() {
		PersistServiceProvider provider = persistServiceProvider.get();
		if(provider == null) {
			provider = globalPersistServiceProvider;
			if(provider == null) {
				provider = globalPersistServiceProvider = new SimplePersistServiceProvider();
			}
		}
		return provider;
	}
	
	private static boolean isRequired(Class<?> clazz, String field) {
		if(clazz != null && field != null) {
			Validations validations = clazz.getAnnotation(Validations.class);
			if(validations != null) {
				for(Validate validate : validations.value()) {
					String[] fields = validate.field().split("\\s*,\\s*");
					for(String f : fields) {
						if(f.equals(field)) {
							return validate.isNotBlank() || validate.isNotNull()
									|| !"".equals(validate.min()) || !"".equals(validate.max())
									|| !"".equals(validate.isIn());
						}
					}
				}
			}
		}
		return false;
	}
	
	protected static boolean notEquals(Object o1, Object o2) {
		return (o1 != null && !o1.equals(o2)) || o2 != null; 
	}

	public static void removeObserver(Observer<?> observer) {
		Observer.removeObserver(observer);
	}

	public static void removeObservers(Class<?> clazz) {
		Observer.removeObservers(clazz);
	}

	public static PersistServiceProvider setGlobalPersistService(PersistService service) {
		PersistServiceProvider services = new SimplePersistServiceProvider(service);
		setGlobalPersistServiceProvider(services);
		return services;
	}

	public static void setGlobalPersistServiceProvider(PersistServiceProvider services) {
		globalPersistServiceProvider = services;
	}

	public static void setLogger(Logger service) {
		logService.set(service);
	}

	public static void setObserversActive(boolean active) {
		Observer.active = active;
	}
	
	public static PersistServiceProvider setPersistService(PersistService service) {
		PersistServiceProvider services = new SimplePersistServiceProvider(service);
		setPersistServiceProvider(services);
		return services;
	}
	
	public static void setPersistServiceProvider(PersistServiceProvider services) {
		persistServiceProvider.set(services);
	}

	public static String toJson(Collection<? extends Model> models) {
		String json = ModelJsonBuilder.buildJson(models);
		Logger logger = logService.get();
		if(logger == null) {
			logger = LogProvider.getLogger(Model.class);
		}
		if(logger.isLoggingTrace()) {
			logger.trace("Model#toJson(" + StringUtils.asString(models) + ") -> \n  " + json);
		}
		return json;
	}
	
	public static String toJson(Collection<? extends Model> models, String include, Object...values) {
		String json = ModelJsonBuilder.buildJson(models, include, values);
		Logger logger = logService.get();
		if(logger == null) {
			logger = LogProvider.getLogger(Model.class);
		}
		if(logger.isLoggingTrace()) {
			logger.trace("Model#toJson(" + StringUtils.asString(models) + ", " + include + ((values.length == 0) ? "" : (", " + StringUtils.asString(values))) + ") -> \n  " + json);
		}
		return json;
	}

	
	protected final Logger logger;
	private final Map<String, Object> fields;
	private Object id;
	private PersistService persistor;
	private Map<String, ArrayList<String>> errors;

	/**
	 * The value of this model's id before it was destroyed.
	 * For use only by {@link Observer#afterDestroy(int)}.
	 */
	Object destroyed;

	public Model() {
		logger = getLogger();
		fields = new HashMap<String, Object>();
	}

	/**
	 * Add a single error message to the base (null) subject.
	 * Usefull for adding errors not related to a particular field.
	 * @param message the error message to be added
	 */
	public final void addError(String message) {
		addError(null, message);
	}
	
	/**
	 * Add a single error message to the given subject.
	 * Used by the internal validation mechanism and available for use
	 * in custom code.
	 * @param subject the subject that the error is related to (a field, for example)
	 * @param message the error message
	 */
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
	
	protected void afterCreate() {
		// sublcasses to implement
	}
	
	protected void afterDestroy(Object id) {
		// sublcasses to implement
	}
	
	protected void afterUpdate() {
		// sublcasses to implement
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
	
	protected void beforeCreate() {
		// sublcasses to implement
	}
	
	protected void beforeDestroy() {
		// sublcasses to implement
	}
	
	protected void beforeUpdate() {
		// sublcasses to implement
	}
	
	public final boolean canCreate() {
		Set<Model> validated = new HashSet<Model>();
		return canCreate(validated);
	}
	
	private boolean canCreate(Set<Model> validated) {
		validated.add(this);
		clearErrors();
		Observer.runBeforeValidateCreate(this);
		runValidations(Validate.CREATE, validated);
		validateCreate();
		Observer.runAfterValidateCreate(this);
		return !hasErrors();
	}

	public final boolean canDestroy() {
		clearErrors();
		if(!getAdapter(getClass()).isDeletable()) {
			addError("Destroy is not permitted.");
		}
		if(isNew()) {
			addError("cannot destroy a model that has not been saved");
		}
		Observer.runBeforeValidateDestroy(this);
		runValidations(Validate.DESTROY, null);
		validateDestroy();
		Observer.runAfterValidateDestroy(this);
		return !hasErrors();
	}
	
	public final boolean canSave() {
		Set<Model> validated = new HashSet<Model>();
		return canSave(validated);
	}
	
	private boolean canSave(Set<Model> validated) {
		validated.add(this);
		clearErrors();
		Observer.runBeforeValidateSave(this);
		validateSave();
		if(!hasErrors()) {
			if(isNew()) {
				Observer.runBeforeValidateCreate(this);
				runValidations(Validate.CREATE, validated);
				validateCreate();
				Observer.runAfterValidateCreate(this);
			} else {
				Observer.runBeforeValidateUpdate(this);
				runValidations(Validate.UPDATE, validated);
				validateUpdate();
				Observer.runAfterValidateUpdate(this);
			}
		}
		Observer.runAfterValidateSave(this);
		return !hasErrors();
	}
	
	public final boolean canUpdate() {
		Set<Model> validated = new HashSet<Model>();
		return canUpdate(validated);
	}
	
	private boolean canUpdate(Set<Model> validated) {
		validated.add(this);
		clearErrors();
		if(!getAdapter(getClass()).isUpdatable()) {
			addError("Updates are not permitted.");
			return false;
		}
		if(isNew()) {
			addError("cannot update a model that has not been created");
			return false;
		}
		if(isEmpty()) {
			addError("nothing to update");
			return false;
		}
		Observer.runBeforeValidateUpdate(this);
		runValidations(Validate.UPDATE, validated);
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

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private int compare(Object value, String validator, String field) {
		if(value == null) {
			return -1;
		}
		ModelAdapter adapter = ModelAdapter.getAdapter(this);
		Object v2 = coerce(validator, adapter.getClass(field));
		if(value instanceof Comparable) {
			return ((Comparable) value).compareTo(v2);
		}
		throw new IllegalArgumentException("value is not comparable");
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

	public void CREATE() {
		boolean result = create();
		if(result == false) {
			String message = "Errors creating the model: " + this + " \n" + StringUtils.join(getErrorsList(), '\n');
			throw new IllegalStateException(message);
		}
	}
	
	public boolean destroy() {
		if(canDestroy()) {
			return doDestroy();
		}
		return false;
	}

	public void DESTROY() {
		boolean result = destroy();
		if(result == false) {
			String message = "Errors destroying the model: " + this + " \n" + StringUtils.join(getErrorsList(), '\n');
			throw new IllegalStateException(message);
		}
	}
	
	private void destroy(String field) {
		Object o = get(field);
		fields.remove(field);
		if(o instanceof Model) {
			Model related = (Model) o;
			if(related != null && !related.isNew()) {
				related.destroy();
			}
		} else if(o instanceof Collection) {
			Collection<?> collection = (Collection<?>) o;
			for(Object model : collection) {
				Model related = (Model) model;
				if(related != null && !related.isNew()) {
					related.destroy();
				}
			}
		}
	}
	
	private List<String> destroyDependents() {
		ModelAdapter adapter = ModelAdapter.getAdapter(this);

		// destroy fields that must be destroyed before this model
		for(String field : adapter.getHasOneFields()) {
			Relation relation = adapter.getRelation(field);
			if(relation.dependent() == Relation.DESTROY) {
				if(adapter.isOneToOne(field) && !adapter.hasKey(field)) {
					destroy(field);
				}
			}
		}
		for(String field : adapter.getHasManyFields()) {
			if(adapter.isManyToOne(field)) {
				Relation relation = adapter.getRelation(field);
				if(relation.dependent() == Relation.DESTROY) {
					destroy(field);
				}
			}
		}
		
		// collect a list of fields that must be destroyed after this model
		List<String> fields = new ArrayList<String>();
		for(String field : adapter.getHasOneFields()) {
			Relation relation = adapter.getRelation(field);
			if(relation.dependent() == Relation.DESTROY) {
				if(!adapter.isOneToOne(field) || adapter.hasKey(field)) {
					fields.add(field);
				}
			}
		}
		for(String field : adapter.getHasManyFields()) {
			if(!adapter.isManyToOne(field)) {
				Relation relation = adapter.getRelation(field);
				if(relation.dependent() == Relation.DESTROY) {
					fields.add(field);
				}
			}
		}
		
		// load fields that are not currently loaded
		if(!fields.isEmpty()) {
			if(this.fields.isEmpty()) {
				load();
			} else {
				for(String field : fields) {
					if(!isSet(field)) {
						// at least one is not set - load, but replace all that are currently set
						Map<String, Object> orig = new HashMap<String, Object>(this.fields);
						load();
						this.fields.putAll(orig);
						// only need to do this once, so exit
						break;
					}
				}
			}
		}
		
		return fields;
	}

	private void destroyDependents(List<String> fields) {
		for(String field : fields) {
			destroy(field);
		}
	}

	private boolean doCreate() {
		boolean saved = false;
		Observer.runBeforeCreate(this);
	
		if(!hasErrors()) {
			try {
				beforeCreate();
				PersistService service = getPersistor();
				service.create(this);
				saved = true;
				afterCreate();
				if(!(service instanceof RemotePersistService)) {
					Observer.runAfterCreate(this);
				}
			} catch(Exception e) {
				logger.warn("failed to save " + asSimpleString(), e);
				addError(e.getLocalizedMessage());
			}
		}
		return saved;
	}
	
	private boolean doDestroy() {
		Observer.runBeforeDestroy(this);
		if(!hasErrors()) {
			try {
				beforeDestroy();
				PersistService service = getPersistor();
				List<String> dependents = destroyDependents();
				service.destroy(this);
				destroyDependents(dependents);
				destroyed = id;
				id = null;
				fields.clear();
				afterDestroy(destroyed);
				if(!(service instanceof RemotePersistService)) {
					Observer.runAfterDestroy(this);
				}
			} catch(Exception e) {
				logger.warn("failed to destroy " + asSimpleString(), e);
				addError(e.getLocalizedMessage());
			}
		}
		return (destroyed != null);
	}

	private boolean doUpdate() {
		boolean saved = false;
		Observer.runBeforeUpdate(this);
		
		if(!hasErrors()) {
			try {
				beforeUpdate();
				PersistService service = getPersistor();
				service.update(this);
				saved = true;
				afterUpdate();
				if(!(service instanceof RemotePersistService)) {
					Observer.runAfterUpdate(this);
				}
			} catch(Exception e) {
				logger.warn("failed to save " + asSimpleString(), e);
				addError(e.getLocalizedMessage());
			}
		}
		return saved;
	}
	
	@Override
	public boolean equals(Object obj) {
		// TODO what about comparing to JSON strings and Maps?
		if(obj == null) {
			return false;
		}
		if(obj == this) {
			return true;
		}
		if(isNew()) {
			return false;
		}
		if(obj.getClass() == getClass()) {
			Object oid = ((Model) obj).getId();
			if(oid != null) {
				if(id.getClass() != oid.getClass()) {
					try {
						oid = coerce(oid, id.getClass());
					} catch(Exception e) { /* discard */ }
				}
				return id.equals(oid);
			}
		}
		return false;
	}
	
	public boolean exists() {
		PersistService service = getPersistor();
		if(service != null) {
			try {
				return service.find(getClass(), fields) != null;
			} catch(Exception e) {
				logger.debug(e);
				// fall through
			}
		}
		return false;
	}

	/**
	 * Get a data field from the model object.
	 * <p>If the field is a model field (it is specified in the @{@link ModelDescription} class annotation),
	 * then this method will return the data in the Type specified, performing any coercion as necessary.
	 * <p>If the model is not new then the field will be resolved if it is not already resolved.
	 * @param field the name of the field to be returned
	 * @return the value of the field
	 * @see #get(String, boolean)
	 * @see #peek(String)
	 */
	public Object get(String field) {
		return get(field, true);
	}

	/**
	 * Get a data field from the model object.
	 * <p>If the field is a model field (it is specified in the @{@link ModelDescription} class annotation),
	 * then this method will return the data in the Type specified, performing any coercion as necessary.
	 * <p>If load is true, and the model is not new, then the field will be resolved if it 
	 * is not already resolved.</p>
	 * @param field the name of the field to be returned
	 * @param load true to make sure the field is resolved, false otherwise
	 * @return the value of the field
	 * @throws IllegalStateException if the field must be resolved and there is no persist service set
	 * @throws UnsupportedOperationException if the must be resolved from a different persist services and is not a many-to-one relationship
	 */
	public Object get(String field, boolean load) {
		if(field == null) {
			return null;
		}
		
		if("id".equals(field)) {
			return getId();
		}

		ModelAdapter adapter = getAdapter(this);

		if(fields.containsKey(field)) {
			Class<?> type = adapter.getClass(field);
			if(type == null) {
				return fields.get(field);
			} else {
				Object value = fields.get(field);
				if(value == null || type.isAssignableFrom(value.getClass())) { // TODO also check if correct type for primitive
					return value;
				}
				return set(field, value, type);
			}
		}

		if(adapter.isThrough(field)) {
			String[] sa = adapter.getThrough(field);
			Object through = get(sa[0]);
			if(through instanceof Model) {
				return ((Model) through).get(sa[1]);
			}
			else if(through instanceof List) {
				return ModelUtils.collectHasManyThrough((List<?>) through, sa[1], adapter.getRelationClass(field));
			} else {
				return adapter.hasMany(field) ? new ArrayList<Object>(0) : null;
			}
		}

		if(load && !isNew()) {
			if(hasContained(field)) {
				load();
			} else if(adapter.hasMany(field) || (adapter.isOneToOne(field) && !adapter.hasKey(field))) {
				try {
					Class<? extends Model> type = adapter.getRelationClass(field);
					PersistService p = getPersistor();
					PersistService fp = getPersistService(type);
					if(p == fp) {
						if(p == null) {
							throw new IllegalStateException("no persist service set");
						}
						p.retrieve(this, field);
					}
					else if(adapter.isManyToOne(field)) {
						if(fp == null) {
							throw new IllegalStateException("no persist service set for " + type);
						}
						Map<String, Object> query = Map(adapter.getOpposite(field), getId());
						Object value = fp.findAll(type, query);
						fields.put(field, value);
					}
					else {
						throw new UnsupportedOperationException("only many to one is currently supported for mixed persist services");
					}
				} catch(Exception e) {
					logger.warn("failed to load relation " + field + " in " + asSimpleString(), e);
				}
			}
			return get(field, false); // exit through the if(fields.containsKey(field)) block above
		}

		if(adapter.hasMany(field)) { // prevent returning a null for a hasMany field
			fields.put(field, new ModelList<Model>(this, field));
		}
		
		return fields.get(field);
	}
	
	public <T> T get(String field, Class<T> type) {
		return coerce(get(field), type);
	}
	
	public <T> T get(String field, Class<T> type, boolean load) {
		return coerce(get(field, load), type);
	}

	public <T> T get(String field, T defaultValue) {
		return coerce(get(field), defaultValue);
	}

	public <T> T get(String field, T defaultValue, boolean load) {
		return coerce(get(field, load), defaultValue);
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
	
	public String getError(int index) {
		return getError(null, index);
	}
	
	public String getError(String subject) {
		return getError(subject, 0);
	}
	
	public String getError(String subject, int index) {
		if(errors != null && index >= 0) {
			List<String> list = errors.get(subject);
			if(list != null && index < list.size()) {
				return list.get(index);
			}
		}
		return null;
	}
	
	/**
	 * Get the number of errors.
	 * @return the number of errors; never less than zero.
	 */
	public int getErrorCount() {
		if(errors == null) {
			return 0;
		}
		int count = 0;
		for(ArrayList<String> list : errors.values()) {
			count += list.size();
		}
		return count;
	}

	/**
	 * Get all of the errors contained within this model.
	 * @return a LinkedHashMap of Lists of error messages key by their subject in the order that they were added; never null.
	 */
	public LinkedHashMap<String, List<String>> getErrors() {
		if(errors != null) {
			return new LinkedHashMap<String, List<String>>(errors);
		}
		return new LinkedHashMap<String, List<String>>(0);
	}

	/**
	 * Get the list of errors corresponding to the given subject.
	 * @param subject the subject to get the errors list for
	 * @return the errors {@link List} for the subject, if any; otherwise an empty List; never null.
	 */
	public List<String> getErrors(String subject) {
		if(errors != null) {
			List<String> list = errors.get(subject);
			if(list != null) {
				return new ArrayList<String>(list);
			}
		}
		return new ArrayList<String>(0);
	}
	
	/**
	 * A complete and flattened list of all errors in this model. The subject of each
	 * error has been prepended to the original message.
	 * @return a List of all errors, or an empty list if there are none; never null.
	 */
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
	
	public final Object getId() {
		return getId(false);
	}
	
	public final Object getId(boolean saveFirst) {
		Class<?> type = null;
		PersistService persistor = getPersistor();
		if(persistor != null) {
			ServiceInfo info = persistor.getInfo();
			if(info != null) {
				type = info.getIdType();
			}
		}
		return getId(saveFirst, (type == null) ? Object.class : type);
	}
	
	public final <T> T getId(boolean saveFirst, Class<T> clazz) {
		if(saveFirst && isNew()) {
			save();
		}
		return coerce(id, clazz);
	}
	
	public final <T> T getId(Class<T> clazz) {
		return coerce(getId(false), clazz);
	}

	private int getLength(Object value, String tokenizer) {
		if(value == null) {
			return 0;
		} else if(value instanceof Collection) {
			return ((Collection<?>) value).size();
		} else if(value instanceof Map) {
			return ((Map<?,?>) value).size();
		} else if(value.getClass().isArray()) {
			return Array.getLength(value);
		}
		
		String s = value.toString();
		return blank(tokenizer) ? s.length() : s.split(tokenizer).length;
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

	/**
	 * Find out if this model's persisted object contains the field.
	 * True if the field is an attribute or hasOne relationship.
	 */
	private boolean hasContained(String field) {
		ModelAdapter adapter = getAdapter(getClass());
		return adapter.hasAttribute(field) || (adapter.hasOne(field) && (!adapter.isOneToOne(field) || adapter.hasKey(field)));
	}

	/**
	 * Find out if this model has any errors associated with it.
	 * Only valid after validations have been run.
	 * @return true if there are errors; false otherwise.
	 */
	public boolean hasErrors() {
		return errors != null && !errors.isEmpty();
	}

	/**
	 * Find out if this model has any errors associated with the given subject.
	 * Only valid after validations have been run.
	 * @param subject the subject to check for errors
	 * @return true if there are errors; false otherwise.
	 */
	public boolean hasErrors(String subject) {
		if(errors != null) {
			List<String> list = errors.get(subject);
			return list != null && !list.isEmpty();
		}
		return false;
	}
	
	/**
	 * Find out if there are any errors associated with the nested field.
	 * <p>For example - if a Post model has one Owner model and the Owner model has a name field, you
	 * could use this method on a post to find out if there is an error associated with the
	 * owner's name field: post.hasErrors("owner", "name");</p>
	 * If the given fields array is of zero length, then this method returns false. Similarly,
	 * if the nested field is not specified in the containing model's {@link ModelDescription}, then
	 * this method return false.
	 * @param fields an array of nested fields leading to the one to check for errors
	 * @return true if there are errors; false otherwise.
	 */
	public final boolean hasErrors(String...fields) {
		if(fields.length == 0) {
			return false;
		}
		if(fields.length == 1) {
			return hasErrors(fields[0]);
		}
		Model model = null;
		for(int i = 0; i < fields.length - 1; i++) {
			Object o = get(fields[i]);
			if(o instanceof Model) {
				model = (Model) o;
			} else {
				// only models have an errors list
				return false;
			}
		}
		return model.hasErrors(fields[fields.length-1]);
	}
	
	@Override
	public int hashCode() {
		if(isNew()) {
			return super.hashCode();
		}
		int hash = String.valueOf(id).hashCode() + 2;
		hash = (hash * 31) + getClass().getCanonicalName().hashCode();
		return hash;
	}
	
	@Override
	public final boolean isBlank() {
		return isNew() && blank(getAll());
	}
	
	@Override
	public final boolean isEmpty() {
		return fields.isEmpty();
	}

	@Override
	public final boolean isNew() {
		if(id == null) {
			return true;
		}
		if(id instanceof Number) {
			return ((Number) id).intValue() <= 0;
		}
		if("0".equals(id)) {
			return true;
		}
		if("".equals(id)) {
			return true;
		}
		return false;
	}

	/**
	 * Find out if given field is marked as required in this model's {@link ModelDescription}.
	 * If the given field is not specified in the {@link ModelDescription} then it cannot be
	 * marked as required and this method returns false.
	 * @param field
	 * @return true if the given field is required; false otherwise.
	 * @see ModelAdapter#isRequired(String)
	 */
	public final boolean isRequired(String field) {
		return isRequired(getClass(), field);
	}
	
	/**
	 * Find out if the nested model field is considered to be required (has a validation to prevent it from
	 * being null or blank).
	 * <p>For example - if a Post model has one Owner model and the Owner model has a name field, you
	 * could use this method on a post to find out if the owner's name field is required: 
	 * post.isRequired("owner", "name");</p>
	 * If the given fields array is of zero length, then this method returns false. Similarly,
	 * if the nested field is not specified in the containing model's {@link ModelDescription}, then
	 * this method return false.
	 * @param fields an array of fields leading from this model to the nested model
	 * @return true if the given nested field is required; false otherwise.
	 * @see ModelAdapter#isRequired(String)
	 */
	public final boolean isRequired(String...fields) {
		if(fields.length == 0) {
			return false;
		}
		if(fields.length == 1) {
			return isRequired(fields[0]);
		}
		ModelAdapter adapter = getAdapter(getClass());
		Class<?> clazz = null;
		for(int i = 0; i < fields.length - 1; i++) {
			clazz = adapter.getClass(fields[i]);
			if(Model.class.isAssignableFrom(clazz)) {
				adapter = getAdapter(clazz.asSubclass(Model.class));
			} else {
				// field is not a model (may be a Map...); only models can mark fields as required
				return false;
			}
		}
		return isRequired(clazz, fields[fields.length-1]);
	}
	
	@Override
	public final boolean isSet(String field) {
		return fields.containsKey(field);
	}
	
	public final boolean load() {
		try {
			getPersistor().retrieve(this);
			return true;
		} catch(Exception e) {
			logger.warn("failed to load {}", e, asSimpleString());
		}
		return false;
	}
	
	public final boolean load(String fields) {
		if(fields == null) {
			return load();
		}
		try {
			getPersistor().retrieve(this, fields);
			return true;
		} catch(Exception e) {
			logger.warn("failed to load {}({})", e, asSimpleString(), fields);
		}
		return false;
	}
	
    private String msg(String message, Validate validation) {
		if(blank(validation.message())) {
			return message;
		}
		return validation.message();
	}
	
	/**
	 * Get a data field directly from the model object's fields map.
	 * Unlike the {@link #get(String)} method, {@link #peek(String)}
	 * does <b>NOT</b> coerce the returned value of model fields, and it
	 * does <b>NOT</b> resolve the model regardless of its current state.
	 * @param field the name of the field to be returned
	 * @return the raw value of the field, as stored in the internal fields map; null if the value is null or
	 * the field is not set (model will not be resolved)
	 */
	public Object peek(String field) {
		return fields.get(field);
	}
	
	public <T> T peek(String field, Class<T> type) {
		return coerce(fields.get(field), type);
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
			setId(id);
		}
		fields.put(field, value);
		return this;
	}
	
	/**
	 * Puts the fields of the given model into this model.
	 * @param model
	 * @see #setAll(Map)
	 * @return this, for method chaining
	 */
	@Override
	public Model putAll(JsonModel model) {
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
			setId(id);
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
		Map<String, String> fields = JsonUtils.toStringMap(json);
		if(fields.containsKey("id")) {
			Object id = fields.remove("id");
			setId(id);
		}
		this.fields.putAll(fields);
		return this;
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
     * Removes the error at the specified position in list for the base (null) subject.
     * Shifts any subsequent elements to the left (subtracts one from their indices).
     * @param index the index of the error to be removed
     * @return the error that was removed from the list
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
	public final String removeError(int index) {
		return removeError(null, index);
	}
	
	/**
	 * Removes the first occurrence of the specified error from the list for the base (null) subject,
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
	public final boolean removeError(String error) {
		return removeError(null, error);
	}
	
	/**
     * Removes the error at the specified position in the list for the given subject.
     * Shifts any subsequent elements to the left (subtracts one from their indices).
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
	 * Removes the first occurrence of the specified error from the list for the given subject,
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

	/**
	 * Remove all errors in the list for the given subject.
	 * To remove errors from the base subject, simply pass a null in for the subject.
	 * @param subject the subject whose errors are to be removed
	 * @return the List of errors that was removed, or an empty list if
	 * there weren't any; never null;
	 */
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

	public final boolean resolve() {
		// to be like load(), except it only loads unresolved fields
		throw new UnsupportedOperationException("not yet implemented");
	}
	
	public final boolean resolve(String include) {
		// to be like load(include), except it only loads unresolved fields
		throw new UnsupportedOperationException("not yet implemented");
	}
	
	private boolean run(String methodName, String field) {
		boolean pass = true;
		try {
			if(blank(field)) {
				Method method = getClass().getDeclaredMethod(methodName);
				pass = (Boolean) method.invoke(this);
			} else {
				ModelAdapter adapter = ModelAdapter.getAdapter(this);
				String[] fields = field.split("\\s*,\\s*");
				for(int i = 0; pass && i < fields.length; i++) {
					try {
						Method method = getClass().getDeclaredMethod(methodName, adapter.getClass(fields[i]));
						method.setAccessible(true);
						pass = (Boolean) method.invoke(this, get(fields[i]));
					} catch(NoSuchMethodException e) {
						Method method = getClass().getDeclaredMethod(methodName);
						method.setAccessible(true);
						pass = (Boolean) method.invoke(this);
					}
				}
			}
		} catch(ClassCastException e) {
			logger.warn("validation method does not return a boolean: " + methodName);
		} catch(NoSuchMethodException e) {
			logger.warn("validation method does not exist: " + methodName);
		} catch(Exception e) {
			// discard
		}
		return pass;
	}
	
	private void runValidation(Validate validate, boolean onUpdate) {
		if(!blank(validate.when()) && !run(validate.when(), validate.field())) {
			return;
		}
		if(!blank(validate.unless()) && run(validate.unless(), validate.field())) {
			return;
		}
		if(validate.unlessNull() && get(validate.field()) == null) {
			return;
		}
		if(validate.unlessBlank() && blank(get(validate.field()))) {
			return;
		}
		
		if(validate.with() != Object.class) {
			runValidator(validate.with());
			return;
		}
		
		if(!blank(validate.withMethod())) {
			String[] fields = validate.field().split("\\s*,\\s*");
			for(String field : fields) {
				if(!onUpdate || isSet(field)) {
					if(!run(validate.withMethod(), field)) {
						addError(validate.field(), validate.message());
					}
				}
			}
			return;
		}
		
		String[] fields = validate.field().split("\\s*,\\s*");
		for(String field : fields) {
			if(onUpdate && !isSet(field)) {
				continue;
			}
			
			Object value = get(field);
			if(validate.isBlank() && !blank(value)) {
				addError(field, msg("must be blank", validate));
				continue;
			}
			if(validate.isNotBlank() && blank(value)) {
				addError(field, msg("cannot be blank", validate));
				continue;
			}
			if(validate.isNotNull() && value == null) {
				addError(field, msg("cannot be null", validate));
				continue;
			}
			if(validate.isNull() && value != null) {
				addError(field, msg("must be null", validate));
				continue;
			}
			if(!blank(validate.isNotIn()) && toList(validate.isNotIn()).contains(value)) {
				addError(field, msg("cannot be one of \"" + validate.isNotIn() + "\"", validate));
				continue;
			}
			if(!blank(validate.isIn()) && !toList(validate.isIn()).contains(value)) {
				addError(field, msg("can only be one of \"" + validate.isIn() + "\"", validate));
				continue;
			}
			if(!blank(validate.matches()) && !((value == null) ? "" : value.toString()).matches(validate.matches())) {
				addError(field, msg("must be of format \"" + validate.matches() + "\"", validate));
				continue;
			}
			if(!blank(validate.max()) && compare(value, validate.max(), field) > 0) {
				addError(field, msg("cannot be more than " + validate.max(), validate));
				continue;
			}
			if(!blank(validate.min()) && compare(value, validate.min(), field) < 0) {
				addError(field, msg("cannot be less than " + validate.min(), validate));
				continue;
			}
			if(validate.maxLength() >= 0 && getLength(value, validate.tokenizer()) > validate.maxLength()) {
				addError(field, msg("length cannot be more than " + validate.maxLength(), validate));
				continue;
			}
			if(validate.minLength() >= 0 && getLength(value, validate.tokenizer()) < validate.minLength()) {
				addError(field, msg("length cannot be less than " + validate.minLength(), validate));
				continue;
			}
			if(validate.lengthIs() >= 0 && getLength(value, validate.tokenizer()) != validate.lengthIs()) {
				addError(field, msg("length must be " + validate.lengthIs(), validate));
				continue;
			}
		}
	}
	
	private void runValidations(int on, Set<Model> validated) {
		Validations validations = getClass().getAnnotation(Validations.class);
		if(validations != null) {
			boolean onUpdate = (on == Validate.UPDATE);
			for(Validate validate : validations.value()) {
				if((validate.on() & on) != 0) {
					runValidation(validate, onUpdate);
				}
			}
			if(on == Validate.DESTROY) {
				validateDependentsOnDestroy();
			}
		}
		if(on != Validate.DESTROY) {
			validateRelatedModelsToBeCreated(validated);
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void runValidator(Class<?> validatorClass) {
		try {
			Validator validator = (Validator) validatorClass.newInstance();
			validator.validate(this);
		} catch(InstantiationException e) {
			logger.warn("could not run validator: " + validatorClass);
		} catch(IllegalAccessException e) {
			logger.warn("could not run validator: " + validatorClass);
		} catch(Exception e) {
			logger.warn(e);
		}
	}
	
	public boolean save() {
		boolean result = false;
		if(canSave()) {
			Observer.runBeforeSave(this);
			if(isNew()) {
				result = doCreate();
			} else {
				result = doUpdate();
			}
			Observer.runAfterSave(this);
		}
		return result;
	}

	public void SAVE() {
		boolean result = save();
		if(result == false) {
			String message = "Errors saving the model: " + this + " \n" + StringUtils.join(getErrorsList(), '\n');
			throw new IllegalStateException(message);
		}
	}

	/**
	 * Set the given field to the coerced type of the given value.
	 * <p>The model will be resolved if:
	 * <ul>
	 *   <li>it isn't already resolved</li>
	 *   <li>the given value is an instance of {@link Model}</li>
	 *   <li>the opposite of the given field is a {@link ModelList}
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
			setId(value);
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
			ModelAdapter adapter = getAdapter(this);
			Class<? extends Model> mtype = adapter.getHasManyMemberClass(field);
			Model[] models = (Model[]) coerce(value, Array.newInstance(mtype, 0).getClass());
			if(type == ModelList.class) {
				List<Model> set = new ModelList<Model>(this, field, models);
				if(adapter.isManyToOne(field)) {
					String opposite = adapter.getOpposite(field);
					for(Model model : set) {
						if(model.isNew()) {
							model.put(opposite, this);
						}
					}
				}
				fields.put(field, set);
				return set;
			} else {
				Object o = coerce(models, type);
				fields.put(field, o);
				return o;
			}
		} else {
			Object coercedValue = coerce(value, type);
			if(Model.class.isAssignableFrom(type)) {
				String opposite = getOpposite(field);
				ModelAdapter adapter = getAdapter(type.asSubclass(Model.class));
				if(adapter.hasOne(opposite)) {
					Model newModel = (Model) coercedValue;
					if(newModel != null) {
						newModel.put(opposite, this);
					}
					Model oldModel = (Model) coerce(fields.get(field), type);
					if(oldModel != null) {
						oldModel.put(opposite, null);
					}
				} else if(adapter.hasMany(opposite)) {
					Model newModel = (Model) coercedValue;
					Model oldModel = (Model) coerce(fields.get(field), type);
					if(oldModel != null && !oldModel.equals(newModel)) {
						Object o = oldModel.get(opposite);
						if(o instanceof ModelList<?>) {
							((ModelList<?>) oldModel.get(opposite)).doRemove(this);
						}
					}
					if(newModel != null && !newModel.equals(oldModel)) {
						Object o = newModel.get(opposite);
						if(o instanceof ModelList<?>) {
							((ModelList<?>) o).doAdd(this);
						}
					}
				}
			}
			fields.put(field, coercedValue);
			return coercedValue;
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
	
	protected void setErrors(List<String> errors) {
		clearErrors();
		if(errors != null) {
			for(String error : errors) {
				addError(error);
			}
		}
	}
	
	private void setField(String field, Object value) {
		if("id".equals(field)) {
			setId(value);
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
	public Model setId(Object id) {
		this.id = "null".equals(id) ? null : id;
		return this;
	}

	public void setPersistor(PersistService service) {
		persistor = service;
	}
	
	@Override
	public String toJson() {
		String json = ModelJsonBuilder.buildJson(this);
		if(logger.isLoggingTrace()) {
			logger.trace(this + ".toJson() -> \n  " + json);
		}
		return json;
	}

	public String toJson(String include, Object...values) {
		String json = ModelJsonBuilder.buildJson(this, include, values);
		logger.trace("{}.toJson() -> \n  {}", this, json);
		return json;
	}

	@Override
	public String toString() {
		return asSimpleString();
	}

	public boolean update() {
		if(canUpdate()) {
			return doUpdate();
		}
		return false;
	}

	public void UPDATE() {
		boolean result = update();
		if(result == false) {
			String message = "Errors updating the model: " + this + " " + this + " \n" + StringUtils.join(getErrorsList(), '\n');
			throw new IllegalStateException(message);
		}
	}
	
	protected void validateCreate() {
		// subclasses to override
	}

	/**
	 * When a model is updated or created, any models that are relations of it
	 * will need to be saved first. This method verifies that this is possible
	 * by running the canCreate validation on the related models. The given
	 * validated set is used to make sure a model is validated only once,
	 * thereby preventing infinite loops
	 */
	private void validateRelatedModelsToBeCreated(Set<Model> validated) {
		ModelAdapter adapter = ModelAdapter.getAdapter(this);
		for(String field : fields.keySet()) {
			if(adapter.hasOne(field)) {
				Model model = (Model) get(field, false);
				validateRelatedModelsToBeCreated(model, validated);
			}
			else if(adapter.hasMany(field)) {
				for(Object o : (Iterable<?>) get(field, false)) {
					Model model = (Model) o;
					validateRelatedModelsToBeCreated(model, validated);
				}
			}
		}
	}

	private void validateRelatedModelsToBeCreated(Model model, Set<Model> validated) {
		if(model == null || validated.contains(model)) {
			return;
		}
		if(model.isNew() && !model.canCreate(validated)) {
			if(errors == null) {
				errors = new LinkedHashMap<String, ArrayList<String>>();
			}
			errors.putAll(model.errors);
		}
	}
	
	private void validateDependentsOnDestroy() {
		ModelAdapter adapter = ModelAdapter.getAdapter(this);
		for(String field : adapter.getRelationFields()) {
			Relation relation = adapter.getRelation(field);
			if(relation.dependent() == Relation.DESTROY) {
				if(adapter.hasOne(field)) {
					Model related = (Model) get(field);
					if(related != null) {
						if(!related.canDestroy()) {
							addError(field, "is preventing this model from being destroyed");
						}
					}
				} else {
					Collection<?> related = (Collection<?>) get(field);
					for(Object o : related) {
						if(!((Model) o).canDestroy()) {
							addError(field, "are preventing this model from being destroyed");
							break;
						}
					}
				}
			}
		}
	}
	
	protected void validateDestroy() {
		// subclasses to override
	}

	protected void validateSave() {
		// subclasses to override
	}
	
	protected void validateUpdate() {
		// subclasses to override
	}
	
}
