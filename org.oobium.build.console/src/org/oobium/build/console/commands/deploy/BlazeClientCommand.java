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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import org.oobium.build.clients.blazeds.BlazeProjectGenerator;
import org.oobium.build.console.BuilderCommand;
import org.oobium.build.console.Eclipse;
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
			String appName = "dn2k";
			File appXML = new File(tomcat,"conf/Catalina/localhost/"+appName+".xml") ;
			FileWriter fstream = new FileWriter(appXML);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write("<Context privileged='true' antiResourceLocking='false' antiJARLocking='false' reloadable='true'>\n");
			out.write("</Context>");
			out.close();
			
			//2.  Create web application directory
			File appFile = new File(installDir, appName);
			appFile.mkdir();
			// I. includes WEB-INF
			File wFile = new File(appFile,"WEB-INF");
			wFile.mkdir();
			//(a) includes src dir
			new File(wFile, "src").mkdir();
			//(b) includes flex dir
			File flexFile = new File(wFile, "flex");
			flexFile.mkdir();
			//  i. Add messaging-config.xml
			File mConfigFile = new File(flexFile, "messaging-config.xml");
			writeToFile(mConfigFile, getMessagingConfig());
			// ii. Add proxy-config.xml
			File pConfigFile = new File(flexFile, "proxy-config.xml");
			writeToFile(pConfigFile, getProxyConfig());
			//iii. Add remoting-config.xml
			File rConfigFile = new File(flexFile, "remoting-config.xml");
			writeToFile(rConfigFile, getRemotingConfig());
			// iv. Add services-config.xml
			File sConfigFile = new File(flexFile, "services-config.xml");
			writeToFile(sConfigFile, getServicesConfig());
			//(c) includes lib dir
			//@TODO:  WRITE LIBS
			new File(wFile, "lib").mkdir();
			//(d) includes classes dir
			new File(wFile, "classes").mkdir();
			//(e) includes web.xml 
			File webXMLFile = new File(wFile, "web.xml");
			writeToFile(webXMLFile, getWebXML(appName));
			File htmFile = new File(wFile, "index.htm");
			writeToFile(htmFile, appName+" BlazeDS Classses This is the location of BlazeDS java classes used to communicate with flex clients.");
			//II. includes META-INF
			File mFile = new File(appFile,"META-INF");
			mFile.mkdir();
			// bundle up the blaze project
			// module.createJar(jar, version);
			
			// put it in install directory
			
		} catch(Exception e) {
			console.err.print(e);
		}
	}
	
	public static void writeToFile(File file, String fileText){
		try {
			FileWriter wStream = new FileWriter(file);
			BufferedWriter out = new BufferedWriter(wStream);
			out.write(fileText);
			out.close();
			wStream.close();
		} catch(Exception e){
			e.printStackTrace();
		}
	}

	public static String getWebXML(String appName){
		StringBuffer sb = new StringBuffer();
		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		sb.append("\n");
		sb.append("<!DOCTYPE web-app PUBLIC \"-//Sun Microsystems, Inc.//DTD Web Application 2.3//EN\" \"http://java.sun.com/dtd/web-app_2_3.dtd\">\n");
		sb.append("\n");
		sb.append("<web-app>\n");
		sb.append("    <display-name>"+appName+" Java Objects</display-name>\n");
		sb.append("    <description>"+appName+" Java Objects communicated to flex clients</description>\n");
		sb.append("\n");
		sb.append("    <!-- Http Flex Session attribute and binding listener support -->\n");
		sb.append("    <listener>\n");
		sb.append("        <listener-class>flex.messaging.HttpFlexSession</listener-class>\n");
		sb.append("    </listener>\n");
		sb.append("\n");
		sb.append("    <!-- MessageBroker Servlet -->\n");
		sb.append("    <servlet>\n");
		sb.append("        <servlet-name>MessageBrokerServlet</servlet-name>\n");
		sb.append("        <display-name>MessageBrokerServlet</display-name>\n");
		sb.append("        <servlet-class>flex.messaging.MessageBrokerServlet</servlet-class>\n");
		sb.append("        <init-param>\n");
		sb.append("            <param-name>services.configuration.file</param-name>\n");
		sb.append("            <param-value>/WEB-INF/flex/services-config.xml</param-value>\n");
		sb.append("        </init-param>\n");
		sb.append("        <load-on-startup>1</load-on-startup>\n");
		sb.append("    </servlet>\n");
		sb.append("    <servlet-mapping>\n");
		sb.append("        <servlet-name>MessageBrokerServlet</servlet-name>\n");
		sb.append("        <url-pattern>/messagebroker/*</url-pattern>\n");
		sb.append("    </servlet-mapping>\n");
		sb.append("\n");
		sb.append("    <welcome-file-list>\n");
		sb.append("        <welcome-file>index.htm</welcome-file>\n");
		sb.append("    </welcome-file-list>\n");
		sb.append("\n");
		sb.append("</web-app>\n");
		return sb.toString();
	}
	
	public static String getMessagingConfig(){
		StringBuffer sb = new StringBuffer();
		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		sb.append("<service id=\"message-service\" class=\"flex.messaging.services.MessageService\">\n");
		sb.append("\n");
		sb.append("    <adapters>\n");
		sb.append("        <adapter-definition id=\"actionscript\" class=\"flex.messaging.services.messaging.adapters.ActionScriptAdapter\" default=\"true\" />\n");
		sb.append("        <adapter-definition id=\"jms\" class=\"flex.messaging.services.messaging.adapters.JMSAdapter\"/>\n");
		sb.append("    </adapters>\n");
		sb.append("    \n");
		sb.append("    <default-channels>\n");
		sb.append("		<channel ref=\"my-streaming-amf\"/>\n");
		sb.append("		<channel ref=\"my-polling-amf\"/>\n");
		sb.append("    </default-channels>\n");
		sb.append("    <destination id=\"feed\">\n");
		sb.append("    	<!-- Destination specific channel configuration can be defined if needed\n");
		sb.append("        <channels>\n");
		sb.append("            <channel ref=\"my-streaming-amf\"/>\n");
		sb.append("        </channels>        \n");
		sb.append("         -->\n");
		sb.append("    </destination>\n");
		sb.append("\n");
		sb.append("    <destination id=\"chat\"/>\n");
		sb.append("\n");
		sb.append("    <destination id=\"dashboard\"/>\n");
		sb.append("    \n");
		sb.append("    <destination id=\"market-data-feed\">\n");
		sb.append("        <properties>\n");
		sb.append("            <server>\n");
		sb.append("                <allow-subtopics>true</allow-subtopics>\n");
		sb.append("                <subtopic-separator>.</subtopic-separator>s\n");
		sb.append("            </server>\n");
		sb.append("        </properties>\n");
		sb.append("        <channels>\n");
		sb.append("			<channel ref=\"my-polling-amf\"/>\n");
		sb.append("			<channel ref=\"my-streaming-amf\"/>\n");
		sb.append("            <channel ref=\"per-client-qos-polling-amf\"/>\n");
		sb.append("        </channels>        \n");
		sb.append("    </destination>    \n");
		sb.append("\n");
		sb.append("</service>\n");	
		return sb.toString();
	}
	
	public static String getProxyConfig(){
		StringBuffer sb = new StringBuffer();
		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		sb.append("<service id=\"proxy-service\" class=\"flex.messaging.services.HTTPProxyService\">\n");
		sb.append("\n");
		sb.append("    <properties>\n");
		sb.append("        <connection-manager>\n");
		sb.append("            <max-total-connections>100</max-total-connections>\n");
		sb.append("            <default-max-connections-per-host>2</default-max-connections-per-host>\n");
		sb.append("        </connection-manager>\n");
		sb.append("\n");
		sb.append("        <allow-lax-ssl>true</allow-lax-ssl>\n");
		sb.append("    </properties>\n");
		sb.append("\n");
		sb.append("    <default-channels>\n");
		sb.append("        <channel ref=\"my-http\"/>\n");
		sb.append("        <channel ref=\"my-amf\"/>\n");
		sb.append("    </default-channels>\n");
		sb.append("\n");
		sb.append("    <adapters>\n");
		sb.append("        <adapter-definition id=\"http-proxy\" class=\"flex.messaging.services.http.HTTPProxyAdapter\" default=\"true\"/>\n");
		sb.append("        <adapter-definition id=\"soap-proxy\" class=\"flex.messaging.services.http.SOAPProxyAdapter\"/>\n");
		sb.append("    </adapters>\n");
		sb.append("\n");
		sb.append("    <destination id=\"DefaultHTTP\">\n");
		sb.append("		<properties>\n");
		sb.append("		</properties>\n");
		sb.append("    </destination>\n");
		sb.append("    \n");
		sb.append("    <destination id=\"catalog\">\n");
		sb.append("		<properties>\n");
		sb.append("			<url>/{context.root}/testdrive-httpservice/catalog.jsp</url>\n");
		sb.append("		</properties>\n");
		sb.append("    </destination>\n");
		sb.append("\n");
		sb.append("    <destination id=\"ws-catalog\">\n");
		sb.append("        <properties>\n");
		sb.append("            <wsdl>http://feeds.adobe.com/webservices/mxna2.cfc?wsdl</wsdl>\n");
		sb.append("            <soap>*</soap>\n");
		sb.append("        </properties>\n");
		sb.append("        <adapter ref=\"soap-proxy\"/>\n");
		sb.append("    </destination>\n");
		sb.append("    \n");
		sb.append("</service>\n");
		return sb.toString();
	}
	
	public static String getRemotingConfig(){
		//@TODO: add logic to use package and add UserSessionController
		StringBuffer sb = new StringBuffer();
		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		sb.append("<service id=\"remoting-service\"\n");
		sb.append("    class=\"flex.messaging.services.RemotingService\">\n");
		sb.append("\n");
		sb.append("    <adapters>\n");
		sb.append("        <adapter-definition id=\"java-object\" class=\"flex.messaging.services.remoting.adapters.JavaAdapter\" default=\"true\"/>\n");
		sb.append("    </adapters>\n");
		sb.append("\n");
		sb.append("    <default-channels>\n");
		sb.append("        <channel ref=\"my-amf\"/>\n");
		sb.append("    </default-channels>\n");
		sb.append("    \n");
		
		sb.append("    <destination id=\"UserSessionController\">\n");
		sb.append("    	<properties>\n");
		sb.append("    		<source>com.dn2k.blazeds.controller.SessionController</source>\n");
		sb.append("    		<scope>application</scope>\n");
		sb.append("    	</properties>\n");
		sb.append("    </destination>\n");
		sb.append("    \n");
		sb.append("  </service>\n");
		return sb.toString();
	}
	
	public static String getServicesConfig(){
		StringBuffer sb = new StringBuffer();
		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		sb.append("<services-config>\n");
		sb.append("\n");
		sb.append("    <services>\n");
		sb.append("        \n");
		sb.append("        \n");
		sb.append("        <service-include file-path=\"remoting-config.xml\" />\n");
		sb.append("        <service-include file-path=\"proxy-config.xml\" />\n");
		sb.append("        <service-include file-path=\"messaging-config.xml\" />\n");
		sb.append("        \n");
		sb.append("\n");
		sb.append("    	<!-- \n");
		sb.append("    	Application level default channels. Application level default channels are \n");
		sb.append("    	necessary when a dynamic destination is being used by a service component\n");
		sb.append("    	and no ChannelSet has been defined for the service component. In that case,\n");
		sb.append("    	application level default channels will be used to contact the destination.\n");
		sb.append("        -->   \n");
		sb.append("        <default-channels>\n");
		sb.append("           <channel ref=\"my-amf\"/>\n");
		sb.append("        </default-channels>\n");
		sb.append("    \n");
		sb.append("	</services>\n");
		sb.append("\n");
		sb.append("\n");
		sb.append("    <security>\n");
		sb.append("        <security-constraint id=\"sample-users\">\n");
		sb.append("	    <!--<service class=\"flex.samples.runtimeconfig.EmployeeRuntimeRemotingDestination\" id=\"runtime-employee-ro\" />-->\n");
		sb.append("            <auth-method>Custom</auth-method>\n");
		sb.append("            <roles>\n");
		sb.append("                <role>sampleusers</role>\n");
		sb.append("            </roles>\n");
		sb.append("        </security-constraint>\n");
		sb.append("\n");
		sb.append("		<login-command class=\"flex.messaging.security.TomcatLoginCommand\" server=\"Tomcat\"/>        \n");
		sb.append("        <!-- Uncomment the correct app server\n");
		sb.append("        <login-command class=\"flex.messaging.security.TomcatLoginCommand\" server=\"JBoss\">\n");
		sb.append("        <login-command class=\"flex.messaging.security.JRunLoginCommand\" server=\"JRun\"/>\n");
		sb.append("        <login-command class=\"flex.messaging.security.WeblogicLoginCommand\" server=\"Weblogic\"/>\n");
		sb.append("        <login-command class=\"flex.messaging.security.WebSphereLoginCommand\" server=\"WebSphere\"/>        \n");
		sb.append("        -->\n");
		sb.append("    </security>\n");
		sb.append("\n");
		sb.append("    <channels>\n");
		sb.append("    \n");
		sb.append("        <channel-definition id=\"my-streaming-amf\" class=\"mx.messaging.channels.StreamingAMFChannel\">\n");
		sb.append("            <endpoint url=\"http://{server.name}:{server.port}/{context.root}/messagebroker/streamingamf\" class=\"flex.messaging.endpoints.StreamingAMFEndpoint\"/>\n");
		sb.append("        </channel-definition>\n");
		sb.append("    \n");
		sb.append("        <channel-definition id=\"my-amf\" class=\"mx.messaging.channels.AMFChannel\">\n");
		sb.append("            <endpoint url=\"http://{server.name}:{server.port}/{context.root}/messagebroker/amf\" class=\"flex.messaging.endpoints.AMFEndpoint\"/>\n");
		sb.append("            <properties>\n");
		sb.append("                <polling-enabled>false</polling-enabled>\n");
		sb.append("            </properties>\n");
		sb.append("        </channel-definition>\n");
		sb.append("\n");
		sb.append("        <channel-definition id=\"my-secure-amf\" class=\"mx.messaging.channels.SecureAMFChannel\">\n");
		sb.append("            <endpoint url=\"https://{server.name}:{server.port}/{context.root}/messagebroker/amfsecure\" class=\"flex.messaging.endpoints.SecureAMFEndpoint\"/>\n");
		sb.append("            <properties>\n");
		sb.append("            	<add-no-cache-headers>false</add-no-cache-headers>\n");
		sb.append("            </properties>\n");
		sb.append("        </channel-definition>\n");
		sb.append("\n");
		sb.append("        <channel-definition id=\"my-polling-amf\" class=\"mx.messaging.channels.AMFChannel\">\n");
		sb.append("            <endpoint url=\"http://{server.name}:{server.port}/{context.root}/messagebroker/amfpolling\" class=\"flex.messaging.endpoints.AMFEndpoint\"/>\n");
		sb.append("            <properties>\n");
		sb.append("                <polling-enabled>true</polling-enabled>\n");
		sb.append("                <polling-interval-seconds>4</polling-interval-seconds>\n");
		sb.append("            </properties>\n");
		sb.append("        </channel-definition>\n");
		sb.append("\n");
		sb.append("        <channel-definition id=\"my-http\" class=\"mx.messaging.channels.HTTPChannel\">\n");
		sb.append("            <endpoint url=\"http://{server.name}:{server.port}/{context.root}/messagebroker/http\" class=\"flex.messaging.endpoints.HTTPEndpoint\"/>\n");
		sb.append("        </channel-definition>\n");
		sb.append("\n");
		sb.append("        <channel-definition id=\"my-secure-http\" class=\"mx.messaging.channels.SecureHTTPChannel\">\n");
		sb.append("            <endpoint url=\"https://{server.name}:{server.port}/{context.root}/messagebroker/httpsecure\" class=\"flex.messaging.endpoints.SecureHTTPEndpoint\"/>\n");
		sb.append("            <properties>\n");
		sb.append("            	<add-no-cache-headers>false</add-no-cache-headers>\n");
		sb.append("            </properties>\n");
		sb.append("        </channel-definition>\n");
		sb.append("\n");
		sb.append("        <channel-definition id=\"per-client-qos-polling-amf\" class=\"mx.messaging.channels.AMFChannel\">\n");
		sb.append("            <endpoint url=\"http://{server.name}:{server.port}/{context.root}/messagebroker/qosamfpolling\" class=\"flex.messaging.endpoints.AMFEndpoint\"/>\n");
		sb.append("            <properties>\n");
		sb.append("                <polling-enabled>true</polling-enabled>\n");
		sb.append("                <polling-interval-millis>500</polling-interval-millis>\n");
		sb.append("                <flex-client-outbound-queue-processor class=\"flex.samples.qos.CustomDelayQueueProcessor\">\n");
		sb.append("                    <!--<properties><flush-delay>5000</flush-delay></properties>-->\n");
		sb.append("                </flex-client-outbound-queue-processor>\n");
		sb.append("            </properties>\n");
		sb.append("        </channel-definition>\n");
		sb.append("\n");
		sb.append("    </channels>\n");
		sb.append("\n");
		sb.append("    <logging>\n");
		sb.append("        <!-- You may also use flex.messaging.log.ServletLogTarget -->\n");
		sb.append("        <target class=\"flex.messaging.log.ConsoleTarget\" level=\"Error\">\n");
		sb.append("            <properties>\n");
		sb.append("                <prefix>[BlazeDS] </prefix>\n");
		sb.append("                <includeDate>false</includeDate>\n");
		sb.append("                <includeTime>false</includeTime>\n");
		sb.append("                <includeLevel>true</includeLevel>\n");
		sb.append("                <includeCategory>false</includeCategory>\n");
		sb.append("            </properties>\n");
		sb.append("            <filters>\n");
		sb.append("                <pattern>Endpoint.*</pattern>\n");
		sb.append("                <pattern>Service.*</pattern>\n");
		sb.append("                <pattern>Configuration</pattern>\n");
		sb.append("            </filters>\n");
		sb.append("        </target>\n");
		sb.append("    </logging>\n");
		sb.append("\n");
		sb.append("    <system>\n");
		sb.append("        <redeploy>\n");
		sb.append("            <enabled>true</enabled>\n");
		sb.append("            <watch-interval>20</watch-interval>\n");
		sb.append("            <watch-file>{context.root}/WEB-INF/flex/services-config.xml</watch-file>\n");
		sb.append("            <watch-file>{context.root}/WEB-INF/flex/proxy-config.xml</watch-file>\n");
		sb.append("            <watch-file>{context.root}/WEB-INF/flex/remoting-config.xml</watch-file>\n");
		sb.append("            <watch-file>{context.root}/WEB-INF/flex/messaging-config.xml</watch-file>            \n");
		sb.append("            <touch-file>{context.root}/WEB-INF/web.xml</touch-file>\n");
		sb.append("        </redeploy>\n");
		sb.append("    </system>\n");
		sb.append("\n");
		sb.append("</services-config>\n");
		return sb.toString();
	}
}
