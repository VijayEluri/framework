package org.oobium.build.console.commands.export;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.oobium.build.console.BuilderCommand;
import org.oobium.build.console.BuilderConsoleActivator;
import org.oobium.build.gen.android.AndroidGenerator;
import org.oobium.build.gen.android.GeneratorEvent;
import org.oobium.build.gen.android.GeneratorListener;
import org.oobium.build.workspace.AndroidApp;
import org.oobium.build.workspace.Application;
import org.oobium.build.workspace.ClientExporter;
import org.oobium.build.workspace.Project;
import org.oobium.build.workspace.Workspace;
import org.oobium.build.workspace.Project.Type;

public class ModelsCommand extends BuilderCommand {

	@Override
	protected void configure() {
		applicationRequired = true;
	}
	
	@Override
	protected void run() {
		Application app = getApplication();
		if(app.hasModels()) {
			Workspace workspace = getWorkspace();
			AndroidApp target = null;
			if(hasParam("target")) {
				Project project = workspace.getProject(param("target"));
				if(project == null) {
					console.err.println(param("target") + " does not exist in the workspace");
					return;
				}
				if(project.isAndroid()) {
					target = (AndroidApp) project;
				} else {
					console.err.println(param("target") + " is not an Android Application");
					return;
				}
			}
			if(target == null) {
				Project[] projects = workspace.getProjects(Type.Android);
				if(projects.length == 1) {
					String s = ask("use \"" + projects[0] + "\" as the target? [Y/N] ");
					if("Y".equalsIgnoreCase(s)) {
						target = (AndroidApp) projects[0];
					}
				} else if(projects.length > 1) {
					StringBuilder sb = new StringBuilder();
					sb.append(projects.length).append(" Android projects found:");
					for(int i = 0; i < projects.length; i++) {
						sb.append("\n  " + i + " - " + projects[i]);
					}
					sb.append("\nselect which to use as the target (0-" + (projects.length - 1) + "), or <Enter> for none ");
					String s = ask(sb.toString());
					try {
						int i = Integer.parseInt(s);
						if(i >= 0 && i < projects.length) {
							target = (AndroidApp) projects[i];
						}
					} catch(Exception e) {
						// discard
					}
				}
			}
			try {
				ClientExporter exporter = new ClientExporter(getWorkspace(), getApplication());
				exporter.includeSource(true);
				exporter.setTarget(target);
				File[] jars = exporter.export();
				for(File jar : jars) {
					if(target == null) {
						console.out.println("exported <a href=\"open file " + jar.getAbsolutePath() + "\">" + jar.getName() + "</a>" +
								" to <a href=\"open file " + workspace.getExportDir().getAbsolutePath() + "\">Export Directory</a>");
					} else {
						int len = target.file.getAbsolutePath().length();
						String name = target.name + ": " + jar.getParent().substring(len);
						console.out.println("exported <a href=\"open file " + jar.getAbsolutePath() + "\">" + jar.getName() + "</a>" +
								" to <a href=\"open file " + jar.getParent() + "\">" + name + "</a>");
					}
				}

				if(target != null) {
					String s = ask("create scaffolding? [Y/N] ");
					if("Y".equalsIgnoreCase(s)) {
						AndroidGenerator gen = new AndroidGenerator(app, target);
						gen.setListener(new GeneratorListener() {
							@Override
							public void handleEvent(GeneratorEvent event) {
								console.out.println(event.data);
							}
						});
						List<File> files = gen.generateScaffolding();
						for(File file : files) {
							int len = target.file.getAbsolutePath().length();
							String path = target.file.getAbsolutePath() + "#" + file.getAbsolutePath().substring(len+1);
							console.out.println("exported <a href=\"open file " + path + "\">" + file.getName() + "</a>");
						}
					}

					console.out.println("export complete\n *** models to be accessed by the Android client must be publised first ***");
				
					BuilderConsoleActivator.sendRefresh(target, 100);
				}
			} catch(IOException e) {
				console.err.print(e);
			}
		} else {
			console.err.println(app + " does not have any models");
		}
	}
	
}
