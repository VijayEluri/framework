package org.oobium.eclipse.designer;

import java.io.File;

import static org.oobium.utils.FileUtils.toFile;

import org.oobium.build.eclipse.UpdateSiteBuilder;
import org.oobium.build.workspace.Workspace;

public class DesignerUpdateSiteBuilder {

	private static Workspace loadWorkspace() {
		Workspace ws = new Workspace(toFile("../../studio"));
		
		for(String path : new String[] { "..", "../../framework" }) {
			for(File file : toFile(path).listFiles()) {
				if(file.isDirectory()) {
					ws.load(file);
				}
			}
		}
		
		return ws;
	}

	
	public static void main(String[] args) throws Exception {
		Workspace workspace = loadWorkspace();
		UpdateSiteBuilder builder = new UpdateSiteBuilder(workspace, "com.oobium.designer.update-site");
		builder.setClean(true);
		builder.setIncludeSource(false);
		builder.setEclipse("../../../eclipse");
		builder.setSiteDirectory("../../oobium.com/com.oobium.www.update_site/assets/updates");
		builder.setAssociatedSites("http://download.eclipse.org/releases/indigo", "http://www.oobium.org/updates");
		builder.build();
		
		System.out.println("update-site created in " + builder.getSiteDirectory().getCanonicalPath());
	}

}
