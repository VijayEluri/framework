package org.oobium.eclipse.designer.editor.models.commands;

import static org.oobium.utils.FileUtils.*;

import java.io.File;
import java.io.IOException;

import org.oobium.eclipse.OobiumPlugin;

public class Backup {

	private File original;
	private File backup;
	
	public Backup(File file) {
		if(file != null && file.exists()) {
			original = file;
			backup = new File(OobiumPlugin.getWorkspace().getWorkingDirectory(), ".backups");
			backup = new File(backup, file.getPath() + "-bak");
			while(backup.exists()) {
				backup = new File(backup.getPath() + "k");
			}
			try {
				copy(original, backup, PERSIST_LAST_MODIFIED);
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void dispose() {
		if(backup != null) {
			delete(backup);
			backup = null;
		}
		original = null;
	}
	
	public void restore() {
		if(original != null && backup != null) {
			try {
				move(backup, original, PERSIST_LAST_MODIFIED);
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
	}
	
}
