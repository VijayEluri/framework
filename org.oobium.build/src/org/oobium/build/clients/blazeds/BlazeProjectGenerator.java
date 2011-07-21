package org.oobium.build.clients.blazeds;

import static org.oobium.utils.FileUtils.*;
import static org.oobium.utils.StringUtils.*;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import org.oobium.build.clients.JavaClientExporter;
import org.oobium.build.gen.model.PropertyDescriptor;
import org.oobium.build.model.ModelDefinition;
import org.oobium.build.util.MethodCreator;
import org.oobium.build.util.SourceFile;
import org.oobium.build.workspace.Module;
import org.oobium.build.workspace.Project;
import org.oobium.build.workspace.Workspace;

public class BlazeProjectGenerator {

	private final Workspace workspace;
	private final Module module;
	private final File project;
	
	private boolean exportFlex;
	private File flexProject;
	
	private boolean force;
	
	public BlazeProjectGenerator(Workspace workspace, Module module) {
		this(workspace, module, null);
	}
	
	public BlazeProjectGenerator(Workspace workspace, Module module, File project) {
		this.workspace = workspace;
		this.module = module;
		if(project == null) {
			project = module.file.getParentFile();
		}
		if(project.isDirectory()) {
			this.project = new File(project, module.name + ".blazeds");
		} else {
			this.project = project;
		}
	}
	
	public void create() throws IOException {
		if(force) {
			deleteContents(project);
		}
		else if(project.exists()) {
			throw new UnsupportedOperationException(project.getName() + " already exists");
		}

		createClassPathFile();
		createProjectFile();
		createPrefsFile();
		
		createFolder(project, "bin");
		File src = createFolder(project, "src");

		Project target = workspace.load(project);
		JavaClientExporter javaExporter = new JavaClientExporter(workspace, module);
		javaExporter.setTarget(target);
		javaExporter.export();
		
		for(File file : module.findModels()) {
			ModelDefinition model = new ModelDefinition(file);
			createController(src, model);
			createModel(src, model);
		}
		
		createChannelController(src);
		createSessionController(src);
		createPersistController(src);
		
		if(exportFlex) {
			FlexProjectGenerator flex = new FlexProjectGenerator(module, flexProject);
			flex.setForce(force);
			flexProject = flex.create();
		}
	}

	private void createClassPathFile() {
		writeFile(project, ".classpath",
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
				"<classpath>\n" +
				"\t<classpathentry kind=\"src\" path=\"src\"/>\n" +
				"\t<classpathentry kind=\"con\" path=\"org.eclipse.jdt.launching.JRE_CONTAINER/org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType/JavaSE-1.6\"/>\n" +
				"\t<classpathentry kind=\"output\" path=\"bin\"/>\n" +
				"</classpath>"
			);
	}
	
	private void createProjectFile() {
		writeFile(project, ".project",
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
				"<projectDescription>\n" +
				"\t<name>" + project.getName() + "</name>\n" +
				"\t<comment></comment>\n" +
				"\t<projects>\n" +
				"\t</projects>\n" +
				"\t<buildSpec>\n" +
				"\t\t<buildCommand>\n" +
				"\t\t\t<name>org.eclipse.jdt.core.javabuilder</name>\n" +
				"\t\t\t<arguments>\n" +
				"\t\t\t</arguments>\n" +
				"\t\t</buildCommand>\n" +
				"\t</buildSpec>\n" +
				"\t<natures>\n" +
				"\t\t<nature>org.eclipse.jdt.core.javanature</nature>\n" +
				"\t</natures>\n" +
				"</projectDescription>\n"
			);
	}
	
	private void createPrefsFile() {
		writeFile(createFolder(project, ".settings"), "org.eclipse.jdt.core.prefs",
				"#Sat Jun 25 15:21:51 MDT 2011\n" +
				"eclipse.preferences.version=1\n" +
				"org.eclipse.jdt.core.compiler.codegen.inlineJsrBytecode=enabled\n" +
				"org.eclipse.jdt.core.compiler.codegen.targetPlatform=1.6\n" +
				"org.eclipse.jdt.core.compiler.codegen.unusedLocal=preserve\n" +
				"org.eclipse.jdt.core.compiler.compliance=1.6\n" +
				"org.eclipse.jdt.core.compiler.debug.lineNumber=generate\n" +
				"org.eclipse.jdt.core.compiler.debug.localVariable=generate\n" +
				"org.eclipse.jdt.core.compiler.debug.sourceFile=generate\n" +
				"org.eclipse.jdt.core.compiler.problem.assertIdentifier=error\n" +
				"org.eclipse.jdt.core.compiler.problem.enumIdentifier=error\n" +
				"org.eclipse.jdt.core.compiler.source=1.6"
			);
	}
	
	private void createController(File srcFolder, ModelDefinition model) {
		System.out.println("BLAZE_PROJECT_GENERATOR::CREATE_CONTROLLER");

		String mType = model.getSimpleName();
		String mVar = varName(mType);

		SourceFile sf = new SourceFile();
		sf.packageName = module.packageName(module.controllers);
		sf.simpleName = mType + "Controller";

		sf.imports.add(List.class.getCanonicalName());
		sf.imports.add(SQLException.class.getCanonicalName());
		sf.imports.add(module.packageName(module.models) + "." + mType);

		sf.methods.put("0find(int id)",
				"public " + mType + " find(int id) throws SQLException {\n" +
				"\treturn " + mType + ".find(id);\n" +
				"}"
			);
		
		sf.methods.put("1find(String where)",
				"public " + mType + " find(String where) throws SQLException {\n" +
				"\treturn " + mType + ".find(where);\n" +
				"}"
			);
		
		sf.methods.put("2findAll()",
				"public List<" + mType + "> findAll() throws SQLException {\n" +
				"\treturn " + mType + ".findAll();\n" +
				"}"
			);
		
		sf.methods.put("3findAll(String where)",
				"public List<" + mType + "> findAll(String where) throws SQLException {\n" +
				"\treturn " + mType + ".findAll(where);\n" +
				"}"
			);
		
		sf.methods.put("4create",
				"public void create(" + mType + " " + mVar + ") throws SQLException {\n" +
				"\t" + mVar + ".create();\n" +
				"}"
			);

		sf.methods.put("5destroy",
				"public void destroy(" + mType + " " + mVar + ") throws SQLException {\n" +
				"\t" + mVar + ".destroy();\n" +
				"}"
			);

		sf.methods.put("6update",
				"public void update(" + mType + " " + mVar + ") throws SQLException {\n" +
				"\t" + mVar + ".update();\n" +
				"}"
			);

		writeFile(srcFolder, sf.getFilePath(), sf.toSource());
	}
	
	private void createModel(File srcFolder, ModelDefinition model) throws IOException {
		System.out.println("BLAZE_PROJECT_GENERATOR::CREATE_MODEL");
		String type = model.getSimpleName();
		String var = varName(type);
		String plural = varName(type, true);
		Collection<PropertyDescriptor> props = model.getProperties().values();
		
		SourceFile sf = new SourceFile();
		sf.packageName = model.getPackageName();
		sf.imports.add(List.class.getCanonicalName());
		sf.imports.add(SQLException.class.getCanonicalName());
		sf.simpleName = type;
		sf.superName = sf.simpleName + "Model";

		int i = 0;
		StringBuilder sb;
		
		sb = new StringBuilder();
		sb.append("private static ").append(type).append(" setVars(").append(type).append(' ').append(var).append(") {\n");
		sb.append("\tif(").append(var).append(" != null) {\n");
		sb.append("\t\t").append(var).append(".id = ").append(var).append(".getId();\n");
		for(PropertyDescriptor prop : props) {
			sb.append("\t\t").append(var).append(".").append(prop.variable()).append(" = ").append(var).append(".").append(prop.getterName()).append("();\n");
		}
		sb.append("\t}\n");
		sb.append("\treturn ").append(var).append(";\n");
		sb.append("}");
		sf.staticMethods.put(String.valueOf(i++), sb.toString());
		
		sb = new StringBuilder();
		sb.append("private static List<").append(type).append("> setVars(List<").append(type).append("> ").append(plural).append(") {\n");
		sb.append("\tfor(").append(type).append(' ').append(var).append(" : ").append(plural).append(") {\n");
		sb.append("\t\tsetVars(").append(var).append(");\n");
		sb.append("\t}\n");
		sb.append("\treturn ").append(plural).append(";\n");
		sb.append("}");
		sf.staticMethods.put(String.valueOf(i++), sb.toString());
		
		sb = new StringBuilder();
		sb.append("private static ").append(type).append(" setFields(").append(type).append(' ').append(var).append(") {\n");
		sb.append("\tif(").append(var).append(" != null) {\n");
		sb.append("\t\t").append(var).append(".setId(").append(var).append(".id);\n");
		for(PropertyDescriptor prop : props) {
			sb.append("\t\t").append(var).append(".").append(prop.setterName()).append("(").append(var).append(".").append(prop.variable()).append(");\n");
		}
		sb.append("\t}\n");
		sb.append("\treturn ").append(var).append(";\n");
		sb.append("}");
		sf.staticMethods.put(String.valueOf(i++), sb.toString());
		

		sb = new StringBuilder();
		sf.staticMethods.put(String.valueOf(i++), sb.toString());
		sb = new StringBuilder();
		sb.append("public static ").append(type).append(" find(int id) throws SQLException {\n");
		sb.append("\treturn setVars(").append(sf.superName).append(".find(id));\n");
		sb.append("}");
		sf.staticMethods.put(String.valueOf(i++), sb.toString());
		
		sb = new StringBuilder();
		sb.append("public static ").append(sf.simpleName).append(" find(String where) throws SQLException {\n");
		sb.append("\treturn setVars(").append(sf.superName).append(".find(where));\n");
		sb.append("}");
		sf.staticMethods.put(String.valueOf(i++), sb.toString());
		
		sb = new StringBuilder();
		sb.append("public static List<").append(sf.simpleName).append("> findAll() throws SQLException {\n");
		sb.append("\treturn setVars(").append(sf.superName).append(".findAll());\n");
		sb.append("}");
		sf.staticMethods.put(String.valueOf(i++), sb.toString());
		
		sb = new StringBuilder();
		sb.append("public static List<").append(sf.simpleName).append("> findAll(String where) throws SQLException {\n");
		sb.append("\treturn setVars(").append(sf.superName).append(".findAll(where));\n");
		sb.append("}");
		sf.staticMethods.put(String.valueOf(i++), sb.toString());
		

		sf.variables.put("", "public int id");
		for(PropertyDescriptor prop : props) {
			if(!prop.fullType().startsWith("java.lang")) {
				sf.imports.add(prop.fullType());
			}
			sf.variables.put(prop.variable(), "public " + prop.castType() + " " + prop.variable());
		}

		sb = new StringBuilder();
		sb.append("@Override\n");
		sb.append("public boolean create() {\n");
		sb.append("\tsetFields(this);\n");
		sb.append("\treturn super.create();\n");
		sb.append("}");
		sf.methods.put(String.valueOf(i++), sb.toString());

		sb = new StringBuilder();
		sb.append("@Override\n");
		sb.append("public boolean update() {\n");
		sb.append("\tsetFields(this);\n");
		sb.append("\treturn super.update();\n");
		sb.append("}");
		sf.methods.put(String.valueOf(i++), sb.toString());

		sb = new StringBuilder();
		sb.append("@Override\n");
		sb.append("public boolean destroy() {\n");
		sb.append("\tsetId(id);\n");
		sb.append("\treturn super.destroy();\n");
		sb.append("}");
		sf.methods.put(String.valueOf(i++), sb.toString());
		
		writeFile(srcFolder, sf.getFilePath(), sf.toSource());
	}

	public File getProject() {
		return project;
	}
	
	public File getFlexProject() {
		return flexProject;
	}
	
	public void setExportFlex(boolean exportFlex) {
		this.exportFlex = exportFlex;
	}

	public void setFlexProject(File project) {
		this.flexProject = project;
	}
	
	public void setForce(boolean force) {
		this.force = force;
	}
	
	public void createPersistController(File srcFolder){
		String mType = "Persist";
		SourceFile sf = new SourceFile();
		sf.packageName = module.packageName(module.controllers);
		sf.simpleName = mType + "Controller";
		sf.imports.add("org.oobium.persist.http.HttpPersistService");
		sf.variables.put("INSTANCE", "private static final PersistController INSTANCE = new PersistController();");
	 
	    // Private constructor prevents instantiation from other classes
		MethodCreator m0 = new MethodCreator("0PersistController()");
	    m0.addLine("private PersistController() {");
	    	m0.addLine("HttpPersistService service = new HttpPersistService(\"localhost:5000\", true);");
	    m0.addLine("}");
	    sf.addMethod(m0);
	 
		MethodCreator m1 = new MethodCreator("1getInstance()");
	    m1.addLine("public static PersistController getInstance() {");
	        m1.addLine("return INSTANCE;");
	    m1.addLine("}");
	    sf.addMethod(m1);
	    
		writeFile(srcFolder, sf.getFilePath(), sf.toSource());
	}
	
	public void createChannelController(File srcFolder){
		System.out.println("BLAZE_PROJECT_GENERATOR::CREATE_CHANNEL_CONTROLLER");

		String mType = "Channel";
		String mVar = varName(mType);
		SourceFile sf = new SourceFile();
		sf.packageName = module.packageName(module.controllers);
		sf.simpleName = mType + "Controller";

		sf.imports.add(SQLException.class.getCanonicalName());
		//sf.imports.add(module.packageName(module.models) + "." + mType);
		
		sf.imports.add("flex.messaging.Destination");
		sf.imports.add("import flex.messaging.MessageBroker");
		sf.imports.add("import flex.messaging.MessageDestination");
		sf.imports.add("import flex.messaging.services.MessageService");
		
		sf.methods.put("0removeChannel", 
				"public static void removeChannel(String channelName){\n"+
				"\ttry {\n"+
				"\t\tMessageBroker broker = MessageBroker.getMessageBroker(null);\n"+
				"\t\tMessageService service = (MessageService) broker.getService(\"message-service\");\n"+
				"\t\tDestination destination = (MessageDestination) service.getDestination(channelName);\n" +
				"\t\tif(destination != null) {\n"+
					"\t\t\tservice.removeDestination(destination.getId());\n"+
				"\t\t}\n" +
				
			"\t} catch(Exception e){\n" +
				"\t\t//LogHelper.write(e.toString());\n"+
			"\t}\n"+
		"}");
		
		MethodCreator m = new MethodCreator("1addChannel");
		m.addLine("public static void addChannel(String channelName){");
		m.addLine("try{");
		m.addLine("MessageBroker broker = MessageBroker.getMessageBroker(null);");
		m.addLine("MessageService service = (MessageService) broker.getService(\"message-service\");");
		m.addLine("Destination findDestination = (MessageDestination) service.getDestination(channelName);");
		m.addLine("if(findDestination == null) {");
		m.addLine("Destination destination = (MessageDestination) service.createDestination(channelName);");
		m.addLine("//LogHelper.write((\"CHANNEL_CONTROLLER::SERVICE_IS_STARTED::\"+service.isStarted()));");
		m.addLine("if (service.isStarted()) {");
		m.addLine("destination.start();");
		m.addLine("}");	
		m.addLine("} else {");
		m.addLine("//LogHelper.write(\"CHANNEL_CONTROLLER::CHANNEL_ALREADY_EXISTS::ChannelName=\"+channelName);");
		m.addLine("}");
		m.addLine("} catch(Exception e){");
		m.addLine("//LogHelper.write(e.toString());");
		m.addLine("}");
		m.addLine("}");
		
		sf.methods.put(m.name, m.toString());
		writeFile(srcFolder, sf.getFilePath(), sf.toSource());
	}
	
	public void createSessionController(File srcFolder){
		System.out.println("BLAZE_PROJECT_GENERATOR::CREATE_USER_SESSION_CONTROLLER");

		String mType = "UserSession";

		SourceFile sf = new SourceFile();
		sf.packageName = module.packageName(module.controllers);
		sf.simpleName = mType + "Controller";

		sf.imports.add(SQLException.class.getCanonicalName());
		sf.imports.add("import java.util.List");
		sf.imports.add("import com.dn2k.blazeds.models.User");
		sf.imports.add("import com.dn2k.blazeds.stub.UserSvcStub");
		sf.imports.add("import com.dn2k.blazeds.util.LogHelper");
		sf.imports.add("import flex.messaging.FlexContext");
		sf.imports.add("import flex.messaging.FlexSession");

		sf.variables.put("User", "private static User user");
		sf.variables.put("FlexSession", "private static FlexSession mySession");
		
		MethodCreator m0 = new MethodCreator("0UserSessionController");
		m0.addLine("public UserSessionController(){");
		m0.addLine("mySession = FlexContext.getFlexSession();");
		m0.addLine("}");
		sf.methods.put(m0.name, m0.toString());
		
		MethodCreator m1 = new MethodCreator("1getSessionId()");
		m1.addLine("public String getSessionId(){");
			m1.addLine("return mySession.getId();");
		m1.addLine("}");
		sf.methods.put(m1.name, m1.toString());
		
		MethodCreator m2 = new MethodCreator("2getUserId()");
		m2.addLine("public int getUserId(){");
			m2.addLine("return user.id;");
		m2.addLine("}");
		sf.methods.put(m2.name, m2.toString());
		
		MethodCreator m3 = new MethodCreator("3getUserName()");
		m3.addLine("public String getUserName(){");
			m3.addLine("//LogHelper.write(\"SESSION_CONTROLLER::GET_USER\");");
			m3.addLine("if(user==null) {");
				m3.addLine("return \"\";");
			m3.addLine("}");
			m3.addLine("//LogHelper.write(\"SESSION_CONTROLLER::userName=\"+user.userName);");
			m3.addLine("return user.userName;");
		m3.addLine("}");
		sf.methods.put(m3.name, m3.toString());
	
		MethodCreator m4 = new MethodCreator("4getPassword()");
		m4.addLine("public String getPassword(){");
		m4.addLine("return user.password;");
		m4.addLine("}");
		sf.addMethod(m4);
	
		
		MethodCreator m5 = new MethodCreator("5login()");
		m5.addLine("public boolean login(String userName, String password) {");
			m5.addLine("//LogHelper.write(\"LOGIN::userName=\"+userName+\", password=\"+password);");
			m5.addLine("user = authenticate(userName, password);");
			m5.addLine("if(user!=null){");
			m5.addLine("return true;");
			m5.addLine("} else {");
			m5.addLine("return false;");
			m5.addLine("}");
		m5.addLine("}");
		sf.addMethod(m5);
		
		MethodCreator m6 = new MethodCreator("6logout()");
		m6.addLine("public void logout() {");
			m6.addLine("mySession = FlexContext.getFlexSession();");
			m6.addLine("mySession.removeAttribute(\"userName\");");
			m6.addLine("mySession.removeAttribute(\"password\");");
		m6.addLine("}");
		sf.addMethod(m6);
		
		MethodCreator m7 = new MethodCreator("7authenticate()");
		m7.addLine("public User authenticate(String userName, String password){");
			m7.addLine("try {");
				m7.addLine("mySession = FlexContext.getFlexSession();");
				m7.addLine("if(mySession==null){");
					m7.addLine("//LogHelper.write(\"SESSION is NULL!!!!!!!!!!!!!\");");
				m7.addLine("}");
				m7.addLine("List userList = UserSvcStub.findAll();");
				m7.addLine("//LogHelper.write(\"USER_LIST_SIZE::size=\"+userList.size());");
				m7.addLine("for(Object obj:userList){");
					m7.addLine("User mUser = (User)obj;");
					m7.addLine("//LogHelper.write(\"SESSION_CONTROLLER::authenticate::userName\"+userName+\", password=\"+password);");
					m7.addLine("//LogHelper.write(\"USER_OBJECT::username=\"+mUser.userName+\", password=\"+mUser.password);");
					m7.addLine("if(mUser.userName.equals(userName)){");
						m7.addLine("if(mUser.password.equals(password)){");
							m7.addLine("//LogHelper.write(\"MATCH USER::\"+mUser.userName);");
							m7.addLine("mySession.setAttribute(\"userName\", mUser.userName);");
							m7.addLine("mySession.setAttribute(\"password\", mUser.password);");
							m7.addLine("user = mUser;");
							m7.addLine("return mUser;");
						m7.addLine("}");
					m7.addLine("}");
				m7.addLine("}");
			m7.addLine("} catch(Exception e){");
				m7.addLine("//LogHelper.write(e.toString());");
				m7.addLine("for(StackTraceElement element: e.getStackTrace()){");
					m7.addLine("//LogHelper.write(element.toString());");
				m7.addLine("}");
			m7.addLine("}");
			m7.addLine("return null;");
		m7.addLine("}");
		sf.addMethod(m7);
		
		writeFile(srcFolder, sf.getFilePath(), sf.toSource());
	}

}
