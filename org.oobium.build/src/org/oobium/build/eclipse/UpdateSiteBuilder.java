package org.oobium.build.eclipse;

import static org.oobium.utils.FileUtils.EXECUTABLE;
import static org.oobium.utils.FileUtils.copy;
import static org.oobium.utils.FileUtils.createJar;
import static org.oobium.utils.FileUtils.deleteContents;
import static org.oobium.utils.FileUtils.readFile;
import static org.oobium.utils.FileUtils.toFile;
import static org.oobium.utils.FileUtils.*;
import static org.oobium.utils.StringUtils.source;
import static org.oobium.utils.literal.Map;
import static org.oobium.utils.literal.e;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.oobium.build.workspace.Bundle;
import org.oobium.build.workspace.Project;
import org.oobium.build.workspace.Version;
import org.oobium.build.workspace.Workspace;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class UpdateSiteBuilder {

	private static Workspace loadWorkspace() {
		File projectDirectory = toFile("..");
		File workingDirectory = toFile("../../studio");
		
		Workspace ws = new Workspace(workingDirectory);
		for(File file : projectDirectory.listFiles()) {
			if(file.isDirectory()) {
				ws.load(file);
			}
		}
		
		return ws;
	}

	
	public static void main(String[] args) throws Exception {
		Workspace workspace = loadWorkspace();
		UpdateSiteBuilder builder = new UpdateSiteBuilder(workspace, "org.oobium.framework.update-site");
		builder.setClean(true);
		builder.setIncludeSource(true);
		builder.setEclipse("../../../eclipse");
		builder.setSiteDirectory("../../website/org.oobium.www.update_site/assets/updates");
		builder.setAssociatedSites("http://download.eclipse.org/releases/indigo");
		builder.build();
		
		System.out.println("update-site created in " + builder.getSiteDirectory().getCanonicalPath());
	}
	
	
	private final UpdateSite site;
	private File eclipse;
	private File siteDirectory;

	private String name;
	private String[] children;
	private String[] associatedSites;
	
	private boolean clean;
	private boolean includeSource;
	
	public UpdateSiteBuilder(UpdateSite site) {
		this.site = site;
	}
	
	public UpdateSiteBuilder(Workspace workspace, String name) {
		this.site = new UpdateSite(workspace, name);
	}
	
	private String addFeature(UpdateSite site, String featureId, String featureVersion) throws Exception {
		Project featureProject = site.workspace.getProject(featureId);
		if(featureProject == null) {
			throw new Exception("feature not found: " + featureId);
		}
		
		File feature = new File(featureProject.file, "feature.xml");
		if(!feature.isFile()) {
			throw new Exception("feature.xml not found in \"" + featureProject + "\"");
		}

		// load any library bundles
		File lib = new File(featureProject.file, "lib");
		if(lib.isDirectory()) {
			site.workspace.addRepository(lib);
		}
		
		if(includeSource) {
			// load any library-source bundles
//			TODO fix mongo src bundle
//			File libsrc = new File(featureProject.file, "lib-src");
//			if(libsrc.isDirectory()) {
//				site.workspace.addRepository(libsrc);
//			}
		}
		
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
		Document doc = docBuilder.parse(feature);
		Node fnode = doc.getFirstChild();
		NamedNodeMap attrs = fnode.getAttributes();
		Node attr = attrs.getNamedItem("version");
		String version = attr.getNodeValue();
		if(!featureVersion.equals(version)) {
			throw new Exception("version in feature.xml does not equal '" + featureVersion + "', is '" + version + "' instead");
		}

		String resolvedVersion = new Version(version).resolve(site.date).toString();
		attr.setNodeValue(resolvedVersion);

		List<Bundle> bundles = new ArrayList<Bundle>();
		
		NodeList list = doc.getElementsByTagName("plugin");
		for(int i = 0; i < list.getLength(); i++) {
			Node node = list.item(i);
			if(node.getNodeType() == Node.ELEMENT_NODE) {
				attrs = node.getAttributes();
				String pluginId = attrs.getNamedItem("id").getNodeValue();
				attr = attrs.getNamedItem("version");
				version = attr.getNodeValue();
				Bundle bundle = site.workspace.getBundle(pluginId, version);
				if(bundle == null) {
					throw new Exception("no bundle: " + pluginId + "_" + version);
				}
				attr.setNodeValue(bundle.version.resolve(site.date).toString());
				bundles.add(bundle);
				site.plugins.add(bundle);
			}
		}

		site.features.add(Map(
				e("id", featureId),
				e("version", resolvedVersion),
				e("feature.xml", getXML(doc))
			));
		
		if(includeSource && hasSource(site.workspace, bundles)) {
			Element element = (Element) doc.getElementsByTagName("feature").item(0);
			element.setAttribute("id", element.getAttribute("id") + ".source");
			element.setAttribute("label", element.getAttribute("label") + " Source");

			List<Element> children = new ArrayList<Element>();
			for(int i = 0; i < list.getLength(); i++) {
				Node node = list.item(i);
				if(node.getNodeType() == Node.ELEMENT_NODE) {
					element = (Element) node;
					String id = element.getAttribute("id");
					Bundle bundle = site.workspace.getBundle(id);
					if(bundle != null && hasSource(site.workspace, bundle)) {
						element.setAttribute("id", id + ".source");
					} else {
						children.add(element);
					}
				}
			}
			
			for(Element child : children) {
				child.getParentNode().removeChild(child);
			}
			
			list = doc.getElementsByTagName("requires");
			for(int i = 0; i < list.getLength(); i++) {
				Node node = list.item(i);
				if(node.getNodeType() == Node.ELEMENT_NODE) {
					node.getParentNode().removeChild(node);
				}
			}
			
			site.features.add(Map(
					e("id", featureId),
					e("version", resolvedVersion),
					e("feature.xml", getXML(doc)),
					e("source", "true")
				));
		}
		
		return resolvedVersion;
	}
	
	private boolean hasSource(Workspace workspace, List<Bundle> bundles) throws IOException {
		for(Bundle bundle : bundles) {
			if(hasSource(workspace, bundle)) {
				return true;
			}
		}
		return false;
	}

	private boolean hasSource(Workspace workspace, Bundle bundle) throws IOException {
		if(bundle.hasSource()) {
			return true;
		}
		Project src = workspace.getProject(bundle.getSourceName());
		if(src != null) {
			return true;
		}
		return false;
	}

	public void build() throws Exception {
		if(siteDirectory == null) {
			siteDirectory = new File(site.workspace.getWorkingDirectory(), site.file.getParentFile().getName());
		}
		if(siteDirectory.exists()) {
			if(clean) {
				deleteContents(siteDirectory);
			} else {
				throw new IllegalStateException("site directory already exists");
			}
		} else {
			siteDirectory.mkdirs();
		}

		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
		Document doc = docBuilder.parse(site.file);
		Node siteNode = doc.getElementsByTagName("site").item(0);
		Node indentNode = siteNode.getFirstChild();
		String indent = (indentNode.getNodeType() == Node.TEXT_NODE) ? indentNode.getTextContent().replace("\n", "") : "\t";
		NodeList list = doc.getElementsByTagName("feature");
		for(int i = 0; i < list.getLength(); i++) {
			Node node = list.item(i);
			if(node.getNodeType() == Node.ELEMENT_NODE) {
				Element element = (Element) node;
				String url = element.getAttribute("url");
				if(!url.contains("://")) {
					String id = element.getAttribute("id");
					String version = element.getAttribute("version");
					
					String resolvedVersion = addFeature(site, id, version);
					
					element.setAttribute("version", resolvedVersion);
					if(url.endsWith(version + ".jar")) {
						element.setAttribute("url", url.substring(0, url.length() - (version.length() + 4)) + resolvedVersion + ".jar");
					}
					
					if(includeSource && hasSourceFeature(id, resolvedVersion)) {
						Element source = doc.createElement("feature");
						source.setAttribute("id", id + ".source");
						source.setAttribute("version", resolvedVersion);
						if(url.endsWith(version + ".jar")) {
							source.setAttribute("url", url.substring(0, url.length() - (version.length() + 5)) + ".source_" + resolvedVersion + ".jar");
						} else {
							source.setAttribute("url", element.getAttribute("url"));
						}
						NodeList categories = element.getElementsByTagName("category");
						if(categories.getLength() > 0) {
							source.appendChild(doc.createTextNode("\n" + indent + indent));
							Element category = doc.createElement("category");
							category.setAttribute("name", ((Element) categories.item(0)).getAttribute("name"));
							source.appendChild(category);
							source.appendChild(doc.createTextNode("\n" + indent));
						}
						siteNode.insertBefore(source, element);
						siteNode.insertBefore(doc.createTextNode("\n" + indent), element);
						i++;
					}
				}
			}
		}

		File siteFile = new File(siteDirectory, "site.xml");
		writeFile(siteFile, getXML(doc));

		if(!site.features.isEmpty() && !site.plugins.isEmpty()) {
			File features = new File(siteDirectory, "features");
			for(Map<String, String> feature : site.features) {
				if(feature.containsKey("source")) {
					String name = feature.get("id") + ".source_" + feature.get("version") + ".jar";
					createJar(new File(features, name), site.date.getTime(), new Object[][] {
						new String[] {
								"feature.xml",
								feature.get("feature.xml")
						}
					});
				} else {
					Project project = site.workspace.getProject(feature.get("id"));
					Map<String, File> files = project.getBuildFiles();
					Object[][] srcs = new Object[files.size()][2];
					int i = 0;
					for(Entry<String, File> entry : files.entrySet()) {
						srcs[i][0] = entry.getKey();
						if("feature.xml".equals(srcs[i][0])) {
							srcs[i][1] = feature.get("feature.xml");
						} else {
							srcs[i][1] = readFile(entry.getValue()).toString();
						}
						i++;
					}
					String name = project.name + "_" + feature.get("version") + ".jar";
					createJar(new File(features, name), site.date.getTime(), srcs);
				}
			}

			// Compiler compiler = new Compiler(workspace, site.plugins);
			// compiler.setClean(clean);
			// compiler.compile();

			File plugins = new File(siteDirectory, "plugins");
			plugins.mkdirs();
			for(Bundle plugin : site.plugins) {
				Version version = plugin.version.resolve(site.date);
				plugin.createJar(plugins, version);
				if(includeSource) {
					if(plugin.hasSource()) {
						plugin.createSourceJar(plugins, version);
					} else {
						Project src = site.workspace.getProject(plugin.getSourceName());
						if(src != null) {
							copy(src.file, plugins);
						}
					}
				}
			}
		}
		
		if(eclipse != null && eclipse.isDirectory()) {
			String publisher = eclipse.getCanonicalPath() + File.separator + "plugins" + File.separator + "org.eclipse.equinox.launcher_*.jar";
			String repo = siteDirectory.getCanonicalPath();
			File tmp = writeFile(
					site.workspace.getWorkingDirectory(),
					"build." + ((File.separatorChar == '\\') ? "bat" : "sh"),
					"java -jar " + publisher +
						" -application org.eclipse.equinox.p2.publisher.UpdateSitePublisher" +
						" -metadataRepository file:" + repo +
						" -artifactRepository file:" + repo +
						" -source " + repo +
						" -configs ALL" +
						" -compress",
					EXECUTABLE
				);
			System.out.print("generating p2 repository data...");
			ProcessBuilder pb = new ProcessBuilder(tmp.getCanonicalPath());
			try {
				Process process = pb.start();
				new StreamGobbler(process.getInputStream()).start();
				new StreamGobbler(process.getErrorStream()).start();
				if(process.waitFor() == 0) {
					handleAssociatedSites();
					System.out.println("exported p2 data successfully");
				} else {
					System.out.println("error exporting p2 data");
				}
			} finally {
				tmp.delete();
				siteFile.delete();
			}
		}
		
		if(children != null && children.length > 0) {
			StringBuilder sb = new StringBuilder();
			for(int i = 0; i < children.length; i++) {
				sb.append("  ").append("<child location='").append(children[i]).append("'/>");
				if(i != children.length-1) {
					sb.append('\n');
				}
			}

			String time = String.valueOf(site.date.getTime());
			String size = String.valueOf(children.length);

			writeFile(siteDirectory, "compositeContent.xml", source(
					"<?xml version='1.0' encoding='UTF-8'?>",
					"<?compositeMetadataRepository  version='1.0.0'?>",
					"<repository name='{name}'",
					" type='org.eclipse.equinox.internal.p2.artifact.repository.CompositeMetadataRepository' version='1.0.0'>",
					" <properties size='1'>",
					"  <property name='p2.timestamp' value='{timestamp}'/>",
					" </properties>",
					" <children size='{size}'>",
					sb.toString(),
					" </children>",
					"</repository>"
				).replace("{name}", name).replace("{timestamp}", time).replace("{size}", size)
			);

			writeFile(siteDirectory, "compositeArtifacts.xml", source(
					"<?xml version='1.0' encoding='UTF-8'?>",
					"<?compositeArtifactRepository version='1.0.0'?>",
					"<repository name='{name}'",
					" type='org.eclipse.equinox.internal.p2.artifact.repository.CompositeArtifactRepository' version='1.0.0'>",
					" <properties size='1'>",
					"  <property name='p2.timestamp' value='{timestamp}'/>",
					" </properties>",
					" <children size='{size}'>",
					sb.toString(),
					" </children>",
					"</repository>"
				).replace("{name}", name).replace("{timestamp}", time).replace("{size}", size)
			);
		}
	}

	private boolean hasSourceFeature(String id, String version) {
		for(Map<String, String> feature : site.features) {
			if(feature.containsKey("source") && id.equals(feature.get("id")) && version.equals(feature.get("version"))) {
				return true;
			}
		}
		return false;
	}
	
	private void handleAssociatedSites() throws Exception {
		if(associatedSites != null && associatedSites.length > 0) {
			StringBuilder sb = new StringBuilder();
			for(int i = 0; i < associatedSites.length; i++) {
				if(i != 0) sb.append("\n");
				sb.append("    <repository uri='{site}' url='{site}' type='0' options='1'/>".replace("{site}", associatedSites[i]));
				sb.append("\n    <repository uri='{site}' url='{site}' type='1' options='1'/>".replace("{site}", associatedSites[i]));
			}

			File contentJar = new File(siteDirectory, "content.jar");
			String content = readJarEntry(contentJar, "content.xml");
			if(content.contains("</references>")) {
				sb.append("\n  </references>");
				content = content.replace("  </references>", sb.toString());
			}
			else if(content.contains("<units ")){
				sb.append("\n  <units ");
				content = content.replace("  <units ", sb.toString());
			}

			createJar(contentJar, contentJar.lastModified(), new String[] { "content.xml", content } );
		}
	}
	
	public File getSiteDirectory() {
		return siteDirectory;
	}

	private String getXML(Document doc) throws Exception {
		TransformerFactory transfac = TransformerFactory.newInstance();
		Transformer trans = transfac.newTransformer();
		trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		trans.setOutputProperty(OutputKeys.INDENT, "yes");

		StringWriter sw = new StringWriter();
		StreamResult result = new StreamResult(sw);
		DOMSource source = new DOMSource(doc);
		trans.transform(source, result);
		
		return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + sw.toString();
	}

	public void setAssociatedSites(String...associatedSites) {
		this.associatedSites = associatedSites;
	}
	
	public void setClean(boolean clean) {
		this.clean = clean;
	}

	public void setChildren(String...children) {
		this.children = children;
	}
	
	public void setEclipse(String path) {
		this.eclipse = toFile(path);
	}
	
	public void setIncludeSource(boolean include) {
		this.includeSource = include;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public void setSiteDirectory(String path) {
		this.siteDirectory = toFile(path);
	}
	
}
