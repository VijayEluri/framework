package org.oobium.eclipse.designer.commands;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;
import org.oobium.build.console.Eclipse;

public class OpenDesignerCommand extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IStructuredSelection sel = (IStructuredSelection) HandlerUtil.getActiveMenuSelection(event);
		Object o = sel.getFirstElement();
		if(o instanceof IJavaProject) {
			o = ((IJavaProject) o).getProject();
		}
		if(o instanceof IProject) {
			try {
				File project = ((IProject) o).getLocation().toFile();
				File file = new File(project, ".models");
				if(!file.isFile()) {
					file.createNewFile();
				}
				Eclipse.openFile(project, file);
			} catch(IOException e) {
				// exit
			}
		}
		return null;
	}

}
