package org.oobium.build.workspace;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class AndroidApp extends Project {

	public final File gen;
	public final File assets;
	public final File res;
	public final File drawable;
	public final File layout;
	
	public AndroidApp(File file) {
		super(Type.Android, file, null);
		if(isJar) {
			this.gen = null;
			this.assets = null;
			this.res = null;
			this.drawable = null;
			this.layout = null;
		} else {
			this.gen = new File(file, "gen");
			this.assets = new File(file, "assets");
			this.res = new File(file, "res");
			this.drawable = new File(res, "drawable");
			this.layout = new File(res, "layout");
		}
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
	
	public File getActivity(String name) {
		if(name != null && name.length() > 0 && manifest != null && manifest.isFile()) {
			try {
				DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
				Document doc = docBuilder.parse(manifest);
				NodeList lists = doc.getElementsByTagName("application");
				if(lists.getLength() > 0) {
					String test = (name.charAt(0) != '.') ? ("." + name) : name;
					for(int i = 0; i < lists.getLength(); i++) {
						Node node = lists.item(i);
						if(node.getNodeType() == Node.ELEMENT_NODE) {
							NodeList l2 = ((Element) node).getElementsByTagName("activity");
							if(l2.getLength() > 0) {
								for(int j = 0; j < l2.getLength(); j++) {
									Node n2 = l2.item(j);
									if(n2.getNodeType() == Node.ELEMENT_NODE) {
										String n = ((Element) n2).getAttribute("android:name");
										if(n.endsWith(test)) {
											if(n.charAt(0) == '.') {
												n = getPackage() + n;
											}
											return new File(src, n.replace('.', File.separatorChar) + ".java");
										}
									}
								}
							}
						}
					}
				}
			} catch(Exception e) {
				logger.warn(e);
			}
		}
		return null;
	}

	public File getLayout(String name) {
		if(name.startsWith("R.layout.")) {
			name = name.substring(9);
		}
		return new File(layout, name + ".xml");
	}

	public File[] getMainActivities() {
		if(name != null && name.length() > 0 && manifest != null && manifest.isFile()) {
			try {
				DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
				Document doc = docBuilder.parse(manifest);

				XPath xpath = XPathFactory.newInstance().newXPath();
				XPathExpression exp = xpath.compile("//application/activity/intent-filter/action[@name='android.intent.action.MAIN']");
				
				NodeList nodes = (NodeList) exp.evaluate(doc, XPathConstants.NODESET);
				if(nodes.getLength() > 0) {
					List<File> files = new ArrayList<File>();
					for(int i = 0; i < nodes.getLength(); i++) {
						String n = ((Element) nodes.item(i).getParentNode().getParentNode()).getAttribute("android:name");
						if(n.charAt(0) == '.') {
							n = getPackage() + n;
						}
						files.add(new File(src, n.replace('.', File.separatorChar) + ".java"));
					}
					return files.toArray(new File[files.size()]);
				}
			} catch(Exception e) {
				logger.warn(e);
			}
		}
		return new File[0];
	}

	public String getPackage() {
		if(manifest != null && manifest.isFile()) {
			try {
				DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
				Document doc = docBuilder.parse(manifest);
				return ((Element) doc.getFirstChild()).getAttribute("package");
			} catch(Exception e) {
				logger.warn(e);
			}
		}
		return null;
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

	@Override
	protected File setManifest() {
		return new File(file, "AndroidManifest.xml");
	}

}
