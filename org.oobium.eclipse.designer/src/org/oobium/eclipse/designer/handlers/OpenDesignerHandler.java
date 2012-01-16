package org.oobium.eclipse.designer.handlers;

import static org.oobium.utils.FileUtils.writeFile;

import java.io.File;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;
import org.oobium.build.console.Eclipse;
import org.oobium.build.workspace.Module;
import org.oobium.build.workspace.Project;
import org.oobium.build.workspace.Workspace;
import org.oobium.eclipse.OobiumPlugin;
import org.oobium.eclipse.designer.manager.DataServiceManager;

public class OpenDesignerHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IStructuredSelection sel = (IStructuredSelection) HandlerUtil.getActiveMenuSelection(event);
		Object o = sel.getFirstElement();
		if(o instanceof IJavaProject) {
			o = ((IJavaProject) o).getProject();
		}
		if(o instanceof IProject) {
			Workspace ws = OobiumPlugin.getWorkspace();
			File file = ((IProject) o).getLocation().toFile();
			Project target = ws.getProject(file);
			if(target == null) {
				target = ws.load(file);
			}
			File models = new File(target.file, "oobium.models");
			if(!models.isFile()) {
				if(target instanceof Module) {
					writeFile(models, "{}");
				} else {
					String name = DataServiceManager.instance().getServiceName(target);
					writeFile(models, "{service: \"" + name + "\"}");
				}
			}
			Eclipse.openFile(target.file, models);
		}
		return null;
	}

}
