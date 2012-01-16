package org.oobium.eclipse.designer.editor.internal;

import static org.oobium.utils.FileUtils.copy;
import static org.oobium.utils.FileUtils.findFiles;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.oobium.build.workspace.Bundle;
import org.oobium.build.workspace.Workspace;
import org.oobium.logging.LogProvider;
import org.oobium.logging.Logger;
import org.oobium.utils.Config.Mode;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class ModuleCompiler {

	private final Logger logger;
	private Workspace workspace;
	private final Set<Bundle> bundles;
	
	private boolean clean;
	private boolean compileDependencies;
	private Mode mode;
	
	public List<Bundle> changed;
	public Map<Bundle, List<Diagnostic<? extends JavaFileObject>>> errors;
	public Map<Bundle, List<Diagnostic<? extends JavaFileObject>>> warnings;
	

	public ModuleCompiler(Workspace workspace) {
		this(workspace, workspace.getBundles());
	}
	
	public ModuleCompiler(Workspace workspace, Bundle...bundles) {
		this(workspace, Arrays.asList(bundles));
	}
	
	public ModuleCompiler(Workspace workspace, Collection<? extends Bundle> bundles) {
		this.logger = LogProvider.getLogger();
		this.workspace = workspace;
		this.bundles = new LinkedHashSet<Bundle>(bundles);
		this.mode = Mode.DEV;
		this.compileDependencies = true;
	}

	public boolean hasChanged() {
		return changed != null && !changed.isEmpty();
	}
	
	public void compile() throws IOException {
		changed = new ArrayList<Bundle>();
		Set<Bundle> bundles = new TreeSet<Bundle>();
		for(Bundle bundle : this.bundles) {
			if(clean) {
				bundle.clean();
			}
			if(compileDependencies) {
				Map<Bundle, List<Bundle>> deps = bundle.getDependencies(workspace, mode);
				bundles.addAll(deps.keySet());
				if(bundle.isMigrator()) {
					workspace.removeRuntimeBundle(bundles);
				}
			}
			bundles.add(bundle);
		}
		for(Bundle bundle : bundles) {
			if(!bundle.isJar) {
				if(compile(bundle)) {
					changed.add(bundle);
				}
			}
		}
	}
	
	/**
	 * @param bundle
	 * @return true if there are changes; false otherwise
	 * @throws IOException
	 */
	private boolean compile(Bundle bundle) throws IOException {
		logger.info("compiling {}", bundle);
		
		Map<String, File> sources = getSourceFiles(bundle);
		if(sources.isEmpty()) {
			return false;
		}
		
		boolean changes = false;
		
		if(!bundle.bin.isDirectory()) {
			if(!bundle.bin.mkdirs()) {
				throw new IOException("could not create bin folder: " + bundle.bin);
			}
		} else {
			File[] binFiles = findFiles(bundle.bin);
			if(binFiles.length != 0) {
				Map<String, File> orginalSources = new HashMap<String, File>(sources); // needed for inner classes
				
				int len = bundle.bin.getAbsolutePath().length();
				for(File binFile : binFiles) {
					String srcPath = binFile.getPath();
					if(srcPath.endsWith(".class")) {
						int ix = srcPath.lastIndexOf('$');
						if(ix == -1) {
							srcPath = srcPath.substring(len, srcPath.length()-6) + ".java";
						} else {
							srcPath = srcPath.substring(len, ix) + ".java";
							File srcFile = orginalSources.get(srcPath);
							if(srcFile == null || srcFile.lastModified() > binFile.lastModified()) {
								binFile.delete();
								logger.trace("deleted: {}", binFile);
							}
							continue;
						}
					} else {
						srcPath = srcPath.substring(len);
					}
					File srcFile = sources.get(srcPath);
					if(srcFile == null) {
						binFile.delete();
						logger.trace("deleted: {}", binFile);
						changes = true;
					} else if(srcFile.lastModified() <= binFile.lastModified()) {
						sources.remove(srcPath);
					}
				}
			}
		}
		
		if(!sources.isEmpty()) {
			// remove EFiles (.esp, .emt, .ess, .ejs)
			for(Iterator<String> iter = sources.keySet().iterator(); iter.hasNext(); ) {
				String srcPath = iter.next();
				if(!srcPath.endsWith(".java")) {
					if(!isEFile(srcPath)) {
						File srcFile = sources.get(srcPath);
						File binFile = new File(bundle.bin, srcPath);
						if(srcFile.lastModified() > binFile.lastModified()) {
							copy(srcFile, binFile);
							changes = true;
						}
					}
					iter.remove();
				}
			}
		}
		
		if(!sources.isEmpty()) {
			if(logger.isLoggingTrace()) {
				Map<String, File> map = new TreeMap<String, File>(sources);
				for(Entry<String, File> e : map.entrySet()) {
					String path = e.getValue().getAbsolutePath();
					String name = e.getKey().substring(1, e.getKey().length()-5).replaceAll(File.separator, ".");
					logger.trace("compiling: <a href=\"open file " + path + "\">" + name + "</a>");
				}
			}
			
			JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

			DiagnosticCollector<JavaFileObject> collector = new DiagnosticCollector<JavaFileObject>();
			StandardJavaFileManager fileManager = compiler.getStandardFileManager(collector, null, null);
			
			logger.debug("  getting classpath");
			List<String> options = Arrays.asList(
					"-classpath", bundle.getClasspath(workspace),
					"-d", bundle.bin.getAbsolutePath()
				);
			
			File[] files = sources.values().toArray(new File[sources.size()]);
			Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjects(files);
			
			logger.info("  running Java compiler on {}", bundle);
			boolean success = compiler.getTask(null, fileManager, collector, options, null, compilationUnits).call();
			if(success) {
				logger.info("  Java compiler completed successfully", bundle);
			} else {
				List<Diagnostic<? extends JavaFileObject>> errors = new ArrayList<Diagnostic<? extends JavaFileObject>>();
				List<Diagnostic<? extends JavaFileObject>> warnings = new ArrayList<Diagnostic<? extends JavaFileObject>>();
				List<Diagnostic<? extends JavaFileObject>> diagnostics = collector.getDiagnostics();
				for(Diagnostic<? extends JavaFileObject> d : diagnostics) {
					switch(d.getKind()) {
					case ERROR:
						errors.add(d);
						break;
					case WARNING:
					case MANDATORY_WARNING:
						warnings.add(d);
						break;
					}
				}
				if(!errors.isEmpty()) {
					if(this.errors == null) {
						this.errors = new HashMap<Bundle, List<Diagnostic<? extends JavaFileObject>>>();
					}
					this.errors.put(bundle, errors);
				}
				if(!warnings.isEmpty()) {
					if(this.warnings == null) {
						this.warnings = new HashMap<Bundle, List<Diagnostic<? extends JavaFileObject>>>();
					}
					this.warnings.put(bundle, warnings);
				}
				logger.info("  Java compiler completed with problems ({})", diagnostics.size());
			}
			fileManager.close();
			
			changes = true;
		}
		
		return changes;
	}

	public boolean hasErrors() {
		return errors != null && !errors.isEmpty();
	}
	
	public boolean hasWarnings() {
		return warnings != null && !warnings.isEmpty();
	}

	public boolean hasProblems() {
		return hasErrors() || hasWarnings();
	}
	
	// relative path to absolute file
	private Map<String, File> getSourceFiles(Bundle bundle) {
		Map<String, File> fileMap = new HashMap<String, File>();


		// get source folders that are on the classpath
		Set<String> cpes = new HashSet<String>();
		File classpath = new File(bundle.file, ".classpath");
		if(classpath.exists()) {
			try {
				DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
				Document doc = docBuilder.parse(classpath);
				NodeList list = doc.getElementsByTagName("classpathentry");
				for(int i = 0; i < list.getLength(); i++) {
					Node node = list.item(i);
					if(node.getNodeType() == Node.ELEMENT_NODE) {
						Element cpe = (Element) node;
						String kind = cpe.getAttribute("kind");
						if("src".equals(kind)) {
							String path = bundle.file.getAbsolutePath() + File.separator + cpe.getAttribute("path");
							cpes.add(path);
						}
					}
				}
			} catch(Exception e) {
				logger.warn(e);
			}
		}
		
		// get source folders from the project's build file
		//   only add the ones that are also on the classpath
		List<File> srcFolders = new ArrayList<File>();
		File buildFile = new File(bundle.file, "build.properties");
		if(buildFile.isFile()) {
			Properties props = new Properties();
			try {
				props.load(new FileReader(buildFile));
			} catch(Exception e) {
				throw new IllegalStateException(e);
			}
			String prop = props.getProperty("source..");
			if(prop != null && prop.length() > 0) {
				String[] srcs = prop.split("\\s*,\\s*");
				for(String src : srcs) {
					try {
						File folder = new File(bundle.file, src).getCanonicalFile();
						if(folder.exists() && cpes.contains(folder.getAbsolutePath())) {
							srcFolders.add(folder);
						}
					} catch(IOException e) {
						logger.warn(e.getLocalizedMessage());
					}
				}
			}
		}

		for(File folder : srcFolders) {
			File[] files = findFiles(folder);
			if(files != null) {
				for(File file : files) {
					String path = file.getPath();
					path = path.substring(folder.getAbsolutePath().length());
					fileMap.put(path, file);
				}
			}
		}

		return fileMap;
	}
	
	private boolean isEFile(String path) {
		return path.endsWith(".esp") || path.endsWith(".emt") || path.endsWith(".ess") || path.endsWith(".ejs");
	}
	
	public void setClean(boolean clean) {
		this.clean = clean;
	}
	
	public void setCompileDependencies(boolean compileDependencies) {
		this.compileDependencies = compileDependencies;
	}
	
	public void setMode(Mode mode) {
		this.mode = mode;
	}
	
}
