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
package org.oobium.eclipse.workspace;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;
import org.oobium.build.esp.ESourceFile;
import org.oobium.build.esp.EspDom;
import org.oobium.build.workspace.Application;
import org.oobium.build.workspace.Bundle;
import org.oobium.build.workspace.Module;
import org.oobium.build.workspace.Workspace;
import org.oobium.eclipse.OobiumPlugin;
import org.oobium.logging.Logger;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class OobiumCore {

	private static final Logger logger = Logger.getLogger(OobiumPlugin.class);
	private static final Map<String, String> formatterOptions = createFormatterOptions();
	private static final OobiumCore instance = new OobiumCore();
	
	public static void generateAssetList(IProject project, IProgressMonitor monitor) {
		logger.debug("generating asset list");
		try {
			File dir = project.getLocation().toFile();
			Bundle bundle = OobiumPlugin.getWorkspace().getBundle(dir);
			if(bundle instanceof Module) {
				File genFile = ((Module) bundle).generateAssetList();
				refresh(project, genFile, monitor);
			}
		} catch(Exception e) {
			logger.warn(e);
		}
	}

	public static boolean isEFile(IResource resource) {
		return isEFile(resource.getFileExtension());
	}
	
	public static boolean isEFile(String ext) {
		return "esp".equalsIgnoreCase(ext)
			|| "emt".equalsIgnoreCase(ext)
			|| "ess".equalsIgnoreCase(ext)
			|| "ejs".equalsIgnoreCase(ext);
	}

	public static void generate(IFile file, IProgressMonitor monitor) {
		if(logger.isLoggingDebug()) logger.debug("generating: " + file);
		try {
			File efile = file.getLocation().toFile();
			File projectDir = file.getProject().getLocation().toFile();
			Bundle bundle = OobiumPlugin.getWorkspace().getBundle(projectDir);
			if(bundle instanceof Module) {
				File genFile = ((Module) bundle).generate(efile);
				IResource resource = getResource(file.getProject(), genFile);
				format(resource, monitor);
				refresh(file.getProject(), genFile.getParentFile(), monitor);
			}
		} catch(Exception e) {
			logger.warn(e);
		}
	}

	public static ESourceFile generate(IFile file, String source) {
		File efile = file.getLocation().toFile();
		File projectDir = file.getProject().getLocation().toFile();
		Bundle bundle = OobiumPlugin.getWorkspace().getBundle(projectDir);
		if(bundle instanceof Module) {
			return ((Module) bundle).generate(efile, source);
		}
		return null;
	}

	public static void generateMailer(IFile file, IProgressMonitor monitor) {
		if(logger.isLoggingDebug()) logger.debug("generating mailer: " + file);
		try {
			File mailer = file.getLocation().toFile();
			File projectDir = file.getProject().getLocation().toFile();
			Bundle bundle = OobiumPlugin.getWorkspace().getBundle(projectDir);
			if(bundle instanceof Module) {
				Module module = (Module) bundle;
				module.generateMailer(mailer);
				refresh(file.getProject(), module.genMain, monitor);
			}
		} catch(Exception e) {
			logger.warn(e);
		}
	}

	public static void generateModel(IFile file, IProgressMonitor monitor) {
		if(logger.isLoggingDebug()) logger.debug("generating model: " + file);
		try {
			File model = file.getLocation().toFile();
			File projectDir = file.getProject().getLocation().toFile();
			Bundle bundle = OobiumPlugin.getWorkspace().getBundle(projectDir);
			if(bundle instanceof Module) {
				Workspace workspace = OobiumPlugin.getWorkspace();
				Module module = (Module) bundle;
				List<File> modified = module.generateModel(workspace, model);
				if(module.isApplication()) {
					File schema = ((Application) module).createSchema(workspace, workspace.getMode());
					IProject migrator = ResourcesPlugin.getWorkspace().getRoot().getProject(module.migrator);
					if(migrator.isOpen()) {
						refresh(migrator, schema, monitor);
					}
				}
				for(File mfile : modified) {
					refresh(file.getProject(), mfile, monitor);
				}
			}
		} catch(Exception e) {
			logger.warn(e);
		}
	}

	@SuppressWarnings("unchecked")
	private static Map<String, String> createFormatterOptions() {
		Map<String, String> options = null;
		InputStream in = OobiumBuilder.class.getClassLoader().getResourceAsStream("/org/oobium/eclipse/esp/aspencloud_formatter.xml");
		if(in != null) {
			try {
				options = new HashMap<String, String>();
				DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
				org.w3c.dom.Document doc = builder.parse(in);
				NodeList nodes = doc.getElementsByTagName("setting");
				for(int i = 0; i < nodes.getLength(); i++) {
					Node node = nodes.item(i);
					NamedNodeMap map = node.getAttributes();
					Node key = map.getNamedItem("id");
					Node val = map.getNamedItem("value");
					options.put(key.getTextContent(), val.getTextContent());
				}
				// initialize the compiler settings to be able to format 1.6 code
				options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_6);
				options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_6);
				options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_6);
				return options;
			} catch(ParserConfigurationException e) {
				Logger.getLogger(OobiumPlugin.class).error(e);
			} catch(SAXException e) {
				Logger.getLogger(OobiumPlugin.class).error(e);
			} catch(IOException e) {
				Logger.getLogger(OobiumPlugin.class).error(e);
			}
		}
		
		return DefaultCodeFormatterConstants.getEclipseDefaultSettings();
	}
	
	public static void format(IProject project, File file, IProgressMonitor monitor) {
		format(getResource(project, file));
	}
	
	private static void format(IResource resource) {
		if(!(resource instanceof IFile)) {
			return;
		}
		
		IFile file = (IFile) resource;
		
		// instantiate the default code formatter with the given options
		final CodeFormatter codeFormatter = ToolFactory.createCodeFormatter(formatterOptions);

		StringBuilder builder = new StringBuilder();
		try {
			String line;
			BufferedReader reader = new BufferedReader(new InputStreamReader(file.getContents()));
			while((line = reader.readLine()) != null) {
				builder.append(line).append('\n');
			}
			reader.close();
		} catch(CoreException e) {
			logger.warn(e);
		} catch(IOException e) {
			logger.warn(e);
		}
		String source = builder.toString();

		TextEdit edit = codeFormatter.format(CodeFormatter.K_COMPILATION_UNIT, source, 0, source.length(), 0, System.getProperty("line.separator"));

		if(edit != null) {
			IDocument document = new Document(source);
			try {
				edit.apply(document);
				file.setContents(new ByteArrayInputStream(document.get().getBytes()), true, false, null);
			} catch(MalformedTreeException e) {
				logger.warn(e);
			} catch(BadLocationException e) {
				logger.warn(e);
			} catch(CoreException e) {
				logger.warn(e);
			}
		}
	}
	
	public static void format(IResource resource, IProgressMonitor monitor) {
		if(resource instanceof IFile) {
			try {
				resource.refreshLocal(IResource.DEPTH_ONE, monitor);
				format(resource);
			} catch(CoreException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void generate(IProject project, IProgressMonitor monitor) {
		File dir = project.getLocation().toFile();
		Module module = OobiumPlugin.getWorkspace().getModule(dir);
		if(module != null) {
			if(!module.generate(OobiumPlugin.getWorkspace()).isEmpty()) {
				try {
					project.refreshLocal(IResource.DEPTH_INFINITE, monitor);
				} catch(CoreException e) {
					logger.warn(e);
				}
			}
		}
	}
	
	public static EspDom get(IDocument document) {
		return instance.getDom(document);
	}
	
	public static Module getModule(IFile file) {
		File f = file.getLocation().toFile();
		while(f != null) {
			Bundle bundle = OobiumPlugin.getWorkspace().getBundle(f);
			if(bundle != null) {
				return (bundle instanceof Module) ? (Module) bundle : null;
			}
			f = f.getParentFile();
		}
		return null;
	}
	
	public static IResource getResource(IProject project, File file) {
		if(file != null) {
			int len = project.getLocation().toOSString().length();
			String name = file.getAbsolutePath().substring(len + 1);
			if(file.isDirectory()) {
				return project.getFolder(name);
			}
			return project.getFile(name);
		}
		return null;
	}
	
	public static void refresh(IProject project, File file, IProgressMonitor monitor) {
		IResource resource = getResource(project, file);
		try {
			resource.refreshLocal(file.isDirectory() ? IResource.DEPTH_INFINITE : IResource.DEPTH_ONE, monitor);
		} catch(CoreException e) {
			logger.warn(e);
		}
	}
	
	public static EspDom remove(IDocument document) {
		return instance.removeDom(document);
	}

	public static void remove(IFile file, IProgressMonitor monitor) {
		if(logger.isLoggingDebug()) logger.debug("removing generated file for: " + file);
		try {
			IProject project = file.getProject();
			Bundle bundle = OobiumPlugin.getWorkspace().getBundle(project.getLocation().toFile());
			if(bundle instanceof Module) {
				File[] changed = ((Module) bundle).destroy(file.getLocation().toFile());
				for(File c : changed) {
					File p = c.getParentFile();
					if(p.isDirectory()) {
						refresh(project, p, monitor);
					}
				}
			}
		} catch(Exception e) {
			logger.warn(e);
		}
	}
	
	private Map<IDocument, EspDom> domMap;
	private Set<IDocument> changed;
	private IDocumentListener listener;
	
	private OobiumCore() {
		domMap = new HashMap<IDocument, EspDom>();
		changed = new HashSet<IDocument>();
		listener = new IDocumentListener() {
			@Override
			public void documentAboutToBeChanged(DocumentEvent event) {
				addChanged(event.getDocument());
			}
			@Override
			public void documentChanged(DocumentEvent event) {
				// nothing to do
			}
		};
	}

	private synchronized void addChanged(IDocument document) {
		changed.add(document);
	}
	
	private synchronized EspDom getDom(IDocument document) {
		EspDom dom = domMap.get(document);
		if(dom == null) {
			dom = new EspDom(null, document.get());
			document.addPrenotifiedDocumentListener(listener);
			domMap.put(document, dom);
		} else {
			if(changed.remove(document)) {
				dom.setSource(document.get());
			}
		}
		return dom;
	}
	
	private synchronized EspDom removeDom(IDocument document) {
		document.removeDocumentListener(listener);
		changed.remove(document);
		return domMap.remove(document);
	}

}
