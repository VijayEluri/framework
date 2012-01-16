package org.oobium.eclipse.designer.manager;

import static org.oobium.build.util.ProjectUtils.isModel;
import static org.oobium.utils.literal.Map;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.oobium.build.clients.JavaClientExporter;
import org.oobium.build.clients.OsgiClientExporter;
import org.oobium.build.model.ModelDefinition;
import org.oobium.build.runner.RunEvent;
import org.oobium.build.runner.RunListener;
import org.oobium.build.runner.RunnerService;
import org.oobium.build.workspace.Application;
import org.oobium.build.workspace.Migrator;
import org.oobium.build.workspace.Project;
import org.oobium.build.workspace.Workspace;
import org.oobium.eclipse.OobiumPlugin;
import org.oobium.eclipse.designer.DesignerPlugin;
import org.oobium.eclipse.designer.editor.internal.ModuleCompiler;
import org.oobium.persist.Model;
import org.oobium.persist.Observer;
import org.oobium.utils.Config.Mode;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;

public class DataService {

	private final DataServiceManager manager;
	private final String name;
	private final Mode mode;
	
	Bundle bundle;
	String location;
	private OsgiHttpPersistService persistor;
	
	private final Observer<Model> observer;
	private final List<DataServiceListener> dsListeners;
	private final List<ModelListener> mListeners;
	private final List<Object> connections;
	
	public DataService(DataServiceManager manager, String name, Mode mode) {
		this.manager = manager;
		this.name = name;
		this.mode = mode;

		this.observer = new Observer<Model>() {
			@Override
			protected void afterCreate(Model model) {
				notifyModelListeners(ModelEvent.Type.Created, "created " + model);
			}
			@Override
			protected void afterUpdate(Model model) {
				notifyModelListeners(ModelEvent.Type.Updated, "updated " + model);
			}
			@Override
			protected void afterDestroy(Object id) {
				notifyModelListeners(ModelEvent.Type.Destroyed, "destroyed some model with id: " + id);
			}
		};
		
		this.dsListeners = new ArrayList<DataServiceListener>();
		this.mListeners = new ArrayList<ModelListener>();
		this.connections = new ArrayList<Object>();

		RunnerService.addListener(new RunListener() {
			@Override
			public void handleEvent(RunEvent event) {
				if(persistor != null) {
					Application application = DataService.this.manager.getApplication(DataService.this.name);
					switch(event.type) {
					case Updated:
						if(!RunnerService.isAutoMigrating(application)) {
							reconnect();
						}
						break;
					case Migrated:
						if(RunnerService.isAutoMigrating(application)) {
							reconnect();
						}
						break;
					}
				}
			}
		});
	}

	public Application create() {
		return manager.createApplication(name);
	}
	
	public boolean exists() {
		return manager.getApplicationFor(name) != null;
	}
	
	private void reconnect() {
		synchronized(connections) { // block connect/disconnect while reconnecting
			notifyServiceListeners(DataServiceEvent.Type.Disconnected);
			Model.removeObserver(observer);
			Model.setPersistService(null);
			if(persistor != null) {
				persistor.removeSocketListener();
				persistor.closeSession();
				persistor = null;
			}
			checkBundle();
			persistor = new OsgiHttpPersistService(this);
			
			// TODO refresh the ApiService too
			
			Model.setPersistService(persistor);
			Model.addObserver(observer);
			notifyServiceListeners(DataServiceEvent.Type.Connected);
		}
	}
	
	public void addServiceListener(DataServiceListener listener) {
		if(!dsListeners.contains(listener)) {
			dsListeners.add(listener);
		}
	}

	public void addModelListener(ModelListener listener) {
		if(!mListeners.contains(listener)) {
			mListeners.add(listener);
		}
	}

	private void checkBundle() {
		if(bundle == null) {
			try {
				Workspace workspace = OobiumPlugin.getWorkspace();
				Application application = manager.getApplication(name);
				File jar = exportClientBundle(workspace, application);
				System.out.println(jar);
				bundle = DesignerPlugin.getDefault().getBundle().getBundleContext().installBundle("file://" + jar.getAbsolutePath());
			} catch(BundleException e) {
				throw new RuntimeException(e);
			} catch(IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private void checkPersistor() {
		if(persistor == null) {
			throw new IllegalStateException("persist service is not ready");
		}
	}
	
	private void notifyModelListeners(ModelEvent.Type type, String message) {
		if(!mListeners.isEmpty()) {
			ModelEvent event = new ModelEvent(type);
			event.setMessage(message);
			ModelListener[] la = mListeners.toArray(new ModelListener[mListeners.size()]);
			for(ModelListener l : la) {
				l.handleModelEvent(event);
			}
		}
	}
	
	private void notifyServiceListeners(DataServiceEvent.Type type) {
		if(!dsListeners.isEmpty()) {
			DataServiceEvent event = new DataServiceEvent(this, type);
			DataServiceListener[] la = dsListeners.toArray(new DataServiceListener[dsListeners.size()]);
			for(DataServiceListener l : la) {
				l.handleDataServiceEvent(event);
			}
		}
	}

	/**
	 * ONLY ACCESS FROM {@link #connect(Object)} AND {@link #start()} CALLBACK
	 * @return
	 */
	private boolean connect() {
		if(persistor == null) {
			Application app = manager.getApplication(name);
			if(RunnerService.isRunning(app)) {
				checkBundle();
				persistor = new OsgiHttpPersistService(this);
				Model.setPersistService(persistor);
				Model.addObserver(observer);
				notifyServiceListeners(DataServiceEvent.Type.Connected);
			} else {
				start();
				return false;
			}
		}
		return true;
	}
	
	public boolean connect(Object connection) {
		synchronized(connections) {
			if(connections.contains(connection)) {
				return true; // allow calling multiple times from the same connection
			}
			connections.add(connection);
			return connect();
		}
	}
	
	public void create(Model...models) throws Exception {
		checkBundle();
		checkPersistor();
		persistor.create(models);
	}

	public void destroy(Model...models) throws Exception {
		checkBundle();
		checkPersistor();
		persistor.destroy(models);
	}
	
	public void disconnect(Object connection) {
		synchronized(connections) {
			if(connections.remove(connection)) { // allow calling multiple times from same connection
				notifyServiceListeners(DataServiceEvent.Type.Disconnected);
				Model.removeObserver(observer);
				Model.setPersistService(null);
				if(persistor != null) {
					persistor.removeSocketListener();
					persistor.closeSession();
					persistor = null;
				}
				if(connections.isEmpty()) {
					stop();
				}
			}
		}
	}
	
	public void export(Project target) {
		Workspace workspace = OobiumPlugin.getWorkspace();
		Application application = manager.getApplication(name);
		if(mode == Mode.DEV) {
			if(target != application) {
				RunnerService.pauseUpdaters();
				if(application == null) {
					application = manager.createApplication(name);
				}
				application.generate(workspace);
				application.createInitialMigration(workspace, workspace.getMode());
				Migrator migrator = workspace.getMigratorFor(application, true);
				try {
					ModuleCompiler compiler = new ModuleCompiler(workspace, application, migrator);
					compiler.setClean(false);
					compiler.setCompileDependencies(false);
					compiler.compile();
					
					JavaClientExporter exporter = new JavaClientExporter(workspace, application);
					exporter.setFull(false);
					exporter.includeSource(true);
					exporter.setTarget(target);
					exporter.export();
					
					if(bundle != null) {
						exportClientBundle(workspace, application);
						bundle.update();
					}
				} catch(BundleException e) {
					e.printStackTrace();
				} catch(IOException e) {
					e.printStackTrace();
				} finally {
					RunnerService.unpauseUpdaters();
				}
			}
		}
	}
	
	private File exportClientBundle(Workspace workspace, Application application) throws IOException {
		OsgiClientExporter exporter = new OsgiClientExporter(workspace, application);
		return exporter.export();
	}
	
	public List<? extends Model> findAll(String type) throws Exception {
		if(type != null) {
			checkBundle();
			checkPersistor();
			try {
				Model.setPersistService(persistor);
				Class<?> clazz = bundle.loadClass(type);
				if(Model.class.isAssignableFrom(clazz)) {
					return persistor.findAll(clazz.asSubclass(Model.class));
				}
			} finally {
				Model.setPersistService(null);
			}
		}
		return new ArrayList<Model>(0);
	}

	public String getLocation() {
		Application app = manager.getApplication(name);
		if(app != null) {
			return app.file.getAbsolutePath();
		}
		return null;
	}
	
	public ModelDefinition getModelDefinition(String modelName) {
		Application app = manager.getApplication(name);
		if(app != null) {
			File file = app.getModel(modelName);
			if(isModel(file)) {
				return new ModelDefinition(file);
			}
		}
		return null;
	}

	public List<ModelDefinition> getModelDefinitions() {
		Application app = manager.getApplication(name);
		if(app != null) {
			List<ModelDefinition> defs = new ArrayList<ModelDefinition>();
			for(File file : app.findModels()) {
				defs.add(new ModelDefinition(file));
			}
			return defs;
		}
		return null;
	}

	public List<String> getModelNames() {
		Application app = manager.getApplication(name);
		if(app != null) {
			List<String> names = new ArrayList<String>();
			for(File model : app.findModels()) {
				names.add(app.getModelName(model));
			}
			Collections.sort(names);
			return names;
		}
		return new ArrayList<String>(0);
	}

	public String getName() {
		return name;
	}
	
	public Model newModel(String type) {
		if(type != null) {
			checkBundle();
			try {
				Class<?> clazz = bundle.loadClass(type);
				if(Model.class.isAssignableFrom(clazz)) {
					return clazz.asSubclass(Model.class).newInstance();
				}
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public void removeServiceListener(DataServiceListener listener) {
		dsListeners.remove(listener);
	}
	
	public void removeModelListener(ModelListener listener) {
		mListeners.remove(listener);
	}
	
	private void start() {
		if(mode == Mode.DEV) {
			Workspace workspace = OobiumPlugin.getWorkspace();
			Application app = manager.getApplication(name);
			if(app == null) {
				app = manager.createApplication(name);
			}
			if(app != null) {
				location = app.getLocation(mode);
				if(location != null) {
					RunnerService.addListener(new RunListener() {
						@Override
						public void handleEvent(RunEvent event) {
							switch(event.type) {
							case Started:
								if(!RunnerService.isAutoMigrating(DataService.this.manager.getApplication(DataService.this.name))) {
									RunnerService.removeListener(this);
									connect();
								}
								break;
							case Migrated:
								if(RunnerService.isAutoMigrating(DataService.this.manager.getApplication(DataService.this.name))) {
									RunnerService.removeListener(this);
									connect();
								}
								break;
							}
						}
					});
					RunnerService.run(workspace, app, mode, Map("org.oobium.logging.file", "debug"));
				}
			}
		}
	}
	
	private void stop() {
		if(mode == Mode.DEV) {
			Application app = manager.getApplication(name);
			if(app != null) {
				RunnerService.stop(app);
			}
		}
	}
	
}
