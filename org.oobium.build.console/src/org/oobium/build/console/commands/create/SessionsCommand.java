package org.oobium.build.console.commands.create;

import java.io.File;

import org.oobium.app.http.Action;
import org.oobium.app.sessions.Session;
import org.oobium.build.console.BuilderCommand;
import org.oobium.build.console.Eclipse;
import org.oobium.build.util.SourceFile;
import org.oobium.build.workspace.Application;
import org.oobium.build.workspace.Migrator;

public class SessionsCommand extends BuilderCommand {

	@Override
	public void configure() {
		applicationRequired = true;
		maxParams = 0;
		minParams = 0;
	}
	
	@Override
	public void run() {
		Application app = getApplication();
		Migrator migrator = getWorkspace().getMigratorFor(app);
		if(migrator == null) {
			String s = flag('f') ? "Y" : ask("migrator project does not exist. Create? [Y/N] ");
			if("Y".equalsIgnoreCase(s)) {
				try {
					migrator = getWorkspace().createMigrator(app);
					if(migrator != null) {
						console.out.println("successfully created migrator: " + migrator.name);
						Eclipse.importProject(migrator.name, migrator.file);
					} else {
						console.err.println("failed to create migrator for " + app.name);
						return;
					}
				} catch(Exception e) {
					console.err.println("failed to create migrator: " + e.getMessage());
					return;
				}
			} else {
				console.err.println("cannot create a migration without a migrator project; exiting.");
				return;
			}
		}

		boolean createMigration = true;
		String name = "CreateSessions";
		File migration = migrator.getMigration(name);
		if(migration.exists()) {
			String s = flag('f') ? "Y" : ask("migration \"" + name + "\" already exists. Overwrite? [Y/N] ");
			if("Y".equalsIgnoreCase(s)) {
				migration.delete();
			} else {
				console.out.println("skipping creation of " + name + "");
				createMigration = false;
			}
		}

		if(createMigration) {
			migration = migrator.createMigration(name);
			console.out.println("created migration <a href=\"open migration " + name + "\">" + name + "</a>");
			if(migrator.addMigration(name)) {
				console.out.println("updated <a href=\"open migrator\">migrator</a>");
			}
			Eclipse.refreshProject(migrator.name);
		}
		
		if(app.addImportPackage(Session.class.getPackage().getName())) {
			console.out.println("modified <a href=\"open manifest\">MANIFEST.MF</a>");
		}

		String s = (flag('f') || hasParam("model")) ? "Y" : ask("also create 'login' scaffolding? [Y/N] ");
		if("Y".equalsIgnoreCase(s)) {
			File controller = createController(app);
			name = app.getControllerName(controller);
			console.out.println("created controller <a href=\"open controller " + name + "\">" + name + "</a>");

			File view = createLoginView(app);
			name = app.getViewName(controller);
			console.out.println("created view <a href=\"open view " + name + "\">" + name + "</a>");

			boolean updated = false;
			if(app.addViewRoute(view)) updated = true;
			if(app.addRoute("/login", controller, Action.create)) updated = true;
			if(app.addRoute("/logout", controller, Action.destroy)) updated = true;
			if(updated) {
				console.out.println("added model routes to <a href=\"open activator\">" + app.activator.getName() + "</a>");
			}
		}
		
		Eclipse.refreshProject(app.name);
	}
	
	private File createController(Application app) {
		String model = hasParam("model") ? app.adjust(param("model")) : "User";
		SourceFile sf = new SourceFile();
		sf.imports.add(Session.class.getCanonicalName());
		sf.imports.add(app.packageName(app.models) + ".*");
		sf.imports.add(app.packageName(app.views) + ".pages.Login");
		sf.methods.put("0",
			("@Override\n" +
			"public void create() throws Exception {\n" +
			" {model} model = null;\n" +
			" if(hasParam(\"email\") && hasParam(\"password\")) {\n" +
			"  model = {model}.find(\"where email=?\", param(\"email\"));\n" +
			"  if(model != null && model.isPassword(param(\"password\"))) {\n" +
			"   if(authenticate(model)) {\n" +
			"    if(hasParam(\"goto\")) {\n" +
			"     redirectTo(param(\"goto\"));\n" +
			"    } else {\n" +
			"     redirectToHome();\n" +
			"    }\n" +
			"    return;\n" +
			"   } // else, fall through to try again\n" +
			"  }\n" +
			" }\n" +
			"\n" +
			" setFlashError(\"Login failed: incorrect username or password\");\n" +
			" render(new Login(param(\"email\")));\n" +
			"}").replace("{model}", model));
		sf.methods.put("1",
			"@Override\n" +
			"public void handleRequest() throws Exception {\n" +
			" Session session = getSession(false);\n" +
			" if(session != null) {\n" +
			"  session.destroy();\n" +
			" }\n" +
			" redirectToHome();\n" +
			"}");
		sf.methods.put("2",
			"@Override\n" +
			"public void show() throws Exception {\n" +
			" render(new Login());\n" +
			"}");
		
		return app.createController("SessionController", sf);
	}
	
	private File createLoginView(Application app) {
		return app.createView("pages/Login",
				"Login(String email = \"login\")\n" +
				"\n" +
				"title Oobium : : Log In\n" +
				"\n" +
				"div#logPage\n" +
				"\tmessages\n" +
				"\timg(src:\"logBorder.png\")\n" +
				"\tdiv.Login\n" +
				"\t\tform(action: pathTo(\"sessions\"), method: \"post\")\n" +
				"\t\t\t- if(hasParam(\"goto\")) {\n" +
				"\t\t\t\t\thidden(name: \"goto\", value: param(\"goto\"))\n" +
				"\t\t\t- }\n" +
				"\t\t\ttext.name(name: \"email\", value: \"email\", onfocus: \"enterText(this, 'login');\", onblur: \"exitText(this, 'login');\")\n" +
				"\t\t\tpassword.pass(name: \"password\")\n" +
				"\t\t\tbutton#login Submit\n"
			);
	}
	
}
