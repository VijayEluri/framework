package org.oobium.build.workspace;

import static org.oobium.utils.DateUtils.httpDate;
import static org.oobium.utils.FileUtils.findFiles;
import static org.oobium.utils.FileUtils.readFile;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import org.oobium.utils.FileUtils;

public class AssetList {

	private final Module module;
	private final ArrayList<String> realms;
	private final ArrayList<String> paths;

	public AssetList(Module module) {
		this.module = module;
		this.realms = new ArrayList<String>();
		this.paths = new ArrayList<String>();
	}

	public AssetList load() {
		loadStaticFromAssetsFolder();
		loadStaticFromViewsFolder();
		loadDynamicFromViewsFolder();
		return this;
	}
	
	/**
	 *  get all files in the 'assets' folder and its subfolders
	 *  note that each folder may contain a special "authentication.realm" file
	 *  this file contains the name of the realm that is allowed access to the files in the folder
	 */
	private void loadStaticFromAssetsFolder() {
		if(module.assets != null && module.assets.isDirectory()) {
			int len = module.assets.getPath().length();
			for(File file : findFiles(module.assets)) {
				if(file.getName().equals("authentication.realm")) {
					realms.add(file.getParent().substring(len).replace('\\', '/') + File.separator + "|" + readFile(file));
				} else {
					paths.add(file.getPath().substring(len).replace('\\', '/') + "|" + file.length() + "|" + httpDate(file.lastModified()));
				}
			}
		}
	}
	
	/**
	 *  get all CSS and JavaScript files in the 'views' folder and its subfolders
	 *  note that realms do not exist for these assets - they are all considered public
	 */
	private void loadStaticFromViewsFolder() {
		if(module.views != null && module.views.isDirectory()) {
			int len = module.views.getPath().length();
			for(File file : findFiles(module.views, ".css", ".js")) {
				paths.add(file.getPath().substring(len).replace('\\', '/') + "|" + file.length() + "|" + httpDate(file.lastModified()));
			}
		}
	}
	
	/**
	 *  get all dynamic assets (EJS and ESS files) in the 'views' folder and its subfolders
	 *  note that realms do not exist for these assets - they are all considered public
	 */
	private void loadDynamicFromViewsFolder() {
		if(module.views != null && module.views.isDirectory()) {
			int len = module.src.getPath().length();
			for(File file : findFiles(module.views, ".ess")) {
				String path = file.getPath();
				path = path.substring(len, path.length()-3) + (path.endsWith(".ejs") ? "e.js" : "e.css");
				paths.add(path.replace('\\', '/') + "|" + file.length() + "|" + httpDate(file.lastModified()));
			}
		}
	}
	
	public File writeFile() {
		if(module.assetList != null) {
			if(!realms.isEmpty()) {
				for(String s : realms) {
					String[] sa = s.split("|");
					for(int i = 0; i < paths.size(); i++) {
						String path = paths.get(i);
						if(path.startsWith(sa[0])) {
							paths.set(i, path + "|" + sa[1]);
						}
					}
				}
			}
			
			Collections.sort(paths);
	
			StringBuilder sb = new StringBuilder();
			sb.append("([");
			for(int i = 0; i < paths.size(); i++) {
				if(i != 0) sb.append(',');
				sb.append('\n').append('\t').append('"').append(paths.get(i)).append('"');
			}
			sb.append("\n]);\n");
			
			return FileUtils.writeFile(module.assetList, sb.toString());
		}
		return null;
	}
	
}
