package org.oobium.build.clients.blazeds;

import static org.oobium.build.util.ProjectUtils.getPrefsFileDate;
import static org.oobium.utils.FileUtils.createFolder;
import static org.oobium.utils.FileUtils.deleteContents;
import static org.oobium.utils.FileUtils.writeFile;
import static org.oobium.utils.StringUtils.*;

import java.io.File;
import java.util.UUID;

import org.oobium.build.gen.model.PropertyDescriptor;
import org.oobium.build.model.ModelDefinition;
import org.oobium.build.util.MethodCreator;
import org.oobium.build.workspace.Module;

public class FlexProjectGenerator {

	private final Module module;
	private final File project;
	
	private boolean force;
	
	public FlexProjectGenerator(Module module) {
		this(module, null);
	}
	
	public FlexProjectGenerator(Module module, File project) {
		this.module = module;
		if(project == null) {
			project = module.file.getParentFile();
		}
		if(project.isDirectory()) {
			this.project = new File(project, module.name + ".blazeds.flex");
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
		
		createFolder(project, "bin");
		
		File src = createFolder(project, "src");
		createObserverEventHandlerFile(src);
		for(File file : module.findModels()) {
			createModel(src, file);
		}

		createProjectFile();
		createActionScriptPropertiesFile();
		createFlexLibPropertiesFile();
		createPrefsFile();

		return project;
	}
	
	private void createObserverEventHandlerFile(File src) {
		ActionScriptFile as = new ActionScriptFile();
		
		as.packageName = "org.oobium.persist";
		
		as.imports.add("flash.events.Event");
		as.imports.add("mx.controls.Alert");
		as.imports.add("mx.messaging.Consumer");
		as.imports.add("mx.rpc.events.ResultEvent");
		as.imports.add("mx.rpc.remoting.RemoteObject");
		as.imports.add("mx.messaging.ChannelSet");
		as.imports.add("mx.messaging.channels.AMFChannel");
		as.imports.add("mx.rpc.events.FaultEvent");
		as.imports.add("mx.messaging.events.*");
		
		as.simpleName = "ObserverEventHandler";
		
		as.variables.put("0", "private var messageHandler:Function");
		
		as.constructors.put(0, source(
				"public function ObserverEventHandler(aMessageHandler:Function):void {",
				" messageHandler = aMessageHandler;",
				"}"
			));
		
		as.methods.put("0", source(
				"public function handleEvents(event:ResultEvent):void {",
				" var channelName:String = event.result as String;",
				"",
				" var consumer:Consumer = new Consumer();",
				" consumer.destination = channelName;",
				"",
				" var channelSet:ChannelSet = new ChannelSet();",
				" channelSet.addChannel(new AMFChannel(\"my-polling-amf\", \"{serverUrl}/messagebroker/amfpolling\"));",
				"",
				" consumer.channelSet = channelSet;",
				" consumer.addEventListener(MessageEvent.MESSAGE, messageHandler);",
				" consumer.addEventListener(MessageFaultEvent.FAULT, faultHandler);",
				" consumer.subscribe();",
				"}"
			).replace("{serverUrl}", "http://localhost:8400")); // TODO serverUrl
		
		as.methods.put("1", source(
				"private function faultHandler (event:FaultEvent):void {",
				" Alert.show(event.fault.faultString, 'Error');",
				"}"
			));

		writeFile(src, "org/oobium/persist/ObserverEventHandler.as", as.toSource());
	}
	
	private void createActionScriptPropertiesFile() {
		String appPath = project.getName() + ".as";
		String uuid = UUID.randomUUID().toString();
		writeFile(project, ".actionScriptProperties",
				"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
				"<actionScriptProperties analytics=\"false\" mainApplicationPath=\"" + appPath + "\" projectUUID=\"" + uuid + "\" version=\"10\">\n" +
				" <compiler additionalCompilerArguments=\"-locale en_US\" autoRSLOrdering=\"true\" copyDependentFiles=\"false\" fteInMXComponents=\"false\" generateAccessible=\"false\" htmlExpressInstall=\"true\" htmlGenerate=\"false\" htmlHistoryManagement=\"false\" htmlPlayerVersionCheck=\"true\" includeNetmonSwc=\"false\" outputFolderPath=\"bin\" removeUnusedRSL=\"true\" sourceFolderPath=\"src\" strict=\"true\" targetPlayerVersion=\"0.0.0\" useApolloConfig=\"false\" useDebugRSLSwfs=\"true\" verifyDigests=\"true\" warn=\"true\">\n" +
				"  <compilerSourcePath/>\n" +
				"  <libraryPath defaultLinkType=\"0\">\n" +
				"   <libraryPathEntry kind=\"4\" path=\"\">\n" +
				"    <excludedEntries>\n" +
				"     <libraryPathEntry kind=\"3\" linkType=\"1\" path=\"${PROJECT_FRAMEWORKS}/libs/flex.swc\" useDefaultLinkType=\"false\"/>\n" +
				"     <libraryPathEntry kind=\"3\" linkType=\"1\" path=\"${PROJECT_FRAMEWORKS}/libs/core.swc\" useDefaultLinkType=\"false\"/>\n" +
				"    </excludedEntries>\n" +
				"   </libraryPathEntry>\n" +
				"  </libraryPath>\n" +
				"  <sourceAttachmentPath/>\n" +
				" </compiler>\n" +
				" <applications>\n" +
				"  <application path=\"" + appPath + "\"/>\n" +
				" </applications>\n" +
				" <modules/>\n" +
				" <buildCSSFiles/>\n" +
				" <flashCatalyst validateFlashCatalystCompatibility=\"false\"/>\n" +
				"</actionScriptProperties>"
			);
	}

	private void createFlexLibPropertiesFile() {
		writeFile(project, ".flexLibProperties",
				"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
				"<flexLibProperties includeAllClasses=\"true\" useMultiPlatformConfig=\"false\" version=\"3\">\n" +
				" <includeClasses/>\n" +
				" <includeResources/>\n" +
				" <namespaceManifests/>\n" +
				"</flexLibProperties>"
			);
	}

	private void createModel(File srcFolder, File modelFile) {
		ModelDefinition model = new ModelDefinition(modelFile);
		ActionScriptFile as = new ActionScriptFile();
		as.packageName = model.getPackageName();

		as.imports.add("mx.controls.Alert");
		as.imports.add("mx.rpc.remoting.RemoteObject");
		as.imports.add("mx.rpc.events.ResultEvent");
		as.imports.add("mx.rpc.events.FaultEvent");
		as.imports.add("org.oobium.persist.ObserverEventHandler");
		
		as.classMetaTags.add("RemoteClass(alias=\"" + model.getCanonicalName() + "\")");
		as.simpleName = model.getSimpleName();
		
		as.staticVariables.put("ro", "private static var ro:RemoteObject;");
		
		as.staticInitializers.add("ro = new RemoteObject();");
		as.staticInitializers.add("ro.destination = \"" + model.getControllerName() + "\";");
		as.staticInitializers.add("ro.addEventListener(\"fault\", faultHandler);");
		
		as.staticMethods.put("addObserver", source(
				"public static function addObserver(messageHandler:Function):void {",
				" var handler:ObserverEventHandler = new ObserverEventHandler(messageHandler);",
				" ro.addObserver.addEventListener(handler.handleEvents);",
				" ro.addObserver();",
				"}"
			));

		as.staticMethods.put("fault", source(
				"private static function faultHandler (event:FaultEvent):void {",
				" Alert.show(event.fault.faultString, 'Error');",
				"}"
			));
		
		as.staticMethods.put("find",
				"public static function find(o:Object, callback:Function):void {\n" +
				" " + as.simpleName + ".ro.find.addEventListener(\"result\", callback);\n" +
				" if(typeof(o) == \"number\") {\n" +
				"  " + as.simpleName + ".ro.find(o as int);\n" +
				" } else if(typeof(o) == \"string\") {\n" +
				"  " + as.simpleName + ".ro.find(o as String);\n" +
				" } else if(o != null) {\n" +
				"  " + as.simpleName + ".ro.find(o.toString());\n" +
				" } else {\n" +
				"  throw new Error(\"o cannot be null\");\n" +
				" }\n" +
				"}"
			);

		as.staticMethods.put("findAll",
				"public static function findAll(o:Object, callback:Function):void {\n" +
				" " + as.simpleName + ".ro.findAll.addEventListener(\"result\", callback);\n" +
				" if(o == \"*\") {\n" +
				"  " + as.simpleName + ".ro.findAll();\n" +
				" } else if(typeof(o) == \"string\") {\n" +
				"  " + as.simpleName + ".ro.findAll(o as String);\n" +
				" } else if(o != null) {\n" +
				"  " + as.simpleName + ".ro.findAll(o.toString());\n" +
				" } else {\n" +
				"  throw new Error(\"o cannot be null\");\n" +
				" }\n" +
				"}"
			);
		

		as.variables.put("", "public var id:int");
		for(PropertyDescriptor prop : model.getProperties().values()) {
			if(prop.hasMany()) {
				as.imports.add("mx.collections.ArrayCollection");
				as.variables.put(prop.variable(), "public var " + prop.variable() + ":ArrayCollection");
			} else {
				as.variables.put(prop.variable(), "public var " + prop.variable() + ":" + prop.castType());
			}
		}

		as.methods.put("create", source(
				"public function create(callBack:Function = null):void {",
				" if(callBack != null) {",
				"  ro.create.addEventListener(\"result\", callBack);",
				" }",
				" ro.create(this);",
				"}"
			));
		
		as.methods.put("update", source(
				"public function update(callBack:Function = null):void {",
				" if(callBack != null) {",
				"  ro.update.addEventListener(\"result\", callBack);",
				" }",
				" ro.update(this);",
				"}"
			));
		
		as.methods.put("destroy", source(
				"public function destroy(callBack:Function = null):void {",
				" if(callBack != null) {",
				"  ro.destroy.addEventListener(\"result\", callBack);",
				" }",
				" ro.destroy(this);",
				"}"
			));

		as.methods.put("save", source(
				"public function save(callBack:Function = null):void {",
				" if(id < 1) {",
				"  create(callBack);",
				" } else {",
				"  update(callBack);",
				" }",
				"}"
		));
		
		writeFile(srcFolder, as.getFilePath(), as.toSource());
	}
	
	private void createPrefsFile() {
		writeFile(createFolder(project, ".settings"), "org.eclipse.jdt.core.prefs",
				"#" + getPrefsFileDate() + "\n" +
				"eclipse.preferences.version=1\n" +
				"encoding/<project>=utf-8"
			);
	}
	
	private void createProjectFile() {
		writeFile(project, ".project",
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
				"<projectDescription>\n" +
				" <name>" + project.getName() + "</name>\n" +
				" <comment></comment>\n" +
				" <projects>\n" +
				" </projects>\n" +
				" <buildSpec>\n" +
				"  <buildCommand>\n" +
				"   <name>com.adobe.flexbuilder.project.flexbuilder</name>\n" +
				"   <arguments>\n" +
				"   </arguments>\n" +
				"  </buildCommand>\n" +
				" </buildSpec>\n" +
				" <natures>\n" +
				"  <nature>com.adobe.flexbuilder.project.flexlibnature</nature>\n" +
				"  <nature>com.adobe.flexbuilder.project.actionscriptnature</nature>\n" +
				" </natures>\n" +
				"</projectDescription>\n"
			);
	}
	
	private void createUserSession(File srcFolder){
		ActionScriptFile as = new ActionScriptFile();
		as.packageName = module.packageName(module.models);
		//as.classMetaTags.add("RemoteClass(alias=\"" + module.packageName(module.models) +".UserSession\")");
		as.simpleName = "UserSession";
		as.staticVariables.put("ro", "public static const ro:Object = createRemoteObject()");
		
		MethodCreator m0 = new MethodCreator("0UserSession()");
		m0.addLine("public function UserSession():void{");
			m0.addLine("ro = new RemoteObject();");
			m0.addLine("ro.destination = \"UserSessionController\";");
			m0.addLine("ro.addEventListener(\"fault\", faultHandler);");
		m0.addLine("}");
		as.addMethod(m0);
		

		MethodCreator m1 = new MethodCreator("1login");
		m1.addLine("public function login(userName:String, password:String, callBack:Function):void {");
			m1.addLine("ro.login.addEventListener(\"result\", callBack);");
			m1.addLine("ro.login(userName, password);");
		m1.addLine("}");
		as.addMethod(m1);
		
		MethodCreator m2 = new MethodCreator("2logout()");
		m2.addLine("public function logout(callBack:Function):void {");
			m2.addLine("ro.logout();");
		m2.addLine("}");
		as.addMethod(m2);
		
		MethodCreator m3 = new MethodCreator("3getUserName()");
		m3.addLine("public function getUserName(callBack:Function):void {");
			m3.addLine("ro.getUserName.addEventListener(\"result\", callBack);");
			m3.addLine("ro.getUserName();");
		m3.addLine("}");
		as.addMethod(m3);
		
		MethodCreator m4 = new MethodCreator("4getPassword()");
		m4.addLine("public function getPassword(callBack:Function):void {");
			m4.addLine("ro.getPassword.addEventListener(\"result\", callBack);");
			m4.addLine("ro.getPassword();");
		m4.addLine("}");
		as.addMethod(m4);
		
		as.addMethod("5",
				"public function getSessionId(callBack:Function):void {",
				" ro.getSessionId.addEventListener(\"result\", callBack);",
				" ro.getSessionId();",
				"}"
			);
		
		//PRIVATE_FUNCTIONS used to make the public functions work
	
		MethodCreator m6 = new MethodCreator("6faultHandler()");
		m6.addLine("private function faultHandler (event:FaultEvent):void {");
			m6.addLine("Alert.show(event.fault.faultString, 'Error');");
		m6.addLine("}");
		as.addMethod(m6);
		
		writeFile(srcFolder, as.getFilePath(), as.toSource());
	}
	
	public void setForce(boolean force) {
		this.force = force;
	}

}
