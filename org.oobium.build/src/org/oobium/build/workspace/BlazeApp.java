package org.oobium.build.workspace;

import java.io.File;
import java.util.jar.Manifest;

public class BlazeApp extends JavaApp {

	public static final String NATURE = "org.oobium.blazenature";
	
	
	public final File flex;
	public final File lib;
	public final File oobium;
	public final File webXml;

	BlazeApp(Type type, File file, Manifest manifest) {
		super(type, file, manifest);
		if(!isJar) {
			flex = new File(file, "flex");
			lib = new File(file, "lib");
			oobium = new File(file, "oobium");
			webXml = new File(file, "web.xml");
		} else {
			flex = null;
			lib = null;
			oobium = null;
			webXml = null;
		}
	}

	
	
}
