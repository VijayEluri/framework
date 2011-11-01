package org.oobium.build.clients;

import static org.oobium.utils.FileUtils.createJar;
import static org.oobium.utils.FileUtils.writeFile;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.oobium.build.workspace.Bundle;
import org.oobium.build.workspace.Module;
import org.oobium.build.workspace.Project;
import org.oobium.build.workspace.Workspace;
import org.oobium.utils.Config.Mode;
import org.oobium.utils.FileUtils;

public class OsgiClientExporter {

//	private final Logger logger;
	private final Workspace workspace;
	private final Module module;
	private final File exportDir;
	private final File tmpDir;

	private Project target;
	private Mode mode;
	private boolean clean;
	private boolean includeSource;
	
	public OsgiClientExporter(Workspace workspace, Module module) {
//		this.logger = LogProvider.getLogger(BuildBundle.class);
		this.workspace = workspace;
		this.module = module;
		this.exportDir = workspace.getExportDir();
		this.tmpDir = new File(exportDir, "tmp");
		
		this.mode = Mode.PROD;
	}

	public void setTarget(Project project) {
		this.target = project;
	}
	
	private String relativePath(File binFile, int binPathLength) {
		String relativePath = binFile.getAbsolutePath().substring(binPathLength);
		if('\\' == File.separatorChar) {
			relativePath = relativePath.replace('\\', '/');
		}
		return relativePath;
	}

	private String relativeSrcPath(File srcFile, File binFile, int binPathLength) {
		String relativePath = binFile.getParent().substring(binPathLength) + File.separator + srcFile.getName();
		if('\\' == File.separatorChar) {
			relativePath = relativePath.replace('\\', '/');
		}
		return relativePath;
	}
	
	public void includeSource(boolean include) {
		this.includeSource = include;
	}
	
	/**
	 * Export the application, configured for the given mode.
	 * @return a File object for the exported jar.
	 * @throws IOException
	 */
	public File export() throws IOException {
		if(exportDir.exists()) {
			if(clean) {
				FileUtils.deleteContents(exportDir);
			}
		} else {
			exportDir.mkdirs();
		}
		
		if(tmpDir.exists()) {
			FileUtils.deleteContents(tmpDir);
		} else {
			tmpDir.mkdirs();
		}

		try {
			File targetDir;
			if(target == null) {
				targetDir = exportDir;
			} else {
				targetDir = new File(target.file, "oobium");
			}

			String targetName = module.name + ".client.jar";
			return createJar(targetDir, targetName, targetFiles(), targetManifest(module.name));
		} finally {
			FileUtils.delete(tmpDir);
		}
	}

	private Manifest targetManifest(String name) {
		Manifest manifest = new Manifest();
		Attributes attrs = manifest.getMainAttributes();
		
		attrs.putValue("Manifest-Version", "1.0");
		attrs.putValue("Bundle-ManifestVersion", "2");
		attrs.putValue("Bundle-Name", name + " Client");
		attrs.putValue("Bundle-SymbolicName", name + ".client");
		attrs.putValue("Bundle-Version", "1.0.0.qualifier");
		attrs.putValue("Bundle-RequiredExecutionEnvironment", "JavaSE-1.6");
		attrs.putValue("Import-Package", "org.osgi.framework;version=\"1.4.0\"," +
		                                 "org.jboss.netty.handler.codec.http," +
		                                 "org.oobium.app," +
		                                 "org.oobium.app.controllers," +
		                                 "org.oobium.app.request," +
		                                 "org.oobium.app.response," +
		                                 "org.oobium.app.routing," +
		                                 "org.oobium.app.http," +
		                                 "org.oobium.cache," +
		                                 "org.oobium.logging," +
		                                 "org.oobium.persist," +
		                                 "org.oobium.utils," +
		                                 "org.oobium.utils.coercion," +
		                                 "org.oobium.utils.json"
		                                );
		// attrs.putValue("Bundle-Activator", name + ".client.Activator");
		attrs.putValue("Export-Package", name + ".models");
		attrs.putValue("Bundle-ActivationPolicy", "lazy");

		return manifest;
	}
	
	private Map<String, File> targetFiles() throws IOException {
		Set<Bundle> bundles = new TreeSet<Bundle>();
		Map<Bundle, List<Bundle>> deps = module.getDependencies(workspace, mode);
		bundles.addAll(deps.keySet());
		bundles.add(module);
		
		Map<String, File> applicationFiles = new HashMap<String, File>();
		int len = module.bin.getAbsolutePath().length() + 1;
		for(File model : module.findModels()) {
			File genModel = module.getGenModel(model);
			File[] genModelClasses = module.getBinFiles(genModel);
			for(File genModelClass : genModelClasses) {
				applicationFiles.put(relativePath(genModelClass, len), genModelClass);
			}
			if(includeSource) {
				applicationFiles.put(relativeSrcPath(genModel, genModelClasses[0], len), genModel);
			}
			if(target == null) {
				String fullName = module.packageName(model, true) + "." + module.getModelName(model);
				File tmpClass = new File(tmpDir, fullName.replace('.', File.separatorChar) + ".class");
				fullName = fullName.replace('.', '/');
				createModelClass(tmpClass, fullName);
				applicationFiles.put(fullName + ".class", tmpClass);
			} else {
				createClientModel(model);
			}
		}

		return applicationFiles;
	}

	private void createModelClass(File file, String fullName) {
		byte[] name = fullName.getBytes();
		byte[] shortName = fullName.substring(fullName.lastIndexOf('/') + 1).getBytes();

		byte[][] bas = new byte[][] {
			{ (byte)0xCA,(byte)0xFE,(byte)0xBA,(byte)0xBE,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x32,(byte)0x00,(byte)0x10,(byte)0x07,(byte)0x00,(byte)0x02,(byte)0x01,(byte)0x00 },
			// len name
			{ (byte)0x07,(byte)0x00,(byte)0x04,(byte)0x01,(byte)0x00 },
			// len+5 name
			{ (byte)0x4D,(byte)0x6F,(byte)0x64,(byte)0x65,(byte)0x6C,(byte)0x01,(byte)0x00,(byte)0x06,(byte)0x3C,(byte)0x69,(byte)0x6E,(byte)0x69,(byte)0x74,(byte)0x3E,(byte)0x01,(byte)0x00,(byte)0x03,(byte)0x28,(byte)0x29,(byte)0x56,(byte)0x01,(byte)0x00,(byte)0x04,(byte)0x43,(byte)0x6F,(byte)0x64,(byte)0x65,(byte)0x0A,(byte)0x00,(byte)0x03,(byte)0x00,(byte)0x09,(byte)0x0C,(byte)0x00,(byte)0x05,(byte)0x00,(byte)0x06,(byte)0x01,(byte)0x00,(byte)0x0F,(byte)0x4C,(byte)0x69,(byte)0x6E,(byte)0x65,(byte)0x4E,(byte)0x75,(byte)0x6D,(byte)0x62,(byte)0x65,(byte)0x72,(byte)0x54,(byte)0x61,(byte)0x62,(byte)0x6C,(byte)0x65,(byte)0x01,(byte)0x00,(byte)0x12,(byte)0x4C,(byte)0x6F,(byte)0x63,(byte)0x61,(byte)0x6C,(byte)0x56,(byte)0x61,(byte)0x72,(byte)0x69,(byte)0x61,(byte)0x62,(byte)0x6C,(byte)0x65,(byte)0x54,(byte)0x61,(byte)0x62,(byte)0x6C,(byte)0x65,(byte)0x01,(byte)0x00,(byte)0x04,(byte)0x74,(byte)0x68,(byte)0x69,(byte)0x73,(byte)0x01,(byte)0x00 },
			// len+2 4C name
			{ (byte)0x3B,(byte)0x01,(byte)0x00,(byte)0x0A,(byte)0x53,(byte)0x6F,(byte)0x75,(byte)0x72,(byte)0x63,(byte)0x65,(byte)0x46,(byte)0x69,(byte)0x6C,(byte)0x65,(byte)0x01,(byte)0x00 },
			// slen+5 short name
			{ (byte)0x2E,(byte)0x6A,(byte)0x61,(byte)0x76,(byte)0x61,(byte)0x00,(byte)0x21,(byte)0x00,(byte)0x01,(byte)0x00,(byte)0x03,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x01,(byte)0x00,(byte)0x01,(byte)0x00,(byte)0x05,(byte)0x00,(byte)0x06,(byte)0x00,(byte)0x01,(byte)0x00,(byte)0x07,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x2F,(byte)0x00,(byte)0x01,(byte)0x00,(byte)0x01,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x05,(byte)0x2A,(byte)0xB7,(byte)0x00,(byte)0x08,(byte)0xB1,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x02,(byte)0x00,(byte)0x0A,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x06,(byte)0x00,(byte)0x01,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x03,(byte)0x00,(byte)0x0B,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x0C,(byte)0x00,(byte)0x01,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x05,(byte)0x00,(byte)0x0C,(byte)0x00,(byte)0x0D,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x01,(byte)0x00,(byte)0x0E,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x02,(byte)0x00,(byte)0x0F }
		};
		
		BufferedOutputStream out = null;
		try {
			if(!file.exists()) {
				File folder = file.getParentFile();
				if(!folder.exists()) {
					folder.mkdirs();
				}
				file.createNewFile();
			}
			out = new BufferedOutputStream(new FileOutputStream(file));
			out.write(bas[0]);
			out.write(name.length);
			out.write(name);
			out.write(bas[1]);
			out.write(name.length + 5);
			out.write(name);
			out.write(bas[2]);
			out.write(name.length + 2);
			out.write((byte)0x4C);
			out.write(name);
			out.write(bas[3]);
			out.write(shortName.length + 5);
			out.write(shortName);
			out.write(bas[4]);
		} catch(IOException e) {
			e.printStackTrace();
		} finally {
			if(out != null) {
				try {
					out.close();
				} catch(IOException e) {
					// throw away
				}
			}
		}
	}
	
	private void createClientModel(File model) {
		if(target != null) {
			String path = model.getAbsolutePath().substring(module.src.getAbsolutePath().length());
			File clientModel = new File(target.src, path);
			
			if(!clientModel.exists()) {
				String pkg = module.packageName(model);
				String name = module.getModelName(model);
				String sname = name + "Model";
	
				String src = 
					"package " + pkg + ";\n" +
					"\n" +
					"public class " + name + " extends " + sname + " {\n" +
					"\n" +
					"}";
	
				writeFile(clientModel, src);
			}
		}
	}
	
}
