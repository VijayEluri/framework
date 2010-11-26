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
package org.oobium.build.gen;

import static java.util.Arrays.asList;
import static org.oobium.build.util.ProjectUtils.getGenAnnotations;
import static org.oobium.build.util.ProjectUtils.getSrcAnnotations;
import static org.oobium.utils.FileUtils.getLastModified;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.oobium.build.BuildBundle;
import org.oobium.build.workspace.Bundle;
import org.oobium.build.workspace.Module;
import org.oobium.build.workspace.Workspace;
import org.oobium.logging.Logger;
import org.oobium.utils.Config.Mode;

public class Generator {

	private final Logger logger;
	private final Workspace workspace;
	private final Set<Module> modules;

	private boolean clean;
	private Mode mode;
	
	public Generator(Workspace workspace) {
		this(workspace, workspace.getModules());
	}
	
	public Generator(Workspace workspace, Collection<? extends Module> modules) {
		this.logger = Logger.getLogger(BuildBundle.class);
		this.workspace = workspace;
		this.modules = new LinkedHashSet<Module>(modules);
	}
	
	public Generator(Workspace workspace, Module...modules) {
		this(workspace, Arrays.asList(modules));
	}
	
	/**
	 * Generate all required classes for this module and, optionally, all modules necessary to deploy it.
	 * @return a list of bundles that changed due to the build, and should be re-compiled and deployed;
	 * an empty list if there were no changes - never null
	 */
	public Map<Bundle, List<File>> generate() {
		Set<Bundle> bundles = new TreeSet<Bundle>();
		for(Module module : modules) {
			if(mode != null) {
				bundles.addAll(module.getDependencies(workspace, mode));
			}
			bundles.add(module);
		}

		Map<Bundle, List<File>> changed = new HashMap<Bundle, List<File>>();
		for(Bundle bundle : bundles) {
			if(!bundle.isJar && bundle instanceof Module) {
				Module module = (Module) bundle;
				List<File> generated = generate(module);
				if(!generated.isEmpty()) {
					changed.put(bundle, generated);
				}
			}
		}
		return changed;
	}

	private List<File> generate(Module module)  {
		List<File> generated = new ArrayList<File>();
		
		// Model files
		for(File genFile : module.findGenModels()) {
			File model = module.getSrcModel(genFile);
			if(model == null || !model.exists()) {
				genFile.delete();
			}
		}
		
		List<File> models = module.findModels();
		for(Iterator<File> iter = models.iterator(); iter.hasNext(); ) {
			File srcFile = iter.next();
			File genFile = module.getGenModel(srcFile);
			if(genFile.exists()) {
				if(srcFile.lastModified() <= genFile.lastModified()) {
					iter.remove();
				} else {
					try {
						String srcAnnotations = getSrcAnnotations(srcFile).replaceAll("\\s", "");
						String genAnnotations = getGenAnnotations(genFile).replaceAll("\\s", "");
						if(srcAnnotations.equals(genAnnotations)) {
							iter.remove();
						}
					} catch(IOException e) {
						logger.warn("failed to compare annotations:\n  " + srcFile + "\n  " + genFile, e);
					}
				}
			}
		}
		if(!models.isEmpty()) {
			generated.addAll(asList(ModelGenerator.generate(workspace, module, models)));
		}

		// View (ESP) files
		for(File genFile : module.findGenViews()) { // handles ESP, ESS, and EJS files
			File view = module.getSrcView(genFile);
			if(view == null || !view.exists()) {
				genFile.delete();
			}
		}
		
		List<File> views = module.findViews(); // just ESP files
		for(Iterator<File> iter = views.iterator(); iter.hasNext(); ) {
			File srcFile = iter.next();
			File genFile = module.getGenView(srcFile);
			if(srcFile.lastModified() <= genFile.lastModified()) {
				iter.remove();
			}
		}
		if(!views.isEmpty()) {
			generated.addAll(module.generate(views));
		}
		
		List<File> styles = module.findStyleSheets(); // just ESS files
		for(Iterator<File> iter = styles.iterator(); iter.hasNext(); ) {
			File srcFile = iter.next();
			File genFile = module.getGenFile(srcFile);
			if(srcFile.lastModified() <= genFile.lastModified()) {
				iter.remove();
			}
		}
		if(!styles.isEmpty()) {
			generated.addAll(module.generate(styles));
		}
		
		List<File> scripts = module.findScriptFiles(); // just EJS files
		for(Iterator<File> iter = scripts.iterator(); iter.hasNext(); ) {
			File srcFile = iter.next();
			File genFile = module.getGenFile(srcFile);
			if(srcFile.lastModified() <= genFile.lastModified()) {
				iter.remove();
			}
		}
		if(!scripts.isEmpty()) {
			generated.addAll(module.generate(scripts));
		}
		
		// Mailer files
		for(File genFile : module.findGenMailers()) {
			File mailer = module.getSrcMailer(genFile);
			if(mailer == null || !mailer.exists()) {
				genFile.delete();
			}
		}
		
		List<File> mailers = module.findMailers();
		for(Iterator<File> iter = mailers.iterator(); iter.hasNext(); ) {
			File srcFile = iter.next();
			File genFile = module.getGenMailer(srcFile);
			if(srcFile.lastModified() <= genFile.lastModified()) {
				iter.remove();
			}
		}
		if(!mailers.isEmpty()) {
			generated.addAll(MailerGenerator.generate(module, mailers));
		}

		// Mailer Template (EMT) files
		for(File genFile : module.findGenMailerTemplates()) {
			File template = module.getSrcMailerTemplate(genFile);
			if(template == null || !template.exists()) {
				genFile.delete();
			}
		}

		List<File> templates = module.findMailerTemplates();
		for(Iterator<File> iter = templates.iterator(); iter.hasNext(); ) {
			File srcFile = iter.next();
			File genFile = module.getGenMailerTemplate(srcFile);
			if(srcFile.lastModified() <= genFile.lastModified()) {
				iter.remove();
			}
		}
		if(!templates.isEmpty()) {
			generated.addAll(module.generate(templates));
		}

		// Assets
		if(getLastModified(module.assets) > module.assetList.lastModified()) {
			generated.add(module.generateAssetList());
		}

		return generated;
	}

	public boolean getClean() {
		return clean;
	}

	public Mode getMode() {
		return mode;
	}

	public void setClean(boolean clean) {
		this.clean = clean;
	}

	public void setMode(Mode mode) {
		this.mode = mode;
	}
	
}
