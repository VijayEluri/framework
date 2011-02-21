package org.oobium.build.workspace;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class AndroidApp extends Project {

	public AndroidApp(File file) {
		super(Type.Android, file, null);
	}
	
	@Override
	protected File setManifest() {
		return new File(file, "AndroidManifest.xml");
	}
	
	@Override
	protected File setMain() {
		if(manifest != null && manifest.isFile()) {
			try {
				DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
				Document doc = docBuilder.parse(manifest);
				String path = ((Element) doc.getFirstChild()).getAttribute("package");
				return new File(src, path.replace('.', File.separatorChar));
			} catch(Exception e) {
				logger.warn(e);
			}
		}
		return null;
	}

	public boolean addActivity(String name) {
		if(manifest != null && manifest.isFile()) {
			try {
				DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
				Document doc = docBuilder.parse(manifest);
				NodeList lists = doc.getElementsByTagName("application");
				if(lists.getLength() > 0) {
					for(int i = 0; i < lists.getLength(); i++) {
						Node node = lists.item(i);
						if(node.getNodeType() == Node.ELEMENT_NODE) {
							NodeList l2 = ((Element) node).getElementsByTagName("activity");
							if(l2.getLength() > 0) {
								for(int j = 0; j < l2.getLength(); j++) {
									Node n2 = l2.item(j);
									if(n2.getNodeType() == Node.ELEMENT_NODE) {
										String n = ((Element) n2).getAttribute("android:name");
										if(n.equals(name)) {
											return true;
										}
									}
								}
							}
						}
					}
				}
				lists.item(lists.getLength()-1).appendChild(doc.createTextNode("\t"));
				Element element = doc.createElement("activity");
				element.setAttribute("android:name", name);
				lists.item(lists.getLength()-1).appendChild(element);
				lists.item(lists.getLength()-1).appendChild(doc.createTextNode("\n\t"));
				write(manifest, doc);
				return true;
			} catch(Exception e) {
				logger.warn(e);
			}
		}
		return false;
	}

	public boolean addPermission(String name) {
		if(manifest != null && manifest.isFile()) {
			try {
				DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
				Document doc = docBuilder.parse(manifest);
				NodeList lists = doc.getElementsByTagName("uses-permission");
				if(lists.getLength() > 0) {
					for(int i = 0; i < lists.getLength(); i++) {
						Node node = lists.item(i);
						if(node.getNodeType() == Node.ELEMENT_NODE) {
							String n = ((Element) node).getAttribute("android:name");
							if(n.equals(name)) {
								return true;
							}
						}
					}
				}
				Element man = (Element) doc.getFirstChild();
				man.appendChild(doc.createTextNode("\n\t"));
				Element element = doc.createElement("uses-permission");
				element.setAttribute("android:name", name);
				man.appendChild(element);
				write(manifest, doc);
				return true;
			} catch(Exception e) {
				logger.warn(e);
			}
		}
		return false;
	}

}
