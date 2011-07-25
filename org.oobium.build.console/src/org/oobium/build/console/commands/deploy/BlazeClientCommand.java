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
package org.oobium.build.console.commands.deploy;

import static org.oobium.utils.FileUtils.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import org.oobium.build.console.BuilderCommand;
import org.oobium.build.workspace.Module;
import org.oobium.build.workspace.Workspace;
import org.oobium.utils.FileUtils;
import org.oobium.utils.OSUtils;

public class BlazeClientCommand extends BuilderCommand {

	@Override
	public void configure() {
		moduleRequired = true;
	}

	@Override
	public void run() {
		Workspace workspace = getWorkspace();
		Module module = getModule();
		
		try {
			System.out.println("BLAZE_CLIENT_COMMAND::os="+OSUtils.getName());
			// Obviously, the file path needs to be changed :)
			String fileName="";
			if(OSUtils.isMac()){
				//fileName = "\\Users\\admin\\Downloads\\blaze\\blazeds-turnkey-4.0.0.14931.zip";
				fileName = "/Users/admin/Downloads/blaze/blazeds-turnkey-4.0.0.14931.zip";
			} else {
				fileName="c:\\Users\\jeremyd\\BlazeDS\\blazeds-turnkey-3.3.0.20931.zip";
			}
			System.out.println("FILENAME="+fileName);
			File blazeZip = new File(fileName);
			if(!blazeZip.isFile()) {
				console.err.println("blaze turnkey zip cannot be found");
				return;
			}
			System.out.println("WORKING_DIRECTORY::"+workspace.getWorkingDirectory().getAbsolutePath());
			File wd = new File(workspace.getWorkingDirectory(), "blaze-server");
			if(wd.exists()) {
				FileUtils.deleteContents(wd);
			}

			FileUtils.extract(blazeZip, wd);
			
			File tomcat = new File(wd, "tomcat");
			File installDir = new File(tomcat, "webapps");
			
			//Create tomcat application
			//1.  Add <application_name>.xml descripton for tomcat
			//TODO:  What should we name the application
			File appXML = new File(tomcat,"conf/Catalina/localhost/" + module.name + ".xml") ;
			FileWriter fstream = new FileWriter(appXML);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write("<Context privileged='true' antiResourceLocking='false' antiJARLocking='false' reloadable='true'>\n");
			out.write("</Context>");
			out.close();
			
			//2.  Create web application directory
			File appDir = new File(installDir, module.name);

			// I. includes WEB-INF
			File webInf = new File(appDir,"WEB-INF");

			//(a) includes src dir
			createFolder(webInf, "src");

			//(b) includes flex dir
			File flexDir = createFolder(webInf, "flex");

			//  i. Add messaging-config.xml
//			writeFile(flexDir, "messaging-config.xml", getMessagingConfig());

			// ii. Add proxy-config.xml
//			writeFile(flexDir, "proxy-config.xml", getProxyConfig());

			//iii. Add remoting-config.xml
//			writeFile(flexDir, "remoting-config.xml", getRemotingConfig(module));

			// iv. Add services-config.xml
//			writeFile(flexDir, "services-config.xml", getServicesConfig());

			//(c) includes lib dir
			//@TODO:  WRITE LIBS
			File libs = createFolder(webInf, "lib");

			//(d) includes classes dir
			File classes = createFolder(webInf, "classes");

			//(e) include web.xml 
//			writeFile(webInf, "web.xml", getWebXML(module.name));

			// (f) write out static files (include full flex projects here
			writeFile(webInf, "index.htm", module.name + " BlazeDS Classses This is the location of BlazeDS java classes used to communicate with flex clients.");

			//II. includes META-INF
			File mFile = new File(appDir,"META-INF");
			mFile.mkdir();
			// bundle up the blaze project
			// module.createJar(jar, version);
			
			// put it in install directory
			
		} catch(Exception e) {
			console.err.print(e);
		}
	}
	
}
