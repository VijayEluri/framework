package org.oobium.build.clients.blazeds;

import static org.oobium.build.util.ProjectUtils.getPrefsFileDate;
import static org.oobium.utils.FileUtils.createFolder;
import static org.oobium.utils.FileUtils.deleteContents;
import static org.oobium.utils.FileUtils.*;
import static org.oobium.utils.StringUtils.*;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.oobium.build.model.ModelDefinition;
import org.oobium.build.workspace.Module;

public class FlexTestProjectGenerator {

	private final Module module;
	private final File project;
	
	private boolean force;
	private String flexSdk;

	private String services;
	private String output;
	private String server;
	private String host;
	private String port;
	
	public FlexTestProjectGenerator(Module module) {
		this(module, null);
	}
	
	public FlexTestProjectGenerator(Module module, File project) {
		this.module = module;
		if(project == null) {
			project = module.file.getParentFile();
		}
		if(project.isDirectory()) {
			this.project = new File(project, module.name + ".blazeds.flex.tests");
		} else {
			this.project = project;
		}
	}
	
	public File create() {
		if(force) {
			deleteContents(project);
		}
		else if(project.exists()) {
			throw new UnsupportedOperationException(project.getName() + " already exists");
		}

		createFolder(project, "libs");
		createHtmlTemplateFolder();
		
		File src = createFolder(project, "src");
		
		createTestApplicationFile(src);
		
		String source = getResourceAsString(getClass(), "TestTemplate.mxml");
		for(File file : module.findModels()) {
			createTestCanvasFile(src, file, source);
		}

		createActionScriptPropertiesFile();
		createFlexPropertiesFile();
		createPrefsFile();
		createProjectFile();
		if(flexSdk != null) {
			createExternalToolBuilderFile();
		}

		return project;
	}

	private void createTestApplicationFile(File src) {
		Map<String, String> namespaces = new HashMap<String, String>();
		List<String> contents = new ArrayList<String>();

		for(File file : module.findModels()) {
			ModelDefinition model = new ModelDefinition(file);
			String pkg = model.getPackageName();
			if(!namespaces.containsKey(pkg)) {
				namespaces.put(pkg, "md" + namespaces.size());
			}
			contents.add(
					"<mx:VBox label=\"" + titleize(plural(model.getSimpleName())) + "\">\n" +
					" <" + namespaces.get(pkg) + ":" + model.getSimpleName() + " />\n" +
					"</mx:VBox>"
				);
		}

		for(Entry<String, String> entry : namespaces.entrySet()) {
			String pkg = entry.getKey();
			String ns = entry.getValue();
			namespaces.put(pkg, "xmlns:" + ns + "=\"" + pkg + ".*\"");
		}
		namespaces.put("", "xmlns:mx=\"http://www.adobe.com/2006/mxml\"");
		
		writeFile(src, "Tests.mxml", source(
				"<?xml version=\"1.0\"?>",
				"<mx:Application",
				"{namespaces}",
				" >",
				" <mx:TabNavigator borderStyle=\"solid\" >",
				"{contents}",
				" </mx:TabNavigator>",
				"</mx:Application>"
			).replace("{contents}", source("\t\t", '\t', contents)).replace("{namespaces}", source("\t\t", '\t', namespaces.values())));
	}
	
	private void createActionScriptPropertiesFile() {
		String path = project.getName() + ".as";
		String uuid = UUID.randomUUID().toString();
		String services = (this.services == null) ? "/path/to/services-config.xml" : this.services;
		String output = (this.output == null) ? "bin" : this.output;
		writeFile(project, ".actionScriptProperties", source(
				"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>",
				"<actionScriptProperties analytics=\"false\" mainApplicationPath=\"{path}\" projectUUID=\"{uuid}\" version=\"10\">",
				"  <compiler additionalCompilerArguments=\"-services &quot;{services}&quot; -locale en_US\" autoRSLOrdering=\"true\" copyDependentFiles=\"true\" fteInMXComponents=\"false\" generateAccessible=\"true\" htmlExpressInstall=\"true\" htmlGenerate=\"true\" htmlHistoryManagement=\"true\" htmlPlayerVersionCheck=\"true\" includeNetmonSwc=\"false\" outputFolderLocation=\"{output}\" outputFolderPath=\"bin-debug\" removeUnusedRSL=\"true\" sourceFolderPath=\"src\" strict=\"true\" targetPlayerVersion=\"0.0.0\" useApolloConfig=\"false\" useDebugRSLSwfs=\"true\" verifyDigests=\"true\" warn=\"true\">",
				"    <compilerSourcePath/>",
				"    <libraryPath defaultLinkType=\"0\">",
				"      <libraryPathEntry kind=\"4\" path=\"\">",
				"        <excludedEntries>",
				"          <libraryPathEntry kind=\"3\" linkType=\"1\" path=\"${PROJECT_FRAMEWORKS}/libs/flex.swc\" useDefaultLinkType=\"false\"/>",
				"          <libraryPathEntry kind=\"3\" linkType=\"1\" path=\"${PROJECT_FRAMEWORKS}/libs/core.swc\" useDefaultLinkType=\"false\"/>",
				"        </excludedEntries>",
				"      </libraryPathEntry>",
				"      <libraryPathEntry kind=\"1\" linkType=\"1\" path=\"libs\"/>",
				"    </libraryPath>",
				"    <sourceAttachmentPath/>",
				"  </compiler>",
				"  <applications>",
				"    <application path=\"Main.mxml\"/>",
				"  </applications>",
				"  <modules/>",
				"  <buildCSSFiles/>",
				"  <flashCatalyst validateFlashCatalystCompatibility=\"false\"/>",
				"</actionScriptProperties>"
			).replace("{path}", path).replace("{uuid}", uuid).replace("{services}", services).replace("{output}", output));
	}
	
	private void createExternalToolBuilderFile() {
		writeFile(project, ".externalToolBuilders/Flex_Builder.launch", source(
				"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>",
				"<launchConfiguration type=\"org.eclipse.ui.externaltools.ProgramBuilderLaunchConfigurationType\">",
				" <booleanAttribute key=\"org.eclipse.debug.ui.ATTR_LAUNCH_IN_BACKGROUND\" value=\"true\"/>",
				" <stringAttribute key=\"org.eclipse.ui.externaltools.ATTR_LOCATION\" value=\"{flexSdk}/bin/mxmlc\"/>",
				" <stringAttribute key=\"org.eclipse.ui.externaltools.ATTR_RUN_BUILD_KINDS\" value=\"full,\"/>",
				" <stringAttribute key=\"org.eclipse.ui.externaltools.ATTR_TOOL_ARGUMENTS\" value=\"src/Tests.mxml -output=bin/{module}.tests.swf -library-path+=libs\"/>",
				" <booleanAttribute key=\"org.eclipse.ui.externaltools.ATTR_TRIGGERS_CONFIGURED\" value=\"true\"/>",
				" <stringAttribute key=\"org.eclipse.ui.externaltools.ATTR_WORKING_DIRECTORY\" value=\"${workspace_loc:/{project}}\"/>",
				"</launchConfiguration>"
			).replace("{flexSdk}", flexSdk).replace("{module}", module.name).replace("{project}", project.getName()));
	}
	
	private void createFlexPropertiesFile() {
		String server = (this.server == null) ? "" : this.server;
		String host = (this.host == null) ? "localhost" : this.host;
		String port = (this.port == null) ? "8400" : this.port;
		writeFile(project, ".externalToolBuilders/Flex_Builder.launch", join('\n',
				"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>",
				"<flexProperties enableServiceManager=\"false\" flexServerFeatures=\"4\" flexServerType=\"2\" serverContextRoot=\"/\" serverRoot=\"{server}\" serverRootURL=\"http://{host}:{port}/\" toolCompile=\"true\" useServerFlexSDK=\"false\" version=\"2\"/>"
		).replace("{server}", server).replace("{host}", host).replace("{port}", port));
	}
	
	private void createHtmlTemplateFolder() {
		File folder = createFolder(project, "html-template");
		String[] names = {
				"index.template.html",
				"playerProductInstall.swf",
				"swfobject.js",
				"history/history.css",
				"history/history.js",
				"history/historyFrame.html"
		};
		for(String name : names) {
			writeFile(folder, name, getClass().getResourceAsStream("/lib/flex/html-template/" + name));
		}
	}
	
	private void createPrefsFile() {
		writeFile(createFolder(project, ".settings"), "org.eclipse.jdt.core.prefs",
				"#" + getPrefsFileDate() + "\n" +
				"eclipse.preferences.version=1\n" +
				"encoding/<project>=utf-8"
			);
	}
	
	private void createProjectFile() {
		String builder;
		if(flexSdk == null) {
			builder = 
				"   <name>com.adobe.flexbuilder.project.flexbuilder</name>\n" +
				"   <arguments>\n" +
				"   </arguments>\n";
		} else {
			builder = 
				"   <name>org.eclipse.ui.externaltools.ExternalToolBuilder</name>\n" +
				"   <triggers>auto,full,incremental,</triggers>\n" +
				"   <arguments>\n" +
				"    <dictionary>\n" +
				"     <key>LaunchConfigHandle</key>\n" +
				"     <value>&lt;project&gt;/.externalToolBuilders/Flex_Builder.launch</value>\n" +
				"    </dictionary>\n" +
				"   </arguments>";
		}
		writeFile(project, ".project", source(
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>",
				"<projectDescription>",
				" <name>{project}</name>",
				" <comment></comment>",
				" <projects>",
				" </projects>",
				" <buildSpec>",
				"  <buildCommand>",
				builder +
				"  </buildCommand>",
				" </buildSpec>",
				" <natures>",
				"  <nature>com.adobe.flexbuilder.project.flexlibnature</nature>",
				"  <nature>com.adobe.flexbuilder.project.actionscriptnature</nature>",
				" </natures>",
				"</projectDescription>"
			).replace("{project}", project.getName()).replace("{builder}", builder));
	}
	
	private void createTestCanvasFile(File src, File file, String source) {
		ModelDefinition model = new ModelDefinition(file);
		source = source.replace("{fullType}", model.getCanonicalName()).replace("{type}", model.getSimpleName());
		writeFile(model.getFile(src, "mxml"), source);
	}

	public File getProject() {
		return project;
	}
	
	public void setFlexSdk(String flexSdk) {
		this.flexSdk = flexSdk;
	}

	public void setForce(boolean force) {
		this.force = force;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public void setOutput(String output) {
		this.output = output;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public void setServer(String server) {
		this.server = server;
	}

	public void setServices(String services) {
		this.services = services;
	}
	
}
