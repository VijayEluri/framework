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

import static org.oobium.client.Client.client;
import static org.oobium.utils.CharStreamUtils.find;
import static org.oobium.utils.CharStreamUtils.findAll;
import static org.oobium.utils.FileUtils.findFiles;
import static org.oobium.utils.FileUtils.readFile;
import static org.oobium.utils.FileUtils.writeFile;
import static org.oobium.utils.StringUtils.join;
import static org.oobium.utils.coercion.TypeCoercer.coerce;
import static org.oobium.utils.literal.Map;
import static org.oobium.utils.literal.e;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.oobium.build.BuildBundle;
import org.oobium.client.Client;
import org.oobium.client.ClientResponse;
import org.oobium.logging.Logger;
import org.oobium.utils.FileUtils;
import org.oobium.utils.StringUtils;
import org.oobium.utils.literal;
import org.oobium.utils.Config.Mode;
import org.oobium.utils.json.IConverter;
import org.oobium.utils.json.JsonBuilder;
import org.oobium.utils.json.JsonUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Bundle implements Comparable<Bundle> {

	public enum Type {
		Application, Module, Migrator, TestSuite, Bundle, Fragment
	}

	private static Logger slogger = Logger.getLogger(BuildBundle.class);

	public static Bundle create(File file) {
		Manifest manifest = manifest(file);
		if(manifest != null) {
			Type type = parseType(manifest);
			switch(type) {
			case Application:
				return new Application(type, file, manifest);
			case Module:
				return new Module(type, file, manifest);
			case Migrator:
				return new Migrator(type, file, manifest);
			case TestSuite:
				return new TestSuite(type, file, manifest);
			case Bundle:
				return new Bundle(type, file, manifest);
			case Fragment:
				return new Fragment(type, file, manifest);
			}
		}
		return null;
	}

	private static String createPath(String name, Version version) {
		StringBuilder sb = new StringBuilder();
		sb.append("/bundles/").append(name);
		if(version != null) {
			sb.append('_').append(version);
		}
		return sb.toString();
	}

	@Deprecated
	public static List<Bundle> deploy(String domain, int port, Bundle... bundles) {
		List<Bundle> deployed = new ArrayList<Bundle>();
		Client client = new Client(domain, port);
		for(Bundle bundle : bundles) {
			ClientResponse response = client.get("/bundles/" + bundle.name);
			if(response.isSuccess()) {
				String location = ("file:" + bundle.file.getAbsolutePath());
				List<Object> list = JsonUtils.toList(response.getBody());
				if(list.isEmpty()) {
					response = client.post("/bundles", literal.Map("location", location));
					if(response.isSuccess()) {
						deployed.add(bundle);
						slogger.debug("posted " + location + ": " + response.getStatus());
					} else {
						slogger.debug("posted " + location + ": " + response.getStatus() + "\n" + response.getBody());
						if(response.exceptionThrown()) {
							slogger.debug(response.getException());
						}
					}
				} else {
					Map<?, ?> map = (Map<?, ?>) list.get(0);
					int id = coerce(map.get("id"), int.class);
					if(!bundle.version.equals(map.get("version"))) {
						response = client.put("/bundles/" + id, literal.Map("location", location));
						if(response.isSuccess()) {
							deployed.add(bundle);
							slogger.debug("put /bundles/" + id + ": " + response.getStatus());
						} else {
							slogger.debug("put /bundles/" + id + ": " + response.getStatus() + "\n" + response.getBody());
							if(response.exceptionThrown()) {
								slogger.debug(response.getException());
							}
						}
					} else {
						slogger.debug("skipping /bundles/" + id);
					}
				}
			}
		}
		return deployed;
	}

	public static boolean install(String domain, int port, Bundle... bundles) {
		JsonBuilder builder = JsonBuilder.jsonBuilder(new IConverter() {
			@Override
			public Object convert(Object object) {
				if(object instanceof Bundle) {
					return "file:" + ((Bundle) object).file.getAbsolutePath();
				}
				return object;
			}
		});
		String location = builder.toJson(bundles);
		ClientResponse response = client(domain, port).post("/bundles", literal.Map("location", location));
		if(response.isSuccess()) {
			if(slogger.isLoggingDebug()) {
				slogger.debug("posted " + location + ": " + response.getStatus());
			}
			return true;
		} else {
			if(slogger.isLoggingDebug()) {
				slogger.debug("posted " + location + ": " + response.getStatus());
				if(response.hasBody()) {
					slogger.debug(response.getBody());
				}
				if(response.exceptionThrown()) {
					slogger.debug(response.getException());
				}
			}
			return false;
		}
	}

	public static boolean isDeployed(String domain, int port, Bundle bundle) {
		return isDeployed(domain, port, bundle.name, bundle.version);
	}

	public static boolean isDeployed(String domain, int port, String name) {
		return isDeployed(domain, port, name, null);
	}

	public static boolean isDeployed(String domain, int port, String name, Version version) {
		Client client = new Client(domain, port);
		ClientResponse response = client.get("/bundles/" + name);
		if(response.isSuccess()) {
			List<Object> list = JsonUtils.toList(response.getBody());
			if(!list.isEmpty()) {
				for(Object o : list) {
					if(version == null || version.equals(((Map<?, ?>) o).get("version"))) {
						return true;
					}
				}
			}
		}
		return false;
	}

	private static Manifest manifest(File bundle) {
		if(bundle.isDirectory()) {
			File file = new File(bundle, "META-INF" + File.separator + "MANIFEST.MF");
			if(file.isFile()) {
				FileInputStream fis = null;
				try {
					fis = new FileInputStream(file);
					return new Manifest(fis);
				} catch(Exception e) {
					// throw away
				} finally {
					if(fis != null) {
						try {
							fis.close();
						} catch(IOException e) {
							// throw away
						}
					}
				}
			}
		}
		if(bundle.isFile()) {
			JarFile jar = null;
			try {
				jar = new JarFile(bundle);
				return jar.getManifest();
			} catch(Exception e) {
				// throw away
			} finally {
				if(jar != null) {
					try {
						jar.close();
					} catch(IOException e) {
						// throw away
					}
				}
			}
		}
		return null;
	}

	private static Type parseType(Manifest manifest) {
		Attributes attrs = manifest.getMainAttributes();
		
		String name = (String) attrs.getValue("Oobium-Type");
		if(name != null) {
			String type = StringUtils.camelCase(name);
			try {
				return Type.valueOf(type);
			} catch(Exception e) {
				// discard and fall through
			}
		}
		
		Object frag = manifest.getMainAttributes().getValue("Fragment-Host");
		
		return (frag != null) ? Type.Fragment : Type.Bundle;
	}

	public static void refresh(String domain, int port) {
		client(domain, port).post("refresh");
	}

	public static boolean start(String domain, int port, Bundle bundle) {
		return start(domain, port, bundle.name, bundle.version);
	}

	public static boolean start(String domain, int port, List<Bundle> bundles) {
		return updateState(domain, port, bundles, "32");
	}

	public static boolean start(String domain, int port, String name) {
		return start(domain, port, name, null);
	}

	public static boolean start(String domain, int port, String name, Version version) {
		return updateState(domain, port, createPath(name, version), "32");
	}

	public static boolean stop(String domain, int port, Bundle bundle) {
		return stop(domain, port, bundle.name, bundle.version);
	}

	public static boolean stop(String domain, int port, List<Bundle> bundles) {
		return updateState(domain, port, bundles, "4");
	}

	public static boolean stop(String domain, int port, String name) {
		return stop(domain, port, name, null);
	}

	public static boolean stop(String domain, int port, String name, Version version) {
		return updateState(domain, port, createPath(name, version), "4");
	}

	public static boolean uninstall(String domain, int port, Bundle... bundles) {
		JsonBuilder builder = JsonBuilder.jsonBuilder(new IConverter() {
			@Override
			public Object convert(Object object) {
				if(object instanceof Bundle) {
					Bundle bundle = (Bundle) object;
					return bundle.name + "_" + bundle.version;
				}
				return object;
			}
		});
		String names = builder.toJson(bundles);
		ClientResponse response = client(domain, port).delete("/bundles", literal.Map("bundles", names));
		if(response.isSuccess()) {
			if(slogger.isLoggingDebug()) {
				slogger.debug("delete /bundles: " + response.getStatus());
			}
			return true;
		} else {
			if(slogger.isLoggingDebug()) {
				slogger.debug("delete /bundles: " + response.getStatus() + "\n" + response.getBody());
				if(response.exceptionThrown()) {
					slogger.debug(response.getException());
				}
			}
			return false;
		}
	}

	public static boolean uninstall(String domain, int port, String name) {
		return uninstall(domain, port, name, null);
	}

	public static boolean uninstall(String domain, int port, String name, Version version) {
		String path = createPath(name, version);
		ClientResponse response = client(domain, port).delete(path);
		if(response.isSuccess()) {
			if(slogger.isLoggingDebug()) {
				slogger.debug("delete " + path + ": " + response.getStatus());
			}
			return true;
		} else {
			if(slogger.isLoggingDebug()) {
				slogger.debug("delete " + path + ": " + response.getStatus() + "\n" + response.getBody());
				if(response.exceptionThrown()) {
					slogger.debug(response.getException());
				}
			}
			return false;
		}
	}

	public static boolean uninstall(String domain, int port, String[] names) {
		ClientResponse response = client(domain, port).delete("/bundles", literal.Map("bundles", JsonUtils.toJson(names)));
		if(response.isSuccess()) {
			if(slogger.isLoggingDebug()) {
				slogger.debug("delete /bundles: " + response.getStatus());
			}
			return true;
		} else {
			if(slogger.isLoggingDebug()) {
				slogger.debug("delete /bundles: " + response.getStatus() + "\n" + response.getBody());
				if(response.exceptionThrown()) {
					slogger.debug(response.getException());
				}
			}
			return false;
		}
	}

	public static boolean update(String domain, int port, Bundle bundle, String location) {
		String path = "/bundles/" + bundle.name;
		ClientResponse response = client(domain, port).put(path, literal.Map("location", location));
		if(response.isSuccess()) {
			if(slogger.isLoggingDebug()) {
				slogger.debug("put " + path + ": " + response.getStatus());
			}
			return true;
		} else {
			if(slogger.isLoggingDebug()) {
				slogger.debug("put " + path + ": " + response.getStatus() + "\n" + response.getBody());
				if(response.exceptionThrown()) {
					slogger.debug(response.getException());
				}
			}
			return false;
		}
	}

	private static boolean updateState(String domain, int port, List<Bundle> bundles, String state) {
		JsonBuilder builder = JsonBuilder.jsonBuilder(new IConverter() {
			@Override
			public Object convert(Object object) {
				if(object instanceof Bundle) {
					Bundle bundle = (Bundle) object;
					return bundle.name + "_" + bundle.version;
				}
				return object;
			}
		});
		String names = builder.toJson(bundles);
		ClientResponse response = client(domain, port).put("/bundles", Map(e("state", state), e("bundles", names)));
		if(response.isSuccess()) {
			if(slogger.isLoggingDebug()) {
				slogger.debug("put /bundles: " + response.getStatus());
			}
			return true;
		} else {
			if(slogger.isLoggingDebug()) {
				slogger.debug("put /bundles: " + response.getStatus() + "\n" + response.getBody());
				if(response.exceptionThrown()) {
					slogger.debug(response.getException());
				}
			}
			return false;
		}
	}

	private static boolean updateState(String domain, int port, String path, String state) {
		ClientResponse response = client(domain, port).put(path, literal.Map("state", state));
		if(response.isSuccess()) {
			if(slogger.isLoggingDebug()) {
				slogger.debug("put " + path + ": " + response.getStatus());
			}
			return true;
		} else {
			if(slogger.isLoggingDebug()) {
				slogger.debug("put " + path + ": " + response.getStatus() + "\n" + response.getBody());
				if(response.exceptionThrown()) {
					slogger.debug(response.getException());
				}
			}
			return false;
		}
	}

	
	protected final Logger logger;

	/**
	 * This bundle's root directory, or jar file, on the file system.
	 */
	public final File file;

	/**
	 * This bundle's manifest file, or null if this is a jarred bundle.
	 */
	public final File manifest;

	/**
	 * This bundle's symbolic name, as specified by the manifest header
	 * <code>Bundle-SymbolicName</code>
	 */
	public final String name;

	/**
	 * This bundle's version, as specified by the manifest header
	 * <code>Bundle-Version</code>
	 */
	public final Version version;

	/**
	 * This bundle's type, as specified in the custom manifest header,
	 * <code>Oobium-Type</code>.
	 * <p>
	 * May be one of the following:
	 * <ul>
	 * <li>Application</li>
	 * <li>Module</li>
	 * <li>Migration</li>
	 * <li>TestSuite</li>
	 * <li>Bundle</li>
	 * </ul>
	 * </p>
	 */
	public final Type type;

	/**
	 * A list of bundles that are required by this bundle, as specified by the
	 * manifest header <code>Require-Bundle</code>.
	 */
	public final List<RequiredBundle> requiredBundles;

	/**
	 * A list of packages that are imported by this bundle, as specified by the
	 * manifest header <code>Import-Package</code>.
	 */
	public final Set<ImportedPackage> importedPackages;

	/**
	 * A list of packages that are exported by this bundle, as specified by the
	 * manifest header <code>Export-Package</code>.
	 */
	public final Set<ExportedPackage> exportedPackages;

	/**
	 * True is this bundle is an OSGi framework bundle (exports
	 * org.osgi.framework)
	 */
	private boolean isFramework;

	/**
	 * this project's "bin" directory<br>
	 * The value is obtained from the project's "build.properties" file, if it
	 * exists; otherwise it defaults to simply "bin";
	 */
	public final File bin;

	/**
	 * this project's "src" directory
	 */
	public final File src;

	/**
	 * this project's main source directory
	 */
	public final File main;

	/**
	 * this project's activator file, as specified by the manifest header
	 * <code>Bundle-Activator</code>. Not valid for Fragments.
	 */
	public final File activator;

	/**
	 * this project's .classpath file (created by Eclipse if this project was
	 * created by Eclipse)
	 */
	public final File classpath;

	/**
	 * this project's .project file (created by Eclipse if this project was
	 * created by Eclipse)
	 */
	public final File project;

	/**
	 * true if this bundle is a jar file, false otherwise.
	 */
	public final boolean isJar;

	/**
	 * a list of services that this bundle registers with the OSGi framework
	 */
	private final String[] services;

	Bundle(Type type, File file, Manifest manifest) {
		this.logger = Logger.getLogger(BuildBundle.class);

		this.type = type;
		this.file = file;
		this.name = parseName(manifest);
		this.version = new Version((String) manifest.getMainAttributes().getValue("Bundle-Version"));
		this.requiredBundles = parseRequiredBundles(manifest);
		this.importedPackages = parseImportedPackages(manifest);
		this.exportedPackages = parseExportedPackages(manifest);
		this.services = parseServices(manifest);
		this.isJar = file.isFile() && file.getName().endsWith(".jar");
		if(isJar) {
			this.manifest = null;
			this.bin = null;
			this.src = null;
			this.main = null;
			this.activator = null;
			this.classpath = null;
			this.project = null;
		} else {
			this.manifest = new File(file, "META-INF" + File.separator + "MANIFEST.MF");
			File buildFile = new File(file, "build.properties");
			String binPath = null;
			if(buildFile.isFile()) {
				Properties props = new Properties();
				try {
					props.load(new FileReader(buildFile));
					binPath = props.getProperty("output..", "bin");
				} catch(Exception e) {
					binPath = "bin";
				}
			}
			this.bin = (binPath != null && !".".equals(binPath)) ? new File(file, binPath) : new File(file, "bin");
			this.src = new File(file, "src");
			this.main = new File(src, name.replaceAll("\\.", File.separator));
			this.activator = parseActivator(manifest);
			this.classpath = new File(file, ".classpath");
			this.project = new File(file, ".project");
		}
	}

	/**
	 * @see #getDependencies(Mode)
	 */
	protected void addDependencies(Workspace workspace, Mode mode, Set<Bundle> dependencies) {
		if(requiredBundles != null) {
			for(RequiredBundle requiredBundle : requiredBundles) {
				Bundle bundle = workspace.getBundle(requiredBundle);
				if(bundle != null) {
					if(bundle.name.startsWith("org.oobium.build")) {
						System.out.println("   !!!   requiredBundle: " + this);
					}
					dependencies.add(bundle);
					bundle.addDependencies(workspace, mode, dependencies);
				}
			}
		}
		if(importedPackages != null) {
			for(ImportedPackage importedPackage : importedPackages) {
				Bundle bundle = workspace.getBundle(importedPackage);
				if(bundle != null && bundle != this && !dependencies.contains(bundle)) {
					if(bundle.name.startsWith("org.oobium.build")) {
						System.out.println("   !!!   Import-Package: " + importedPackage + " in: " + this);
					}
					dependencies.add(bundle);
					bundle.addDependencies(workspace, mode, dependencies);
				}
			}
		}
	}

	/**
	 * @see #getDependencies()
	 */
	protected final void addDependencies(Workspace workspace, Set<Bundle> dependencies) {
		if(requiredBundles != null) {
			for(RequiredBundle requiredBundle : requiredBundles) {
				Bundle bundle = workspace.getBundle(requiredBundle);
				if(bundle != null) {
					dependencies.add(bundle);
					bundle.addDependencies(workspace, dependencies);
				}
			}
		}
		if(importedPackages != null) {
			for(ImportedPackage importedPackage : importedPackages) {
				Bundle bundle = workspace.getBundle(importedPackage);
				if(bundle != null && bundle != this && !dependencies.contains(bundle)) {
					dependencies.add(bundle);
					bundle.addDependencies(workspace, dependencies);
				}
			}
		}
	}

	private void addExportedPackage(String str, Set<ExportedPackage> packages) {
		ExportedPackage exportedPackage = new ExportedPackage(str);
		packages.add(exportedPackage);
		if(exportedPackage.isFramework()) {
			isFramework = true;
		}
	}

	public void addExportPackage(String packageName) {
		String exportStr = "Export-Package: ";
		StringBuilder sb = readFile(manifest);
		char[] ca = new char[sb.length()];
		sb.getChars(0, sb.length(), ca, 0);
		int ix = findAll(ca, 0, ca.length - 1, exportStr.toCharArray());
		if(ix == -1) {
			ix = findAll(ca, 0, ca.length - 1, "Import-Package".toCharArray());
			sb.insert(ix, exportStr);
			sb.insert(ix + exportStr.length(), packageName);
			sb.insert(ix + exportStr.length() + packageName.length(), "\n");
		} else {
			int s1 = ix + exportStr.length();
			int s2 = s1;
			while(s2 != -1) {
				s2 = find(ca, '\n', s2 + 1, ca.length - 1);
				if(s2 != -1 && (s2 >= ca.length - 1 || ca[s2 + 1] != ' ')) {
					break;
				}
			}
			String[] exports = new String(ca, s1, s2 - s1 + 1).trim().split("\\s*,\\s*");
			for(String export : exports) {
				if(packageName.equals(export.trim())) {
					// already exported so exit without writing anything
					return;
				}
			}
			exports = Arrays.copyOf(exports, exports.length + 1);
			exports[exports.length - 1] = packageName;
			Arrays.sort(exports);
			sb.replace(s1, s2, join(exports, ",\n "));
		}

		ExportedPackage exportedPackage = new ExportedPackage(packageName + ";" + version.toString(true));
		exportedPackages.add(exportedPackage);

		writeFile(manifest, sb.toString());
	}

	public boolean addImportPackage(String packageName) {
		String importStr = "Import-Package: ";
		StringBuilder sb = readFile(manifest);
		char[] ca = new char[sb.length()];
		sb.getChars(0, sb.length(), ca, 0);
		int ix = findAll(ca, 0, ca.length - 1, importStr.toCharArray());
		if(ix == -1) {
			ix = findAll(ca, 0, ca.length - 1, "Export-Package".toCharArray());
			sb.insert(ix, importStr);
			sb.insert(ix + importStr.length(), packageName);
			sb.insert(ix + importStr.length() + packageName.length(), "\n");
		} else {
			int s1 = ix + importStr.length();
			int s2 = s1;
			while(s2 != -1) {
				s2 = find(ca, '\n', s2 + 1, ca.length - 1);
				if(s2 != -1 && (s2 >= ca.length - 1 || ca[s2 + 1] != ' ')) {
					break;
				}
			}
			String[] imports = new String(ca, s1, s2 - s1 + 1).trim().split("\\s*,\\s*");
			for(String impPkg : imports) {
				if(packageName.equals(impPkg.trim())) {
					// already imported so exit without writing anything
					return false;
				}
			}
			imports = Arrays.copyOf(imports, imports.length + 1);
			imports[imports.length - 1] = packageName;
			Arrays.sort(imports);
			sb.replace(s1, s2, join(imports, ",\n "));
		}

		ImportedPackage importedPackage = new ImportedPackage(packageName);
		importedPackages.add(importedPackage);

		writeFile(manifest, sb.toString());
		return true;
	}

	public boolean addNature(String nature) {
		if(project.isFile()) {
			try {
				DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
				Document doc = docBuilder.parse(project);
				NodeList lists = doc.getElementsByTagName("natures");
				if(lists.getLength() > 0) {
					for(int i = 0; i < lists.getLength(); i++) {
						Node node = lists.item(i);
						if(node.getNodeType() == Node.ELEMENT_NODE) {
							NodeList list = node.getChildNodes();
							for(int j = 0; j < list.getLength(); j++) {
								Node child = list.item(j);
								if(child.getNodeType() == Node.ELEMENT_NODE && "nature".equals(child.getNodeName())) {
									if(nature.equals(child.getFirstChild().getNodeValue())) {
										return true; // nature already present
									}
								}
							}
						}
					}
					lists.item(0).appendChild(doc.createTextNode("\t"));
					Element element = doc.createElement("nature");
					element.setTextContent(nature);
					lists.item(0).appendChild(element);
					write(project, doc);
					return true;
				}
			} catch(Exception e) {
				logger.warn(e);
			}
		}
		return false;
	}

	public void clean() {
		FileUtils.deleteContents(bin);
	}

	@Override
	public int compareTo(Bundle o) {
		int i = name.compareTo(o.name);
		return (i == 0) ? version.compareTo(o.version) : i;
	}

	public void createJar(File jar, Version version) throws IOException {
		Map<String, File> files = getBuildFiles();

		Manifest manifest = manifest(file);
		manifest.getMainAttributes().putValue("Bundle-Version", version.toString());

		FileUtils.createJar(jar, files, manifest);
	}

	public void delete() {
		FileUtils.delete(file);
	}

	public boolean exportsModels() {
		if(this instanceof Module) {
			return exportedPackages.contains(new ExportedPackage(name + ".models"));
		}
		return false;
	}

	public File getBinFile(File srcFile) {
		int len = src.getAbsolutePath().length();
		String path = srcFile.getAbsolutePath();
		if(path.endsWith(".java")) {
			path = path.substring(len, path.length() - 4) + "class";
		} else {
			path = path.substring(len);
		}
		return new File(bin, path);
	}

	public Set<File> getBinFiles(File... srcFiles) {
		return getBinFiles(Arrays.asList(srcFiles));
	}

	public Set<File> getBinFiles(List<File> srcFiles) {
		Set<File> binFiles = new HashSet<File>();

		int len = src.getAbsolutePath().length();
		for(File file : srcFiles) {
			binFiles.add(new File(bin, file.getAbsolutePath().substring(len)));
		}

		return binFiles;
	}

	/**
	 * Returns a map of all files necessary to build the jar for the given
	 * bundle. The map key is the relative path to be used in the JarEntry
	 * object (Windows style path separators are corrected). The map value is
	 * the absolute File object pointing to the class or resource file to be
	 * added to the jar.
	 * 
	 * @param bundle
	 *            the bundle for which to build the jar
	 * @return Map<String, File> all files necessary to build the jar for the
	 *         given bundle
	 */
	private Map<String, File> getBuildFiles() throws IOException {
		Map<String, File> buildFiles = new HashMap<String, File>();
		File[] files = findFiles(bin);
		int len = bin.getAbsolutePath().length() + 1;
		for(File file : files) {
			String relativePath = file.getAbsolutePath().substring(len);
			if('\\' == File.separatorChar) {
				relativePath.replaceAll("\\", "/");
			}
			buildFiles.put(relativePath, file);
		}

		File buildFile = new File(file, "build.properties");
		if(buildFile.isFile()) {
			Properties props = new Properties();
			props.load(new FileReader(buildFile));
			String[] includes = props.getProperty("bin.includes", "").split("\\s*,\\s*");
			len = file.getAbsolutePath().length() + 1;
			for(String include : includes) {
				if(!".".equals(include)) {
					File folder = new File(file, include);
					files = findFiles(folder);
					for(File file : files) {
						String relativePath = file.getAbsolutePath().substring(len);
						if('\\' == File.separatorChar) {
							relativePath.replaceAll("\\", "/");
						}
						buildFiles.put(relativePath, file);
					}
				}
			}
		}
		return buildFiles;
	}

	public String getClasspath() {
		return StringUtils.join(getClasspathEntries(), File.pathSeparatorChar);
	}

	public String getClasspath(Workspace workspace) {
		return StringUtils.join(getClasspathEntries(workspace), File.pathSeparatorChar);
	}

	public String getClasspath(Workspace workspace, Mode mode) {
		return StringUtils.join(getClasspathEntries(workspace, mode), File.pathSeparatorChar);
	}

	private void addClasspathEntries(Set<String> cpes) {
		if(classpath != null && classpath.isFile()) {
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
							String path = file.getAbsolutePath() + File.separator + cpe.getAttribute("path");
							cpes.add(path);
						}
					}
				}
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		
		if(file.isDirectory()) {
			cpes.add(file.getAbsolutePath() + File.separator + "bin");
		} else {
			cpes.add(file.getAbsolutePath());
		}
	}

	public Set<String> getClasspathEntries() {
		Set<String> cpes = new LinkedHashSet<String>();
		addClasspathEntries(cpes);
		return cpes;
	}

	public Set<String> getClasspathEntries(Workspace workspace) {
		Set<String> cpes = new LinkedHashSet<String>();
		addClasspathEntries(cpes);
		
		for(Bundle bundle : getDependencies(workspace)) {
			bundle.addClasspathEntries(cpes);
		}

		return cpes;
	}

	public Set<String> getClasspathEntries(Workspace workspace, Mode mode) {
		Set<String> cpes = new LinkedHashSet<String>();
		addClasspathEntries(cpes);
		
		for(Bundle bundle : getDependencies(workspace, mode)) {
			bundle.addClasspathEntries(cpes);
		}

		return cpes;
	}

	/**
	 * Get all classpath dependencies (those required to build this particular
	 * bundle)
	 * 
	 * @return a set of bundles that are required to build this bundle
	 */
	public final Set<Bundle> getDependencies(Workspace workspace) {
		Set<Bundle> dependencies = new TreeSet<Bundle>();
		addDependencies(workspace, dependencies);
		return dependencies;
	}

	/**
	 * Get all dependencies (those required to build and deploy this particular
	 * bundle - includes configured services)
	 * 
	 * @return a set of bundles that are required to build and deploy this
	 *         bundle
	 */
	public Set<Bundle> getDependencies(Workspace workspace, Mode mode) {
		Set<Bundle> dependencies = new TreeSet<Bundle>();
		// long start = System.currentTimeMillis();
		addDependencies(workspace, mode, dependencies);
		// logger.debug("getDependencies(" + mode + "): " +
		// (System.currentTimeMillis() - start));
		return dependencies;
	}

	public ExportedPackage getExportedPackage(String packageName) {
		if(exportedPackages != null && packageName != null) {
			for(ExportedPackage exportedPackage : exportedPackages) {
				if(exportedPackage.name.equals(packageName)) {
					return exportedPackage;
				}
			}
		}
		return null;
	}

	public Set<ExportedPackage> getExportedPackages() {
		return (exportedPackages == null) ? new HashSet<ExportedPackage>(0) : new TreeSet<ExportedPackage>(exportedPackages);
	}

	public Set<ImportedPackage> getImportedPackages() {
		return (importedPackages == null) ? new HashSet<ImportedPackage>(0) : new TreeSet<ImportedPackage>(importedPackages);
	}

	public String getName() {
		return name + "_" + version.toString(true);
	}

	public Set<String> getNatures() {
		Set<String> natures = new LinkedHashSet<String>();
		if(project.isFile()) {
			try {
				DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
				Document doc = docBuilder.parse(project);
				NodeList list = doc.getElementsByTagName("nature");
				for(int i = 0; i < list.getLength(); i++) {
					Node node = list.item(i);
					if(node.getNodeType() == Node.ELEMENT_NODE) {
						String nature = node.getFirstChild().getNodeValue();
						natures.add(nature);
					}
				}
			} catch(Exception e) {
				logger.warn(e);
			}
		}
		return natures;
	}

	public List<RequiredBundle> getRequiredBundles() {
		return (requiredBundles == null) ? new ArrayList<RequiredBundle>(0) : new ArrayList<RequiredBundle>(requiredBundles);
	}

	public boolean hasNature(String nature) {
		return getNatures().contains(nature);
	}

	/**
	 * @return true if this is a Oobium Application.
	 */
	public boolean isApplication() {
		return type == Type.Application;
	}

	/**
	 * Checks whether or not this bundle is an OSGi framework bundle, meaning
	 * that it exports the org.osgi.framework package.
	 * 
	 * @return true if this bundle is an OSGi framework bundle; false otherwise.
	 */
	public boolean isFramework() {
		return isFramework;
	}

	/**
	 * @return true if this is a Library.
	 */
	public boolean isLibrary() {
		return type == Type.Bundle;
	}

	/**
	 * @return true if this is a Oobium Migration.
	 */
	public boolean isMigration() {
		return type == Type.Migrator;
	}

	/**
	 * @return true if this is a Oobium Module or Application (Application
	 *         extends Module).
	 */
	public boolean isModule() {
		return type == Type.Module || type == Type.Application;
	}

	public boolean isService() {
		return services.length > 0;
	}

	/**
	 * @return true if this is a Oobium TestSuite.
	 */
	public boolean isTestSuite() {
		return type == Type.TestSuite;
	}

	/**
	 * Get the java package name for the given file.<br>
	 * The given file may be an actual file, or a directory.
	 */
	public String packageName(File file) {
		if(file.isFile()) {
			file = file.getParentFile();
		}
		int ix = src.getAbsolutePath().length();
		String name = file.getAbsolutePath().substring(ix + 1).replaceAll(File.separator, ".");
		return name;
	}

	private File parseActivator(Manifest manifest) {
		String name = (String) manifest.getMainAttributes().getValue("Bundle-Activator");
		if(name != null) {
			int ix = name.lastIndexOf('.');
			if(ix != -1) {
				String path = name.substring(0, ix + 1).replaceAll("\\.", File.separator);
				name = name.substring(ix + 1) + ".java";
				return new File(src, path + name);
			}
		}
		return null;
	}

	private Set<ExportedPackage> parseExportedPackages(Manifest manifest) {
		Set<ExportedPackage> packages = new TreeSet<ExportedPackage>();
		String str = (String) manifest.getMainAttributes().getValue("Export-Package");
		if(str != null && str.trim().length() > 0) {

			int s = 0;
			for(int i = 0; i < str.length(); i++) {
				char c = str.charAt(i);
				if(c == '"') {
					i++;
					while(i < str.length()) {
						if(str.charAt(i) == '"') {
							break;
						}
						i++;
					}
					if(i == str.length() - 1) {
						addExportedPackage(str.substring(s, str.length()).trim(), packages);
					}
				} else if(str.charAt(i) == ',') {
					addExportedPackage(str.substring(s, i).trim(), packages);
					s = i + 1;
				} else if(i == str.length() - 1) {
					addExportedPackage(str.substring(s, str.length()).trim(), packages);
					s = i + 1;
				}
			}
		}
		return packages;
	}

	private Set<ImportedPackage> parseImportedPackages(Manifest manifest) {
		Set<ImportedPackage> packages = new TreeSet<ImportedPackage>();
		String str = (String) manifest.getMainAttributes().getValue("Import-Package");
		if(str != null && str.trim().length() > 0) {
			int s = 0;
			for(int i = 0; i < str.length(); i++) {
				char c = str.charAt(i);
				if(c == '"') {
					i++;
					while(i < str.length()) {
						if(str.charAt(i) == '"') {
							break;
						}
						i++;
					}
					if(i == str.length() - 1) {
						packages.add(new ImportedPackage(str.substring(s, str.length()).trim()));
					}
				} else if(str.charAt(i) == ',') {
					packages.add(new ImportedPackage(str.substring(s, i).trim()));
					s = i + 1;
				} else if(i == str.length() - 1) {
					packages.add(new ImportedPackage(str.substring(s, str.length()).trim()));
					s = i + 1;
				}
			}
		}
		return packages;
	}

	private String parseName(Manifest manifest) {
		String name = (String) manifest.getMainAttributes().getValue("Bundle-SymbolicName");
		if(name != null) {
			int ix = name.indexOf(';');
			if(ix == -1) {
				return name.trim();
			} else {
				return name.substring(0, ix).trim();
			}
		}
		return "";
	}

	private List<RequiredBundle> parseRequiredBundles(Manifest manifest) {
		String str = (String) manifest.getMainAttributes().getValue("Require-Bundle");
		if(str != null && str.trim().length() > 0) {
			List<RequiredBundle> bundles = new ArrayList<RequiredBundle>();

			int s = 0;
			for(int i = 0; i < str.length(); i++) {
				char c = str.charAt(i);
				if(c == '"') {
					i++;
					while(i < str.length()) {
						if(str.charAt(i) == '"') {
							break;
						}
						i++;
					}
					if(i == str.length() - 1) {
						bundles.add(new RequiredBundle(str.substring(s, str.length()).trim()));
					}
				} else if(str.charAt(i) == ',') {
					bundles.add(new RequiredBundle(str.substring(s, i).trim()));
					s = i + 1;
				} else if(i == str.length() - 1) {
					bundles.add(new RequiredBundle(str.substring(s, str.length()).trim()));
				}
			}

			return Collections.unmodifiableList(bundles);
		}
		return null;
	}

	/**
	 * @param manifest
	 * @return an array of service names that this module registers
	 */
	private String[] parseServices(Manifest manifest) {
		String serviceHeader = (String) manifest.getMainAttributes().getValue("Oobium-Service");
		if(serviceHeader != null) {
			return serviceHeader.split(",");
		} else {
			return new String[0];
		}
	}

	public boolean removeExportPackage(String packageName) {
		String exportStr = "Export-Package: ";
		StringBuilder sb = readFile(manifest);
		char[] ca = new char[sb.length()];
		sb.getChars(0, sb.length(), ca, 0);
		int ix = findAll(ca, 0, ca.length - 1, exportStr.toCharArray());
		if(ix == -1) {
			return false;
		} else {
			int s1 = ix + exportStr.length();
			int s2 = s1;
			while(s2 != -1) {
				s2 = find(ca, '\n', s2 + 1, ca.length - 1);
				if(s2 != -1 && (s2 >= ca.length - 1 || ca[s2 + 1] != ' ')) {
					break;
				}
			}
			String[] exports = new String(ca, s1, s2 - s1 + 1).trim().split("\\s*,\\s*");
			int foundAt = -1;
			for(int i = 0; i < exports.length; i++) {
				if(packageName.equals(exports[i])) {
					foundAt = i;
					break;
				}
			}
			if(foundAt == -1) {
				return false;
			}
			String[] tmp = new String[exports.length - 1];
			System.arraycopy(exports, 0, tmp, 0, foundAt);
			System.arraycopy(exports, foundAt + 1, tmp, foundAt, tmp.length - foundAt);
			exports = tmp;
			Arrays.sort(exports);
			sb.replace(s1, s2, join(exports, ",\n "));
		}

		ExportedPackage exportedPackage = new ExportedPackage(packageName + ";" + version.toString(true));
		exportedPackages.remove(exportedPackage);

		writeFile(manifest, sb.toString());
		return true;
	}

	public boolean removeImportPackage(String packageName) {
		String exportStr = "Import-Package: ";
		StringBuilder sb = readFile(manifest);
		char[] ca = new char[sb.length()];
		sb.getChars(0, sb.length(), ca, 0);
		int ix = findAll(ca, 0, ca.length - 1, exportStr.toCharArray());
		if(ix == -1) {
			return false;
		} else {
			int s1 = ix + exportStr.length();
			int s2 = s1;
			while(s2 != -1) {
				s2 = find(ca, '\n', s2 + 1, ca.length - 1);
				if(s2 != -1 && (s2 >= ca.length - 1 || ca[s2 + 1] != ' ')) {
					break;
				}
			}
			String[] imports = new String(ca, s1, s2 - s1 + 1).trim().split("\\s*,\\s*");
			int foundAt = -1;
			for(int i = 0; i < imports.length; i++) {
				if(packageName.equals(imports[i])) {
					foundAt = i;
					break;
				}
			}
			if(foundAt == -1) {
				return false;
			}
			String[] tmp = new String[imports.length - 1];
			System.arraycopy(imports, 0, tmp, 0, foundAt);
			System.arraycopy(imports, foundAt + 1, tmp, foundAt, tmp.length - foundAt);
			imports = tmp;
			Arrays.sort(imports);
			sb.replace(s1, s2, join(imports, ",\n "));
		}

		ImportedPackage importedPackage = new ImportedPackage(packageName + ";" + version.toString(true));
		importedPackages.remove(importedPackage);

		writeFile(manifest, sb.toString());
		return true;
	}

	public boolean removeNature(String nature) {
		if(project.isFile()) {
			try {
				DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
				Document doc = docBuilder.parse(project);
				NodeList lists = doc.getElementsByTagName("natures");
				if(lists.getLength() > 0) {
					for(int i = 0; i < lists.getLength(); i++) {
						Node node = lists.item(i);
						if(node.getNodeType() == Node.ELEMENT_NODE) {
							NodeList list = node.getChildNodes();
							for(int j = 0; j < list.getLength(); j++) {
								Node child = list.item(j);
								if(child.getNodeType() == Node.ELEMENT_NODE && "nature".equals(child.getNodeName())) {
									if(nature.equals(child.getFirstChild().getNodeValue())) {
										node.removeChild(child);
										if(j > 0 && list.item(j - 1).getNodeType() == Node.TEXT_NODE) {
											node.removeChild(list.item(j - 1));
										}
										write(project, doc);
										return true;
									}
								}
							}
						}
					}
				}
				return true; // nature not present
			} catch(Exception e) {
				logger.warn(e);
			}
		}
		return false;
	}

	public boolean resolves(ImportedPackage importedPackage) {
		if(exportedPackages != null) {
			for(ExportedPackage exportedPackage : exportedPackages) {
				if(exportedPackage.resolves(importedPackage)) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean resolves(RequiredBundle requiredBundle) {
		return name.equals(requiredBundle.name) && version.resolves(requiredBundle.versionRange);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(type).append(':').append(' ').append(name).append('_').append(version);
		if(isJar) {
			sb.append(" (jarred)");
		}
		return sb.toString();
	}

	private void write(File file, Document doc) throws Exception {
		TransformerFactory transfac = TransformerFactory.newInstance();
		Transformer trans = transfac.newTransformer();
		trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		trans.setOutputProperty(OutputKeys.INDENT, "yes");

		StringWriter sw = new StringWriter();
		StreamResult result = new StreamResult(sw);
		DOMSource source = new DOMSource(doc);
		trans.transform(source, result);
		String xmlString = sw.toString();

		FileUtils.writeFile(file, xmlString);
	}

}
