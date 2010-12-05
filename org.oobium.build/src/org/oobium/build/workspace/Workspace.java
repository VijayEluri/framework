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
package org.oobium.build.workspace;

import static java.util.Arrays.asList;
import static org.oobium.utils.StringUtils.blank;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.oobium.build.BuildBundle;
import org.oobium.build.gen.ProjectGenerator;
import org.oobium.utils.Config;
import org.oobium.utils.Config.Mode;
import org.oobium.utils.Config.OsgiRuntime;

public class Workspace {

	public enum EventType {
		/**
		 * An individual bundle has been either added, removed, or refreshed.<br/>
		 * Added: oldValue == null, and newValue == {@link Bundle}.<br/>
		 * Removed: oldValue == {@link Bundle}, and newValue == null.<br/>
		 * Removed: oldValue == {@link Bundle}, and newValue == {@link Bundle}.<br/>
		 */
		Bundle,
		/**
		 * The bundle repositories have been refreshed, causing all bundles to be recalculated and refreshed.<br/>
		 * Both oldValue and newValue will be {@link List}s of {@link Bundle} objects.
		 */
		Bundles,
		/**
		 * The bundle repositories have been set.<br/>
		 * Both oldValue and newValue will be arrays of of {@link File} objects.
		 */
		BundleRepos,
		/**
		 * The workspace directory has been set.<br/>
		 * The oldValue will be either null or a {@link File} representing the old setting.<br/>
		 * The newValue will be either null or a {@link File} representing the new setting.
		 */
		Workspace
	}

	public static final String BUNDLE_REPOS = "org.oobium.bundle.repos";
	public static final String JAVA_DIR = "org.oobium.java.dir";
	public static final String WORKSPACE = "org.oobium.workspace";
	public static final String RUNTIME = "org.oobium.runtime";

	public static final String RUNTIME_EQUINOX = "equinox";
	public static final String RUNTIME_FELIX = "felix";


	private final ReadWriteLock lock;
	
	private Mode mode;
	private Workspace parentWorkspace;
	private final Map<File, Bundle> bundles;
	private Map<File, List<File>> repos;
	private List<File> ibundles;
	private final Map<File, Application> applications;
//	private Set<File> workspaceFiles;
//	private Set<File> bundleRepoFiles;
	private WorkspaceListener[] listeners;
//	private File dir;
	private File workingDir;
	
	private Bundle buildBundle;
	private Bundle equinoxRuntimeBundle;
	private Bundle felixRuntimeBundle;
	private Bundle knopplerFishRuntimeBundle;
	
	// always handy to have a place for spare data :)
	// follows the style of SWT widgets
	private Object data;

	
	public Workspace() {
		this(null);
	}

	public Workspace(File workingDirectory) {
		lock = new ReentrantReadWriteLock();
		mode = Mode.DEV;
		bundles = new HashMap<File, Bundle>();
		applications = new HashMap<File, Application>();
		listeners = new WorkspaceListener[0];
		setWorkingDirectory(workingDirectory);
	}
	
	private Bundle addBundle(File file) {
		try {
			File cfile = file.getCanonicalFile();
			Bundle bundle = Bundle.create(cfile);
			if(bundle != null) {
				bundles.put(cfile, bundle);
				if(bundle instanceof Application) {
					applications.put(cfile, (Application) bundle);
				}
				if(bundle.name.equals(BuildBundle.ID)) {
					buildBundle = bundle;
				} else if(bundle.name.equals("org.eclipse.osgi")) {
					equinoxRuntimeBundle = bundle;
				} else if(bundle.name.equals("org.apache.felix.main")) {
					felixRuntimeBundle = bundle;
				}
				System.out.println("added bundle: " + bundle);
				return bundle;
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public void addListener(WorkspaceListener listener) {
		synchronized(listeners) {
			listeners = Arrays.copyOf(listeners, listeners.length + 1);
			listeners[listeners.length-1] = listener;
		}
	}

	public Application createApplication(File file, Map<String, String> properties) {
		if(file != null) {
			return (Application) loadBundle(ProjectGenerator.createApplication(file, properties));
		}
		return null;
	}
	
	public Migrator createMigrator(Module module) {
		if(module != null) {
			Set<Bundle> dependencies = module.getDependencies(this);
			return (Migrator) loadBundle(ProjectGenerator.createMigrator(module, dependencies));
		}
		return null;
	}
	
	public Module createModule(File file, Map<String, String> properties) {
		if(file != null) {
			return (Module) loadBundle(ProjectGenerator.createModule(file, properties));
		}
		return null;
	}
	
	public TestSuite createTestSuite(Module module, Map<String, String> properties) {
		if(module != null) {
			return (TestSuite) loadBundle(ProjectGenerator.createTests(module, properties));
		}
		return null;
	}
	
	public Module createWebservice(File file, Map<String, String> properties) {
		if(file != null) {
			return (Module) loadBundle(ProjectGenerator.createWebservice(file, properties));
		}
		return null;
	}

	void fireEvent(EventType eventType, Object oldValue, Object newValue) {
		WorkspaceEvent event = new WorkspaceEvent(eventType, oldValue, newValue);
		for(WorkspaceListener listener : Arrays.copyOf(this.listeners, this.listeners.length)) {
			listener.handleEvent(event);
		}
	}
	
	public Application getApplication(File file) {
		if(file == null) {
			return null;
		}
		
		lock.readLock().lock();
		try {
			Application app = applications.get(file);
			if(app == null && parentWorkspace != null) {
				return parentWorkspace.getApplication(file);
			}
			return app;
		} finally {
			lock.readLock().unlock();
		}
	}
	
	public Application getApplication(String name) {
		if(name == null || name.length() == 0) {
			return null;
		}

		lock.readLock().lock();
		try {
			for(Application application : applications.values()) {
				if(application.name.equals(name)) {
					return application;
				}
			}
			if(parentWorkspace != null) {
				return parentWorkspace.getApplication(name);
			}
			return null;
		} finally {
			lock.readLock().unlock();
		}
	}
	
	public Application[] getApplications() {
		lock.readLock().lock();
		try {
			Set<Application> apps = new HashSet<Application>(applications.values());
			return apps.toArray(new Application[apps.size()]);
		} finally {
			lock.readLock().unlock();
		}
	}
	
	public Bundle getBuildBundle() {
		if(buildBundle != null) {
			return buildBundle;
		}
		if(parentWorkspace != null) {
			return parentWorkspace.getBuildBundle();
		}
		return null;
	}
	
	public Bundle getBundle(File file) {
		if(file == null) {
			return null;
		}
		
		lock.readLock().lock();
		try {
			Bundle bundle = bundles.get(file);
			if(bundle == null && parentWorkspace != null) {
				return parentWorkspace.getBundle(file);
			}
			return bundle;
		} finally {
			lock.readLock().unlock();
		}
	}
	
	public Bundle getBundle(ImportedPackage importedPackage) {
		lock.readLock().lock();
		try {
			for(Bundle bundle : bundles.values()) {
				if(bundle.resolves(importedPackage)) {
					return bundle;
				}
			}
			if(parentWorkspace != null) {
				return parentWorkspace.getBundle(importedPackage);
			}
			return null;
		} finally {
			lock.readLock().unlock();
		}
	}
	
	public Bundle getBundle(OsgiRuntime runtime) {
		Bundle bundle = null;
		switch(runtime) {
		case Equinox:		bundle = equinoxRuntimeBundle; break;
		case Felix:			bundle = felixRuntimeBundle; break;
		case Knopplerfish:	bundle = knopplerFishRuntimeBundle; break;
		default:			throw new IllegalArgumentException("unsupported runtime: " + runtime);
		}
		if(bundle == null && parentWorkspace != null) {
			return parentWorkspace.getBundle(runtime);
		}
		return bundle;
	}
	
	public Bundle getBundle(RequiredBundle requiredBundle) {
		lock.readLock().lock();
		try {
			for(Bundle bundle : bundles.values()) {
				if(bundle.resolves(requiredBundle)) {
					return bundle;
				}
			}
			if(parentWorkspace != null) {
				return parentWorkspace.getBundle(requiredBundle);
			}
			return null;
		} finally {
			lock.readLock().unlock();
		}
	}
	
	/**
	 * Get the first bundle that matches the given full name (the name and version range).
	 * If the fullName does not include a version range, than a version range of [*, *] is used.
	 * @param fullName
	 * @return the matching {@link Bundle} object
	 */
	public Bundle getBundle(String fullName) {
		if(blank(fullName)) {
			return null;
		}

		String name;
		VersionRange range;
		int ix = fullName.lastIndexOf('_');
		if(ix == -1) {
			name = fullName;
			range = null;
		} else {
			try {
				range = new VersionRange(fullName.substring(ix+1));
				name = fullName.substring(0, ix);
			} catch(IllegalArgumentException e) {
				// it may not actually be a version number at the end... oh well
				name = fullName;
				range = null;
			}
		}
		
		lock.readLock().lock();
		try {
			for(Bundle bundle : bundles.values()) {
				if(bundle.name.equals(name) && bundle.version.resolves(range)) {
					return bundle;
				}
			}
			if(parentWorkspace != null) {
				return parentWorkspace.getBundle(fullName);
			}
			return null;
		} finally {
			lock.readLock().unlock();
		}
	}
	
	public Bundle getBundle(String name, String version) {
		return getBundle(name, new Version(version));
	}
	
	/**
	 * Get the bundle that matches the given name and exact version.
	 * @param name the name of the bundle
	 * @param version the exact version of the bundle
	 * @return the matching {@link Bundle} object if found; null otherwise (also returns null
	 * if name is empty or version is null)
	 */
	public Bundle getBundle(String name, Version version) {
		if(blank(name) || version == null) {
			return null;
		}

		lock.readLock().lock();
		try {
			for(Bundle bundle : bundles.values()) {
				if(bundle.name.equals(name) && bundle.version.equals(version)) {
					return bundle;
				}
			}
			if(parentWorkspace != null) {
				return parentWorkspace.getBundle(name);
			}
			return null;
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * Get an array of paths for the bundle repositories that this workspace is currently using.
	 * @return an array of absolute paths; never null
	 */
	public String[] getBundleRepositories() {
		if(repos != null) {
			List<String> repos = new ArrayList<String>();
			for(File repo : this.repos.keySet()) {
				repos.add(repo.getAbsolutePath());
			}
			return repos.toArray(new String[repos.size()]);
		}
		return new String[0];
	}

	public Bundle[] getBundles() {
		lock.readLock().lock();
		try {
			Set<Bundle> bundles = new HashSet<Bundle>(this.bundles.values());
			return bundles.toArray(new Bundle[bundles.size()]);
		} finally {
			lock.readLock().unlock();
		}
	}
	
	/**
	 * Get all the bundles that match the given full name (the name and version range).
	 * If the fullName does not include a version range, than a version range of [*, *] is used.<br/>
	 * Does NOT include bundles from the parent workspace.
	 * @param fullName
	 * @return an array of matching {@link Bundle} objects; never null
	 */
	public Bundle[] getBundles(String fullName) {
		if(blank(fullName)) {
			return null;
		}

		String name;
		VersionRange range;
		int ix = fullName.lastIndexOf('_');
		if(ix == -1) {
			name = fullName;
			range = null;
		} else {
			try {
				range = new VersionRange(fullName.substring(ix+1));
				name = fullName.substring(0, ix);
			} catch(IllegalArgumentException e) {
				// it may not actually be a version number at the end... oh well
				name = fullName;
				range = null;
			}
		}

		lock.readLock().lock();
		try {
			List<Bundle> list = new ArrayList<Bundle>();
			for(Bundle bundle : bundles.values()) {
				if(bundle.name.equals(name) && bundle.version.resolves(range)) {
					list.add(bundle);
				}
			}
			return list.toArray(new Bundle[list.size()]);
		} finally {
			lock.readLock().unlock();
		}
	}

	public Object getData() {
		return data;
	}

	public Object getData(String key) {
		return (data instanceof Map<?,?>) ? ((Map<?,?>) data).get(key) : null;
	}
	
	public Migrator getMigrator(File file) {
		Bundle bundle = getBundle(file);
		if(bundle instanceof Migrator) {
			return (Migrator) bundle;
		}
		if(parentWorkspace != null) {
			return parentWorkspace.getMigrator(file);
		}
		return null;
	}
	
	public Migrator getMigrator(String name) {
		Bundle bundle = getBundle(name);
		if(bundle instanceof Migrator) {
			return (Migrator) bundle;
		}
		if(parentWorkspace != null) {
			return parentWorkspace.getMigrator(name);
		}
		return null;
	}
	
	public Migrator getMigratorFor(Module module) {
		return getMigrator(new File(module.file.getAbsoluteFile() + ".migrator"));
	}
	
	public Mode getMode() {
		return mode;
	}
	
	public Module getModule(File file) {
		Bundle bundle = getBundle(file);
		if(bundle instanceof Module) {
			return (Module) bundle;
		}
		if(parentWorkspace != null) {
			return parentWorkspace.getModule(file);
		}
		return null;
	}
	
	public Module getModule(String fullName) {
		Bundle bundle = getBundle(fullName);
		if(bundle instanceof Module) {
			return (Module) bundle;
		}
		if(parentWorkspace != null) {
			return parentWorkspace.getModule(fullName);
		}
		return null;
	}

	public Module[] getModules() {
		lock.readLock().lock();
		try {
			Set<Module> modules = new HashSet<Module>();
			for(Bundle bundle : bundles.values()) {
				if(bundle instanceof Module) {
					modules.add((Module) bundle);
				}
			}
			return modules.toArray(new Module[modules.size()]);
		} finally {
			lock.readLock().unlock();
		}
	}
	
	public Workspace getParent() {
		return parentWorkspace;
	}
	
	private File[] getProjects(File repo) {
		if(repo.isDirectory()) {
			File[] files = repo.listFiles(new FileFilter() {
				@Override
				public boolean accept(File file) {
					if(file.isFile() && file.getName().endsWith(".jar")) {
						return true;
					}
					if(file.isDirectory()) {
						return new File(file, "META-INF" + File.separator + "MANIFEST.MF").exists();
					}
					return false;
				}
			});
			return files;
		}
		return new File[0];
	}
	
	public TestSuite getTestSuite(File file) {
		Bundle bundle = getBundle(file);
		if(bundle instanceof TestSuite) {
			return (TestSuite) bundle;
		}
		if(parentWorkspace != null) {
			return parentWorkspace.getTestSuite(file);
		}
		return null;
	}
	
	public TestSuite getTestSuite(String name) {
		Bundle bundle = getBundle(name);
		if(bundle instanceof TestSuite) {
			return (TestSuite) bundle;
		}
		if(parentWorkspace != null) {
			return parentWorkspace.getTestSuite(name);
		}
		return null;
	}
	
	public Migrator getTestSuiteFor(Module module) {
		return getMigrator(new File(module.file.getAbsoluteFile() + ".tests"));
	}

	/**
	 * Get the location of the working directory for this workspace.
	 * The working directory is a location that the system has write access
	 * to and can be used for building and exporting bundles.
	 * @return a File for the working directory, or null if it has not been set
	 */
	public File getWorkingDirectory() {
		// TODO lock needed anymore?
		lock.readLock().lock();
		try {
			return workingDir;
		} finally {
			lock.readLock().unlock();
		}
	}

	private void addToRepo(File repo, File bundle) {
		List<File> bundles = repos.get(repo);
		if(bundles == null) {
			bundles = new ArrayList<File>();
			repos.put(repo, bundles);
		}
		bundles.add(bundle);
	}
	
	public Bundle loadBundle(File file) {
		if(file == null || !file.exists()) {
			return null;
		}
		
		lock.writeLock().lock();
		try {
			Bundle bundle = bundles.get(file);
			if(bundle == null) {
				bundle = addBundle(file);
				if(bundle != null) {
					boolean foundRepo = false;
					File parent = file.getParentFile();
					if(repos != null) {
						for(File repo : repos.keySet()) {
							if(parent.equals(repo)) {
								addToRepo(repo, file);
								foundRepo = true;
								break;
							}
						}
					}
					if(!foundRepo) {
						if(ibundles == null) {
							ibundles = new ArrayList<File>();
						}
						ibundles.add(file);
					}
				}
			}
			return bundle;
		} finally {
			lock.writeLock().unlock();
		}
	}

	/**
	 * Refreshes all bundles and repositories currently loaded in this workspace.
	 * Independent bundles will be reloaded if they exist, and removed if they don't.
	 * Repositories (and their contained bundles) will be reloaded if they exist, and
	 * removed if they don't.
	 */
	public void refresh() {
		List<Bundle> added = new ArrayList<Bundle>();
		List<Bundle> removed = new ArrayList<Bundle>();
		lock.writeLock().lock();
		try {
			// save state
			List<File> reposBak = (repos != null) ? new ArrayList<File>(repos.keySet()) : null;
			List<File> ibundlesBak = (ibundles != null) ? new ArrayList<File>(ibundles) : null;
			
			// remove everything
			removed.addAll(bundles.values());
			if(repos != null) {
				File[] files = repos.keySet().toArray(new File[bundles.size()]);
				for(File repo : files) {
					doRemoveRepository(repo);
				}
			}
			File[] files = bundles.keySet().toArray(new File[bundles.size()]);
			for(File file : files) {
				removeBundle(file);
			}

			// add everything back
			if(reposBak != null) {
				for(File repo : reposBak) {
					doAddRepository(repo);
				}
			}
			if(ibundlesBak != null) {
				for(File file : ibundlesBak) {
					if(!bundles.containsKey(file)) { // may be part of a repo now...
						addBundle(file);
					}
				}
			}
			
			added.addAll(bundles.values());
		} finally {
			lock.writeLock().unlock();
		}

		fireEvent(EventType.Bundles, removed.toArray(), added.toArray());
	}
	
	public void refresh(Bundle bundle) {
		refresh(bundle.file);
	}
	
	public void refresh(File file) {
		Bundle oldBundle;
		Bundle newBundle;
		lock.writeLock().lock();
		try {
			oldBundle = removeBundle(file);
			newBundle = loadBundle(file);
		} finally {
			lock.writeLock().unlock();
		}

		fireEvent(EventType.Bundle, oldBundle, newBundle);
	}
	
	public void remove(Bundle bundle) {
		remove(bundle.file);
	}

	public void remove(File file) {
		Bundle oldBundle;
		lock.writeLock().lock();
		try {
			oldBundle = removeBundle(file);
			if(oldBundle != null) {
				fireEvent(EventType.Bundle, oldBundle, null);
			}
		} finally {
			lock.writeLock().unlock();
		}
	}

	private void removeFromRepos(File bundle) {
		for(List<File> bundles : repos.values()) {
			if(bundles != null && bundles.remove(bundle)) {
				return;
			}
		}
	}
	
	private Bundle removeBundle(File file) {
		if(repos != null) removeFromRepos(file);
		if(applications != null) applications.remove(file);
		if(bundles != null) return bundles.remove(file);
		return null;
	}
	
	public void removeListener(WorkspaceListener listener) {
		synchronized(listeners) {
			for(int i = 0; i < listeners.length; i++) {
				if(listeners[i] == listener) {
					WorkspaceListener[] tmp = new WorkspaceListener[listeners.length-1];
					System.arraycopy(listeners, 0, tmp, 0, i);
					System.arraycopy(listeners, i+1, tmp, i, listeners.length-1-i);
					listeners = tmp;
					break;
				}
			}
		}
	}
	
	public void removeRuntimeBundle(Collection<Bundle> bundles) {
		Bundle bundle = getBundle(OsgiRuntime.Equinox);
		if(bundle != null) {
			bundles.remove(bundle);
		}
		bundle = getBundle(OsgiRuntime.Felix);
		if(bundle != null) {
			bundles.remove(bundle);
		}
		bundle = getBundle(OsgiRuntime.Knopplerfish);
		if(bundle != null) {
			bundles.remove(bundle);
		}
	}

	private void doAddRepository(File repo) {
		if(repos != null) {
			if(repos.containsKey(repo)) {
				return; // don't add twice
			}
		} else {
			repos = new HashMap<File, List<File>>();
		}
		
		List<File> bundles = new ArrayList<File>(asList(getProjects(repo)));
		repos.put(repo, bundles);

		for(File bundle : bundles) {
			addBundle(bundle);
		}
	}
	
	private List<File> doRemoveRepository(File repo) {
		if(repos != null) {
			List<File> bundles = repos.get(repo);
			if(bundles != null) {
				for(File bundle : bundles) {
					remove(bundle);
				}
			}
			repos.remove(repo);
			return bundles;
		}
		return new ArrayList<File>(0);
	}
	
	public void addRepository(File repo) {
		lock.writeLock().lock();
		try {
			doAddRepository(repo);
			fireEvent(EventType.BundleRepos, null, repo);
		} finally {
			lock.writeLock().unlock();
		}
	}
	
	public void removeRepository(File repo) {
		lock.writeLock().lock();
		try {
			doRemoveRepository(repo);
			fireEvent(EventType.BundleRepos, repo, null);
		} finally {
			lock.writeLock().unlock();
		}
	}

	/**
	 * Set the repositories to the given list of repositories. Removes
	 * any existing repositories first.
	 * @param repos
	 */
	public void setRepositories(File...repos) {
		lock.writeLock().lock();
		try {
			String[] oldRepos = getBundleRepositories();
			if(this.repos != null) {
				for(File repo : this.repos.keySet()) {
					doRemoveRepository(repo);
				}
			}
			for(File repo : repos) {
				doAddRepository(repo);
			}
			fireEvent(EventType.BundleRepos, oldRepos, getBundleRepositories());
		} finally {
			lock.writeLock().unlock();
		}
	}
	
	/**
	 * @param commaSeparatedRepos a comma separated list of absolute file paths
	 */
	public void setRepositories(String commaSeparatedRepos) {
		String[] sa = commaSeparatedRepos.split("\\s*,\\s*");
		File[] repos = new File[sa.length];
		for(int i = 0; i < sa.length; i++) {
			repos[i] = new File(sa[i]);
			if(!repos[i].isAbsolute()) {
				throw new IllegalArgumentException("only absolute file paths are permitted (" + commaSeparatedRepos + ")");
			}
		}
		setRepositories(repos);
	}

	public void setData(Object data) {
		this.data = data;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void setData(String key, Object value) {
		if(data instanceof Map) {
			((Map) data).put(key, value);
		} else {
			Map map = new HashMap();
			map.put(key, value);
			data = map;
		}
	}
	
	public void setMode(Mode mode) {
		this.mode = mode;
	}

	public void setParent(Workspace workspace) {
		this.parentWorkspace = workspace;
	}

	public void setRuntimeBundle(Config configuration, Mode mode, Collection<Bundle> bundles) {
		setRuntimeBundle(configuration.getRuntime(mode), bundles);
	}
	
	public void setRuntimeBundle(OsgiRuntime runtime, Collection<Bundle> bundles) {
		Bundle equinox = getBundle(OsgiRuntime.Equinox);
		Bundle felix = getBundle(OsgiRuntime.Felix);
		Bundle kfish = getBundle(OsgiRuntime.Knopplerfish);
		switch(runtime) {
		case Equinox:
			throw new UnsupportedOperationException("TODO: Equinox Runtime is not yet implemented");
		case Felix:
			if(felix == null) {
				throw new IllegalStateException("unresolved runtime: " + OsgiRuntime.Felix);
			} else {
				bundles.add(felix);
				if(equinox != null || kfish != null) {
					for(Iterator<Bundle> iter = bundles.iterator(); iter.hasNext(); ) {
						Bundle bundle = iter.next();
						if(bundle == equinox) {
							iter.remove();
							equinox = null;
							if(kfish == null) break;
						} else if(bundle == kfish) {
							iter.remove();
							kfish = null;
							if(equinox == null) break;
						}
					}
				}
			}
			break;
		case Knopplerfish:
			throw new UnsupportedOperationException("TODO: Knopplerfish Runtime is not yet implemented");
		}
	}

	/**
	 * Set the location of the working directory for this workspace.
	 * The working directory is a location that the system has write access
	 * to and can be used for building and exporting bundles.
	 * It is often the same as the workspace directory, but this is not required.
	 * @param dir the working directory (will be created if it doesn't exist)
	 */
	public void setWorkingDirectory(File dir) {
		if(dir != null && !dir.exists()) {
			dir.mkdirs();
		}
		this.workingDir = dir;
	}
	
}
