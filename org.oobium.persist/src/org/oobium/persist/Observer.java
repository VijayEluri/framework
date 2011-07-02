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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.oobium.logging.Logger;
import org.oobium.logging.LogProvider;

public class Observer<T extends Model> {

	private static final Logger slogger = LogProvider.getLogger(PersistService.class);

	private static final Map<Class<?>, List<Observer<? extends Model>>> observerMap = new HashMap<Class<?>, List<Observer<? extends Model>>>();

	synchronized static void addObserver(Class<? extends Observer<?>> observerClass) {
		try {
			Observer<?> observer = observerClass.newInstance();
			addObserver(observer);
		} catch(Exception e) {
			slogger.error("error adding observer " + observerClass, e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * @param observer
	 * @return true if the observer was added to the list of observers for its modelClass; false if it
	 * already exists in the list of observers.
	 */
	protected synchronized static boolean addObserver(Observer<? extends Model> observer) {
		List<Observer<? extends Model>> observers = observerMap.get(observer.modelClass);
		
		if(observers == null) {
			observers = new ArrayList<Observer<? extends Model>>();
			observerMap.put(observer.modelClass, observers);

			// add the socket listener if RemotePersistService and first time adding this modelClass
			PersistService service = Model.getPersistService(observer.modelClass);
			if(service instanceof RemotePersistService) {
				try {
					((RemotePersistService) service).addSocketListener();
				} catch(Exception e) { // catch all, or just IllegalArgumentException?
					slogger.warn("could not add socket listener to remote persist service: " + e.getClass() + ": " + e.getLocalizedMessage());
				}
			}
		}

		if(observers.add(observer)) {
			if(slogger.isLoggingInfo()) {
				slogger.info("added observer (" + observer.getClass().getSimpleName() + ", " + observer.modelClass.getSimpleName() + ")");
			}
			return true;
		}
		return false;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static Class<? extends Model> findModelClass(Class<? extends Observer> clazz) {
		Type type = clazz.getGenericSuperclass();
		if(type instanceof ParameterizedType) {
			ParameterizedType pt = (ParameterizedType) type;
			return (Class<? extends Model>) pt.getActualTypeArguments()[0];
		} else {
			throw new IllegalArgumentException("Observer class must be parameterized");
		}
	}
	
	synchronized static void removeObservers(Class<?> clazz) {
		if(Model.class.isAssignableFrom(clazz)) {
			List<Observer<?>> removed = observerMap.remove(clazz);
			if(slogger.isLoggingInfo() && removed != null) {
				for(Observer<?> observer : removed) {
					slogger.info("removed observer (" + observer.getClass().getSimpleName() + ", " + clazz.getSimpleName() + ")");
				}
			}
			removeSocketListener(clazz.asSubclass(Model.class));
		} else if(Observer.class.isAssignableFrom(clazz)) {
			for(Iterator<List<Observer<?>>> observersIter = observerMap.values().iterator(); observersIter.hasNext(); ) {
				List<Observer<?>> observers = observersIter.next();
				for(Iterator<Observer<?>> observerIter = observers.iterator(); observerIter.hasNext(); ) {
					Observer<?> observer = observerIter.next();
					if(observer.getClass() == clazz) {
						observerIter.remove();
						if(slogger.isLoggingInfo() && observers != null) {
							slogger.info("removed observer (" + observer.getClass().getSimpleName() + ", " + clazz.getSimpleName() + ")");
						}
					}
				}
				if(observers.isEmpty()) {
					observersIter.remove();
					removeSocketListener(findModelClass(clazz.asSubclass(Observer.class)));
				}
			}
		}
	}
	
	protected synchronized static <T extends Model> void removeObserver(Observer<T> observer) {
		List<Observer<?>> observers = observerMap.get(observer.modelClass);
		if(observers != null) {
			observers.remove(observer);
			if(observers.isEmpty()) {
				observerMap.remove(observer.modelClass);
				removeSocketListener(observer.modelClass);
			}
			if(slogger.isLoggingInfo()) {
				slogger.info("removed observer (" + observer.getClass().getSimpleName() + ", " + observer.modelClass.getSimpleName() + ")");
			}
		}
	}
	
	private static void removeSocketListener(Class<? extends Model> modelClass) {
		PersistService service = Model.getPersistService(modelClass);
		if(service instanceof RemotePersistService) {
			try {
				((RemotePersistService) service).removeSocketListener();
			} catch(Exception e) { // catch all, or just IllegalArgumentException?
				slogger.warn("could not add socket listener to remote persist service: " + e.getClass() + ": " + e.getLocalizedMessage());
			}
		}
	}

	
	private static final int AFTER_CREATE			= 0;
	private static final int AFTER_DESTROY			= 1;
	private static final int AFTER_SAVE				= 2;
	private static final int AFTER_UPDATE			= 3;
	private static final int AFTER_VALIDATE_CREATE	= 4;
	private static final int AFTER_VALIDATE_DESTROY	= 5;
	private static final int AFTER_VALIDATE_SAVE	= 6;
	private static final int AFTER_VALIDATE_UPDATE	= 7;

	private static final int BEFORE_CREATE			= 8;
	private static final int BEFORE_DESTROY			= 9;
	private static final int BEFORE_SAVE			= 10;
	private static final int BEFORE_UPDATE			= 11;
	private static final int BEFORE_VALIDATE_CREATE	= 12;
	private static final int BEFORE_VALIDATE_DESTROY= 13;
	private static final int BEFORE_VALIDATE_SAVE	= 14;
	private static final int BEFORE_VALIDATE_UPDATE	= 15;
	
	
	@SuppressWarnings("unchecked")
	private static <T extends Model> void run(T model, int method) {
		List<Observer<?>> observers = observerMap.get(model.getClass());
		if(observers != null && !observers.isEmpty()) {
			Logger logger = Model.getLogger();
			for(Observer<?> o : observers) {
				Observer<T> observer = (Observer<T>) o;
				observer.logger = logger;
				switch(method) {
				case AFTER_CREATE:				observer.afterCreate(model);			break;
				case AFTER_DESTROY:				observer.afterDestroy(model.destroyed);	break;
				case AFTER_SAVE:				observer.afterSave(model);				break;
				case AFTER_UPDATE:				observer.afterUpdate(model);			break;
				case AFTER_VALIDATE_CREATE:		observer.afterValidateCreate(model);	break;
				case AFTER_VALIDATE_DESTROY:	observer.afterValidateDestroy(model);	break;
				case AFTER_VALIDATE_SAVE:		observer.afterValidateSave(model);		break;
				case AFTER_VALIDATE_UPDATE:		observer.afterValidateUpdate(model);	break;

				case BEFORE_CREATE:				observer.beforeCreate(model);			break;
				case BEFORE_DESTROY:			observer.beforeDestroy(model);			break;
				case BEFORE_SAVE:				observer.beforeSave(model);				break;
				case BEFORE_UPDATE:				observer.beforeUpdate(model);			break;
				case BEFORE_VALIDATE_CREATE:	observer.beforeValidateCreate(model);	break;
				case BEFORE_VALIDATE_DESTROY:	observer.beforeValidateDestroy(model);	break;
				case BEFORE_VALIDATE_SAVE:		observer.beforeValidateSave(model);		break;
				case BEFORE_VALIDATE_UPDATE:	observer.beforeValidateUpdate(model);	break;
				}
			}
		}
	}
	
	static void runAfterCreate(Model model) {
		run(model, AFTER_CREATE);
	}

	static void runAfterDestroy(Model model) {
		run(model, AFTER_DESTROY);
	}

	static void runAfterSave(Model model) {
		run(model, AFTER_SAVE);
	}

	static void runAfterUpdate(Model model) {
		run(model, AFTER_UPDATE);
	}

	static void runAfterValidateCreate(Model model) {
		run(model, AFTER_VALIDATE_CREATE);
	}

	static void runAfterValidateDestroy(Model model) {
		run(model, AFTER_VALIDATE_DESTROY);
	}

	static void runAfterValidateSave(Model model) {
		run(model, AFTER_VALIDATE_SAVE);
	}

	static void runAfterValidateUpdate(Model model) {
		run(model, AFTER_VALIDATE_UPDATE);
	}

	static void runBeforeCreate(Model model) {
		run(model, BEFORE_CREATE);
	}

	static void runBeforeDestroy(Model model) {
		run(model, BEFORE_DESTROY);
	}

	static void runBeforeSave(Model model) {
		run(model, BEFORE_SAVE);
	}

	static void runBeforeUpdate(Model model) {
		run(model, BEFORE_UPDATE);
	}

	static void runBeforeValidateCreate(Model model) {
		run(model, BEFORE_VALIDATE_CREATE);
	}

	static void runBeforeValidateDestroy(Model model) {
		run(model, BEFORE_VALIDATE_DESTROY);
	}

	static void runBeforeValidateSave(Model model) {
		run(model, BEFORE_VALIDATE_SAVE);
	}

	static void runBeforeValidateUpdate(Model model) {
		run(model, BEFORE_VALIDATE_UPDATE);
	}
	
	
	protected Logger logger;
	protected final Class<? extends Model> modelClass;

	public Observer() {
		modelClass = findModelClass(getClass());
	}

	protected void afterCreate(T model) {
		// subclasses to implement if necessary
	}

	protected void afterDestroy(int id) {
		// subclasses to implement if necessary
	}

	protected void afterSave(T model) {
		// subclasses to implement if necessary
	}

	protected void afterUpdate(T model) {
		// subclasses to implement if necessary
	}

	protected void afterValidateCreate(T model) {
		// subclasses to implement if necessary
	}

	protected void afterValidateDestroy(T model) {
		// subclasses to implement if necessary
	}

	protected void afterValidateSave(T model) {
		// subclasses to implement if necessary
	}

	protected void afterValidateUpdate(T model) {
		// subclasses to implement if necessary
	}

	/**
	 * Run during {@link Model#save()}, after {@link #beforeSave(Model)} if the model has <b>not</b> been
	 * previously saved (so that this is a create event, and not an update event).<br/>
	 * To cancel the save, simply add an error using {@link Model#addError(String)}.
	 * @param model the model being saved
	 */
	protected void beforeCreate(T model) {
		// subclasses to implement if necessary
	}

	/**
	 * Run at the beginning of {@link Model#destroy()} if the model has been previously saved
	 * (unsaved models cannot be destroyed and the method will fail before reaching this point).<br/>
	 * To cancel the destroy, simply add an error using {@link Model#addError(String)}.
	 * @param model the model being destroyed
	 */
	protected void beforeDestroy(T model) {
		// subclasses to implement if necessary
	}

	/**
	 * Run during {@link Model#save()}, after a validation, 
	 * just before actually performing the save.<br/>
	 * Note that this method is called for both create and update events.<br/>
	 * To cancel the save, simply add an error using {@link Model#addError(String)}.
	 * @param model the model being saved
	 */
	protected void beforeSave(T model) {
		// subclasses to implement if necessary
	}

	/**
	 * Run during {@link Model#save()}, after {@link #beforeSave(Model)} if the model has been
	 * previously saved (so that this is an update event, and not a create event).<br/>
	 * To cancel the save, simply add an error using {@link Model#addError(String)}.
	 * @param model the model being saved
	 */
	protected void beforeUpdate(T model) {
		// subclasses to implement if necessary
	}

	protected void beforeValidateCreate(T model) {
		// subclasses to implement if necessary
	}

	protected void beforeValidateDestroy(T model) {
		// subclasses to implement if necessary
	}

	protected void beforeValidateSave(T model) {
		// subclasses to implement if necessary
	}

	protected void beforeValidateUpdate(T model) {
		// subclasses to implement if necessary
	}

	public Class<? extends Model> getModelClass() {
		return modelClass;
	}

}
