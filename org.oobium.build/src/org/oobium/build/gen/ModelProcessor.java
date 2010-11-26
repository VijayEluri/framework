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

import static org.oobium.build.util.ProjectUtils.getSrcAnnotations;
import static org.oobium.utils.FileUtils.readFile;
import static org.oobium.utils.FileUtils.writeFile;
import static org.oobium.utils.StringUtils.plural;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;

import org.oobium.app.model.Indexes;
import org.oobium.build.BuildBundle;
import org.oobium.build.model.ModelDefinition;
import org.oobium.build.workspace.Application;
import org.oobium.build.workspace.Bundle;
import org.oobium.build.workspace.Module;
import org.oobium.build.workspace.Workspace;
import org.oobium.logging.Logger;
import org.oobium.persist.ModelDescription;

@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class ModelProcessor extends AbstractProcessor {

	public static boolean isValidAction(int action) {
		return action >= 0 && action <= GEN_SCHEMA;
	}
	

	private Logger logger = Logger.getLogger(BuildBundle.class);
	
	private static final Set<String> supportedOptions = new TreeSet<String>();
	
	private static final Set<String> supportedTypes = new TreeSet<String>();
	
	public static final int
		GEN_MODELS 	 	= 1 << 0,
		GEN_VIEWS 		= 1 << 1,
		GEN_CONTROLLERS = 1 << 2,
		GEN_SCHEMA		= 1 << 3,
		GEN_TESTS		= 1 << 4,
		GEN_APP 		= GEN_MODELS | GEN_VIEWS | GEN_CONTROLLERS,
		GEN_APP_WS 		= GEN_MODELS | GEN_CONTROLLERS,
		GEN_ALL 		= GEN_MODELS | GEN_VIEWS | GEN_CONTROLLERS | GEN_SCHEMA | GEN_TESTS;

	static {
		supportedOptions.add("name");
		supportedOptions.add("version");
		supportedOptions.add("path");
		supportedOptions.add("type");
		supportedOptions.add("action");
		supportedOptions.add("webservice"); // a nature that can be set in the project file
		supportedTypes.add(ModelDescription.class.getCanonicalName());
	}


	static ThreadLocal<ModelGenerator> generators = new ThreadLocal<ModelGenerator>();

	public static ModelGenerator getGenerator() {
		return generators.get();
	}
	
	public static Workspace getWorkspace() {
		ModelGenerator gen = generators.get();
		if(gen != null) {
			return gen.getWorkspace();
		}
		return null;
	}
	

	/**
	 * Execute a command on the given class in the current Workspace
	 */
	private Object exec(File file, Class<?> clazz, String methodOrFieldName, Object...params) {
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		try {
			Class<?> processorClass = loader.loadClass(ModelProcessor.class.getName());
			Class<?> workspaceClass = loader.loadClass(Workspace.class.getName());
			Class<?> objectClass = loader.loadClass(clazz.getName());

			Object workspace = processorClass.getMethod("getWorkspace").invoke(processorClass);

			Method getObject;
			if(clazz == Bundle.class) {
				getObject = workspaceClass.getMethod("getBundle", File.class);
			} else if(clazz == Module.class) {
				getObject = workspaceClass.getMethod("getModule", File.class);
			} else if(clazz == Application.class) {
				getObject = workspaceClass.getMethod("getApplication", File.class);
			} else {
				throw new IllegalArgumentException();
			}

			Object object = getObject.invoke(workspace, file);
			
			if(methodOrFieldName.startsWith("get")) {
				Class<?>[] paramClasses = new Class<?>[params.length];
				for(int i = 0; i < params.length; i++) {
					paramClasses[i] = params[i].getClass();
				}
				Method method = objectClass.getMethod(methodOrFieldName, paramClasses);
				Object value = method.invoke(object, params);
				return value;
			} else {
				Field field = objectClass.getField(methodOrFieldName);
				Object value = field.get(object);
				return value;
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private void generateControllerFiles(String appPath, Map<ModelDefinition, File> models, boolean webservice) {
		for(ModelDefinition model : models.keySet()) {
			File file = new File(appPath);
			Module module = (Module) Bundle.create(file);
			
			String src = ControllerGenerator.generate(module, model);

			File controller = module.getController(model.getSimpleName());
			log(writeFile(controller, src));
		}
	}
	
	private void generateModelFiles(String appPath, Map<ModelDefinition, File> models) {
		for(ModelDefinition model : models.keySet()) {
			File moduleFile = new File(appPath);
			File modelFile = models.get(model);

			File genFile = (File) exec(moduleFile, Module.class, "getGenModel", modelFile);

			try {
				String annotations = getSrcAnnotations(modelFile);
				String src = ModelGenerator.generate(annotations, model);
				log(writeFile(genFile, src));
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void generateSchemaFile(String appName, String appVersion, String appType, String appPath, List<ModelDefinition> models) {
		File appFile = new File(appPath);
		
//		TODO support model in jars
//		
//		Map<String, JarFile> jars = new HashMap<String, JarFile>();
//		
//		ClassLoader loader = Thread.currentThread().getContextClassLoader();
//		try {
//			Class<?> workspaceClass = loader.loadClass(Workspace.class.getName());
//			Class<?> bundleClass = loader.loadClass(Bundle.class.getName());
//			Method getBundle = workspaceClass.getMethod("getBundle", File.class);
//			Method getDependencies = bundleClass.getMethod("getDependencies");
//			Method exportsModels = bundleClass.getMethod("exportsModels");
//			Field name = bundleClass.getField("name");
//			Field file = bundleClass.getField("file");
//
//			Object appBundle = getBundle.invoke(workspaceClass, appFile);
//
//			for(Object bundle : (Set<?>) getDependencies.invoke(appBundle)) {
//				if((Boolean) exportsModels.invoke(bundle)) {
//					try {
//						String path = ((String) name.get(bundle)).replaceAll("\\.", "/") + "/models";
//						jars.put(path, new JarFile((File) file.get(bundle)));
//					} catch(IOException e) {
//						logger.error(e);
//					}
//				}
//			}
//		} catch(ClassNotFoundException e1) {
//			e1.printStackTrace();
//		} catch(SecurityException e) {
//			e.printStackTrace();
//		} catch(NoSuchMethodException e) {
//			e.printStackTrace();
//		} catch(IllegalArgumentException e) {
//			e.printStackTrace();
//		} catch(IllegalAccessException e) {
//			e.printStackTrace();
//		} catch(InvocationTargetException e) {
//			e.printStackTrace();
//		} catch(NoSuchFieldException e) {
//			e.printStackTrace();
//		}
//
//		for(String path : jars.keySet()) {
//			try {
//				JarFile jar = jars.get(path);
//				URLClassLoader ucl = URLClassLoader.newInstance(new URL[] { new URL("jar:file:" + jar.getName() + "!/") });
//				for(Enumeration<JarEntry> entries = jar.entries(); entries.hasMoreElements(); ) {
//					JarEntry entry = entries.nextElement();
//					String name = entry.getName();
//					if(name.startsWith(path) && name.indexOf('$') == -1 && !name.endsWith("Model.class")) {
//						name = name.substring(0, name.lastIndexOf('.')).replaceAll("/", ".");
//						try {
//							Class<?> clazz = ucl.loadClass(name);
//							ModelDescription description = clazz.getAnnotation(ModelDescription.class);
//							if(description != null) {
//								Indexes annotation = clazz.getAnnotation(Indexes.class);
//								String[] indexes = (annotation != null) ? annotation.value() : new String[0];
//								ModelDefinition model = new ModelDefinition(name, description, indexes);
//								if(model.build()) {
//									models.add(model);
//								}
//							}
//						} catch(ClassNotFoundException e) {
//							logger.warn(e.getMessage());
//						}
//					}
//				}
//			} catch(MalformedURLException e) {
//				logger.warn(e.getMessage());
//			}
//		}

		String src = DbGenerator.generate(appName, appVersion, appType, models);
		
		File schema = (File) exec(appFile, Application.class, "getSchema");
		log(writeFile(schema, src));
	}
	
	private void generateTestFiles(Map<ModelDefinition, File> models) {
		// TODO generateTestFiles
	}

	private void generateViewFiles(String appPath, Map<ModelDefinition, File> models) {
		for(ModelDefinition model : models.keySet()) {
			String name = model.getSimpleName();
			String plur = plural(name);
			File folder = (File) exec(new File(appPath), Module.class, "getViewsFolder", name);
			
			ViewGenerator gen = new ViewGenerator(model);

			log(writeFile(folder, "ShowEdit" + name + ".esp", gen.generateShowEditView()));
			log(writeFile(folder, "ShowAll"  + plur + ".esp", gen.generateShowAllView()));
			log(writeFile(folder, "ShowNew"  + name + ".esp", gen.generateShowNewView()));
			log(writeFile(folder, "Show" 	 + name + ".esp", gen.generateShowView()));
			log(writeFile(folder, name	   + "Form" + ".esp", gen.generateForm()));
		}
	}
	
	private JavaFileObject getJavaFileObject(Object obj, Field sourcefile) {
		try {
			Object value = sourcefile.get(obj);
			if(value instanceof JavaFileObject) {
				return (JavaFileObject) value;
			}
		} catch(IllegalArgumentException e) {
			logger.error(e);
		} catch(IllegalAccessException e) {
			logger.error(e);
		}
		return null;
	}

	private Field getSourceFileField(Object obj) {
		try {
			return obj.getClass().getField("sourcefile");
		} catch(SecurityException e) {
			logger.error(e);
		} catch(NoSuchFieldException e) {
			logger.error(e);
		}
		return null;
	}
	
	@Override
	public Set<String> getSupportedAnnotationTypes() {
		return supportedTypes;
	}

	@Override
	public Set<String> getSupportedOptions() {
		return supportedOptions;
	}
	
	private void log(File file) {
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		try {
			Class<?> processorClass = loader.loadClass(ModelProcessor.class.getName());
			Class<?> generatorClass = loader.loadClass(ModelGenerator.class.getName());
			Object generator = processorClass.getMethod("getGenerator").invoke(processorClass);
			generatorClass.getMethod("addFile", File.class).invoke(generator, file);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	private void modifySrcFile(ModelDefinition model, File file) {
		StringBuilder sb = readFile(file);
		String s = "class " + model.getSimpleName();
		int start = sb.indexOf(s) + s.length();
		int end = sb.indexOf("implements", start);
		if(end == -1) {
			end = sb.indexOf("{", start);
		}
		String superStr = " " + model.getSimpleName() + "Model ";
		s = sb.toString().substring(start, end);
		if(!s.contains(superStr)) {
			sb.replace(start, end, " extends" + superStr);
			log(writeFile(file, sb.toString()));
		}
	}
	
	private void modifySrcFiles(Map<ModelDefinition, File> models) {
		for(ModelDefinition model : models.keySet()) {
			modifySrcFile(model, models.get(model));
		}
	}
	
	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		int action = Integer.parseInt(processingEnv.getOptions().get("action"));
		if(!isValidAction(action)) {
			return true;
		}
		
		if(!annotations.isEmpty() && !roundEnv.getRootElements().isEmpty()) {
			Map<ModelDefinition, File> models = new HashMap<ModelDefinition, File>();

			for(Element element : roundEnv.getRootElements()) {
				ModelDescription description = element.getAnnotation(ModelDescription.class);
				if(description != null) {
					Field sourcefile = getSourceFileField(element);
					if(sourcefile != null) {
						String name = element.asType().toString();
						Indexes annotation = element.getAnnotation(Indexes.class);
						String[] indexes = (annotation != null) ? annotation.value() : new String[0];
						ModelDefinition model = new ModelDefinition(name, description, indexes);
						if(model.build()) {
							JavaFileObject jfo = getJavaFileObject(element, sourcefile);
							if(jfo != null) {
								File file = new File(jfo.toUri().getPath());
								models.put(model, file);
							}
						}
					}
				}
			}
			
			setOpposites(models.keySet());

			String path = processingEnv.getOptions().get("path");

			if((action & GEN_MODELS) != 0) {
				generateModelFiles(path, models);
				modifySrcFiles(models);
			}
			
			if((action & GEN_VIEWS) != 0) {
				generateViewFiles(path, models);
			}

			if((action & GEN_CONTROLLERS) != 0) {
				boolean webservice = Boolean.parseBoolean(processingEnv.getOptions().get("webservice"));
				generateControllerFiles(path, models, webservice);
			}
			
			if((action & GEN_SCHEMA) != 0) {
				String name = processingEnv.getOptions().get("name");
				String version = processingEnv.getOptions().get("version");
				String type = processingEnv.getOptions().get("type");
				generateSchemaFile(name, version, type, path, new ArrayList<ModelDefinition>(models.keySet()));
			}
			
			if((action & GEN_TESTS) != 0) {
				generateTestFiles(models);
			}
		}

		return true;
	}

	private void setOpposites(Collection<ModelDefinition> models) {
		for(ModelDefinition model : models) {
			model.setOpposites(models);
		}
	}
	
}
