package org.oobium.build.eclipse;

import java.io.File;
import java.io.StringWriter;

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
import org.oobium.utils.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class UpdateSiteBuilder {

	private static Workspace loadWorkspace() {
		File projectDirectory = new File("/home/jeremyd/Oobium/workspace/framework");
		File workingDirectory = new File("/home/jeremyd/Oobium/workspace/studio");
		
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
		builder.build();
	}
	
	
	private final UpdateSite site;
	private File siteDirectory;
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
			throw new Exception("feature.xml not found in '" + featureProject);
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
				site.plugins.add(bundle);
			}
		}

		String xml = getXML(doc);
		site.features.put(featureId + "_" + resolvedVersion + ".jar", xml);
		
		if(includeSource) {
			Element element = (Element) doc.getElementsByTagName("feature").item(0);
			element.setAttribute("id", element.getAttribute("id") + ".source");
			element.setAttribute("label", element.getAttribute("label") + " Source");
			for(int i = 0; i < list.getLength(); i++) {
				Node node = list.item(i);
				if(node.getNodeType() == Node.ELEMENT_NODE) {
					element = (Element) node;
					element.setAttribute("id", element.getAttribute("id") + ".source");
				}
			}
			
			xml = getXML(doc);
			site.features.put(featureId + ".source_" + resolvedVersion + ".jar", xml);
		}
		
		return resolvedVersion;
	}

	private void build() throws Exception {
		siteDirectory = new File(site.workspace.getWorkingDirectory(), site.file.getParentFile().getName());
		if(siteDirectory.exists()) {
			if(clean) {
				FileUtils.deleteContents(siteDirectory);
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
					if(includeSource) {
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
		FileUtils.writeFile(siteFile, getXML(doc));

		if(!site.features.isEmpty() && !site.plugins.isEmpty()) {
			File features = new File(siteDirectory, "features");
			for(String feature : site.features.keySet()) {
				File jar = new File(features, feature);
				FileUtils.createJar(jar, site.date.getTime(), new String[][] {
					new String[] {
							"feature.xml",
							site.features.get(feature)
					}
				});
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
					plugin.createSourceJar(plugins, version);
				}
			}
		}
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
		
		return sw.toString();
	}

	public void setClean(boolean clean) {
		this.clean = clean;
	}

	public void setIncludeSource(boolean include) {
		this.includeSource = include;
	}
	
}